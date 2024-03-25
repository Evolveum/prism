/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.schema.features;

import com.evolveum.midpoint.prism.SchemaMigration;

import com.evolveum.midpoint.prism.SchemaMigrationOperation;
import com.evolveum.midpoint.prism.schema.DefinitionFeatureParser;

import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

import java.util.ArrayList;
import java.util.List;

import static com.evolveum.midpoint.prism.PrismConstants.*;
import static com.evolveum.midpoint.prism.impl.schema.SchemaProcessorUtil.getAnnotationElements;
import static org.apache.commons.lang3.StringUtils.*;

public class SchemaMigrationXsomParser implements DefinitionFeatureParser<SchemaMigrationXsomParser.SchemaMigrations, Object> {

    private static final SchemaMigrationXsomParser INSTANCE = new SchemaMigrationXsomParser();

    public static SchemaMigrationXsomParser instance() {
        return INSTANCE;
    }

    @Override
    public @Nullable SchemaMigrations getValue(@Nullable Object source) throws SchemaException {
        ArrayList<SchemaMigration> migrations = new ArrayList<>();
        for (Element schemaMigrationElement : getAnnotationElements(source, A_SCHEMA_MIGRATION)) {
            migrations.add(parseSchemaMigration(schemaMigrationElement));
        }
        return SchemaMigrations.wrap(migrations);
    }

    private SchemaMigration parseSchemaMigration(Element schemaMigrationElement) throws SchemaException {
        Element elementElement =
                MiscUtil.requireNonNull(
                        DOMUtil.getChildElement(schemaMigrationElement, A_SCHEMA_MIGRATION_ELEMENT),
                        "Missing schemaMigration element");
        Element versionElement =
                MiscUtil.requireNonNull(
                        DOMUtil.getChildElement(schemaMigrationElement, A_SCHEMA_MIGRATION_VERSION),
                        "Missing schemaMigration version");
        Element operationElement =
                MiscUtil.requireNonNull(
                        DOMUtil.getChildElement(schemaMigrationElement, A_SCHEMA_MIGRATION_OPERATION),
                        "Missing schemaMigration operation");

        Element replacementElement = DOMUtil.getChildElement(schemaMigrationElement, A_SCHEMA_MIGRATION_REPLACEMENT);
        QName replacementName = replacementElement != null ? DOMUtil.getQNameValue(replacementElement) : null;

        return new SchemaMigration(
                DOMUtil.getQNameValue(elementElement),
                trim(versionElement.getTextContent()),
                SchemaMigrationOperation.parse(trim(operationElement.getTextContent())),
                replacementName);
    }

    /** Just the value holder to ensure type safety. */
    public static class SchemaMigrations
            extends AbstractValueWrapper.ForList<SchemaMigration> {

        SchemaMigrations(List<SchemaMigration> values) {
            super(values);
        }

        static @Nullable List<SchemaMigration> unwrap(@Nullable SchemaMigrations wrapper) {
            return wrapper != null ? wrapper.getValue() : null;
        }

        static @Nullable SchemaMigrations wrap(@Nullable List<SchemaMigration> values) {
            return values != null && !values.isEmpty() ? new SchemaMigrations(values) : null;
        }
    }
}
