package com.evolveum.midpoint.prism.foo;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.PrismReferenceValueImpl;
import com.evolveum.midpoint.prism.path.ItemName;

import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;

import jakarta.xml.bind.annotation.*;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Created by Dominik.
 */

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "FocusType", propOrder = {
        "identities",
        "linkRef",
        "personaRef",
        "activation",
        "jpegPhoto",
        "costCenter",
        "locality",
        "preferredLanguage",
        "locale",
        "timezone",
        "emailAddress",
        "telephoneNumber",
        "credentials",
        "behavior"
})
@XmlSeeAlso({
        GenericObjectType.class,
        UserType.class,
        AbstractRoleType.class
})
public abstract class FocusType
        extends AssignmentHolderType
        implements Objectable {

    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName("FocusType");
    public static final ItemName F_LINK_REF = ItemName.interned(NS_FOO,"linkRef");
    public static final ItemName F_LOCALITY = ItemName.interned(NS_FOO, "locality");
    public static final ItemName F_IDENTITIES = ItemName.interned(NS_FOO, "identities");
    public static final ItemName F_PERSONA_REF = ItemName.interned(NS_FOO, "personaRef");
    public static final ItemName F_ACTIVATION = ItemName.interned(NS_FOO, "activation");
    public static final ItemName F_JPEG_PHOTO = ItemName.interned(NS_FOO, "jpegPhoto");
    public static final ItemName F_COST_CENTER = ItemName.interned(NS_FOO, "costCenter");
    public static final ItemName F_PREFERRED_LANGUAGE = ItemName.interned(NS_FOO, "preferredLanguage");
    public static final ItemName F_LOCALE = ItemName.interned(NS_FOO, "locale");
    public static final ItemName F_TIMEZONE = ItemName.interned(NS_FOO, "timezone");
    public static final ItemName F_EMAIL_ADDRESS = ItemName.interned(NS_FOO, "emailAddress");
    public static final ItemName F_TELEPHONE_NUMBER = ItemName.interned(NS_FOO, "telephoneNumber");
    public static final ItemName F_CREDENTIALS = ItemName.interned(NS_FOO, "credentials");
    public static final ItemName F_BEHAVIOR = ItemName.interned(NS_FOO, "behavior");


    public FocusType() {
        super();
    }

    @XmlElement(name = "linkRef")
    public List<ObjectReferenceType> getLinkRef() {
        return this.prismGetReferencableList(ObjectReferenceType.FACTORY, F_LINK_REF, ObjectReferenceType.class);
    }

    public FocusType linkRef(ObjectReferenceType value) {
        getLinkRef().add(value);
        return this;
    }

    public FocusType linkRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return linkRef(ort);
    }

    public FocusType linkRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return linkRef(ort);
    }

    public void setLocality(PolyStringType value) {
        this.prismSetPropertyValue(F_LOCALITY, value);
    }
}
