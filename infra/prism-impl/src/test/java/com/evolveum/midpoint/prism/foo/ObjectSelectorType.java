package com.evolveum.midpoint.prism.foo;

import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismReferenceValue;
import com.evolveum.midpoint.prism.impl.PrismReferenceValueImpl;
import com.evolveum.midpoint.prism.impl.binding.AbstractMutableContainerable;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.util.Producer;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;

import jakarta.xml.bind.annotation.*;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "ObjectSelectorType", propOrder = {
        "name",
        "description",
        "documentation",
        "parent",
        "type",
        "subtype",
        "archetypeRef",
        "orgRef",
        "filter"
})
@XmlSeeAlso({
        SubjectedObjectSelectorType.class
})
public class ObjectSelectorType  extends AbstractMutableContainerable {

}
