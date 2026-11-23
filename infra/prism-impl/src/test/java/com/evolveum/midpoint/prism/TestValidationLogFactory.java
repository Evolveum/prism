/*
 * Copyright (c) 2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import javax.xml.namespace.QName;

import org.testng.annotations.Test;

import com.evolveum.concepts.Argument;
import com.evolveum.concepts.Argument.ArgumentType;
import com.evolveum.concepts.SourceLocation;
import com.evolveum.concepts.ValidationLog;
import com.evolveum.concepts.ValidationLogType;
import com.evolveum.midpoint.prism.foo.UserType;
import com.evolveum.midpoint.prism.impl.lex.ValidatorUtil;
import com.evolveum.midpoint.prism.impl.xnode.MapXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.PrimitiveXNodeImpl;

/**
 * Validation log is built from one template and its arguments: the user message gets short
 * renderings of the arguments, the technical message keeps the template and the typed values.
 */
public class TestValidationLogFactory extends AbstractPrismTest {

    private static final SourceLocation LOCATION = SourceLocation.from("test", 7, 3);
    private static final QName NAME = new QName("http://example.com/ns", "fullName");

    @Test
    public void argumentTypeIsInferredForBasicValues() {
        assertThat(Argument.of("text").type()).isEqualTo(ArgumentType.STRING);
        assertThat(Argument.of(5).type()).isEqualTo(ArgumentType.INT);
        assertThat(Argument.of(NAME).type()).isEqualTo(ArgumentType.QNAME);
        assertThat(Argument.of(new Object()).type()).isEqualTo(ArgumentType.UNKNOWN);
        assertThat(Argument.of(null).type()).isEqualTo(ArgumentType.UNKNOWN);
        assertThat(Argument.of("text").value()).isEqualTo("text");
    }

    @Test
    public void explicitArgumentIsKept() {
        Argument raw = Argument.raw("dump");

        assertThat(raw.type()).isEqualTo(ArgumentType.RAW);
        assertThat(Argument.of(raw)).isSameAs(raw);
        assertThat(ValidatorUtil.argument(raw)).isSameAs(raw);
    }

    @Test
    public void argumentTypeIsInferredForPrismValues() {
        PrismObjectDefinition<UserType> userDef = getUserTypeDefinition();

        assertThat(ValidatorUtil.argument(new MapXNodeImpl()).type()).isEqualTo(ArgumentType.XNODE);
        assertThat(ValidatorUtil.argument(new PrimitiveXNodeImpl<>("x")).type()).isEqualTo(ArgumentType.XNODE);
        assertThat(ValidatorUtil.argument(userDef).type()).isEqualTo(ArgumentType.DEFINITION);
        assertThat(ValidatorUtil.argument(userDef.getComplexTypeDefinition()).type()).isEqualTo(ArgumentType.DEFINITION);
        assertThat(ValidatorUtil.argument(List.of(userDef)).type()).isEqualTo(ArgumentType.DEFINITION_LIST);
        assertThat(ValidatorUtil.argument(List.of("a")).type()).isEqualTo(ArgumentType.UNKNOWN);
        assertThat(ValidatorUtil.argument(NAME).type()).isEqualTo(ArgumentType.QNAME);
    }

    @Test
    public void errorHasShortUserMessageAndTypedTechnicalMessage() {
        PrismObjectDefinition<UserType> userDef = getUserTypeDefinition();
        MapXNodeImpl node = new MapXNodeImpl();
        node.setElementName(new QName("http://example.com/ns", "assignment"));

        ValidationLog log = ValidatorUtil.error(LOCATION, "Item '%s' in '%s' has %s values, see '%s'", NAME, userDef, 2, node);

        assertThat(log.validationLogType()).isEqualTo(ValidationLogType.ERROR);
        assertThat(log.specification()).isEqualTo(ValidationLogType.Specification.UNKNOWN);
        assertThat(log.location()).isEqualTo(LOCATION);
        assertThat(log.message()).isEqualTo("Item 'fullName' in 'user' has 2 values, see 'assignment'");
        assertThat(log.technicalMessage().message()).isEqualTo("Item '%s' in '%s' has %s values, see '%s'");
        assertThat(log.technicalMessage().arguments())
                .extracting(Argument::type)
                .containsExactly(ArgumentType.QNAME, ArgumentType.DEFINITION, ArgumentType.INT, ArgumentType.XNODE);
        assertThat(log.technicalMessage().arguments())
                .extracting(Argument::value)
                .containsExactly(NAME, userDef, 2, node);
    }

    @Test
    public void nodeWithoutElementNameIsRenderedGenerically() {
        ValidationLog log = ValidatorUtil.error(LOCATION, "Cannot parse property from '%s'", new MapXNodeImpl());

        assertThat(log.message()).isEqualTo("Cannot parse property from 'node'");
    }

    @Test
    public void messageWithoutArgumentsIsUsedAsIs() {
        ValidationLog log = ValidatorUtil.error(LOCATION, "Disk is 100% full, %s");

        assertThat(log.message()).isEqualTo("Disk is 100% full, %s");
        assertThat(log.technicalMessage().arguments()).isEmpty();
    }

    @Test
    public void wrongNumberOfArgumentsDoesNotFail() {
        ValidationLog tooFew = ValidatorUtil.error(LOCATION, "First '%s', second '%s'", "a");
        ValidationLog tooMany = ValidatorUtil.error(LOCATION, "First '%s'", "a", "b");

        assertThat(tooFew.message()).startsWith("First 'a', second '");
        assertThat(tooMany.message()).startsWith("First 'a'").contains("b");
    }

    @Test
    public void missingDefinitionAddsCandidatesToTechnicalMessageOnly() {
        PrismObjectDefinition<UserType> userDef = getUserTypeDefinition();

        ValidationLog log = ValidatorUtil.missingDefinition(
                LOCATION, List.of(userDef), "Item '%s' has no definition", NAME);

        assertThat(log.specification()).isEqualTo(ValidationLogType.Specification.MISSING_DEFINITION);
        assertThat(log.message()).isEqualTo("Item 'fullName' has no definition");
        assertThat(log.technicalMessage().message()).startsWith("Item '%s' has no definition").endsWith("'%s'");
        assertThat(log.technicalMessage().arguments())
                .extracting(Argument::type)
                .containsExactly(ArgumentType.QNAME, ArgumentType.DEFINITION_LIST);
    }

    @Test
    public void missingDefinitionWithoutCandidatesKeepsTemplate() {
        ValidationLog log = ValidatorUtil.missingDefinition(LOCATION, null, "No definition for type '%s'", NAME);

        assertThat(log.specification()).isEqualTo(ValidationLogType.Specification.MISSING_DEFINITION);
        assertThat(log.technicalMessage().message()).isEqualTo("No definition for type '%s'");
        assertThat(log.technicalMessage().arguments()).hasSize(1);
    }
}
