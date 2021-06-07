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

package com.github.lapesd.rdfit.source.fixer.impl;

import com.github.lapesd.rdfit.source.fixer.FixerParser;
import com.github.lapesd.rdfit.util.GrowableByteBuffer;
import com.github.lapesd.rdfit.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.github.lapesd.rdfit.util.Utils.isInSmall;
import static java.nio.charset.StandardCharsets.UTF_8;

public class TurtleFixerParsers {
    private static final Logger logger = LoggerFactory.getLogger(TurtleFixerParsers.class);

    private static final byte[] TRIPLE_SEP = {',', '.', ';'};                 // sorted
    private static final byte[] POST_LEXICAL = {',', '.', ';', '^'};          // sorted
    private static final byte[] VALID_ESCAPES = "\"'\\bfnrt".getBytes(UTF_8); // sorted


    protected static abstract class State implements FixerParser {
        protected @Nonnull GrowableByteBuffer output;

        public State(@Nonnull GrowableByteBuffer output) {
            this.output = output;
        }

        @Override public @Nonnull FixerParser reset() { return this; }

        @Override public void flush() { }
    }

    /**
     * Start state of a NT/Turtle/TriG {@link FixerParser}
     */
    public static class Start extends State {
        private final @Nonnull TurtleIRI iri;
        private final @Nonnull StringLiteral string;

        /**
         *
         * @param override where to write validated/fixed turtle bytes
         * @param context If an error is found or fixed, use this as a description of the source
         */
        public Start(@Nonnull GrowableByteBuffer override, @Nullable String context) {
            super(override);
            this.iri = new TurtleIRI(this, context);
            this.string = new StringLiteral(this, context);
        }

        @Override public @Nonnull FixerParser feedByte(int value) {
            output.add(value);
            if      (value ==  '<') return iri.reset();
            else if (value ==  '"') return string.reset('"');
            else if (value == '\'') return string.reset('\'');
            return this;
        }
    }

    public static class StringLiteral extends State {
        private boolean opened = false, escaped = false;
        private int openSymbol = '"', openCount = 0, closeCount = 0;
        private int consumedHex = 0, expectedHex = 0;
        private final byte[] hexDigits = new byte[8];
        private final @Nonnull PostString postString;
        private final @Nullable String context;

        public StringLiteral(@Nonnull Start start, @Nullable String context) {
            super(start.output);
            this.postString = new PostString(start, context);
            this.context = context;
        }

        public @Nonnull StringLiteral reset(char open) {
            openSymbol = open;
            openCount = 1;
            closeCount = 0;
            opened = escaped = false;
            return this;
        }

        @Override public @Nonnull State feedByte(int value) {
            output.add(value);
            if (!opened) {
                if (value == openSymbol) {
                    ++openCount;
                    if (openCount == 3)
                        opened = true;
                    return this;
                } else {
                    opened = true;
                    if (openCount == 2 || openCount == 3) {
                        output.removeLast();
                        return postString.reset().feedByte(value);
                    }// else: fall through the other if's to process value
                }
            }
            if (openCount == 1 &&  (value == '\n' || value == '\r')) {
                output.removeLast().add('\\').add((value == '\n' ? 'n' : 'r'));
            } else if (expectedHex > 0) {
                output.removeLast();
                if (Utils.isHexDigit(value)) {
                    hexDigits[consumedHex++] = (byte)value;
                    if (consumedHex == expectedHex) {
                        output.add(expectedHex==8 ? 'U':'u').add(hexDigits, 0, expectedHex);
                        expectedHex = consumedHex = 0;
                    }
                } else {
                    GrowableByteBuffer bb = new GrowableByteBuffer()
                            .add(expectedHex == 8 ? 'U' : 'u')
                            .add(hexDigits, 0, consumedHex).add(value);
                    output.add(bb);
                    logger.warn("{}Bad UCHAR sequence '{}' (last codePoint={}). Replacing \\ " +
                                "with \\\\", context == null ? "" : context+": ", bb, value);
                    expectedHex = consumedHex = 0;
                }
            } else if (escaped) { // previous char was \
                escaped = false;
                if (value == 'U') {
                    output.removeLast();
                    expectedHex = 8;
                    consumedHex = 0;
                } else if (value == 'u') {
                    output.removeLast();
                    expectedHex = 4;
                    consumedHex = 0;
                } else if (!isInSmall(value, VALID_ESCAPES)) {// escape previous \, writing \\
                    logger.warn("{}Invalid ECHAR: \"{}\" (codePoint={}), " +
                                "replacing \"\\\" with \"\\\\\".",
                                context == null ? "" : context+": ", "\\"+(char)value, value);
                    output.removeLast().add('\\').add(value);
                }
            } else if (value == '\\') { // start a \-escape
                escaped = true;
            } else if (value == openSymbol) { // maybe we are closing the string
                ++closeCount;
                if (closeCount == openCount)
                    return postString; // lexical form ended, now expect (@|^^|,|.|\\s*)
            } else {
                closeCount = 0; // discard fake close sequence
            }
            return this; // still in the lexical form
        }

        @Override public @Nonnull String toString() {
            return "StringLiteral{opened=" + opened + ", escaped=" + escaped +
                    ", openSymbol=" + openSymbol + ", openCount=" + openCount +
                    ", closeCount=" + closeCount + ", postString=" + postString + '}';
        }
    }

    public static class PostString extends State {
        private final @Nonnull Start start;
        private final @Nonnull LangTag langTagState;
        private final @Nullable String context;

        public PostString(@Nonnull Start start, @Nullable String context) {
            super(start.output);
            this.start = start;
            this.langTagState = new LangTag(start, context);
            this.context = context;
        }

        @Override public @Nonnull PostString reset() {
            return (PostString) super.reset();
        }

        @Override public @Nonnull State feedByte(int value) {
            output.add(value);
            if (value == '@') {
                return langTagState.reset();
            } else if (Utils.isAsciiSpace(value)) {
                return this;
            } else if (!Utils.isInSmall(value, POST_LEXICAL)) {
                String pref = context == null ? "" : context+": ";
                logger.warn("{}Ignoring lexical form followed by {} (codePoint={}) instead of" +
                            " whitespace or a triple separator ([,.;])", pref, (char)value, value);
                return start;
            } else {
                return start; // start parsing new triple
            }
        }
    }

    public static class LangTag extends State {
        private final @Nonnull Start start;
        private boolean silence = false;
        private final @Nullable String context;

        public LangTag(@Nonnull Start start, @Nullable String context) {
            super(start.output);
            this.start = start;
            this.context = context;
        }

        @Override public @Nonnull LangTag reset() {
            super.reset();
            silence = false;
            return this;
        }

        @Override public @Nonnull FixerParser feedByte(int value) {
            if (Utils.isAsciiSpace(value) || Utils.isInSmall(value, TRIPLE_SEP)) {
                silence = false; // we should output the byte as it is not part of the lang tag
                output.add(value);
                return start; // lang tag finished
            } else if (value == '_' || value == '-') {
                logger.warn("{}Erasing {}-suffix from lang tag.",
                            context == null ? "" : context + ": ", (char)value);
                silence = true; // bad lang tag, ignore _ and anything after it
            } else if (!silence) {
                output.add(value);
            }
            return this;
        }

        @Override public String toString() {
            return "LangTagState{silence=" + silence + '}';
        }
    }

    public static class TurtleIRI extends IRIFixerParser {
        private static final Logger logger = LoggerFactory.getLogger(TurtleIRI.class);
        private static final byte[] SLASH = "%5C".getBytes(UTF_8);

        private final @Nonnull Start start;
        private boolean slashed = false;
        private int consumedHex = 0, expectedHex = 0;
        private final byte[] hexDigits = new byte[8];

        public TurtleIRI(@Nonnull Start start, @Nullable String context) {
            super(start.output, context);
            this.start = start;
        }

        @Override public @Nonnull FixerParser reset() {
            return super.reset();
        }

        @Override public @Nonnull FixerParser feedByte(int value) {
            if (slashed) { // preceded by \
                slashed = false;
                if (value == 'u') {
                    expectedHex = 4;
                    consumedHex = 0;
                    return this;
                } else if (value == 'U') {
                    expectedHex = 8;
                    consumedHex = 0;
                    return this;
                } else {
                    logger.warn("{}Replacing \\ in \\\\{} with %5C",
                                getContextPrefix("Bad \\-escape at %s: "),  value);
                    output.add(SLASH);
                }
            } else if (expectedHex > 0) { // preceded by \\u or \\U
                if (Utils.isHexDigit(value)) {
                    hexDigits[consumedHex++] = (byte)value;
                    if (consumedHex == expectedHex) {
                        output.add('\\').add(expectedHex == 8 ? 'U' : 'u');
                        output.add(hexDigits, 0, consumedHex);
                        expectedHex = consumedHex = 0;
                    }
                    return this;
                } else {
                    String in = (expectedHex == 8 ? 'U' : 'u')
                              + new String(hexDigits, 0, consumedHex);
                    logger.warn("{}Replacing \\\\{} in \\\\{} with %5C{}",
                                getContextPrefix("Bad UCHAR at %s: "),
                                in, in+(char)value, in);
                    output.add(SLASH).add(hexDigits, 0, consumedHex);
                    expectedHex = 0;
                }
            } else { // not handling Turtle UCHAR
                if (value == '>') { //
                    flush();
                    output.add(value);
                    return start;
                } else if (value == '\\') {
                    slashed = true;
                }
            }
            return super.feedByte(value);
        }
    }
}
