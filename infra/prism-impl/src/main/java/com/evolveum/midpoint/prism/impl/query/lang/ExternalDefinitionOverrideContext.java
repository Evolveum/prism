package com.evolveum.midpoint.prism.impl.query.lang;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser.ItemFilterContext;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.path.ItemPathComparatorUtil;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.ValueFilter;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class ExternalDefinitionOverrideContext implements DefinitionOverrideContext {

    private abstract class ValueContext<T extends PrismValue> {

        T value;

        abstract boolean isApplicable(ItemFilterContext itemFilter);

        void applyValue(ValueFilter<T,?> valueFilter) {
            value = valueFilter.getSingleValue();
        }

        boolean isKnown() {
            return value != null;
        }
    }

    private  class RefValueContext extends ValueContext<PrismReferenceValue> {

        boolean isApplicable(ItemFilterContext itemFilter) {
            // only matches with subfilter specified is acceptable

            return FilterNames.MATCHES.equals(parser.filterName(itemFilter))
                && itemFilter.subfilterOrValue() != null
                && itemFilter.subfilterOrValue().subfilterSpec() != null;
        }

    }

    private class SimpleValueContext extends ValueContext<PrismPropertyValue<?>> {

        boolean isApplicable(ItemFilterContext itemFilter) {
            // Only equal filter with single value is acceptable
            return FilterNames.EQUAL.equals(parser.filterName(itemFilter))
                    && itemFilter.subfilterOrValue() != null
                    && itemFilter.subfilterOrValue().singleValue() != null;
        }

    }


    final PrismQueryLanguageParserImpl parser;

    final ValueBasedDefinitionLookupHelper schemaLookup;
    Map<ItemPath, ValueContext> items = new HashMap<>();

    public ExternalDefinitionOverrideContext(PrismQueryLanguageParserImpl parser, ComplexTypeDefinition typeDef, Set<ItemPath> items, ValueBasedDefinitionLookupHelper schemaLookup) {
        this.parser = parser;
        this.schemaLookup = schemaLookup;

        for (var item : items) {
            var def = typeDef.findItemDefinition(item);
            if (def instanceof PrismPropertyDefinition<?>) {
                this.items.put(item, new SimpleValueContext());
            } else if (def instanceof PrismReferenceDefinition) {
                this.items.put(item, new RefValueContext());
            } else {
                throw new IllegalArgumentException("Only Reference and Property values are supported");
            }
        }
    }

    @Nullable ValueContext<?> findValueSpec(ItemPath path) {
        for (var item : items.entrySet()) {
            if (ItemPathComparatorUtil.equivalent(path, item.getKey())) {
                return item.getValue();
            }
        }
        return null;
    }

    @Override
    public boolean isApplicable(QueryParsingContext.Local context, ItemFilterContext itemFilter) {
        ItemPath path = parser.path(context.itemDef(), itemFilter.path());
        QName filterName = parser.filterName(itemFilter);
        var valueSpec = findValueSpec(path);
        if (valueSpec == null) {
            return false;
        }
        return (valueSpec.isApplicable(itemFilter));
    }

    @Override
    public void process(QueryParsingContext.Local context, ItemFilterContext itemFilter) throws SchemaException {
        // This is guarder by isApplicable
        // should only parse filters we are interested in
        var filter = (ValueFilter<?,?>) parser.itemFilter(context, itemFilter);
        var valueSpec = findValueSpec(filter.getPath());
        if (valueSpec != null) {
            //noinspection rawtypes
            valueSpec.applyValue((ValueFilter) filter);
        }


    }

    @Override
    public boolean shouldRemove(ItemFilterContext itemFilter) {
        return false;
    }

    @Override
    public ObjectFilter toFilter() {
        return null;
    }

    @Override
    public boolean isComplete() {
        // All required items must have value associated
        for (var item : items.entrySet()) {
            if (!item.getValue().isKnown()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void apply(QueryParsingContext.Local context) {

        Map<ItemPath, PrismValue> hintValues = new HashMap<>();
        for (var item : items.entrySet()) {
            hintValues.put(item.getKey(), item.getValue().value);
        }
        var typeDef = schemaLookup.findComplexTypeDefinition(schemaLookup.baseTypeName(), hintValues);
        if (typeDef != null) {
            context.typeDef(typeDef);
        }
    }

    @Override
    public boolean addsFilter() {
        return false;
    }
}
