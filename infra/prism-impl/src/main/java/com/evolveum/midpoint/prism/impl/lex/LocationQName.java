package com.evolveum.midpoint.prism.impl.lex;

import javax.xml.namespace.QName;

/**
 * Extended QName.... TODO write javadocs
 *
 * Created by Dominik.
 */
public class LocationQName extends QName {

    private int startOffset = -1;
    private int endOffset = -1;

    public LocationQName(String localPart) {
        super(localPart);
    }

    public int getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(int endOffset) {
        this.endOffset = endOffset;
    }
}
