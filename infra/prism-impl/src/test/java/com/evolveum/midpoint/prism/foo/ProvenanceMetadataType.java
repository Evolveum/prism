package com.evolveum.midpoint.prism.foo;

import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.impl.binding.AbstractMutableContainerable;
import com.evolveum.midpoint.prism.impl.xjc.PrismForJAXBUtil;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.util.Producer;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "ProvenanceMetadataType", propOrder = {
        "acquisition",
        "mappingSpecification",
        "policyRule",
        "markingRule",
        "policyStatement",
        "assignmentPath"
})
public class ProvenanceMetadataType extends AbstractMutableContainerable {

    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "ProvenanceMetadataType");
    public static final ItemName F_ACQUISITION = ItemName.interned(ObjectType.NS_FOO, "acquisition");
    public static final ItemName F_MAPPING_SPECIFICATION = ItemName.interned(ObjectType.NS_FOO, "mappingSpecification");
    public static final ItemName F_POLICY_RULE = ItemName.interned(ObjectType.NS_FOO, "policyRule");
    public static final ItemName F_MARKING_RULE = ItemName.interned(ObjectType.NS_FOO, "markingRule");
    public static final ItemName F_POLICY_STATEMENT = ItemName.interned(ObjectType.NS_FOO, "policyStatement");
    public static final ItemName F_ASSIGNMENT_PATH = ItemName.interned(ObjectType.NS_FOO, "assignmentPath");
    public static final Producer<ProvenanceMetadataType> FACTORY = new Producer<ProvenanceMetadataType>() {

        private static final long serialVersionUID = 201105211233L;

        public ProvenanceMetadataType run() {
            return new ProvenanceMetadataType();
        }

    }
            ;

    public ProvenanceMetadataType() {
        super();
    }

    @Deprecated
    public ProvenanceMetadataType(PrismContext context) {
        super();
    }

    @XmlElement(name = "acquisition")
    public List<ProvenanceAcquisitionType> getAcquisition() {
        return this.prismGetContainerableList(ProvenanceAcquisitionType.FACTORY, F_ACQUISITION, ProvenanceAcquisitionType.class);
    }

    public List<ProvenanceAcquisitionType> createAcquisitionList() {
        PrismForJAXBUtil.createContainer(asPrismContainerValue(), F_ACQUISITION);
        return getAcquisition();
    }

    @XmlElement(name = "mappingSpecification")
    public MappingSpecificationType getMappingSpecification() {
        return this.prismGetSingleContainerable(F_MAPPING_SPECIFICATION, MappingSpecificationType.class);
    }

    public void setMappingSpecification(MappingSpecificationType value) {
        this.prismSetSingleContainerable(F_MAPPING_SPECIFICATION, value);
    }

    @XmlElement(name = "policyRule")
    public PolicyRuleSpecificationType getPolicyRule() {
        return this.prismGetSingleContainerable(F_POLICY_RULE, PolicyRuleSpecificationType.class);
    }

    public void setPolicyRule(PolicyRuleSpecificationType value) {
        this.prismSetSingleContainerable(F_POLICY_RULE, value);
    }

    @XmlElement(name = "markingRule")
    public MarkingRuleSpecificationType getMarkingRule() {
        return this.prismGetSingleContainerable(F_MARKING_RULE, MarkingRuleSpecificationType.class);
    }

    public void setMarkingRule(MarkingRuleSpecificationType value) {
        this.prismSetSingleContainerable(F_MARKING_RULE, value);
    }

    @XmlElement(name = "policyStatement")
    public PolicyStatementSpecificationType getPolicyStatement() {
        return this.prismGetSingleContainerable(F_POLICY_STATEMENT, PolicyStatementSpecificationType.class);
    }

    public void setPolicyStatement(PolicyStatementSpecificationType value) {
        this.prismSetSingleContainerable(F_POLICY_STATEMENT, value);
    }

    @XmlElement(name = "assignmentPath")
    public AssignmentPathMetadataType getAssignmentPath() {
        return this.prismGetSingleContainerable(F_ASSIGNMENT_PATH, AssignmentPathMetadataType.class);
    }

    public void setAssignmentPath(AssignmentPathMetadataType value) {
        this.prismSetSingleContainerable(F_ASSIGNMENT_PATH, value);
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public ProvenanceMetadataType acquisition(ProvenanceAcquisitionType value) {
        getAcquisition().add(value);
        return this;
    }

    public ProvenanceAcquisitionType beginAcquisition() {
        ProvenanceAcquisitionType value = new ProvenanceAcquisitionType();
        acquisition(value);
        return value;
    }

    public ProvenanceMetadataType mappingSpecification(MappingSpecificationType value) {
        setMappingSpecification(value);
        return this;
    }

    public MappingSpecificationType beginMappingSpecification() {
        MappingSpecificationType value = new MappingSpecificationType();
        mappingSpecification(value);
        return value;
    }

    public ProvenanceMetadataType policyRule(PolicyRuleSpecificationType value) {
        setPolicyRule(value);
        return this;
    }

    public PolicyRuleSpecificationType beginPolicyRule() {
        PolicyRuleSpecificationType value = new PolicyRuleSpecificationType();
        policyRule(value);
        return value;
    }

    public ProvenanceMetadataType markingRule(MarkingRuleSpecificationType value) {
        setMarkingRule(value);
        return this;
    }

    public MarkingRuleSpecificationType beginMarkingRule() {
        MarkingRuleSpecificationType value = new MarkingRuleSpecificationType();
        markingRule(value);
        return value;
    }

    public ProvenanceMetadataType policyStatement(PolicyStatementSpecificationType value) {
        setPolicyStatement(value);
        return this;
    }

    public PolicyStatementSpecificationType beginPolicyStatement() {
        PolicyStatementSpecificationType value = new PolicyStatementSpecificationType();
        policyStatement(value);
        return value;
    }

    public ProvenanceMetadataType assignmentPath(AssignmentPathMetadataType value) {
        setAssignmentPath(value);
        return this;
    }

    public AssignmentPathMetadataType beginAssignmentPath() {
        AssignmentPathMetadataType value = new AssignmentPathMetadataType();
        assignmentPath(value);
        return value;
    }

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public ProvenanceMetadataType clone() {
        return ((ProvenanceMetadataType) super.clone());
    }


}
