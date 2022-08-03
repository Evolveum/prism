/*
 * Copyright (C) 2020-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.query.lang;

import static com.evolveum.midpoint.prism.impl.query.lang.FilterNames.*;
import static com.evolveum.midpoint.util.MiscUtil.schemaCheck;

import java.util.*;
import java.util.function.Function;
import javax.xml.namespace.QName;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;

import com.evolveum.axiom.lang.antlr.AxiomAntlrLiterals;
import com.evolveum.axiom.lang.antlr.AxiomQuerySource;
import com.evolveum.axiom.lang.antlr.AxiomStrings;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser.*;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.PrismPropertyValueImpl;
import com.evolveum.midpoint.prism.impl.PrismReferenceValueImpl;
import com.evolveum.midpoint.prism.impl.marshaller.ItemPathHolder;
import com.evolveum.midpoint.prism.impl.query.*;
import com.evolveum.midpoint.prism.impl.xnode.PrimitiveXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.RootXNodeImpl;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.query.FuzzyStringMatchFilter;
import com.evolveum.midpoint.prism.query.FuzzyStringMatchFilter.FuzzyMatchingMethod;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.OrgFilter.Scope;
import com.evolveum.midpoint.prism.query.PrismQueryExpressionFactory;
import com.evolveum.midpoint.prism.query.PrismQueryLanguageParser;
import com.evolveum.midpoint.prism.query.TypeFilter;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.util.exception.SchemaException;

public class PrismQueryLanguageParserImpl implements PrismQueryLanguageParser {

    public static final String QUERY_NS = "http://prism.evolveum.com/xml/ns/public/query-3";
    public static final String MATCHING_RULE_NS = "http://prism.evolveum.com/xml/ns/public/matching-rule-3";

    private static final String POLYSTRING_ORIG = "orig";
    private static final String POLYSTRING_NORM = "norm";

    private static final String REF_OID = "oid";
    private static final String REF_TYPE = "targetType";
    private static final String REF_REL = "relation";
    private static final String REF_TARGET_ALIAS = "@";
    private static final String REF_TARGET = "target";
    private static final QName VALUES = new QName(PrismConstants.NS_QUERY, "values");

    private static final Map<String, Class<?>> POLYSTRING_PROPS = ImmutableMap.<String, Class<?>>builder()
            .put(POLYSTRING_ORIG, String.class).put(POLYSTRING_NORM, String.class).build();

    public interface ItemFilterFactory {
        ObjectFilter create(PrismContainerDefinition<?> parentDef, ComplexTypeDefinition typeDef, ItemPath itemPath, ItemDefinition<?> itemDef,
                QName matchingRule, SubfilterOrValueContext subfilterOrValue) throws SchemaException;
    }

    public static class FilterArgumentSpec<T> {
        public final QName name;
        public final boolean required;
        public final Class<T> type;
        public final T defaultValue;

        public FilterArgumentSpec(QName name, Class<T> type) {
            super();
            this.name = name;
            this.type = type;
            this.required = true;
            this.defaultValue = null;
        }

        public FilterArgumentSpec(QName name, Class<T> type, T defaultValue) {
            super();
            this.name = name;
            this.type = type;
            this.required = false;
            this.defaultValue = defaultValue;
        }
    }

    public static class ValuesArgument<T> extends FilterArgumentSpec<T> {

        public ValuesArgument(Class<T> type) {
            super(VALUES, type);
        }
    }


    private static final ValuesArgument<String> STRING_VALUES = new ValuesArgument<>(String.class);
    private static final FilterArgumentSpec<Integer> LEVENSHTEIN_THRESHOLD = new FilterArgumentSpec<>(FuzzyStringMatchFilter.THRESHOLD, Integer.class);
    private static final FilterArgumentSpec<Float> SIMILARITY_THRESHOLD = new FilterArgumentSpec<>(FuzzyStringMatchFilter.THRESHOLD, Float.class);
    private static final FilterArgumentSpec<Boolean> INCLUSIVE = new FilterArgumentSpec<>(FuzzyStringMatchFilter.INCLUSIVE, Boolean.class, true);


    private static final List<FilterArgumentSpec<?>> LEVENSHTEIN_ARGUMENTS = ImmutableList.of(
            STRING_VALUES,
            LEVENSHTEIN_THRESHOLD,
            INCLUSIVE
    );

    private static final List<FilterArgumentSpec<?>> SIMILARITY_ARGUMENTS = ImmutableList.of(
            STRING_VALUES,
            SIMILARITY_THRESHOLD,
            INCLUSIVE
    );

    private abstract class PropertyFilterFactory implements ItemFilterFactory {

        @Override
        public ObjectFilter create(PrismContainerDefinition<?> parentDef, ComplexTypeDefinition typeDef, ItemPath path, ItemDefinition<?> definition,
                QName matchingRule, SubfilterOrValueContext subfilterOrValue) throws SchemaException {
            schemaCheck(definition != null, "Path %s is not property", path);
            schemaCheck(subfilterOrValue != null, "Value or subfilter is missing");
            schemaCheck(definition instanceof PrismPropertyDefinition<?>, "Definition %s is not property", definition);
            PrismPropertyDefinition<?> propDef = (PrismPropertyDefinition<?>) definition;

            var expression = subfilterOrValue.expression();
            if (expression != null) {
                var expressionWrapper = parseExpression(expression);
                return expressionFilter(propDef, path, matchingRule, expressionWrapper);
            }

            var valueSet = subfilterOrValue.valueSet();
            if (valueSet != null) {

                ArrayList<Object> values = new ArrayList<>();
                for (SingleValueContext value : valueSet.values) {
                    schemaCheck(value.literalValue() != null, "Only literal value is supported if multiple values are enumerated");
                    values.add(parseLiteral(propDef, value.literalValue()));
                }
                return valuesFilter(propDef, path, matchingRule, values);
            }

            SingleValueContext valueSpec = subfilterOrValue.singleValue();
            schemaCheck(valueSpec != null, "Single value is required.");
            if (valueSpec.path() != null) {
                ItemPath rightPath = path(parentDef, valueSpec.path());
                if (isVariablePath(valueSpec.path())) {
                    return expressionFilter(propDef, path, matchingRule, parseExpression(rightPath));
                } else {
                    PrismPropertyDefinition<?> rightDef = findDefinition(parentDef, typeDef, rightPath, PrismPropertyDefinition.class);
                    schemaCheck(rightDef != null, "Path %s does not reference property", rightPath);
                    return propertyFilter(propDef, path, matchingRule, rightPath, rightDef);
                }
            } else if (valueSpec.literalValue() != null) {
                Object parsedValue = parseLiteral(propDef, valueSpec.literalValue());
                return valueFilter(propDef, path, matchingRule, parsedValue);
            }
            throw new IllegalStateException();
        }

        protected ObjectFilter valuesFilter(PrismPropertyDefinition<?> propDef, ItemPath path,
                QName matchingRule, ArrayList<Object> values) throws SchemaException {
            schemaCheck(false, "Multiple values are not supported");
            return null;
        }

        abstract ObjectFilter valueFilter(PrismPropertyDefinition<?> definition, ItemPath path, QName matchingRule,
                Object value) throws SchemaException;

        abstract ObjectFilter propertyFilter(PrismPropertyDefinition<?> definition, ItemPath path, QName matchingRule,
                ItemPath rightPath, PrismPropertyDefinition<?> rightDef) throws SchemaException;

        abstract ObjectFilter expressionFilter(PrismPropertyDefinition<?> definition, ItemPath path, QName matchingRule,
                ExpressionWrapper expression);

    }

    private abstract class FunctionLikeFilterFactory implements ItemFilterFactory {

        protected final String filterName;
        private final List<FilterArgumentSpec<?>> argumentDef;

        public FunctionLikeFilterFactory(QName filterName, List<FilterArgumentSpec<?>> argumentDef) {
            this.filterName = (filterName.getLocalPart());
            this.argumentDef = argumentDef;
        }


        @Override
        public ObjectFilter create(PrismContainerDefinition<?> parentDef, ComplexTypeDefinition typeDef,
                ItemPath itemPath, ItemDefinition<?> itemDef, QName matchingRule,
                SubfilterOrValueContext subfilterOrValue) throws SchemaException {
            schemaCheck(subfilterOrValue.valueSet() != null, "Filter %s requires set of arguments", filterName);

            Map<QName, Object> arguments = parsePositionalValueSet(argumentDef, subfilterOrValue.valueSet());

            return createFromArguments(itemPath, itemDef, matchingRule, arguments);
        }

        protected abstract ObjectFilter createFromArguments(ItemPath itemPath, ItemDefinition<?> itemDef,
                QName matchingRule, Map<QName, Object> arguments);

    }

    private abstract static class SelfFilterFactory implements ItemFilterFactory {

        protected final String filterName;

        public SelfFilterFactory(QName filterName) {
            this(filterName.getLocalPart());
        }

        public SelfFilterFactory(String filterName) {
            this.filterName = filterName;
        }

        @Override
        public ObjectFilter create(PrismContainerDefinition<?> parentDef, ComplexTypeDefinition typeDef, ItemPath itemPath, ItemDefinition<?> itemDef,
                QName matchingRule, SubfilterOrValueContext subfilterOrValue) throws SchemaException {
            schemaCheck(itemPath.isEmpty(), "Only '.' is supported for %s", filterName);
            return create(parentDef, matchingRule, subfilterOrValue);
        }

        protected abstract ObjectFilter create(PrismContainerDefinition<?> parentDef, QName matchingRule,
                SubfilterOrValueContext subfilterOrValue) throws SchemaException;

    }

    private class SubstringFilterFactory extends PropertyFilterFactory {

        private final boolean anchorStart;
        private final boolean anchorEnd;

        public SubstringFilterFactory(boolean anchorStart, boolean anchorEnd) {
            this.anchorStart = anchorStart;
            this.anchorEnd = anchorEnd;
        }

        @Override
        ObjectFilter propertyFilter(PrismPropertyDefinition<?> definition, ItemPath path, QName matchingRule,
                ItemPath rightPath, PrismPropertyDefinition<?> rightDef) throws SchemaException {
            throw new SchemaException("substring filter does not support path or right side.");
        }

        @Override
        ObjectFilter valueFilter(PrismPropertyDefinition<?> definition, ItemPath path, QName matchingRule, Object value) {
            return SubstringFilterImpl.createSubstring(path, definition, matchingRule, value, anchorStart,
                    anchorEnd);
        }

        @Override
        ObjectFilter expressionFilter(PrismPropertyDefinition<?> definition, ItemPath path, QName matchingRule,
                ExpressionWrapper expression) {
            return SubstringFilterImpl.createSubstring(path, definition, matchingRule, expression, anchorStart,
                    anchorEnd);
        }
    }

    private final ItemFilterFactory equalFilter = new PropertyFilterFactory() {

        @Override
        public ObjectFilter create(PrismContainerDefinition<?> parentDef, ComplexTypeDefinition typeDef, ItemPath path,
                ItemDefinition<?> definition, QName matchingRule, SubfilterOrValueContext subfilterOrValue)
                throws SchemaException {
            schemaCheck(subfilterOrValue != null, "Value or subfilter is missing");

            /*
             *  Special case for reference `ref` filter with expression, since expression can return
             *  object, we could use equals
             */
            if (definition instanceof PrismReferenceDefinition) {
                var refDef = (PrismReferenceDefinition) definition;
                if (subfilterOrValue.expression() != null) {
                    return RefFilterImpl.createReferenceEqual(path, refDef, parseExpression(subfilterOrValue.expression()));
                } else if (isVariablePath(subfilterOrValue.singleValue())) {
                    var rightPath = path(parentDef, subfilterOrValue.singleValue().path());
                    return RefFilterImpl.createReferenceEqual(path, refDef, parseExpression(rightPath));
                }
            }

            return super.create(parentDef, typeDef, path, definition, matchingRule, subfilterOrValue);
        }

        @Override
        public ObjectFilter valueFilter(PrismPropertyDefinition<?> definition, ItemPath path,
                QName matchingRule, Object value) {
            return EqualFilterImpl.createEqual(path, definition, matchingRule, value);
        }

        @Override
        public ObjectFilter expressionFilter(PrismPropertyDefinition<?> definition, ItemPath path,
                QName matchingRule, ExpressionWrapper value) {
            return EqualFilterImpl.createEqual(path, definition, matchingRule, value);
        }

        @Override
        public ObjectFilter propertyFilter(PrismPropertyDefinition<?> definition, ItemPath path,
                QName matchingRule, ItemPath rightPath, PrismPropertyDefinition<?> rightDef) {
            return EqualFilterImpl.createEqual(path, definition, matchingRule, rightPath, rightDef);
        }

        @Override
        protected ObjectFilter valuesFilter(PrismPropertyDefinition<?> definition, ItemPath path, QName matchingRule,
                ArrayList<Object> values) {
            return EqualFilterImpl.createEqual(path, definition, matchingRule, values.toArray());
        }
    };

    private final Map<QName, ItemFilterFactory> filterFactories = ImmutableMap.<QName, ItemFilterFactory>builder()
            .put(EQUAL, equalFilter)
            .put(ANY_IN, new PropertyFilterFactory() {

                @Override
                public ObjectFilter valueFilter(PrismPropertyDefinition<?> definition, ItemPath path,
                        QName matchingRule, Object value) {
                    return AnyInFilterImpl.createAnyIn(path, definition, matchingRule, value);
                }

                @Override
                public ObjectFilter expressionFilter(PrismPropertyDefinition<?> definition, ItemPath path,
                        QName matchingRule, ExpressionWrapper value) {
                    return AnyInFilterImpl.createAnyIn(path, definition, matchingRule, value);
                }

                @Override
                public ObjectFilter propertyFilter(PrismPropertyDefinition<?> definition, ItemPath path,
                        QName matchingRule, ItemPath rightPath, PrismPropertyDefinition<?> rightDef) {
                    return AnyInFilterImpl.createAnyIn(path, definition, matchingRule, rightPath, rightDef);
                }

                @Override
                protected ObjectFilter valuesFilter(PrismPropertyDefinition<?> definition, ItemPath path, QName matchingRule,
                        ArrayList<Object> values) {
                    return AnyInFilterImpl.createAnyIn(path, definition, matchingRule, values.toArray());
                }
            })
            .put(NOT_EQUAL, new ItemFilterFactory() {

                @Override
                public ObjectFilter create(PrismContainerDefinition<?> parentDef, ComplexTypeDefinition typeDef, ItemPath itemPath, ItemDefinition<?> itemDef,
                        QName matchingRule, SubfilterOrValueContext subfilterOrValue) throws SchemaException {
                    return NotFilterImpl.createNot(equalFilter.create(parentDef, typeDef, itemPath, itemDef, matchingRule, subfilterOrValue));
                }
            })
            .put(GREATER, new PropertyFilterFactory() {
                @Override
                ObjectFilter valueFilter(PrismPropertyDefinition<?> definition, ItemPath path, QName matchingRule,
                        Object value) {
                    return GreaterFilterImpl.createGreater(path, definition, matchingRule, value, false, context);
                }

                @Override
                ObjectFilter expressionFilter(PrismPropertyDefinition<?> definition, ItemPath path, QName matchingRule,
                        ExpressionWrapper value) {
                    return GreaterFilterImpl.createGreater(path, definition, matchingRule, value, false);
                }

                @Override
                ObjectFilter propertyFilter(PrismPropertyDefinition<?> definition, ItemPath path, QName matchingRule,
                        ItemPath rightPath, PrismPropertyDefinition<?> rightDef) {
                    return GreaterFilterImpl.createGreater(path, definition, matchingRule, rightPath, rightDef, false);
                }
            })
            .put(GREATER_OR_EQUAL, new PropertyFilterFactory() {
                @Override
                ObjectFilter valueFilter(PrismPropertyDefinition<?> definition, ItemPath path, QName matchingRule,
                        Object value) {
                    return GreaterFilterImpl.createGreater(path, definition, matchingRule, value, true, context);
                }

                @Override
                ObjectFilter expressionFilter(PrismPropertyDefinition<?> definition, ItemPath path, QName matchingRule,
                        ExpressionWrapper value) {
                    return GreaterFilterImpl.createGreater(path, definition, matchingRule, value, true);
                }

                @Override
                ObjectFilter propertyFilter(PrismPropertyDefinition<?> definition, ItemPath path, QName matchingRule,
                        ItemPath rightPath, PrismPropertyDefinition<?> rightDef) {
                    return GreaterFilterImpl.createGreater(path, definition, matchingRule, rightPath, rightDef, true);
                }
            })
            .put(LESS, new PropertyFilterFactory() {
                @Override
                ObjectFilter valueFilter(PrismPropertyDefinition<?> definition, ItemPath path, QName matchingRule,
                        Object value) {
                    return LessFilterImpl.createLess(path, definition, matchingRule, value, false, context);
                }

                @Override
                ObjectFilter expressionFilter(PrismPropertyDefinition<?> definition, ItemPath path, QName matchingRule,
                        ExpressionWrapper value) {
                    return LessFilterImpl.createLess(path, definition, matchingRule, value, false);
                }

                @Override
                ObjectFilter propertyFilter(PrismPropertyDefinition<?> definition, ItemPath path, QName matchingRule,
                        ItemPath rightPath, PrismPropertyDefinition<?> rightDef) {
                    return LessFilterImpl.createLess(path, definition, matchingRule, rightPath, rightDef, false);
                }
            })
            .put(LESS_OR_EQUAL, new PropertyFilterFactory() {
                @Override
                ObjectFilter valueFilter(PrismPropertyDefinition<?> definition, ItemPath path, QName matchingRule,
                        Object value) {
                    return LessFilterImpl.createLess(path, definition, matchingRule, value, true, context);
                }

                @Override
                ObjectFilter expressionFilter(PrismPropertyDefinition<?> definition, ItemPath path, QName matchingRule,
                        ExpressionWrapper value) {
                    return LessFilterImpl.createLess(path, definition, matchingRule, value, true);
                }

                @Override
                ObjectFilter propertyFilter(PrismPropertyDefinition<?> definition, ItemPath path, QName matchingRule,
                        ItemPath rightPath, PrismPropertyDefinition<?> rightDef) {
                    return LessFilterImpl.createLess(path, definition, matchingRule, rightPath, rightDef, true);
                }
            })
            .put(CONTAINS, new SubstringFilterFactory(false, false))
            .put(STARTS_WITH, new SubstringFilterFactory(true, false))
            .put(ENDS_WITH, new SubstringFilterFactory(false, true))
            .put(MATCHES, this::matchesFilter)
            .put(EXISTS, new ItemFilterFactory() {
                @Override
                public ObjectFilter create(PrismContainerDefinition<?> parentDef, ComplexTypeDefinition typeDef, ItemPath itemPath,
                        ItemDefinition<?> itemDef, QName matchingRule, SubfilterOrValueContext subfilterOrValue) {
                    return ExistsFilterImpl.createExists(itemPath, parentDef, null);
                }
            })
            .put(FULL_TEXT, new SelfFilterFactory("fullText") {

                @Override
                protected ObjectFilter create(PrismContainerDefinition<?> parentDef, QName matchingRule,
                        SubfilterOrValueContext subfilterOrValue) throws SchemaException {
                    return FullTextFilterImpl.createFullText(requireLiterals(String.class, filterName, subfilterOrValue));
                }
            })
            .put(IN_OID, new SelfFilterFactory("inOid") {

                @Override
                protected ObjectFilter create(PrismContainerDefinition<?> parentDef, QName matchingRule,
                        SubfilterOrValueContext subfilterOrValue) throws SchemaException {
                    return InOidFilterImpl.createInOid(requireLiterals(String.class, filterName, subfilterOrValue));
                }
            })
            .put(OWNED_BY_OID, new SelfFilterFactory("ownedByOid") {

                @Override
                protected ObjectFilter create(PrismContainerDefinition<?> parentDef, QName matchingRule,
                        SubfilterOrValueContext subfilterOrValue) throws SchemaException {
                    return InOidFilterImpl.createOwnerHasOidIn(requireLiterals(String.class, filterName, subfilterOrValue));
                }
            })
            .put(IN_ORG, new SelfFilterFactory("inOrg") {

                @Override
                protected ObjectFilter create(PrismContainerDefinition<?> parentDef, QName matchingRule,
                        SubfilterOrValueContext subfilterOrValue) throws SchemaException {
                    Scope scope = Scope.SUBTREE;
                    if (matchingRule != null) {
                        scope = Scope.valueOf(matchingRule.getLocalPart());
                    }
                    return OrgFilterImpl.createOrg(requireLiteral(String.class, filterName, subfilterOrValue.singleValue()), scope);
                }
            })
            .put(IS_ROOT, new SelfFilterFactory("isRoot") {

                @Override
                protected ObjectFilter create(PrismContainerDefinition<?> parentDef, QName matchingRule,
                        SubfilterOrValueContext subfilterOrValue) {
                    return OrgFilterImpl.createRootOrg();
                }
            })
            .put(TYPE, new SelfFilterFactory("type") {

                @Override
                protected ObjectFilter create(PrismContainerDefinition<?> parentDef, QName matchingRule,
                        SubfilterOrValueContext subfilterOrValue) throws SchemaException {
                    QName type = requireLiteral(QName.class, filterName, subfilterOrValue.singleValue());
                    return TypeFilterImpl.createType(type, null);
                }
            })
            .put(REFERENCED_BY, new SelfFilterFactory(REFERENCED_BY) {

                @Override
                protected ObjectFilter create(PrismContainerDefinition<?> parentDef, QName matchingRule, SubfilterOrValueContext subfilterOrValue) throws SchemaException {
                    var subfilter = subfilterOrValue.subfilterSpec().filter();

                    List<FilterContext> andChildren = new ArrayList<>();
                    expand(andChildren, AndFilterContext.class, AndFilterContext::filter, Collections.singletonList(subfilter));

                    QName type = consumeFromAnd(QName.class, "@type", andChildren);
                    ItemPath path = consumeFromAnd(ItemPath.class, "@path", andChildren);

                    QName relation = consumeFromAnd(QName.class, "@relation", andChildren);

                    var referrerSchema = context.getSchemaRegistry().findComplexTypeDefinitionByType(type);
                    var reffererCont = context.getSchemaRegistry().findContainerDefinitionByType(type);
                    ObjectFilter filter = andFilter(reffererCont, referrerSchema, andChildren);
                    return ReferencedByFilterImpl.create(type, path, filter, relation);
                }

            })
            .put(OWNED_BY, new SelfFilterFactory(OWNED_BY) {

                @Override
                protected ObjectFilter create(PrismContainerDefinition<?> parentDef, QName matchingRule, SubfilterOrValueContext subfilterOrValue) throws SchemaException {
                    var subfilter = subfilterOrValue.subfilterSpec().filter();

                    List<FilterContext> andChildren = new ArrayList<>();
                    expand(andChildren, AndFilterContext.class, AndFilterContext::filter, Collections.singletonList(subfilter));

                    QName type = consumeFromAnd(QName.class, "@type", andChildren);
                    ItemPath path = consumeFromAnd(ItemPath.class, "@path", andChildren);
                    var referrerSchema = context.getSchemaRegistry().findComplexTypeDefinitionByType(type);
                    ObjectFilter filter = andFilter(null, referrerSchema, andChildren);
                    return OwnedByFilterImpl.create(type, path, filter);
                }

            })
            .put(LEVENSHTEIN, new FunctionLikeFilterFactory(LEVENSHTEIN, LEVENSHTEIN_ARGUMENTS) {

                @Override
                protected ObjectFilter createFromArguments(ItemPath itemPath, ItemDefinition<?> itemDef,
                        QName matchingRule, Map<QName, Object> arguments) {
                    int threshold = getArgument(LEVENSHTEIN_THRESHOLD, arguments);
                    boolean inclusive = getArgument(INCLUSIVE, arguments);
                    FuzzyMatchingMethod method = FuzzyStringMatchFilter.levenshtein(threshold, inclusive );
                    var values = getValues(STRING_VALUES,arguments);
                    List<PrismPropertyValue<String>> prismValues = toPrismValues(values);
                    var filter = FuzzyStringMatchFilterImpl.create(itemPath,
                            (PrismPropertyDefinition<String>) itemDef, method, prismValues);
                    filter.setMatchingRule(matchingRule);
                    return filter;
                }
            })
            .put(SIMILARITY, new FunctionLikeFilterFactory(SIMILARITY, SIMILARITY_ARGUMENTS) {

                @Override
                protected ObjectFilter createFromArguments(ItemPath itemPath, ItemDefinition<?> itemDef,
                        QName matchingRule, Map<QName, Object> arguments) {
                    float threshold = getArgument(SIMILARITY_THRESHOLD, arguments);
                    boolean inclusive = getArgument(INCLUSIVE, arguments);
                    FuzzyMatchingMethod method = FuzzyStringMatchFilter.similarity(threshold, inclusive );
                    var values = getValues(STRING_VALUES,arguments);
                    List<PrismPropertyValue<String>> prismValues = toPrismValues(values);
                    var filter = FuzzyStringMatchFilterImpl.create(itemPath,
                            (PrismPropertyDefinition<String>) itemDef, method, prismValues);
                    filter.setMatchingRule(matchingRule);
                    return filter;
                }
            })
            .build();

    private final Map<QName, ItemFilterFactory> notFilterFactories = ImmutableMap.<QName, ItemFilterFactory>builder()
            .put(EXISTS, new ItemFilterFactory() {
                @Override
                public ObjectFilter create(PrismContainerDefinition<?> parentDef, ComplexTypeDefinition typeDef, ItemPath itemPath,
                        ItemDefinition<?> itemDef, QName matchingRule, SubfilterOrValueContext subfilterOrValue) {
                    if (itemDef instanceof PrismPropertyDefinition<?>) {
                        return EqualFilterImpl.createEqual(itemPath, (PrismPropertyDefinition<?>) itemDef,
                                matchingRule);
                    }
                    return NotFilterImpl.createNot(ExistsFilterImpl.createExists(itemPath, parentDef, null));
                }
            })
            .build();

    private final PrismContext context;
    private final Map<String, String> namespaceContext;
    private final PrismQueryExpressionFactory expressionParser;

    public PrismQueryLanguageParserImpl(PrismContext context) {
        this(context, ImmutableMap.of(), null);
    }

    private <T> List<PrismPropertyValue<T>> toPrismValues(List<T> rawValues) {
        List<PrismPropertyValue<T>> ret = new ArrayList<>();
        for (T raw : rawValues) {
            ret.add(new PrismPropertyValueImpl<T>(raw));
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getValues(ValuesArgument<T> def, Map<QName, Object> arguments) {
        Object maybe = arguments.get(def.name);
        if (maybe instanceof List<?>) {
            return (List) maybe;
        }
        if (maybe == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(def.type.cast(maybe));
    }

    private <T> T getArgument(FilterArgumentSpec<T> spec, Map<QName, Object> arguments) {
        Object maybe = arguments.get(spec.name);
        return spec.type.cast(maybe);
    }

    private Map<QName, Object> parsePositionalValueSet(List<FilterArgumentSpec<?>> definitions,
            ValueSetContext values) throws SchemaException {
        var defIter = definitions.iterator();
        var valueIter = values.values.iterator();
        Map<QName, Object> ret = new HashMap<>();
        while(defIter.hasNext()) {
            var def = defIter.next();
            schemaCheck(!def.required || valueIter.hasNext(), "Required argument %s is not specified", def.name.getLocalPart());
            if (valueIter.hasNext()) {
                var value = valueIter.next();
                if (value.literalValue() != null) {
                    ret.put(def.name, parseLiteral(def.type, value));
                }
                // TODO Add parsing path
            }
        }
        return ret;
    }

    protected boolean isVariablePath(SingleValueContext singleValue) {
        return singleValue != null && isVariablePath(singleValue.path());
    }

    public boolean isVariablePath(PathContext path) {
        return path.getText().contains("$");
    }

    public PrismQueryLanguageParserImpl(PrismContext context, Map<String, String> namespaceContext) {
        this(context, namespaceContext, null);
    }

    public PrismQueryLanguageParserImpl(PrismContext context, Map<String, String> namespaceContext, PrismQueryExpressionFactory expressionParser) {
        this.context = context;
        this.namespaceContext = namespaceContext;
        this.expressionParser = expressionParser;
    }

    ExpressionWrapper parseExpression(ItemPath rightPath) throws SchemaException {
        if (expressionParser == null) {
            throw new SchemaException("Expressions are not supported");
        }
        return expressionParser.parsePath(rightPath);
    }

    ExpressionWrapper parseExpression(ExpressionContext expression) throws SchemaException {
        if (expressionParser == null) {
            throw new SchemaException("Expressions are not supported");
        }
        if (expression.constant() != null) {
            return parseConstant(expression.constant());
        }

        if (expression.script() == null) {
            throw new SchemaException("Expression '" + expression.getText() + "' must contain script");
        }
        return parseScript(expression.script());
    }

    private ExpressionWrapper parseConstant(ConstantContext constant) {
        return expressionParser.parseScript(namespaceContext, "const", constant.name.getText());
    }

    private ExpressionWrapper parseScript(ScriptContext script) {
        String scriptLang = script.language != null ? script.language.getText() : null;
        String scriptText;
        if (script.scriptMultiline() != null) {
            scriptText = AxiomStrings.removeQuotes(AxiomStrings.TRIPLE_BACKTICK, script.scriptMultiline().getText());
        } else if (script.scriptSingleline() != null) {
            scriptText = AxiomStrings.fromSingleBacktick(script.scriptSingleline().getText());
        } else {
            throw new IllegalStateException("No script present.");
        }
        return expressionParser.parseScript(namespaceContext, scriptLang, scriptText);
    }

    public Object parseLiteral(PrismPropertyDefinition<?> propDef, LiteralValueContext literalValue) {
        if (propDef.getTypeClass() != null) {
            // shortcut
            return parseLiteral(propDef.getTypeClass(), literalValue);
        }
        PrismNamespaceContext nsCtx = PrismNamespaceContext.from(namespaceContext);
        RootXNodeImpl xnode = new RootXNodeImpl(propDef.getItemName(), nsCtx);
        xnode.setSubnode(new PrimitiveXNodeImpl<>(extractTextForm(literalValue), nsCtx));
        try {
            PrismPropertyValue<?> itemValue = context.parserFor(xnode).definition(propDef).parseItemValue();
            return itemValue.getRealValue();
        } catch (SchemaException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    protected <T> Collection<T> requireLiterals(Class<T> type, String filterName, SubfilterOrValueContext subfilterOrValue) throws SchemaException {
        schemaCheck(subfilterOrValue.subfilterSpec() == null, "Value required for filter %s", filterName);
        if (subfilterOrValue.singleValue() != null) {
            return Collections.singletonList(requireLiteral(type, filterName, subfilterOrValue.singleValue()));
        } else if (subfilterOrValue.valueSet() != null) {
            ValueSetContext valueSet = subfilterOrValue.valueSet();
            ArrayList<T> ret = new ArrayList<>(valueSet.values.size());
            for (SingleValueContext value : valueSet.values) {
                ret.add(requireLiteral(type, filterName, value));
            }
            return ret;
        }
        throw new IllegalStateException();
    }

    protected <T> T requireLiteral(Class<T> type, String filterName, SingleValueContext value) throws SchemaException {
        schemaCheck(value != null, "%s literal must be specified for %s.", type.getSimpleName(), filterName);
        schemaCheck(value.literalValue() != null || value.path() != null, "%s literal must be specified for %s.", type.getSimpleName(), filterName);
        return parseLiteral(type, value);
    }

    @Override
    public <C extends Containerable> ObjectFilter parseQuery(Class<C> typeClass, String query) throws SchemaException {
        return parseQuery(typeClass, AxiomQuerySource.from(query));
    }

    @Override
    public ObjectFilter parseQuery(PrismContainerDefinition<?> definition, String query) throws SchemaException {
        return parseQuery(definition, AxiomQuerySource.from(query));
    }

    public <C extends Containerable> ObjectFilter parseQuery(Class<C> typeClass, AxiomQuerySource source)
            throws SchemaException {
        PrismContainerDefinition<?> complexType = context.getSchemaRegistry()
                .findContainerDefinitionByCompileTimeClass(typeClass);
        if (complexType == null) {
            throw new IllegalArgumentException("Couldn't find definition for complex type " + typeClass);
        }
        return parseQuery(complexType, source);
    }

    public ObjectFilter parseQuery(PrismContainerDefinition<?> definition, AxiomQuerySource source)
            throws SchemaException {
        return parseFilter(definition, definition.getComplexTypeDefinition(), source.root());
    }

    private ObjectFilter parseFilter(PrismContainerDefinition<?> container, ComplexTypeDefinition typeDef, FilterContext root)
            throws SchemaException {
        if (root instanceof AndFilterContext) {
            return andFilter(container, typeDef, (AndFilterContext) root);
        } else if (root instanceof OrFilterContext) {
            return orFilter(container, typeDef, (OrFilterContext) root);
        } else if (root instanceof GenFilterContext) {
            return itemFilter(container, typeDef, ((GenFilterContext) root).itemFilter());
        } else if (root instanceof SubFilterContext) {
            return parseFilter(container, typeDef, ((SubFilterContext) root).subfilterSpec().filter());
        }
        throw new IllegalStateException("Unsupported Filter Context");
    }

    private ObjectFilter andFilter(PrismContainerDefinition<?> complexType, ComplexTypeDefinition typeDef, AndFilterContext root)
            throws SchemaException {
        List<FilterContext> unparsed = new ArrayList<>();

        expand(unparsed, AndFilterContext.class, AndFilterContext::filter, root.filter());
        return andFilter(complexType, typeDef, unparsed);
    }

    private ObjectFilter andFilter(PrismContainerDefinition<?> complexType, ComplexTypeDefinition typeDef, List<FilterContext> unparsed) throws SchemaException {
        ImmutableList.Builder<ObjectFilter> filters = ImmutableList.builder();

        TypeFilter typeFilter = null;
        var iterator = unparsed.iterator();
        while (iterator.hasNext()) {
            var next = iterator.next();
            if (next instanceof GenFilterContext) {
                ItemFilterContext itemFilter = ((GenFilterContext) next).itemFilter();
                // If AND contains type filter, we extract it out in order to determine
                // more specific type
                if (itemFilter.negation() == null && FilterNames.TYPE.equals(filterName(itemFilter))) {
                    typeFilter = (TypeFilter) itemFilter(complexType, typeDef, itemFilter);
                    iterator.remove(); // We remove it from subfilters, since we are moving it u
                }
            }
        }
        if (typeFilter != null) {
            typeDef = complexType.getPrismContext().getSchemaRegistry().findComplexTypeDefinitionByType(typeFilter.getType());
        }
        for (FilterContext filter : unparsed) {
            filters.add(parseFilter(complexType, typeDef, filter));
        }
        ObjectFilter andFilter = context.queryFactory().createAndOptimized(filters.build());
        if (typeFilter != null) {
            typeFilter.setFilter(andFilter);
            return typeFilter;
        }
        return andFilter;
    }

    private <E extends FilterContext> void expand(List<FilterContext> expanded, Class<E> expandable, Function<E, List<FilterContext>> expander, List<FilterContext> notExpanded) {
        for (FilterContext filterContext : notExpanded) {
            if (filterContext instanceof SubFilterContext) {
                var subfilter = (SubFilterContext) filterContext;
                var nestedFilter = subfilter.subfilterSpec().filter();
                // Subfilter is of same type as parent filter, so we can safely remove subfilter and use nested filter
                if (expandable.isInstance(nestedFilter)) {
                    filterContext = nestedFilter;
                }
            }
            if (expandable.isInstance(filterContext)) {
                expand(expanded, expandable, expander, expander.apply(expandable.cast(filterContext)));
            } else {
                expanded.add(filterContext);
            }
        }
    }

    private ObjectFilter orFilter(PrismContainerDefinition<?> complexType, ComplexTypeDefinition typeDef, OrFilterContext root)
            throws SchemaException {
        List<FilterContext> unparsed = new ArrayList<>();
        expand(unparsed, OrFilterContext.class, OrFilterContext::filter, root.filter());

        Builder<ObjectFilter> filters = ImmutableList.builder();
        for (FilterContext filterContext : unparsed) {
            filters.add(parseFilter(complexType, typeDef, filterContext));
        }
        return context.queryFactory().createOrOptimized(filters.build());
    }

    private ObjectFilter itemFilter(PrismContainerDefinition<?> parent, ComplexTypeDefinition typeDef, ItemFilterContext itemFilter)
            throws SchemaException {
        QName filterName = filterName(itemFilter);
        QName matchingRule = itemFilter.matchingRule() != null
                ? toFilterName(MATCHING_RULE_NS, itemFilter.matchingRule().prefixedName())
                : null;
        ItemPath path = path(parent, itemFilter.path());
        ItemDefinition<?> itemDefinition = findDefinition(parent, typeDef, path, ItemDefinition.class);
        schemaCheck(itemDefinition != null, "Path %s is not present in type %s", path, typeDef.getTypeName());
        ItemFilterFactory factory = filterFactories.get(filterName);
        schemaCheck(factory != null, "Unknown filter %s", filterName);

        if (itemFilter.negation() != null) {
            ItemFilterFactory notFactory = notFilterFactories.get(filterName);
            if (notFactory != null) {
                return notFactory.create(parent, typeDef, path, itemDefinition, matchingRule, itemFilter.subfilterOrValue());
            }
        }
        ObjectFilter filter = createItemFilter(factory, parent, typeDef, path, itemDefinition, matchingRule,
                itemFilter.subfilterOrValue());
        if (itemFilter.negation() != null) {
            return new NotFilterImpl(filter);
        }
        return filter;

    }

    private <T extends ItemDefinition<?>> T findDefinition(PrismContainerDefinition<?> parent, ComplexTypeDefinition typeDef, ItemPath path, Class<T> type) {
        if (path.isEmpty() && type.isInstance(parent)) {
            return type.cast(parent);
        }
        // FIXME: Workaround for
        return typeDef.findItemDefinition(path, type);
    }

    private ObjectFilter createItemFilter(ItemFilterFactory factory, PrismContainerDefinition<?> parent, ComplexTypeDefinition typeDef, ItemPath path,
            ItemDefinition<?> itemDef, QName matchingRule, SubfilterOrValueContext subfilterOrValue)
            throws SchemaException {
        return factory.create(parent, typeDef, path, itemDef, matchingRule, subfilterOrValue);
    }

    private ItemPath path(PrismContainerDefinition<?> complexType, PathContext path) {
        // FIXME: Implement proper parsing of decomposed item path from Antlr
        return ItemPathHolder.parseFromString(path.getText(), namespaceContext);
    }

    private QName filterName(ItemFilterContext filter) {
        if (filter.filterNameAlias() != null) {
            return FilterNames.fromAlias(filter.filterNameAlias().getText()).orElseThrow();
        }
        FilterNameContext filterName = filter.filterName();
        if (filterName.filterNameAlias() != null) {
            return FilterNames.fromAlias(filterName.filterNameAlias().getText()).orElseThrow();
        }
        return toFilterName(QUERY_NS, filterName.prefixedName());
    }

    private QName toFilterName(String defaultNs, PrefixedNameContext itemName) {
        // FIXME: Add namespace detection
        return new QName(defaultNs, itemName.localName.getText());
    }

    private <T> T parseLiteral(Class<T> targetType, LiteralValueContext string) {
        String text = extractTextForm(string);
        return XmlTypeConverter.toJavaValue(text, namespaceContext, targetType);
    }

    private <T> T parseLiteral(Class<T> targetType, SingleValueContext singleValue) throws SchemaException {
        if (QName.class.equals(targetType)) {
            schemaCheck(singleValue.path() instanceof DescendantPathContext, "Invalid value for QName");
            DescendantPathContext path = (DescendantPathContext) singleValue.path();
            String text = path.itemPathComponent().get(0).getText();
            return XmlTypeConverter.toJavaValue(text, namespaceContext, targetType);
        } else {
            return parseLiteral(targetType, singleValue.literalValue());
        }
    }

    private String extractTextForm(LiteralValueContext string) {
        if (string instanceof StringValueContext) {
            return AxiomAntlrLiterals.convertString((StringValueContext) string);
        }
        return string.getText();
    }

    private ObjectFilter matchesFilter(PrismContainerDefinition<?> parent, ComplexTypeDefinition typeDef, ItemPath path, ItemDefinition<?> definition,
            QName matchingRule, SubfilterOrValueContext subfilterOrValue) throws SchemaException {
        schemaCheck(subfilterOrValue.subfilterSpec() != null, "matches filter requires subfilter");
        if (definition instanceof PrismContainerDefinition<?>) {
            PrismContainerDefinition<?> containerDef = (PrismContainerDefinition<?>) definition;
            FilterContext subfilterTree = subfilterOrValue.subfilterSpec().filter();
            ObjectFilter subfilter = parseFilter(containerDef, containerDef.getComplexTypeDefinition(), subfilterTree);
            return ExistsFilterImpl.createExists(path, (PrismContainerDefinition<?>) parent, subfilter);
        } else if (definition instanceof PrismReferenceDefinition) {
            return matchesReferenceFilter(path, (PrismReferenceDefinition) definition,
                    subfilterOrValue.subfilterSpec().filter());
        } else if (definition instanceof PrismPropertyDefinition<?>) {
            if (PolyString.class.isAssignableFrom(definition.getTypeClass())) {
                return matchesPolystringFilter(path, (PrismPropertyDefinition<?>) definition,
                        subfilterOrValue.subfilterSpec().filter());
            }
        }
        throw new UnsupportedOperationException("Unknown schema type");
    }

    /**
     * <code>
     * name matches (orig = "foo")
     * name matches (norm = "bar")
     * name matches (orig = "foo" and norm = "bar")
     * </code>
     */
    private ObjectFilter matchesPolystringFilter(ItemPath path, PrismPropertyDefinition<?> definition,
            FilterContext filter) throws SchemaException {
        Map<String, Object> props = valuesFromFilter("PolyString", POLYSTRING_PROPS, filter, new HashMap<>(), true);
        String orig = (String) props.get(POLYSTRING_ORIG);
        String norm = (String) props.get(POLYSTRING_NORM);
        schemaCheck(orig != null || norm != null, "orig or norm must be defined in matches polystring filter.");
        if (orig != null && norm != null) {
            return EqualFilterImpl.createEqual(path, definition, PrismConstants.POLY_STRING_STRICT_MATCHING_RULE_NAME, new PolyString(orig, norm));
        }
        if (orig != null) {
            return EqualFilterImpl.createEqual(path, definition, PrismConstants.POLY_STRING_ORIG_MATCHING_RULE_NAME, new PolyString(orig));
        } else if (norm != null) {
            return EqualFilterImpl.createEqual(path, definition, PrismConstants.POLY_STRING_NORM_MATCHING_RULE_NAME, new PolyString(norm, norm));
        }
        throw new SchemaException("Incorrect syntax for matches polystring");
    }

    @SuppressWarnings("unchecked")
    private <T> T extractValue(Class<T> type, SubfilterOrValueContext subfilterOrValue) throws SchemaException {
        schemaCheck(subfilterOrValue.singleValue() != null, "Constant value required");
        if (QName.class.isAssignableFrom(type)) {
            PathContext path = subfilterOrValue.singleValue().path();
            schemaCheck(path != null, "QName value expected");
            return (T) XmlTypeConverter.toJavaValue(path.getText(), new HashMap<>(), QName.class);

        }
        var singleValue = subfilterOrValue.singleValue();
        schemaCheck(singleValue != null, "Single value is required.");

        if (ItemPath.class.equals(type)) {
            var pathContext = singleValue.path();
            schemaCheck(pathContext != null, "path is required");
            return (T) path(null, pathContext);
        }

        LiteralValueContext literalContext = singleValue.literalValue();
        schemaCheck(literalContext != null, "Literal value required");
        return type.cast(parseLiteral(type, literalContext));
    }

    /**
     * oidAsAny targetAsAny relationshipAsAny
     */
    private ObjectFilter matchesReferenceFilter(ItemPath path, PrismReferenceDefinition definition,
            FilterContext filter) throws SchemaException {
        List<FilterContext> andChildren = new ArrayList<>();
        expand(andChildren, AndFilterContext.class, AndFilterContext::filter, Collections.singletonList(filter));

        boolean oidNullAsAny = !andContains(REF_OID, andChildren);
        boolean typeNullAsAny = !andContains(REF_TYPE, andChildren);

        String oid = consumeFromAnd(String.class, REF_OID, andChildren);
        QName relation = consumeFromAnd(QName.class, REF_REL, andChildren);
        QName type = consumeFromAnd(QName.class, REF_TYPE, andChildren);

        QName targetType = definition.getTargetTypeName();
        if (targetType == null) {
            targetType = type;
        } else {
            targetType = context.getSchemaRegistry().selectMoreSpecific(type, targetType);
        }

        ObjectFilter targetFilter = null;
        if (andChildren.size() == 1) {
            var targetCtx = consumeFromAnd(REF_TARGET_ALIAS, MATCHES, andChildren);
            if (targetCtx == null) {
                targetCtx = consumeFromAnd(REF_TARGET, MATCHES, andChildren);
            }
            if (targetCtx == null) {
                throw new SchemaException("Additional unsupported filter specified: " + andChildren.get(0).getText());
            }
            var targetSchema = context.getSchemaRegistry().findObjectDefinitionByType(targetType);
            targetFilter = parseFilter(targetSchema, targetSchema.getComplexTypeDefinition(), targetCtx.subfilterOrValue().subfilterSpec().filter());
        }

        PrismReferenceValue value = new PrismReferenceValueImpl(oid, type);
        value.setRelation(relation);
        RefFilterImpl result = (RefFilterImpl) RefFilterImpl.createReferenceEqual(path, definition,
                Collections.singletonList(value), targetFilter);
        result.setOidNullAsAny(oidNullAsAny);
        result.setTargetTypeNullAsAny(typeNullAsAny);

        return result;
    }

    private Map<String, Object> valuesFromFilter(String typeName, Map<String, Class<?>> props, FilterContext child,
            Map<String, Object> result, boolean strict) throws SchemaException {
        if (child instanceof GenFilterContext) {
            ItemFilterContext filter = ((GenFilterContext) child).itemFilter();
            if (EQUAL.equals(filterName(filter))) {
                String name = filter.path().getText();
                Class<?> propType = props.get(name);
                schemaCheck(propType != null, "Unknown property %s for %s", name, typeName);
                if (name.equals(filter.path().getText())) {
                    result.put(name, extractValue(propType, filter.subfilterOrValue()));
                }
            } else if (strict) {
                throw new SchemaException("Only 'equals' and 'and' filters are supported.");
            }
        } else if (child instanceof AndFilterContext) {
            valuesFromFilter(typeName, props, ((AndFilterContext) child).left, result, strict);
            valuesFromFilter(typeName, props, ((AndFilterContext) child).right, result, strict);
        } else if (strict) {
            throw new SchemaException("Only 'equals' and 'and' filters are supported.");
        }
        return result;
    }

    private void collectAndChildren(FilterContext filter, Collection<FilterContext> result) {
        if (filter instanceof AndFilterContext) {
            var and = (AndFilterContext) filter;
            collectAndChildren(and.left, result);
            collectAndChildren(and.right, result);

        } else if (filter instanceof SubFilterContext) {
            FilterContext child = ((SubFilterContext) filter).subfilterSpec().filter();
            if (child instanceof AndFilterContext) {
                collectAndChildren(child, result);
            } else {
                result.add(filter);
            }
        } else {
            result.add(filter);
        }
    }

    private <T> T consumeFromAnd(Class<T> valueType, String path, Collection<FilterContext> andFilters) throws SchemaException {
        ItemFilterContext maybe = consumeFromAnd(path, EQUAL, andFilters);
        if (maybe != null) {
            return extractValue(valueType, maybe.subfilterOrValue());
        }
        return null;
    }

    private ItemFilterContext consumeFromAnd(String path, QName filterName, Collection<FilterContext> andFilters) {
        var iterator = andFilters.iterator();
        while (iterator.hasNext()) {
            var maybe = iterator.next();
            if (maybe instanceof GenFilterContext) {
                ItemFilterContext filter = ((GenFilterContext) maybe).itemFilter();
                // If we have equals filter and name matches, extract value and remove it from list
                // for further processing
                if (path.equals(filter.path().getText()) && (filterName == null || filterName.equals(filterName(filter)))) {
                    iterator.remove();
                    return filter;
                }
            }
        }
        return null;
    }

    private boolean andContains(String path, Collection<FilterContext> andFilters) {
        for (FilterContext maybe : andFilters) {
            if (maybe instanceof GenFilterContext) {
                ItemFilterContext filter = ((GenFilterContext) maybe).itemFilter();
                // If we have equals filter and name matches, extract value and remove it from list
                // for further processing
                if (EQUAL.equals(filterName(filter)) && path.equals(filter.path().getText())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static PrismQueryLanguageParserImpl create(PrismContext prismContext) {
        return new PrismQueryLanguageParserImpl(prismContext);
    }
}
