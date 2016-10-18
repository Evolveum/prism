package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.lex.LexicalProcessor;
import com.evolveum.midpoint.prism.xnode.RootXNode;
import com.evolveum.midpoint.util.exception.SchemaException;
import org.jetbrains.annotations.NotNull;

/**
 * @author mederly
 */
public class SerializerStringTarget extends SerializerTarget<String> {

    @NotNull private final String language;

    public SerializerStringTarget(@NotNull PrismContextImpl prismContext, @NotNull String language) {
        super(prismContext);
        this.language = language;
    }

    @NotNull
    @Override
    public String write(@NotNull RootXNode xroot, SerializationContext context) throws SchemaException {
        LexicalProcessor<String> lexicalProcessor = prismContext.getLexicalProcessorRegistry().processorFor(language);
        return lexicalProcessor.write(xroot, context);
    }
}
