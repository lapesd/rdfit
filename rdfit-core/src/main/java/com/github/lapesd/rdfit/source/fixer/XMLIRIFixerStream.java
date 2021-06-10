/*
 * Copyright 2021 Alexis Armin Huf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.lapesd.rdfit.source.fixer;

import com.github.lapesd.rdfit.source.fixer.impl.IRIFixerParser;
import com.github.lapesd.rdfit.util.GrowableByteBuffer;
import com.github.lapesd.rdfit.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;

public class XMLIRIFixerStream extends InputStream {
    private static final Logger logger = LoggerFactory.getLogger(XMLIRIFixerStream.class);
    private static final int PREFERRED_BUFFER_SIZE = 8192;
    private static final byte[] OWL = "http://www.w3.org/2002/07/owl#".getBytes(UTF_8);
    private static final byte[] RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#".getBytes(UTF_8);
    private static final byte[] RDF_KEY = "rdf".getBytes(UTF_8);
    private static final byte[] OWL_KEY = "owl".getBytes(UTF_8);
    private static final byte[] ABOUT = "about".getBytes(UTF_8);
    private static final byte[] RESOURCE = "resource".getBytes(UTF_8);
    private static final byte[] DATATYPE = "datatype".getBytes(UTF_8);
    private static final byte[] IRI = "IRI".getBytes(UTF_8);


    /* --- --- --- input state --- --- --- */
    private final @Nullable String context;
    private final @Nonnull InputStream delegate;
    private @Nullable byte[] input;
    private int inputPos = 0, inputSize = 0;

    /* --- --- --- output state --- --- --- */

    private int nextCleaned = 0;
    private final GrowableByteBuffer cleaned = new GrowableByteBuffer(256);

    /* --- --- --- State implementations --- --- --- */

    private final IRIFixerParser iriFixer;
    private final NamespaceExtractor extractor = new NamespaceExtractor();
    private FixerParser currentState = extractor;

    private enum Construct {
        NONE,
        LT,
        LT_Q,
        LT_Q_X,
        LT_Q_XM,
        XML,
        XML_Q,
        LT_EX,
        LT_EX_D,
        LT_EX_B,
        LT_EX_B_C,
        LT_EX_B_CD,
        LT_EX_B_CDA,
        LT_EX_B_CDAT,
        LT_EX_B_CDATA,
        TAG,
        CDATA,
        CDATA_B,
        CDATA_B_B,
        COMMENT,
        COMMENT_D,
        COMMENT_D_D;

        private static Construct badStart(@Nullable String context, @Nonnull String what,
                                          @Nonnull String prefix, int value) {
            logger.warn("Bad {} start{}: {}{} (codePoint {}), reverting to out-of-tag state.",
                        what, context == null ? "" : " at "+context+" ", prefix, (char)value, value);
            return NONE;
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public boolean isXMLProcessorTag() {
            switch (this) {
                case LT:
                case LT_Q:
                case LT_Q_X:
                case LT_Q_XM:
                case XML:
                case XML_Q:
                    return true;
                default:
                    return false;
            }
        }

        public @Nonnull Construct next(int value, @Nullable String context) {
            switch (this) {
                case NONE:
                    return value == '<' ? LT : NONE;
                case LT:
                    return value == '!' ? LT_EX : (value == '?' ? LT_Q : TAG);
                case LT_Q:
                    return value == 'X' || value == 'x' ? LT_Q_X : TAG;
                case LT_Q_X:
                    return value == 'M' || value == 'm' ? LT_Q_XM : TAG;
                case LT_Q_XM:
                    return value == 'L' || value == 'l' ? XML : TAG;
                case XML:
                    return value == '?' ? XML_Q : XML;
                case XML_Q:
                    return value == '>' ? NONE : XML;
                case LT_EX:
                    switch (value) {
                        case '-': return LT_EX_D;
                        case '[': return LT_EX_B;
                        default : return TAG;
                    }
                case LT_EX_D:
                    return value == '-' ? COMMENT : badStart(context, "comment", "<!-", value);
                case LT_EX_B:
                    return value == 'C' ? LT_EX_B_C : badStart(context, "CDATA", "<![", value);
                case LT_EX_B_C:
                    return value == 'D' ? LT_EX_B_CD : badStart(context, "CDATA", "<![C", value);
                case LT_EX_B_CD:
                    return value == 'A' ? LT_EX_B_CDA : badStart(context, "CDATA", "<![CD", value);
                case LT_EX_B_CDA:
                    return value == 'T' ? LT_EX_B_CDAT : badStart(context, "CDATA", "<![CDA", value);
                case LT_EX_B_CDAT:
                    return value == 'A' ? LT_EX_B_CDATA : badStart(context, "CDATA", "<![CDAT", value);
                case LT_EX_B_CDATA:
                    return value == '[' ? CDATA : badStart(context, "CDATA", "<![CDATA", value);
                case TAG:
                    return value == '>' ? NONE : TAG;
                case CDATA:
                    return value == ']' ? CDATA_B : CDATA;
                case CDATA_B:
                    return value == ']' ? CDATA_B_B : CDATA;
                case CDATA_B_B:
                    return value == '>' ? NONE : CDATA;
                case COMMENT:
                    return value == '-' ? COMMENT_D : COMMENT;
                case COMMENT_D:
                    return value == '-' ? COMMENT_D_D : COMMENT;
                case COMMENT_D_D:
                    return value == '>' ? NONE : COMMENT;
                default:
                    throw new UnsupportedOperationException("FIXME: no next() for "+this);
            }
        }

    }

    private abstract class AttributeFixerParser implements FixerParser {
        protected final @Nonnull byte[][] expected;
        private final @Nonnull int[] matchedCol;
        private boolean hadMatch = false;
        private int matchedRow = -1;
        protected @Nonnull Construct construct = Construct.NONE;

        public AttributeFixerParser(@Nonnull byte[][] expected) {
            this.expected = expected;
            this.matchedCol = new int[expected.length];
            for (int i = 0; i < expected.length; i++)
                matchedCol[i] = 0;
        }

        protected @Nonnull FixerParser onMatch(int matchedRow) { return this; }
        protected @Nonnull FixerParser onCloseMatchingTag(int matchedRow) { return this; }
        protected @Nonnull FixerParser onAfterMatch(int matchedRow, int byteValue) { return this; }

        @Override public @Nonnull FixerParser reset() {
            clearMatches();
            construct = Construct.NONE;
            return this;
        }

        protected void clearMatches() {
            for (int i = 0, len = matchedCol.length; i < len; i++)
                matchedCol[i] = 0;
            matchedRow = -1;
        }

        @Override public void flush() { }

        protected boolean matchesAny(int byteValue) {
            for (int row = 0, len = expected.length; matchedRow < 0 && row < len; row++) {
                byte ex = expected[row][matchedCol[row]];
                byte ex2 = (ex >= 'a' && ex <= 'z') ? (byte) (ex - ('a'-'A')) : ex;
                if (byteValue == ex || byteValue == ex2) {
                    if (++matchedCol[row] == expected[row].length)
                        matchedRow = row;
                } else {
                    ex = expected[row][0];
                    ex2 = (ex >= 'a' && ex <= 'z') ? (byte) (ex + ('A'-'a')) : ex;
                    matchedCol[row] = byteValue == ex || byteValue == ex2 ? 1 : 0;
                }
            }
            return matchedRow >= 0;
        }

        @Override public @Nonnull FixerParser feedByte(int value) {
            cleaned.add(value);
            boolean wasTag = construct == Construct.TAG;
            if ((construct = construct.next(value, context)) == Construct.TAG) {
                if (matchedRow >= 0) {
                    hadMatch = true;
                    return onAfterMatch(matchedRow, value);
                } else if (matchesAny(value)) {
                    return onMatch(matchedRow);
                }
            } else if (wasTag) {
                FixerParser next = this;
                if (hadMatch) {
                    next = onCloseMatchingTag(matchedRow);
                    hadMatch = false;
                }
                clearMatches();
                return next;
            }
            return this;
        }

        @Override public String toString() {
            return getClass().getSimpleName() + "{matchedRow=" + matchedRow
                    + ", construct=" + construct + '}';
        }
    }


    private class NamespaceExtractor extends AttributeFixerParser {
        private @Nonnull Construct shadowConstruct = Construct.NONE;
        private boolean forbidProcessorTag = false;
        private boolean pastEquals, atIRI;
        private int owlMatched, rdfMatched;
        private final GrowableByteBuffer key = new GrowableByteBuffer();
        private byte[] owlKey, rdfKey;

        public NamespaceExtractor() {
            super(new byte[][]{"xmlns".getBytes(UTF_8)});
        }

        @Override protected void clearMatches() {
            owlMatched = rdfMatched = 0;
            pastEquals = atIRI = false;
            key.clear();
            super.clearMatches();
        }

        @Override public @Nonnull FixerParser reset() {
            owlKey = rdfKey = null;
            forbidProcessorTag = false;
            return super.reset();
        }

        @Override public @Nonnull FixerParser feedByte(int value) {
            boolean forbidProcessorTag = this.forbidProcessorTag;
            if (forbidProcessorTag && construct == Construct.NONE) {
                // we are parsing a <?xml ... ?> tag or value could start one
                Construct old = shadowConstruct;
                shadowConstruct = shadowConstruct.next(value, context);
                if (old == Construct.NONE && shadowConstruct == Construct.NONE) {
                    cleaned.add(value); // whitespace or tag contents
                    return this;
                } else if (old != Construct.XML_Q && !shadowConstruct.isXMLProcessorTag()) {
                    // whatever we were parsing was not a <?xml ?> tag:
                    // "output" bytes we were hiding and pass parsing control to superclass
                    if (old == Construct.LT)
                        cleaned.add('<');
                    else if (old == Construct.LT_Q)
                        cleaned.add('<').add('?');
                    else if (old == Construct.LT_Q_X)
                        cleaned.add('<').add('?').add('x');
                    else if (old == Construct.LT_Q_XM)
                        cleaned.add('<').add('?').add('x').add('m');
                    else
                        assert false : "Unexpected transition from old to !isXMLProcessorTag()";
                    construct = old;
                    shadowConstruct = Construct.NONE;
                } else {
                    return this; // still looks like <?xml ... ?>
                }
            }
            FixerParser successor = super.feedByte(value);
            if (!forbidProcessorTag && !construct.isXMLProcessorTag())
                this.forbidProcessorTag = true; // we just read somehting that is not <?xml ... ?>
            return successor;
        }

        @Override protected @Nonnull FixerParser onMatch(int matchedRow) {
            iriFixer.reset();
            return super.onMatch(matchedRow);
        }

        @Override protected @Nonnull FixerParser onCloseMatchingTag(int matchedRow) {
            if (rdfKey == null && owlKey == null) {
                logger.warn("No OWL nor RDF namespaces detected! will use \"rdf\" " +
                        "and \"owl\" as default prefix names");
            }
            return new AssertionIRIsFixer(rdfKey, owlKey);
        }

        @Override protected @Nonnull FixerParser onAfterMatch(int matchedRow, int byteValue) {
            if (pastEquals) {
                if (atIRI) {
                    if (byteValue == '"') {
                        cleaned.removeLast();
                        iriFixer.flush();
                        cleaned.add('"');
                        clearMatches();
                    } else {
                        if (owlKey == null && owlMatched >= 0) {
                            if (byteValue == OWL[owlMatched]) {
                                if (++owlMatched == OWL.length)
                                    owlKey = key.toArray();
                            } else {
                                owlMatched = -1;
                            }
                        }
                        if (rdfKey == null && rdfMatched >= 0) {
                            if (byteValue == RDF[rdfMatched]) {
                                if (++rdfMatched == RDF.length)
                                    rdfKey = key.toArray();
                            } else {
                                rdfMatched = -1;
                            }
                        }
                        cleaned.removeLast();
                        iriFixer.feedByte(byteValue);
                    }
                } else if (byteValue == '"') {
                    atIRI = true;
                } else {
                    logger.warn("Erasing '{}' (codePoint {}) before '\"' for {}{}", (char)byteValue,
                                byteValue, new String(expected[matchedRow]), key.asString());
                    cleaned.removeLast();
                }
            } else {
                if (byteValue == '=') {
                    pastEquals = true;
                } else if (Utils.isAsciiSpace(byteValue)) {
                    logger.warn("Erasing whitespace after xmlns:{} and before '='", key.asString());
                    cleaned.removeLast();
                } else if (byteValue != ':') {
                    key.add(byteValue);
                }
            }
            return this;
        }
    }

    private class AssertionIRIsFixer extends AttributeFixerParser {
        public AssertionIRIsFixer(@Nullable byte[] rdfKey, @Nullable byte[] owlKey) {
            super(createIRIAttributeExpressions(rdfKey, owlKey));
        }

        @Override protected @Nonnull FixerParser onMatch(int matchedRow) {
            iriFixer.reset();
            return super.onMatch(matchedRow);
        }

        @Override protected @Nonnull FixerParser onAfterMatch(int matchedRow, int byteValue) {
            cleaned.removeLast();
            if (byteValue == '"') {
                iriFixer.flush();
                clearMatches();
                cleaned.add('"');
            } else {
                iriFixer.feedByte(byteValue);
            }
            return this;
        }
    }

    private static @Nonnull byte[] toAttributeExpression(@Nonnull byte[] nsKey,
                                                         @Nonnull byte[] localName) {
        int givenLen = nsKey.length + (nsKey.length == 0 ? 0 : 1) + localName.length;
        byte[] expression = Arrays.copyOf(nsKey, givenLen+2);
        int i = nsKey.length;
        if (i > 0)
            expression[i++] = ':';
        System.arraycopy(localName, 0, expression, i, localName.length);
        i += localName.length;
        expression[i++] = '=';
        expression[i  ] = '"';
        return expression;
    }

    private static byte[][] createIRIAttributeExpressions(byte[] rdfKey, byte[] owlKey) {
        rdfKey = rdfKey == null ? RDF_KEY : rdfKey;
        owlKey = owlKey == null ? OWL_KEY : owlKey;
        return new byte[][] {
                toAttributeExpression(rdfKey, ABOUT),
                toAttributeExpression(rdfKey, RESOURCE),
                toAttributeExpression(rdfKey, DATATYPE),
                toAttributeExpression(owlKey, IRI)
        };
    }

    private int nextInputByte(int maxBytesToRead) throws IOException {
        if (input == null)
            input = new byte[Math.max(maxBytesToRead, PREFERRED_BUFFER_SIZE)];

        if (inputPos == inputSize) {
            inputSize = delegate.read(input, 0, Math.min(input.length, maxBytesToRead));
            inputPos = 0;
        }
        return inputSize == 0 ? delegate.read() : (inputSize < 0 ? -1 : input[inputPos++] & 0xFF);
    }

    /* --- --- --- Public interface --- --- --- */

    /**
     * Create an {@link InputStream} that returns the same bytes as delegate, unless delegate
     * contains bytes that are not valid RDF/XML nor OWL/XML. In case of errors in IRIs, the
     * returned bytes will encode hopefully the same IRIs sans RFC 3987 violations. In case of
     * XML syntax errors, violations are logged but the stream is not altered.
     *  @param delegate source of bytes stream
     * 
     */
    public XMLIRIFixerStream(@Nonnull InputStream delegate) {
        this(delegate, delegate.toString());
    }

    /**
     * Create an {@link InputStream} that returns the same bytes as delegate, unless delegate
     * contains bytes that are not valid RDF/XML nor OWL/XML. In case of errors in IRIs, the
     * returned bytes will encode hopefully the same IRIs sans RFC 3987 violations. In case of
     * XML syntax errors, violations are logged but the stream is not altered.
     *
     * @param delegate source of bytes stream
     * @param context if errors are fixed/found, use this as a description of the data source
     */
    public XMLIRIFixerStream(@Nonnull InputStream delegate, @Nullable String context) {
        this.delegate = delegate;
        this.context = context;
        this.iriFixer = new IRIFixerParser(cleaned, context);
    }

    @Override public int available() throws IOException {
        return Math.max(0, nextCleaned - cleaned.size()) + delegate.available();
    }

    @Override public int read() throws IOException {
        while (true) {
            if (nextCleaned < cleaned.size())
                return cleaned.get(nextCleaned++) & 0xFF;    // return a byte output by currentState
            else if (nextCleaned != 0)
                nextCleaned = cleaned.clear().size(); // exhausted override, clear it
            int value = delegate.read();
            if (value < 0) {
                currentState.flush();
                if (!cleaned.isEmpty())
                    return cleaned.get(nextCleaned++) & 0xFF;
                return value; // EOF
            } else {
                currentState = currentState.feedByte(value); // writes to output
            }
        }
    }

    @Override public int read(@Nonnull byte[] out, int off, int len) throws IOException {
        assert off+len <= out.length;
        int i = off, end = off+len, val = 0;
        while (i < end && val > -1) {
            int cleanedLen = Math.min(end - i, cleaned.size()- nextCleaned);
            if (cleanedLen > 0) {
                System.arraycopy(cleaned.getArray(), nextCleaned, out, i, cleanedLen);
                if ((nextCleaned += cleanedLen) == cleaned.size())
                    nextCleaned = cleaned.clear().size(); //exhausted override
                if ((i += cleanedLen) == end)
                    break;                                  // exhausted out
            }
            val = nextInputByte(len);
            if (val >= 0) {
                currentState = currentState.feedByte(val);
            } else {
                currentState.flush();
                if (!cleaned.isEmpty()) val = 0;
            }
        }
        int count = i - off;
        return count > 0 ? count : -1;
    }

    @Override public String toString() {
        return getClass().getSimpleName() + "{delegate=" + delegate +
                ", nextOverride=" + nextCleaned + ", output=" + cleaned +
                ", currentState=" + currentState + '}';
    }
}
