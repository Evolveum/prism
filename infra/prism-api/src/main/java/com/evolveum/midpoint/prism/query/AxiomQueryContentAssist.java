package com.evolveum.midpoint.prism.query;

import com.evolveum.midpoint.prism.ItemDefinition;
import org.jetbrains.annotations.Nullable;

public interface AxiomQueryContentAssist {

    ContentAssist process(@Nullable ItemDefinition<?> rootItem, String query, int cursorPosition, ContentAssist.Options option);

}
