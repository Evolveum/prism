/*
 * Copyright (c) 2010-2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.schema.xjc.schema;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.*;
import com.evolveum.midpoint.prism.impl.xjc.PrismForJAXBUtil;
import com.evolveum.midpoint.schema.xjc.PrefixMapper;
import com.sun.codemodel.*;
import com.sun.tools.xjc.model.CClassInfo;
import org.apache.commons.lang3.Validate;
import jakarta.xml.bind.annotation.*;
import javax.xml.namespace.QName;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.*;
import static com.evolveum.midpoint.schema.xjc.util.ProcessorUtils.*;

/**
 * Custom XJC plugin used to update JAXB classes implementation and use Prism stuff as
 * internal data representation.
 *
 * @author lazyman
 */
public class BaseSchemaProcessor {

    protected static final boolean PRINT_DEBUG_INFO = false;

    public static final QName A_OBJECT_REFERENCE = new QName(PrefixMapper.A.getNamespace(), "objectReference");

    //annotations for schema processor
    public static final QName A_PRISM_CONTAINER = new QName(PrefixMapper.A.getNamespace(), "container");
    public static final QName A_PRISM_OBJECT = new QName(PrefixMapper.A.getNamespace(), "object");
    public static final QName A_RAW_TYPE = new QName(PrefixMapper.A.getNamespace(), "rawType");

    //Public fields
    protected static final String COMPLEX_TYPE_FIELD_NAME = "COMPLEX_TYPE";

    // Public generated methods
    // The "as" prefix is chosen to avoid clash with usual "get" for the fields and also to indicate that
    //   the it returns the same object in a different representation and not a composed/aggregated object
    public static final String METHOD_AS_PRISM_OBJECT = "asPrismObject";
    public static final String METHOD_AS_PRISM_OBJECT_VALUE = "asPrismObjectValue";
    public static final String METHOD_AS_PRISM_CONTAINER_VALUE = "asPrismContainerValue";
    protected static final String METHOD_AS_PRISM_CONTAINER = "asPrismContainer";
    // The "setup" prefix is chosen avoid collision with regular setters for generated fields
    public static final String METHOD_SETUP_CONTAINER_VALUE = "setupContainerValue";
    public static final String METHOD_SETUP_CONTAINER = "setupContainer";
    public static final String METHOD_AS_REFERENCE_VALUE = "asReferenceValue";
    public static final String METHOD_GET_OBJECT = "getObject";
    public static final String METHOD_GET_OBJECTABLE = "getObjectable";
    public static final String METHOD_SETUP_REFERENCE_VALUE = "setupReferenceValue";

    // Internal fields and methods. Although some of these fields needs to be public (so they can be used by
    // prism classes), they are not really intended for public usage. We also want to avoid conflicts with code
    // generated for regular fields. Hence the underscore.
    protected static final String CONTAINER_FIELD_NAME = "_container";
    protected static final String CONTAINER_VALUE_FIELD_NAME = "_containerValue";
    protected static final String REFERENCE_VALUE_FIELD_NAME = "_referenceValue";
    protected static final String METHOD_GET_CONTAINER_NAME = "_getContainerName";
    protected static final String METHOD_GET_CONTAINER_TYPE = "_getContainerType";


    //methods in PrismForJAXBUtil
    protected static final String METHOD_PRISM_UTIL_GET_PROPERTY_VALUE = "getPropertyValue";
    protected static final String METHOD_PRISM_UTIL_GET_PROPERTY_VALUES = "getPropertyValues";
    protected static final String METHOD_PRISM_UTIL_SET_PROPERTY_VALUE = "setPropertyValue";
    protected static final String METHOD_PRISM_UTIL_GET_REFERENCE_OBJECTABLE = "getReferenceObjectable";
    protected static final String METHOD_PRISM_UTIL_SET_REFERENCE_VALUE_AS_REF = "setReferenceValueAsRef";
    protected static final String METHOD_PRISM_UTIL_GET_FILTER = "getFilter";
    protected static final String METHOD_PRISM_UTIL_SET_REFERENCE_FILTER_CLAUSE_XNODE = "setReferenceFilterClauseXNode";
    protected static final String METHOD_PRISM_UTIL_GET_REFERENCE_TARGET_NAME = "getReferenceTargetName";
    protected static final String METHOD_PRISM_UTIL_SET_REFERENCE_TARGET_NAME = "setReferenceTargetName";
    protected static final String METHOD_PRISM_UTIL_OBJECTABLE_AS_REFERENCE_VALUE = "objectableAsReferenceValue";
    protected static final String METHOD_PRISM_UTIL_SETUP_CONTAINER_VALUE = "setupContainerValue";
    protected static final String METHOD_PRISM_UTIL_CREATE_TARGET_INSTANCE = "createTargetInstance";

    protected static final String METHOD_PRISM_GET_ANY = "getAny";

    protected static final String METHOD_CONTAINER_GET_VALUE = "getValue";

    //equals, toString, hashCode methods
    protected static final String METHOD_TO_STRING = "toString";
    protected static final String METHOD_HASH_CODE = "hashCode";
    protected static final String METHOD_EQUALS = "equals";
    protected static final String METHOD_EQUIVALENT = "equivalent";

    //referenced class map
    protected static final Map<Class, JClass> CLASS_MAP = new HashMap<Class, JClass>() {

        @Override
        public JClass get(Object o) {
            JClass clazz = super.get(o);
            Validate.notNull(clazz, "Class '" + o + "' not registered.");
            return clazz;
        }
    };

    protected void updateClassAnnotation(JDefinedClass definedClass) {
        try {
            List<JAnnotationUse> existingAnnotations = getAnnotations(definedClass);
            for (JAnnotationUse annotation : existingAnnotations) {
                if (isAnnotationTypeOf(annotation, XmlAccessorType.class)) {
                    Field field = getField(JAnnotationUse.class, "memberValues");
                    field.setAccessible(true);
                    Map<String, Object> map = (Map<String, Object>) field.get(annotation);
                    field.setAccessible(false);
                    map.clear();
                    annotation.param("value", XmlAccessType.PROPERTY);
                }
                if (isAnnotationTypeOf(annotation, XmlType.class)) {
                    Field field = getField(JAnnotationUse.class, "memberValues");
                    field.setAccessible(true);
                    Map<String, Object> map = (Map<String, Object>) field.get(annotation);
                    Object propOrder = map.get("propOrder");
                    if (propOrder != null) {
                        JAnnotationArrayMember paramArray = (JAnnotationArrayMember)propOrder;
                        Field valField = getField(JAnnotationArrayMember.class, "values");
                        valField.setAccessible(true);
                        List<JAnnotationValue> values = (List<JAnnotationValue>) valField.get(paramArray);
                        for (int i=0; i < values.size(); i++) {
                            JAnnotationValue jAnnValue = values.get(i);
                            String value = extractString(jAnnValue);
                            if (value.startsWith("_")) {
                                paramArray.param(value.substring(1));
                                values.set(i, values.get(values.size() - 1));
                                values.remove(values.size() - 1);
                            }
//                            String valAfter = extractString(values.get(i));
//                            print("PPPPPPPPPPPPPPPPPPP: "+value+" -> "+valAfter);
                        }
                        valField.setAccessible(false);
                    }
                    field.setAccessible(false);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    protected void updateObjectReferenceType(JDefinedClass definedClass, JMethod getReference) {
        JFieldVar typeField = definedClass.fields().get("type");
        JMethod getType = recreateMethod(findMethod(definedClass, "getType"), definedClass);
        copyAnnotations(getType, typeField);
        JBlock body = getType.body();
        body._return(JExpr.invoke(JExpr.invoke(getReference), "getTargetType"));

        definedClass.removeField(typeField);

        JMethod setType = recreateMethod(findMethod(definedClass, "setType"), definedClass);
        body = setType.body();
        JInvocation invocation = body.invoke(JExpr.invoke(getReference), "setTargetType");
        invocation.arg(setType.listParams()[0]);
        invocation.arg(JExpr.lit(true));

        JFieldVar targetNameField = definedClass.fields().get("targetName");
        JMethod getTargetName = recreateMethod(findMethod(definedClass, "getTargetName"), definedClass);
        copyAnnotations(getTargetName, targetNameField);
        JBlock getTargetNamebody = getTargetName.body();
        JInvocation getTargetNameInvocation = CLASS_MAP.get(PrismForJAXBUtil.class).staticInvoke(METHOD_PRISM_UTIL_GET_REFERENCE_TARGET_NAME);
        getTargetNameInvocation.arg(JExpr.invoke(getReference));
        getTargetNamebody._return(getTargetNameInvocation);

        definedClass.removeField(targetNameField);

        JMethod setTargetName = recreateMethod(findMethod(definedClass, "setTargetName"), definedClass);
        JBlock setTargetNamebody = setTargetName.body();
        JInvocation setTagetNameInvocation = setTargetNamebody.staticInvoke(CLASS_MAP.get(PrismForJAXBUtil.class), METHOD_PRISM_UTIL_SET_REFERENCE_TARGET_NAME);
        setTagetNameInvocation.arg(JExpr.invoke(getReference));
        setTagetNameInvocation.arg(setTargetName.listParams()[0]);
    }

    protected void updateObjectReferenceRelation(JDefinedClass definedClass, JMethod asReferenceMethod) {
        JFieldVar typeField = definedClass.fields().get("relation");
        JMethod getType = recreateMethod(findMethod(definedClass, "getRelation"), definedClass);
        copyAnnotations(getType, typeField);
        JBlock body = getType.body();
        body._return(JExpr.invoke(JExpr.invoke(asReferenceMethod), "getRelation"));

        definedClass.removeField(typeField);
        JMethod setType = recreateMethod(findMethod(definedClass, "setRelation"), definedClass);
        body = setType.body();
        JInvocation invocation = body.invoke(JExpr.invoke(asReferenceMethod), "setRelation");
        invocation.arg(setType.listParams()[0]);
    }

    protected void updateObjectReferenceOid(JDefinedClass definedClass, JMethod getReference) {
        JFieldVar oidField = definedClass.fields().get("oid");
        JMethod getOid = recreateMethod(findMethod(definedClass, "getOid"), definedClass);
        copyAnnotations(getOid, oidField);
        definedClass.removeField(oidField);
        JBlock body = getOid.body();
        body._return(JExpr.invoke(JExpr.invoke(getReference), getOid.name()));

        JMethod setOid = recreateMethod(findMethod(definedClass, "setOid"), definedClass);
        body = setOid.body();
        JInvocation invocation = body.invoke(JExpr.invoke(getReference), setOid.name());
        invocation.arg(setOid.listParams()[0]);
    }

    protected void updateObjectReferenceDescription(JDefinedClass definedClass, JMethod getReference) {
        JFieldVar descriptionField = definedClass.fields().get("description");
        JMethod getDescription = recreateMethod(findMethod(definedClass, "getDescription"), definedClass);
        copyAnnotations(getDescription, descriptionField);
        definedClass.removeField(descriptionField);
        JBlock body = getDescription.body();
        body._return(JExpr.invoke(JExpr.invoke(getReference), getDescription.name()));

        JMethod setDescription = recreateMethod(findMethod(definedClass, "setDescription"), definedClass);
        body = setDescription.body();
        JInvocation invocation = body.invoke(JExpr.invoke(getReference), setDescription.name());
        invocation.arg(setDescription.listParams()[0]);
    }

    protected void updateObjectReferenceFilter(JDefinedClass definedClass, JMethod asReferenceValue) {
        JFieldVar filterField = definedClass.fields().get("filter");

        JMethod getFilter = recreateMethod(findMethod(definedClass, "getFilter"), definedClass);
        copyAnnotations(getFilter, filterField);
        definedClass.removeField(filterField);
        JBlock body = getFilter.body();
        JInvocation getFilterElementInvocation = CLASS_MAP.get(PrismForJAXBUtil.class).staticInvoke(METHOD_PRISM_UTIL_GET_FILTER);
        getFilterElementInvocation.arg(JExpr.invoke(asReferenceValue));
        body._return(getFilterElementInvocation);

        JMethod setFilter = recreateMethod(findMethod(definedClass, "setFilter"), definedClass);
        body = setFilter.body();
        JInvocation invocation = CLASS_MAP.get(PrismForJAXBUtil.class).staticInvoke(METHOD_PRISM_UTIL_SET_REFERENCE_FILTER_CLAUSE_XNODE);
        invocation.arg(JExpr.invoke(asReferenceValue));
        invocation.arg(setFilter.listParams()[0]);
        body.add(invocation);
    }

    protected void updateObjectReferenceResolutionTime(JDefinedClass definedClass, JMethod asReferenceMethod) {
        JFieldVar typeField = definedClass.fields().get("resolutionTime");
        JMethod getType = recreateMethod(findMethod(definedClass, "getResolutionTime"), definedClass);
        copyAnnotations(getType, typeField);
        JBlock body = getType.body();
        body._return(JExpr.invoke(JExpr.invoke(asReferenceMethod), "getResolutionTime"));

        definedClass.removeField(typeField);
        JMethod setType = recreateMethod(findMethod(definedClass, "setResolutionTime"), definedClass);
        body = setType.body();
        JInvocation invocation = body.invoke(JExpr.invoke(asReferenceMethod), "setResolutionTime");
        invocation.arg(setType.listParams()[0]);
    }

    protected void updateObjectReferenceReferentialIntegrity(JDefinedClass definedClass, JMethod asReferenceMethod) {
        JFieldVar typeField = definedClass.fields().get("referentialIntegrity");
        JMethod getType = recreateMethod(findMethod(definedClass, "getReferentialIntegrity"), definedClass);
        copyAnnotations(getType, typeField);
        JBlock body = getType.body();
        body._return(JExpr.invoke(JExpr.invoke(asReferenceMethod), "getReferentialIntegrity"));

        definedClass.removeField(typeField);
        JMethod setType = recreateMethod(findMethod(definedClass, "setReferentialIntegrity"), definedClass);
        body = setType.body();
        JInvocation invocation = body.invoke(JExpr.invoke(asReferenceMethod), "setReferentialIntegrity");
        invocation.arg(setType.listParams()[0]);
    }

    protected void updateObjectReferenceGetObject(JDefinedClass definedClass, JMethod asReferenceMethod) {
        JMethod method = definedClass.method(JMod.PUBLIC, PrismObject.class, METHOD_GET_OBJECT);
        JBlock body = method.body();
        body._return(JExpr.invoke(JExpr.invoke(asReferenceMethod), "getObject"));
    }

    protected void updateObjectReferenceGetObjectable(JDefinedClass definedClass, JMethod asReferenceMethod) {
        JMethod method = definedClass.method(JMod.PUBLIC, Objectable.class, METHOD_GET_OBJECTABLE);
        JBlock body = method.body();
        JInvocation invocation = CLASS_MAP.get(PrismForJAXBUtil.class).staticInvoke(METHOD_PRISM_UTIL_GET_REFERENCE_OBJECTABLE);
        invocation.arg(JExpr.invoke(asReferenceMethod));
        body._return(invocation);
    }

    protected JMethod findMethod(JDefinedClass definedClass, String methodName) {
        for (JMethod method : definedClass.methods()) {
            if (method.name().equals(methodName)) {
                return method;
            }
        }

        throw new IllegalArgumentException("Couldn't find method '" + methodName
                + "' in defined class '" + definedClass.name() + "'");
    }

    protected JMethod createDefaultConstructor(JDefinedClass definedClass) {
        JMethod constructor = definedClass.constructor(JMod.PUBLIC);
        constructor.body().invoke("super").invoke("aaa");
        return constructor;
    }

    protected JMethod createPrismContextContainerableConstructor(JDefinedClass definedClass, JMethod setupContainerMethod) {
        JMethod constructor = definedClass.constructor(JMod.PUBLIC);
        constructor.param(PrismContext.class, "prismContext");

        JBlock body = constructor.body();
        body.invoke(setupContainerMethod)                                                        // setupContainerValue(
                .arg(JExpr._new(CLASS_MAP.get(PrismContainerValueImpl.class).narrow(new JClass[0]))  //    new PrismContainerValueImpl<>(
                        .arg(JExpr._this())                                                      //       this,
                        .arg(constructor.params().get(0)));                                      //       prismContext);
        return constructor;
    }

    protected void createEqualsMethod(JDefinedClass definedClass, String baseMethod) {
        JMethod equals = definedClass.getMethod(METHOD_EQUALS, new JType[]{CLASS_MAP.get(Object.class)});

        if (equals != null) {
//            removeOldCustomGeneratedEquals(classOutline, hasParentAnnotation(classOutline, PRISM_OBJECT));  todo can this be removed?
            equals = recreateMethod(equals, definedClass);
        } else {
            equals = definedClass.method(JMod.PUBLIC, boolean.class, METHOD_EQUALS);
        }
        equals.annotate(CLASS_MAP.get(Override.class));

        JBlock body = equals.body();
        JVar obj = equals.listParams()[0];
        JBlock ifNull = body._if(obj._instanceof(definedClass).not())._then();
        ifNull._return(JExpr.lit(false));

        JVar other = body.decl(definedClass, "other", JExpr.cast(definedClass, obj));

        JInvocation invocation = JExpr.invoke(baseMethod).invoke(METHOD_EQUIVALENT);
        invocation.arg(other.invoke(baseMethod));
        body._return(invocation);
    }
    /*
        public UserType(PrismContext prismContext) {
            setupContainer(new PrismObject(_getContainerName(), this.getClass(), prismContext));
        }
     */

    protected void createAsPrismContainer(JDefinedClass definedClass, JVar container) {
        JMethod getContainer = definedClass.method(JMod.PUBLIC, CLASS_MAP.get(PrismObject.class),
                METHOD_AS_PRISM_CONTAINER);

        //create method body
        JBlock body = getContainer.body();
        JBlock then = body._if(container.eq(JExpr._null()))._then();

        JInvocation newContainer = JExpr._new(CLASS_MAP.get(PrismObjectImpl.class));
        newContainer.arg(JExpr.invoke(METHOD_GET_CONTAINER_NAME));
        newContainer.arg(JExpr._this().invoke("getClass"));
//        newContainer.arg(JExpr.dotclass(definedClass));
        then.assign(container, newContainer);

        body._return(container);
    }

    private QName getCClassInfoQName(CClassInfo info) {
        QName qname = info.getTypeName();
        if (qname == null) {
            qname = info.getElementName();
        }

        return qname;
    }

    protected JMethod createPrismContextObjectableConstructor(JDefinedClass definedClass) {
        JMethod constructor = definedClass.constructor(JMod.PUBLIC);
        constructor.param(PrismContext.class, "prismContext");

        JBlock body = constructor.body();
        body.invoke("setupContainer")
                .arg(JExpr._new(CLASS_MAP.get(PrismObjectImpl.class))
                        .arg(JExpr.invoke("_getContainerName"))
                        .arg(JExpr.invoke("getClass"))
                        .arg(constructor.params().get(0)));
        return constructor;
    }

    protected void createAsPrismContainerValueInObject(JDefinedClass definedClass) {
        JMethod getContainer = definedClass.method(JMod.PUBLIC, CLASS_MAP.get(PrismContainerValue.class),
                METHOD_AS_PRISM_CONTAINER_VALUE);
        getContainer.annotate(CLASS_MAP.get(Override.class));

        //create method body
        JBlock body = getContainer.body();
        body._return(JExpr.invoke(METHOD_AS_PRISM_CONTAINER).invoke(METHOD_CONTAINER_GET_VALUE));
    }

    protected void createAsPrismContainerValue(JDefinedClass definedClass, JVar containerValueVar) {
        JMethod getContainer = definedClass.method(JMod.PUBLIC, CLASS_MAP.get(PrismContainerValue.class),
                METHOD_AS_PRISM_CONTAINER_VALUE);
//        getContainer.annotate(CLASS_MAP.get(XmlTransient.class));

        //create method body
        JBlock body = getContainer.body();
        body._if(containerValueVar.eq(JExpr._null())).                                              // if (_containerValue == null) {
            _then()                                                                                 //
                .assign(containerValueVar,                                                          //    _containerValue =
                        JExpr._new(CLASS_MAP.get(PrismContainerValueImpl.class).narrow(new JClass[0]))  //       new PrismContainerValueImpl<>(
                                .arg(JExpr._this())                                                 //          this)
                );
        body._return(containerValueVar);
    }

    protected void createAsPrismObject(JDefinedClass definedClass) {
        JClass prismObjectClass = CLASS_MAP.get(PrismObject.class);
        JType returnType;
        if (definedClass.isAbstract()) {
            returnType = prismObjectClass.narrow(definedClass.wildcard());
        } else {
            // e.g. PrismObject<TaskType> for TaskType
            // we assume that we don't subclass a non-abstract object class into another one
            returnType = prismObjectClass.narrow(definedClass);
        }
        JMethod asPrismObject = definedClass.method(JMod.PUBLIC, returnType, METHOD_AS_PRISM_OBJECT);
        asPrismObject.annotate(CLASS_MAP.get(Override.class));

        //create method body
        JBlock body = asPrismObject.body();
        body._return(JExpr.invoke(METHOD_AS_PRISM_CONTAINER));
    }

    protected String extractString(JAnnotationValue jAnnValue) {
        StringWriter writer = new StringWriter();
        JFormatter formatter = new JFormatter(writer);
        jAnnValue.generate(formatter);
        String value = writer.getBuffer().toString();
        return value.substring(1, value.length() - 1);
    }

    protected boolean isAnnotationTypeOf(JAnnotationUse annotation, Class clazz) {
        try {
            Field field = getField(JAnnotationUse.class, "clazz");
            field.setAccessible(true);
            JClass jClass = (JClass) field.get(annotation);
            field.setAccessible(false);

            if (CLASS_MAP.get(clazz).equals(jClass)) {
                return true;
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }

        return false;
    }

    protected void createToDebugName(JDefinedClass definedClass) {
        JMethod method = definedClass.method(JMod.PUBLIC, String.class, "toDebugName");
        method.annotate(CLASS_MAP.get(Override.class));
        JBlock body = method.body();
        JVar builder = body.decl(CLASS_MAP.get(StringBuilder.class), "builder",
                JExpr._new(CLASS_MAP.get(StringBuilder.class)));

        invokeAppendOnBuilder(body, builder, JExpr.dotclass(definedClass).invoke("getSimpleName"));
        invokeAppendOnBuilder(body, builder, JExpr.lit("["));
        invokeAppendOnBuilder(body, builder, JExpr.invoke("getOid"));
        invokeAppendOnBuilder(body, builder, JExpr.lit(", "));
        invokeAppendOnBuilder(body, builder, JExpr.invoke("getName"));
        invokeAppendOnBuilder(body, builder, JExpr.lit("]"));
        body._return(JExpr.invoke(builder, "toString"));
    }

    protected void createToDebugType(JDefinedClass definedClass) {
        JMethod method = definedClass.method(JMod.PUBLIC, String.class, "toDebugType");
        method.annotate(CLASS_MAP.get(Override.class));
        JBlock body = method.body();
        JVar builder = body.decl(CLASS_MAP.get(StringBuilder.class), "builder",
                JExpr._new(CLASS_MAP.get(StringBuilder.class)));

        invokeAppendOnBuilder(body, builder, JExpr.dotclass(definedClass).invoke("getSimpleName"));

        body._return(JExpr.invoke(builder, "toString"));
    }

    protected void invokeAppendOnBuilder(JBlock body, JVar builder, JExpression expression) {
        JInvocation invocation = body.invoke(builder, "append");
        invocation.arg(expression);
    }

    protected void createHashCodeMethod(JDefinedClass definedClass, String baseMethodName) {
        JMethod hashCode = definedClass.getMethod(METHOD_HASH_CODE, new JType[]{});
        if (hashCode == null) {
            hashCode = definedClass.method(JMod.PUBLIC, int.class, METHOD_HASH_CODE);
        } else {
            hashCode = recreateMethod(hashCode, definedClass);
        }
        hashCode.annotate(CLASS_MAP.get(Override.class));
        JBlock body = hashCode.body();
        body._return(JExpr.invoke(baseMethodName).invoke(METHOD_HASH_CODE));
    }

    protected void createToStringMethod(JDefinedClass definedClass, String baseMethod) {
        JMethod toString = definedClass.getMethod("toString", new JType[]{});
        if (toString == null) {
            toString = definedClass.method(JMod.PUBLIC, CLASS_MAP.get(String.class), METHOD_TO_STRING);
        } else {
            toString = recreateMethod(toString, definedClass);
        }
        toString.annotate(CLASS_MAP.get(Override.class));

        JBlock body = toString.body();
        JInvocation invocation = JExpr.invoke(baseMethod).invoke(METHOD_TO_STRING);
        body._return(invocation);
    }

    protected JMethod createSetContainerValueMethod(JDefinedClass definedClass, JVar container) {
        JMethod setContainer = definedClass.method(JMod.PUBLIC, void.class, METHOD_SETUP_CONTAINER_VALUE);
        JVar methodContainer = setContainer.param(PrismContainerValue.class, "containerValue");
        //create method body
        JBlock body = setContainer.body();
        body.assign(JExpr._this().ref(container), methodContainer);
        return setContainer;
    }

    protected void createSetContainerValueMethodInObject(JDefinedClass definedClass, JVar container) {
        JMethod setContainerValue = definedClass.method(JMod.PUBLIC, void.class, METHOD_SETUP_CONTAINER_VALUE);
        setContainerValue.annotate(CLASS_MAP.get(Override.class));
        JVar containerValue = setContainerValue.param(PrismContainerValue.class, "containerValue");
        //create method body
        JBlock body = setContainerValue.body();
        JInvocation invocation = CLASS_MAP.get(PrismForJAXBUtil.class).staticInvoke(METHOD_PRISM_UTIL_SETUP_CONTAINER_VALUE);
        invocation.arg(JExpr.invoke(METHOD_AS_PRISM_CONTAINER));
        invocation.arg(containerValue);
        body.assign(container, invocation);
    }

    protected JMethod createSetContainerMethod(JDefinedClass definedClass, JVar container) {
        JMethod setContainer = definedClass.method(JMod.PUBLIC, void.class, METHOD_SETUP_CONTAINER);
        JVar methodContainer = setContainer.param(PrismObject.class, "container");
        //create method body
        JBlock body = setContainer.body();
        body.assign(JExpr._this().ref(container), methodContainer);
        return setContainer;
    }

    protected boolean isAuxiliaryField(JFieldVar fieldVar) {
        String field = fieldVar.name();
        return "serialVersionUID".equals(field) || COMPLEX_TYPE_FIELD_NAME.equals(field)
                || CONTAINER_FIELD_NAME.equals(field) || CONTAINER_VALUE_FIELD_NAME.equals(field)
                || "otherAttributes".equals(field) && fieldVar.type().name().equals("Map<QName,String>")
                || isFField(fieldVar);
    }

    protected boolean isFField(JFieldVar fieldVar) {
        boolean isPublicStaticFinal = (fieldVar.mods().getValue() & (JMod.STATIC | JMod.FINAL)) != 0;
        if (fieldVar.name().startsWith("F_") && isPublicStaticFinal) {
            //our QName constant fields
            return true;
        }
        return false;
    }

    protected void createFieldReferenceSetterBody(JFieldVar field, JVar param, JBlock body) {
        JVar cont = body.decl(CLASS_MAP.get(PrismReferenceValue.class), REFERENCE_VALUE_FIELD_NAME,
                JOp.cond(param.ne(JExpr._null()), JExpr.invoke(param, METHOD_AS_REFERENCE_VALUE), JExpr._null()));
        JInvocation invocation = body.staticInvoke(CLASS_MAP.get(PrismForJAXBUtil.class),
                METHOD_PRISM_UTIL_SET_REFERENCE_VALUE_AS_REF);
        invocation.arg(JExpr.invoke(METHOD_AS_PRISM_CONTAINER_VALUE));
        invocation.arg(JExpr.ref(fieldFPrefixUnderscoredUpperCase(field.name())));
        invocation.arg(cont);
    }

    protected void createFieldReferenceCreateItemBody(JFieldVar field, JMethod method) {
        JClass type = ((JClass) field.type()).getTypeParameters().get(0);

        JBlock body = method.body();
        JExpression initExpr;
        initExpr = constructorExpression(method, type);
        JVar decl = body.decl(type, field.name(), initExpr);
        JInvocation invocation = body.invoke(decl, METHOD_SETUP_REFERENCE_VALUE);
        invocation.arg(method.listParams()[0]);
        body._return(decl);
    }

    protected JExpression constructorExpression(JMethod method, JClass type) {
        JExpression initExpr;
        if (type.isAbstract()) {
            JInvocation invocation = CLASS_MAP.get(PrismForJAXBUtil.class).staticInvoke(METHOD_PRISM_UTIL_CREATE_TARGET_INSTANCE);
            invocation.arg(method.listParams()[0]);
            initExpr = invocation;
        } else {
            initExpr = JExpr._new(type);
        }
        return initExpr;
    }

    protected void createFieldReferenceGetValueFrom(JFieldVar field, JMethod method) {
        JBlock body = method.body();
        body._return(JExpr.invoke(method.listParams()[0], METHOD_AS_REFERENCE_VALUE));
    }

    protected void createFieldReferenceWillClear(JFieldVar field, JMethod method) {
        JBlock body = method.body();
        JInvocation getObject = JExpr.invoke(method.listParams()[0], "getObject");
        body._return(getObject.eq(JExpr._null()));
    }

    protected void createFieldReferenceUseWillClear(JFieldVar field, JMethod method) {
        JBlock body = method.body();
        JInvocation getObject = JExpr.invoke(method.listParams()[0], "getObject");
        body._return(getObject.ne(JExpr._null()));
    }

    protected void createFieldReferenceUseCreateItemBody(JFieldVar field, JMethod method) {
        JClass type = ((JClass) field.type()).getTypeParameters().get(0);

        JBlock body = method.body();
        JExpression initExpr;
        initExpr = constructorExpression(method, type);
        JVar decl = body.decl(type, field.name(), initExpr);
        JInvocation invocation = body.invoke(decl, METHOD_SETUP_CONTAINER);
        invocation.arg(JExpr.invoke(method.listParams()[0], "getObject"));
        body._return(decl);
    }

    protected void createFieldReferenceUseGetValueFrom(JFieldVar field, JMethod method) {
        JBlock body = method.body();

        JInvocation invocation = CLASS_MAP.get(PrismForJAXBUtil.class).staticInvoke(METHOD_PRISM_UTIL_OBJECTABLE_AS_REFERENCE_VALUE);
        invocation.arg(method.listParams()[0]);
        invocation.arg(JExpr.invoke("getReference"));
        body._return(invocation);
    }


    protected boolean isInstantiable(JType type) {
        if (!(type instanceof JClass)) {
            return false;
        }
        JClass clazz = (JClass) type;
        if (clazz.isAbstract()) {
            return false;
        }
        if (clazz instanceof JDefinedClass) {
            if (hasAnnotationClass(((JDefinedClass) clazz), XmlEnum.class)) {
                return false;
            }
        }
        return true;
    }

    protected JType getContentType(JFieldVar field) {
        boolean multi = isList(field.type());
        JType valueClass;
        if (multi) {
            valueClass = ((JClass) field.type()).getTypeParameters().get(0);
        } else {
            valueClass = field.type();
        }
        return valueClass;
    }

    protected void createFieldContainerCreateItemBody(JFieldVar field, JMethod method) {
        JClass list = (JClass) field.type();
        JClass listType = list.getTypeParameters().get(0);

        JBlock body = method.body();
        JVar decl = body.decl(listType, field.name(), constructorExpression(method, listType));
        JInvocation invocation = body.invoke(decl, METHOD_SETUP_CONTAINER_VALUE);
        invocation.arg(method.listParams()[0]);
        body._return(decl);
    }

    protected void createFieldContainerGetValueFrom(JFieldVar field, JMethod method) {
        JBlock body = method.body();
        body._return(JExpr.invoke(method.listParams()[0], METHOD_AS_PRISM_CONTAINER_VALUE));
    }

    protected boolean isList(JType type) {
        boolean isList = false;
        if (type instanceof JClass) {
            isList = CLASS_MAP.get(List.class).equals(((JClass) type).erasure());
        }

        return isList;
    }

    protected void annotateFieldAsRaw(JFieldVar fieldVar) {
        fieldVar.annotate(CLASS_MAP.get(Raw.class));
    }

    protected void annotateMethodWithXmlElement(JMethod method, JFieldVar field) {
        List<JAnnotationUse> existingAnnotations = getAnnotations(method);
        for (JAnnotationUse annotation : existingAnnotations) {
            if (isAnnotationTypeOf(annotation, XmlAttribute.class) ||
                    isAnnotationTypeOf(annotation, XmlAnyElement.class) ||
                    isAnnotationTypeOf(annotation, XmlAnyAttribute.class)) {
                return;
            }
        }

        JAnnotationUse xmlElement = null;
        for (JAnnotationUse annotation : existingAnnotations) {
            if (!isAnnotationTypeOf(annotation, XmlElement.class)) {
                continue;
            }
            xmlElement = annotation;
            break;
        }
        if (xmlElement == null) {
            xmlElement = method.annotate(CLASS_MAP.get(XmlElement.class));
        }
        xmlElement.param("name", field.name());
    }

    protected void createFieldSetterBody(JMethod method, JFieldVar field) {
        JBlock body = method.body();
        JInvocation invocation = body.staticInvoke(CLASS_MAP.get(PrismForJAXBUtil.class),
                METHOD_PRISM_UTIL_SET_PROPERTY_VALUE);
        //push arguments
        invocation.arg(JExpr.invoke(METHOD_AS_PRISM_CONTAINER_VALUE));
        invocation.arg(JExpr.ref(fieldFPrefixUnderscoredUpperCase(field.name())));
        invocation.arg(method.listParams()[0]);
    }

    protected <T> boolean hasAnnotationClass(JAnnotatable method, Class<T> annotationType) {
        List<JAnnotationUse> annotations = getAnnotations(method);
        for (JAnnotationUse annotation : annotations) {
            if (isAnnotationTypeOf(annotation, annotationType)) {
                return true;
            }
        }

        return false;
    }

    protected void createFieldGetterBody(JMethod method, JFieldVar field, boolean isList) {
        JBlock body = method.body();
        JInvocation invocation;
        if (hasAnnotationClass(method, XmlAnyElement.class)) {
            //handling xsd any
            invocation = CLASS_MAP.get(PrismForJAXBUtil.class).staticInvoke(METHOD_PRISM_GET_ANY);
            invocation.arg(JExpr.invoke(METHOD_AS_PRISM_CONTAINER_VALUE));

            JClass clazz = (JClass) field.type();
            invocation.arg(JExpr.dotclass(clazz.getTypeParameters().get(0)));
            body._return(invocation);
            return;
        }

        if (isList) {
            invocation = CLASS_MAP.get(PrismForJAXBUtil.class).staticInvoke(METHOD_PRISM_UTIL_GET_PROPERTY_VALUES);
        } else {
            invocation = CLASS_MAP.get(PrismForJAXBUtil.class).staticInvoke(METHOD_PRISM_UTIL_GET_PROPERTY_VALUE);
        }
        //push arguments
        invocation.arg(JExpr.invoke(METHOD_AS_PRISM_CONTAINER_VALUE));
        invocation.arg(JExpr.ref(fieldFPrefixUnderscoredUpperCase(field.name())));
        JType type = field.type();
        if (type.isPrimitive()) {
            JPrimitiveType primitive = (JPrimitiveType) type;
            invocation.arg(JExpr.dotclass(primitive.boxify()));
        } else {
            JClass clazz = (JClass) type;
            if (isList) {
                invocation.arg(JExpr.dotclass(clazz.getTypeParameters().get(0)));
            } else {
                invocation.arg(JExpr.dotclass(clazz));
            }
        }

        body._return(invocation);
    }

    public static void print(String s) {
        if (PRINT_DEBUG_INFO) {
            System.out.println(s);
        }
    }

    public static void printWarning(String s) {
        System.out.println(s);
    }

}
