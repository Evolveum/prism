/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.query.builder;

import com.evolveum.midpoint.prism.ComplexTypeDefinition;
import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.query.builder.S_FilterEntryOrEmpty;

/**
 * Here is the language structure:
 * <pre>
 * Query ::= Filter? ('ASC(path)' | 'DESC(path)')* 'OFFSET(n)'? 'MAX-SIZE(n)'?
 *
 * Filter ::= 'ALL'
 * | 'NONE'
 * | 'UNDEFINED'
 * | 'NOT' Filter
 * | Filter ('AND'|'OR') Filter
 * | 'BLOCK' Filter? 'END-BLOCK'
 * | 'TYPE(type)' Filter?
 * | 'EXISTS(path)' Filter?
 * | 'REF(path, values*?)' Filter?
 * | 'REFERENCED-BY(class, path, relation?)' Filter?
 * | 'OWNED-BY(class, path?)' Filter?
 * | 'FULLTEXT(value)'
 * | 'ITEM(path)' ValueComparisonCondition
 * | 'ITEM(path)' ItemComparisonCondition 'ITEM(path)'
 * | 'ITEM(path)' 'IS-NULL'
 * | 'ID(values)'
 * | 'OWNER-ID(values)'
 * | OrgFilter
 *
 * ValueComparisonCondition ::= 'EQ(value)' | 'GT(value)' | 'GE(value)' | 'LT(value)' | 'LE(value)'
 * | 'STARTS-WITH(value)' | 'ENDS-WITH(value)' | 'CONTAINS(value)'
 * | 'REF(value)'
 * | 'ORG(value)'
 *
 * ItemComparisonCondition ::= 'EQ' | 'GT' | 'GE' | 'LT' | 'LE'
 *
 * OrgFilter ::= 'IS-ROOT'
 * | 'IS-DIRECT-CHILD-OF(value)'
 * | 'IS-CHILD-OF(value)'
 * | 'IS-PARENT-OF(value)'
 * | 'IS-IN-SCOPE-OF(value, scope)'
 * </pre>
 *
 * It can be visualized e.g. using https://www.bottlecaps.de/rr/ui
 * <p/>
 * Individual keywords ('AND', 'OR', 'BLOCK', ...) are mapped to methods.
 * Connections between these keywords are mapped to interfaces.
 * It can be viewed as interfaces = states, keywords = transitions. (Or vice versa, but this is more natural.)
 * The interfaces have names starting with S_ (for "state").
 * <p/>
 * Interfaces are implemented by classes that aggregate state of the query being created.
 * This is quite hacked for now... to be implemented more seriously.
 */
public final class QueryBuilder {

    private final Class<? extends Containerable> queryClass;
    private final PrismContext prismContext;

    private QueryBuilder(Class<? extends Containerable> queryClass, PrismContext prismContext) {
        this.queryClass = queryClass;
        this.prismContext = prismContext;
        ComplexTypeDefinition containerCTD = prismContext.getSchemaRegistry().findComplexTypeDefinitionByCompileTimeClass(queryClass);
        if (containerCTD == null) {
            throw new IllegalArgumentException("Couldn't find definition for complex type " + queryClass);
        }
    }

    Class<? extends Containerable> getQueryClass() {
        return queryClass;
    }

    public PrismContext getPrismContext() {
        return prismContext;
    }

    public static S_FilterEntryOrEmpty queryFor(Class<? extends Containerable> queryClass, PrismContext prismContext) {
        return R_Filter.create(new QueryBuilder(queryClass, prismContext));
    }
}
