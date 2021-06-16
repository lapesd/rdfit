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

import java.nio.CharBuffer;
import java.nio.charset.*;
import java.util.regex.Pattern;

import static com.github.lapesd.rdfit.util.Utils.asciiLower;
import static com.github.lapesd.rdfit.util.Utils.isInSmall;
import static java.nio.charset.StandardCharsets.UTF_8;

public class TurtleFixerParsers {
    private static final Logger logger = LoggerFactory.getLogger(TurtleFixerParsers.class);

    private static final byte[] TRIPLE_SEP = {',', '.', ';'};                      // sorted
    private static final byte[] POST_LEXICAL = {',', '.', ';', '^'};               // sorted
    private static final byte[] VALID_ESCAPES = "\"'\\bfnrt".getBytes(UTF_8);      // sorted
    private static final byte[] NUMBER_START = "+-.0123456789".getBytes(UTF_8);    // sorted
    private static final byte[] NUMBER_CHAR = "+-.0123456789Ee".getBytes(UTF_8);   // sorted
    private static final byte[] NUMBER_END = "\t\n\r ,;".getBytes(UTF_8);          // sorted
    private static final byte[] BOOLEAN_START = {'F', 'T', 'f', 't'};              // sorted
    private static final byte[] ETHER_CHARS = "\t\n\r ,.;[]{}".getBytes(UTF_8);    // sorted
    private static final byte[] UNQUOTED_END = "\t\n\r ,.;".getBytes(UTF_8);       // sorted


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
        private final @Nonnull NumberLiteral number;
        private final @Nonnull BoolLiteral bool;
        private final @Nonnull UnquotedStringLiteral unquoted;
        private final @Nonnull Comment comment;

        /**
         * Create a Start state
         *
         * @param override where to write validated/fixed turtle bytes
         * @param context If an error is found or fixed, use this as a description of the source
         */
        public Start(@Nonnull GrowableByteBuffer override, @Nullable String context) {
            super(override);
            this.iri = new TurtleIRI(this, context);
            this.string = new StringLiteral(this, this.iri, context);
            this.unquoted = new UnquotedStringLiteral(this, this.string, this.iri);
            this.number = new NumberLiteral(this, this.unquoted);
            this.bool = new BoolLiteral(this, this.unquoted);
            this.comment = new Comment(this);
        }

        @Override public @Nonnull FixerParser feedByte(int value) {
            output.add(value);
            if      (value ==  '<')                 return iri.reset();
            else if (value ==  '"')                 return string.reset('"');
            else if (value == '\'')                 return string.reset('\'');
            else if (value ==  '#')                 return comment.reset();
            else if (isInSmall(value, ETHER_CHARS)) return this;

            output.removeLast(); // the following states buffer
            if      (isInSmall(value, NUMBER_START))  return number.reset().feedByte(value);
            else if (isInSmall(value, BOOLEAN_START)) return bool.reset().feedByte(value);
            else                                      return unquoted.reset().feedByte(value);
        }
    }

    public static class NumberLiteral extends State {
        /** cf. https://www.w3.org/TR/turtle/#grammar-production-NumericLiteral */
        private static final Pattern NUMBER_RX = Pattern.compile("[+-]?(?:" +
                "[0-9]+|" +
                "[0-9]*\\.[0-9]+|" +
                "(?:[0-9]+\\.[0-9]*|" +
                   "\\.?[0-9]+)" +
                "[eE][+-]?[0-9]+" +
                ")"
        );
        private final @Nonnull Start start;
        private final @Nonnull UnquotedStringLiteral unquoted;
        private final StringBuilder builder = new StringBuilder();

        public NumberLiteral(@Nonnull Start start, @Nonnull UnquotedStringLiteral unquoted) {
            super(start.output);
            this.start = start;
            this.unquoted = unquoted;
        }

        @Override public @Nonnull NumberLiteral reset() {
            builder.setLength(0);
            return (NumberLiteral) super.reset();
        }

        @Override public void flush() {
            for (int i = 0, len = builder.length(); i < len; i++)
                output.add(builder.charAt(i));
            builder.setLength(0);
        }

        @Override public @Nonnull FixerParser feedByte(int byteValue) {
            boolean ok = isInSmall(byteValue, NUMBER_CHAR);
            if (ok) {
                builder.append((char) byteValue);
            } else if (isInSmall(byteValue, NUMBER_END)) {
                ok = NUMBER_RX.matcher(builder).matches();
                if (ok) {
                    flush();
                    return start.feedByte(byteValue);
                }
            }
            if (ok)
                return this;
            FixerParser s = unquoted.reset();
            for (int i = 0, len = builder.length(); i < len; i++)
                s = s.feedByte(builder.charAt(i));
            return s.feedByte(byteValue);
        }
    }

    public static class BoolLiteral extends State {
        private static final byte[] FALSE = "false".getBytes(UTF_8);
        private static final byte[] TRUE = "true".getBytes(UTF_8);
        private byte[] expected = null;
        private final byte[] buffer = new byte[5];
        private int matched = 0;
        private final @Nonnull Start start;
        private final @Nonnull UnquotedStringLiteral unquoted;

        public BoolLiteral(@Nonnull Start start, @Nonnull UnquotedStringLiteral unquoted) {
            super(start.output);
            this.start = start;
            this.unquoted = unquoted;
        }

        @Override public @Nonnull FixerParser reset() {
            expected = null;
            matched = 0;
            return super.reset();
        }

        @Override public void flush() {
            if (expected != null && matched == expected.length) {
                output.add(expected);
                reset();
            }
            dumpToUnquoted().flush();
        }

        @Override public @Nonnull FixerParser feedByte(int byteValue) {
            boolean ok;
            int lower = asciiLower(byteValue);
            if (expected == null) {
                ok = (expected = lower == 'f' ? FALSE : (lower == 't' ? TRUE : null)) != null;
            } else if (matched == expected.length) {
                if ((ok = isInSmall(byteValue, UNQUOTED_END))) {
                    output.add(expected).add(byteValue);
                    reset();
                    return start;
                }
            } else {
                ok = lower == expected[matched];
            }
            if (ok) {
                buffer[matched++] = (byte) byteValue;
                return this;
            } else {
                return dumpToUnquoted().feedByte(byteValue);
            }
        }

        private @Nonnull FixerParser dumpToUnquoted() {
            FixerParser s = unquoted.reset();
            for (int i = 0; i < matched; i++)
                s = s.feedByte(buffer[i] & 0xFF);
            reset();
            return s;
        }
    }

    public static class PrefixOrBase extends State {
        private final @Nonnull TurtleIRI iri;

        public PrefixOrBase(@Nonnull GrowableByteBuffer output, @Nonnull TurtleIRI iri) {
            super(output);
            this.iri = iri;
        }

        @Override public @Nonnull FixerParser feedByte(int byteValue) {
            output.add(byteValue);
            return byteValue == '<' ? iri.reset() : this;
        }
    }

    public static class Comment extends State {
        private final @Nonnull Start start;

        public Comment(@Nonnull Start start) {
            super(start.output);
            this.start = start;
        }

        @Override public @Nonnull FixerParser feedByte(int byteValue) {
            output.add(byteValue);
            return byteValue == '\n' ? start : this;
        }
    }

    public static class UnquotedStringLiteral extends State {
        private static final byte[] AT_PREFIX = "@prefix ".getBytes(UTF_8);
        private static final byte[] AT_BASE = "@base ".getBytes(UTF_8);
        private static final byte[] PREFIX = "PREFIX ".getBytes(UTF_8);
        private static final byte[] BASE = "BASE ".getBytes(UTF_8);
        private static final byte[] A = "a ".getBytes(UTF_8);
        private final @Nonnull GrowableByteBuffer buffer = new GrowableByteBuffer();
        private final @Nonnull Start start;
        private final @Nonnull StringLiteral string;
        private final @Nonnull PrefixOrBase prefixOrBase;
        private @Nullable byte[] matching;
        private int matched = 0;
        private boolean hadColon = false;

        public UnquotedStringLiteral(@Nonnull Start start, @Nonnull StringLiteral string,
                                     @Nonnull TurtleIRI iri) {
            super(start.output);
            this.start = start;
            this.string = string;
            this.prefixOrBase = new PrefixOrBase(start.output, iri);
        }

        @Override public void flush() {
            if (hadColon) {
                output.add(buffer);
            } else if (!buffer.isEmpty()) {
                output.add('"');
                string.reset('"');
                byte[] array = buffer.getArray();
                for (int i = 0, len = buffer.size(); i < len; i++)
                    string.feedByte(array[i] & 0xFF);
                string.feedByte('"');
            }
            buffer.clear();
        }

        @Override public @Nonnull FixerParser reset() {
            hadColon = false;
            buffer.clear();
            matching = null;
            matched = 0;
            return super.reset();
        }

        @Override public @Nonnull FixerParser feedByte(int byteValue) {
            buffer.add(byteValue);
            if (matching != null) {
                int lower = matching[0] == '@' ? asciiLower(byteValue) : byteValue;
                if (lower == matching[matched]) {
                    if (++matched == matching.length) {
                        output.add(matching);
                        return prefixOrBase;
                    }
                    return this;
                } else {
                    matching = null;
                    matched = -1;
                    // fall trough the ifs below
                }
            }
            if (matched == 0) { //first byte
                matched = 1;
                if      (byteValue == 'P') matching = PREFIX;
                else if (byteValue == 'B') matching = BASE;
                else if (byteValue == 'a') matching = A;
                else if (byteValue != '@') matched = -1; // failed match
            } else if (matched == 1) { // first byte after '@'
                matched = 2;
                int lower = asciiLower(byteValue);
                if      (lower == 'p') matching = AT_PREFIX;
                else if (lower == 'b') matching = AT_BASE;
                else                   matched = -1; //failed match
            } else if (isInSmall(byteValue, UNQUOTED_END)) {
                buffer.removeLast();
                flush();
                return start.feedByte(byteValue);
            } else if (byteValue == ':') {
                hadColon = true;
            }
            return this;
        }
    }

    public abstract static class UCharFixer {
        private static final @Nonnull byte[] BYTE_PREFIX = "\\u00".getBytes(UTF_8);

        protected final @Nonnull GrowableByteBuffer output;
        protected final @Nonnull GrowableByteBuffer buffer = new GrowableByteBuffer();
        private final @Nonnull GrowableByteBuffer encodedBytes = new GrowableByteBuffer();
        private final @Nonnull CharsetDecoder u8Dec = UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
        private final @Nonnull CharBuffer u8Out = CharBuffer.allocate(2);
        private boolean escaped = false;
        private int consumedHex = 0, expectedHex = 0;

        public UCharFixer(@Nonnull GrowableByteBuffer output) {
            this.output = output;
        }

        private int ucharLen(int value) {
            return value == 'u' ? 4 : value == 'U' ? 8 : 0;
        }

        /**
         * Handle an invalid UCHAR sequence starting at begin and ending at end
         *
         * @param buffer byte buffer containing the invalid UCHAR sequence defined by begin and end
         * @param begin index of the first byte part of the UCHAR sequence
         * @param len the number of bytes in the invalid UCHAR sequence
         */
        protected abstract void handleInvalid(@Nonnull GrowableByteBuffer buffer,
                                              int begin, int len);

        public void reset() {
            escaped = false;
            consumedHex = expectedHex = 0;
            buffer.clear();
            encodedBytes.clear();
        }

        public void flush() {
            if (buffer.isEmpty())
                return;
            byte[] data = buffer.getArray();
            int expectedBytes = 0;
            boolean hadControl = false;
            for (int i = 0, size = buffer.size(); i < size; ) {
                int uIdx = i + 1;
                if (uIdx >= size) {
                    flushEncodedBytes();
                    output.add(data[i]);
                    break;
                }
                int digits = ucharLen(data[i+1]), end = i+2+digits;
                assert digits == 4 || digits == 8;
                if (end > size)
                    handleInvalid(buffer, i, size-i);
                boolean isByte = true;
                for (int j = i+2, zeroEnd = i+2+(digits-2); isByte && j < zeroEnd; j++)
                    isByte = data[j] == '0';
                if (isByte) {
                    int byteValue = Utils.parseHexByte(data[end - 2], data[end - 1]);
                    hadControl |= byteValue < 0x20 || (byteValue >= 0x80 && byteValue <= 0xa0);
                    if (encodedBytes.isEmpty())  {
                        if      ((byteValue & 0b11000000) == 0b10000000) expectedBytes = 1;
                        else if ((byteValue & 0b11100000) == 0b11000000) expectedBytes = 2;
                        else if ((byteValue & 0b11110000) == 0b11100000) expectedBytes = 3;
                        else if ((byteValue & 0b11111000) == 0b11110000) expectedBytes = 4;
                        else                                             expectedBytes = 0;
                        if (expectedBytes > 0) {
                            encodedBytes.add(byteValue);
                        } else {
                            output.add(data, i, 2+digits);
                            hadControl = false;
                        }
                    } else if (encodedBytes.add(byteValue).size() == expectedBytes) {
                        boolean decoded = false;
                        if (hadControl) {
                            u8Out.clear();
                            CoderResult r = u8Dec.decode(encodedBytes.asByteBuffer(), u8Out, true);
                            assert !r.isOverflow();
                            if ((decoded = !r.isError())) {
                                int codePoint = Utils.toCodePoint(u8Out);
                                if ((decoded = codePoint >= 0))
                                    output.add(String.format("\\u%04X", codePoint).getBytes(UTF_8));
                                else
                                    assert false : "u8Dec decoded no char!";
                            }
                        }
                        if (!decoded)
                            flushEncodedBytes();
                        encodedBytes.clear();
                    }
                } else {
                    output.add(buffer.getArray(), i, end-i);
                    encodedBytes.clear();
                }
                i = end;
            }
            flushEncodedBytes();
            buffer.clear();
        }

        private void flushEncodedBytes() {
            byte[] a = encodedBytes.getArray();
            for (int j = 0, size = encodedBytes.size(); j < size; j++)
                Utils.writeHexByte(output.add(BYTE_PREFIX), a[j] & 0xFF);
            encodedBytes.clear();
        }

        /**
         * Processes the given byte value and return whether the input was accepted or rejected.
         *
         * If the input byte is rejected, {@link UCharFixer#flush()} is called before return
         * and the caller is responsible for handling the byte represented by value. An input
         * will be rejected if value is not part of a UCHAR nor is the start of one
         *
         * @param value the byte value to process.
         * @return true if value was accepted, false if rejected and the caller must handle it.
         */
        public boolean feedByte(int value) {
            buffer.add(value);
            if (expectedHex == 0) {
                if (value == '\\' && !escaped) {
                    return escaped = true;
                } else if (escaped) {
                    escaped = false;
                    if ((expectedHex = ucharLen(value)) > 0)
                        return true;
                }
            } else if (Utils.isHexDigit(value)) {
                if (++consumedHex == expectedHex)
                    expectedHex = consumedHex = 0;
                return true;
            }
            buffer.removeLast(); // value is not a valid escape, do not process it
            flush();
            return false;
        }
    }

    protected static class StringUCharFixer extends UCharFixer {
        private final @Nonnull String contextPrefix;
        public StringUCharFixer(@Nonnull GrowableByteBuffer output, @Nullable String context) {
            super(output);
            contextPrefix = context == null ? "" : context+": ";
        }

        @Override
        protected void handleInvalid(@Nonnull GrowableByteBuffer buffer, int begin, int len) {
            logger.warn("{}Replacing \\ with \\\\ in {}",
                        contextPrefix, buffer.asString(begin, len));
            output.add('\\').add(buffer.getArray(), begin, len-1);
        }
    }

    public static class StringLiteral extends State {
        private boolean opened = false, escaped = false, uCharActive = false;
        private int openSymbol = '"', openCount = 0, closeCount = 0;
        private final @Nonnull PostString postString;
        private final @Nullable String context;
        private final @Nonnull StringUCharFixer uChar;

        public StringLiteral(@Nonnull Start start, @Nonnull TurtleIRI iri,
                             @Nullable String context) {
            super(start.output);
            this.postString = new PostString(start, context, iri);
            this.context = context;
            this.uChar = new StringUCharFixer(start.output, context);
        }

        public @Nonnull StringLiteral reset(char open) {
            openSymbol = open;
            openCount = 1;
            closeCount = 0;
            opened = escaped = false;
            return this;
        }

        @Override public void flush() {
            if (uCharActive) uChar.flush();
            super.flush();
        }

        @Override public @Nonnull FixerParser feedByte(int value) {
            // handle opening sequence of quotes
            if (!opened) {
                if (value == openSymbol) {
                    if (++openCount == 3)
                        opened = true;
                    output.add(value);
                    return this;
                } else {
                    opened = true;
                    if (openCount == 2 || openCount == 3)
                        return postString.reset().feedByte(value);
                    // else: fall through the other if's to process value
                }
            }

            //handle escapes
            if (escaped) {
                escaped = false;
                if (value == 'u' || value == 'U') { // handling a UCHAR
                    uCharActive = true;
                    uChar.feedByte('\\');
                    uChar.feedByte(value);
                } else if (!isInSmall(value, VALID_ESCAPES)) {// escape previous \, writing \\
                    logger.warn("{}Invalid ECHAR: \"{}\" (codePoint={}), " +
                                    "replacing \"\\\" with \"\\\\\".",
                            context == null ? "" : context+": ", "\\"+(char)value, value);
                    uChar.flush();
                    output.add('\\').add('\\').add(value);
                } else { // valid, non-UCHAR escape
                    uChar.flush();
                    output.add('\\').add(value);
                }
                return this;
            } else if (value == '\\') { // start a new escape
                escaped = true;
                return this;
            } else if (uCharActive) { // handling the code point of an UCHAR
                if (uChar.feedByte(value))
                    return this; // value is part of the UCHAR
                else
                    uCharActive = false; // UCHAR ended and value was rejected by uChar
            }

            if (openCount == 1 &&  (value == '\n' || value == '\r')) { // escape line breaks
                output.add('\\').add((value == '\n' ? 'n' : 'r'));
            } else if (value == openSymbol) { // maybe we are closing the string
                output.add(value);
                ++closeCount;
                if (closeCount == openCount)
                    return postString; // lexical form ended, now expect (@|^^|,|.|\\s*)
            } else { // value requires no special treatment
                output.add(value == '\0' ? ' ' : value);
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
        private final @Nonnull TurtleIRI iri;
        private final @Nullable String context;

        public PostString(@Nonnull Start start, @Nullable String context, @Nonnull TurtleIRI iri) {
            super(start.output);
            this.start = start;
            this.iri = iri;
            this.langTagState = new LangTag(start, context);
            this.context = context;
        }

        @Override public @Nonnull FixerParser feedByte(int value) {
            output.add(value);
            if (value == '@') {
                return langTagState.reset();
            } else if (value == '^') {
                output.removeLast();
                return this;
            } else if (value == '<') {
                output.removeLast().add('^').add('^').add('<');
                return iri.reset();
            } else if (Utils.isAsciiSpace(value)) {
                return this;
            } else if (!Utils.isInSmall(value, POST_LEXICAL)) {
                String pref = context == null ? "" : context+": ";
                logger.warn("{}Cannot fix lexical form followed by {} (codePoint={}) instead of" +
                            " whitespace or a triple separator ([,.;])", pref, (char)value, value);
                return start;
            } else {
                return start; // start parsing new triple
            }
        }
    }

    public static class LangTag extends State {
        private final @Nonnull Start start;
        private final @Nullable String context;

        public LangTag(@Nonnull Start start, @Nullable String context) {
            super(start.output);
            this.start = start;
            this.context = context;
        }

        @Override public @Nonnull FixerParser feedByte(int value) {
            output.add(value);
            if (Utils.isAsciiSpace(value) || Utils.isInSmall(value, TRIPLE_SEP)) {
                return start; // lang tag finished
            } else if (value == '_') {
                logger.warn("{}replacing _ with - in lang tag.",
                        context == null ? "" : context + ": ");
                output.removeLast().add('-');
            } else if (value != '-' && !Utils.isAsciiAlphaNum(value)) {
                logger.warn("{}erasing unexpected char {} (codePoint={}) from lang tag",
                            context == null ? "" : context + ": ", (char) value, value);
                output.removeLast();
            }
            return this;
        }
    }

    protected static class IRIUChar extends UCharFixer {
        private final @Nonnull String contextPrefix;
        public IRIUChar(@Nonnull GrowableByteBuffer output, @Nullable String context) {
            super(output);
            contextPrefix = context == null ? "" : context+": ";
        }

        @Override
        protected void handleInvalid(@Nonnull GrowableByteBuffer buffer, int begin, int len) {
            logger.warn("{}Replacing \\ in {} with %5C",
                        contextPrefix,  buffer.asString(begin, begin+len));
            output.add('%').add('5').add('C').add(buffer.getArray(), begin+1, len);
        }
    }

    public static class TurtleIRI extends IRIFixerParser {
        private final @Nonnull Start start;
        private final @Nonnull IRIUChar uchar;

        public TurtleIRI(@Nonnull Start start, @Nullable String context) {
            super(start.output, context);
            this.start = start;
            this.uchar = new IRIUChar(start.output, context);
        }

        @Override public @Nonnull FixerParser reset() {
            uchar.reset();
            return super.reset();
        }

        @Override public void flush() {
            uchar.flush();
            super.flush();
        }

        @Override public @Nonnull FixerParser feedByte(int value) {
            if (!uchar.feedByte(value)) {
                if (value == '>') { // end of IRI
                    flush();
                    output.add(value);
                    return start;
                }
                return super.feedByte(value);
            }
            return this;
        }
    }
}
