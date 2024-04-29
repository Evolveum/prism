package com.evolveum.midpoint.prism.foo;

import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.impl.xjc.PrismForJAXBUtil;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.util.Producer;
import com.evolveum.prism.xml.ns._public.query_3.PagingType;

import jakarta.xml.bind.annotation.*;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "GuiObjectListPanelConfigurationType", propOrder = {
        "searchBoxConfiguration",
        "distinct",
        "column",
        "includeDefaultColumns",
        "disableSorting",
        "disableCounting",
        "refreshInterval",
        "paging",
        "dataProvider"
})
@XmlSeeAlso({
        GuiObjectListViewType.class
})
public class GuiObjectListPanelConfigurationType extends UserInterfaceFeatureType {

    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "GuiObjectListPanelConfigurationType");
    public static final ItemName F_INCLUDE_DEFAULT_COLUMNS = new ItemName(ObjectType.NS_FOO, "includeDefaultColumns");
    public static final ItemName F_DISABLE_SORTING = new ItemName(ObjectType.NS_FOO, "disableSorting");
    public static final ItemName F_DISABLE_COUNTING = new ItemName(ObjectType.NS_FOO, "disableCounting");
    public static final ItemName F_REFRESH_INTERVAL = new ItemName(ObjectType.NS_FOO, "refreshInterval");
    public static final ItemName F_PAGING = new ItemName(ObjectType.NS_FOO, "paging");

    public GuiObjectListPanelConfigurationType() {
        super();
    }

    @Deprecated
    public GuiObjectListPanelConfigurationType(PrismContext context) {
        super();
    }

    @XmlElement(name = "includeDefaultColumns")
    public Boolean isIncludeDefaultColumns() {
        return this.prismGetPropertyValue(F_INCLUDE_DEFAULT_COLUMNS, Boolean.class);
    }

    public void setIncludeDefaultColumns(Boolean value) {
        this.prismSetPropertyValue(F_INCLUDE_DEFAULT_COLUMNS, value);
    }

    @XmlElement(name = "disableSorting")
    public Boolean isDisableSorting() {
        return this.prismGetPropertyValue(F_DISABLE_SORTING, Boolean.class);
    }

    public void setDisableSorting(Boolean value) {
        this.prismSetPropertyValue(F_DISABLE_SORTING, value);
    }

    @XmlElement(name = "disableCounting")
    public Boolean isDisableCounting() {
        return this.prismGetPropertyValue(F_DISABLE_COUNTING, Boolean.class);
    }

    public void setDisableCounting(Boolean value) {
        this.prismSetPropertyValue(F_DISABLE_COUNTING, value);
    }

    @XmlElement(name = "refreshInterval")
    public Integer getRefreshInterval() {
        return this.prismGetPropertyValue(F_REFRESH_INTERVAL, Integer.class);
    }

    public void setRefreshInterval(Integer value) {
        this.prismSetPropertyValue(F_REFRESH_INTERVAL, value);
    }

    @XmlElement(name = "paging")
    public PagingType getPaging() {
        return this.prismGetPropertyValue(F_PAGING, PagingType.class);
    }

    public void setPaging(PagingType value) {
        this.prismSetPropertyValue(F_PAGING, value);
    }


    public boolean equals(Object other) {
        return super.equals(other);
    }

    public GuiObjectListPanelConfigurationType id(Long value) {
        setId(value);
        return this;
    }

    public GuiObjectListPanelConfigurationType includeDefaultColumns(Boolean value) {
        setIncludeDefaultColumns(value);
        return this;
    }

    public GuiObjectListPanelConfigurationType disableSorting(Boolean value) {
        setDisableSorting(value);
        return this;
    }

    public GuiObjectListPanelConfigurationType disableCounting(Boolean value) {
        setDisableCounting(value);
        return this;
    }

    public GuiObjectListPanelConfigurationType refreshInterval(Integer value) {
        setRefreshInterval(value);
        return this;
    }

    public GuiObjectListPanelConfigurationType paging(PagingType value) {
        setPaging(value);
        return this;
    }

    public GuiObjectListPanelConfigurationType identifier(String value) {
        setIdentifier(value);
        return this;
    }

    public GuiObjectListPanelConfigurationType description(String value) {
        setDescription(value);
        return this;
    }

    public GuiObjectListPanelConfigurationType documentation(String value) {
        setDocumentation(value);
        return this;
    }


    public GuiObjectListPanelConfigurationType displayOrder(Integer value) {
        setDisplayOrder(value);
        return this;
    }

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public GuiObjectListPanelConfigurationType clone() {
        return ((GuiObjectListPanelConfigurationType) super.clone());
    }
}
