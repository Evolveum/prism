package com.evolveum.midpoint.prism.impl.lex.dom.locator;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

/**
 * Created by Dominik.
 */
public class PositionTrackingReader extends Reader {
    private final Reader in;
    private int line = 1;
    private int column = 0;
    private int lastChar = -1;

    private final Queue<TagPosition> tagQueue = new LinkedList<>();
    private final StringBuilder tagBuffer = new StringBuilder();

    private boolean inTag = false;
    private boolean isEndTag = false;
    private boolean isSelfClosing = false;
    private boolean sawName = false;

    private int tagStartLine = 0;
    private int tagStartColumn = 0;

    public PositionTrackingReader(Reader in) {
        this.in = in;
    }

    public TagPosition pollTag(TagPosition.Type type) {
        while (!tagQueue.isEmpty()) {
            TagPosition pos = tagQueue.peek();
            if (pos.type() == type) {
                return tagQueue.poll();
            } else {
                tagQueue.poll(); // discard mismatched type
            }
        }
        return null;
    }

    @Override
    public int read(char @NotNull [] cbuf, int off, int len) throws IOException {
        int n = in.read(cbuf, off, len);
        for (int i = 0; i < n; i++) {
            char ch = cbuf[off + i];
            updatePosition(ch);
            processChar(ch);
        }
        return n;
    }

    private void updatePosition(int ch) {
        if (lastChar == '\r' && ch == '\n') {
            lastChar = ch;
            return;
        }

        if (ch == '\r' || ch == '\n') {
            line++;
            column = 0;
        } else {
            column++;
        }

        lastChar = ch;
    }

    private void processChar(char ch) {
        if (ch == '<') {
            inTag = true;
            isEndTag = false;
            isSelfClosing = false;
            sawName = false;
            tagBuffer.setLength(0);
            tagStartLine = line;
            tagStartColumn = column;
        } else if (inTag) {
            if (!sawName && ch == '/') {
                isEndTag = true;
            } else if (ch == '>') {
                String name = tagBuffer.toString().trim();
                inTag = false;
                if (!name.isEmpty() && !name.startsWith("?") && !name.startsWith("!")) {
                    if (isEndTag) {
                        tagQueue.offer(new TagPosition(name, tagStartLine, tagStartColumn, TagPosition.Type.END));
                    } else if (isSelfClosing) {
                        tagQueue.offer(new TagPosition(name, tagStartLine, tagStartColumn, TagPosition.Type.START));
                        tagQueue.offer(new TagPosition(name, tagStartLine, tagStartColumn, TagPosition.Type.END));
                    } else {
                        tagQueue.offer(new TagPosition(name, tagStartLine, tagStartColumn, TagPosition.Type.START));
                    }
                }

            } else if (ch == '/' && !tagBuffer.isEmpty()) {
                isSelfClosing = true;

            } else if (!Character.isWhitespace(ch)) {
                // Capture name (with prefix if any)
                if (!sawName) {
                    tagBuffer.append(ch);
                }
                if (Character.isLetterOrDigit(ch) || ch == ':' || ch == '-' || ch == '_') {
                    tagBuffer.append(ch);
                }
                sawName = true;
            }
        }
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
