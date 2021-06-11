/*
 *    Copyright 2021 Alexis Armin Huf
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.lapesd.rdfit.source.syntax.impl;

import com.github.lapesd.rdfit.source.syntax.LangDetector;
import com.github.lapesd.rdfit.util.Literal;
import com.github.lapesd.rdfit.util.LiteralParser;
import com.github.lapesd.rdfit.util.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static com.github.lapesd.rdfit.source.syntax.RDFLangs.*;
import static java.lang.Character.isWhitespace;
import static java.util.stream.Collectors.toSet;

/**
 * {@link LangDetector} that detects either NQ or TRIG input (NT and TTL are subsets of TRIG)
 *
 * It will consume up to the first triple in the input and use only that for determining
 * the syntax. Since NT is a subset of Turtle and TriG is a superset of turtle, not
 * encountering Turtle or TriG features until a certain point of the input does not imply
 * that the input does not have that features. Thus, TriG is returned since a TriG parser
 * can handle all three languages.
 */
public class TurtleFamilyDetector implements LangDetector {
    @Override public @Nonnull LangDetector.State createState() {
        return new State();
    }

    public static class State implements LangDetector.State {
        protected abstract static class SubState {
            public abstract @Nullable RDFLang feed(byte value);
            public abstract @Nullable RDFLang end(boolean hardEnd);
        }

        protected class BOM extends SubState {
            private int index = 0;
            private byte ex = 0;
            private final byte[] fed = new byte[3];

            @Override public @Nullable RDFLang feed(byte value) {
                fed[index] = value;
                boolean fail = false;
                if (index == 0) {
                    if      (value == (byte)0xfe) ex = (byte)0xff;
                    else if (value == (byte)0xff) ex = (byte)0xfe;
                    else if (value == (byte)0xef) ex = (byte)0xbb;
                    else                          fail = true;
                    if (!fail) {
                        index = 1;
                        return null;
                    }
                } else if (index == 1) {
                    if      (value != ex)         fail = true;
                    else if (value == (byte)0xbb) ex = (byte)0xbf;
                    else                          ex = 0;
                } else {
                    assert index == 2;
                    if (value == ex) ex = 0;
                    else             fail = true;
                }

                if (fail) { //re-feed bytes of would-be BOM
                    subState = new Preamble();
                    return feedAll(fed, index+1, false, false);
                } else if (ex == 0) { // advance state and forget consumed BOM bytes
                    subState = new Preamble();
                    return null;
                } else { // expect next BOM byte
                    ++index;
                    return null;
                }
            }

            @Override public @Nullable RDFLang end(boolean hardEnd) {
                return UNKNOWN; //empty input
            }
        }

        protected class Preamble extends SubState {
            private boolean hadComment, active;
            private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            @Override public @Nullable RDFLang feed(byte value) {
                if (active) { // handling a #comment, @base or @prefix
                    buffer.write(value);
                    if (value == '\n') {
                        active = false;
                        byte[] data = buffer.toByteArray();
                        String str = new String(data, StandardCharsets.UTF_8);
                        buffer = new ByteArrayOutputStream();
                        if (str.startsWith("PREFIX") || str.startsWith("BASE")) {
                            return TRIG;
                        } else if (!str.startsWith("#")) {
                            subState = new Body(hadComment);
                            return feedAll(data, data.length, false, false);
                        }
                    } else if (value < ' ' && value != '\t' && value != '\r') {
                        return UNKNOWN; // binary data
                    }
                } else if (value == '@') {
                    return TRIG;
                } else if (value == 'B' || value == 'P' || value == '#') {
                    buffer.write(value);
                    active = true;
                    hadComment |= value == '#';
                } else if (!Utils.isAsciiSpace(value)) { //body has started
                    subState = new Body(hadComment);
                    return feedByte(value);
                }
                return null;
            }

            @Override public @Nullable RDFLang end(boolean hardEnd) {
                if (hadComment)
                    return hardEnd ? TTL : TRIG;
                return hardEnd ? (buffer.size() > 0 ? TTL : NT) : null;
            }
        }

        protected static class Body extends SubState {
            private static final String XML_TAG = " xmlns:";
            private static final Set<Byte> LIT_CHARS;
            private static final Set<Byte> LIT_BEGIN;
            private static final Set<Byte> BNODE_CHARS;
            private final StringBuilder buffer = new StringBuilder();
            private final LiteralParser litParser = new LiteralParser();
            private byte begin = 0;
            boolean needsSpace = false, bNodeSubject = false, xmlFirst = false, forceTTL;
            int termIndex = 0, triples = 0;
            int xmlTagIdx = 0;

            static {
                LIT_CHARS = "falsetrue-+eE.0123456789".chars().boxed().map(Integer::byteValue)
                                                              .collect(toSet());
                LIT_BEGIN = "ft-+.0123456789".chars().boxed().map(Integer::byteValue)
                                                     .collect(toSet());
                Set<Byte> set = new HashSet<>();
                for (byte i = 'A'; i <= 'Z'; i++) set.add(i);
                for (byte i = 'a'; i <= 'z'; i++) set.add(i);
                for (byte i = '0'; i <= '9'; i++) set.add(i);
                set.add((byte)'-');
                set.add((byte)'_');
                set.add((byte)'.');
                BNODE_CHARS = set;
            }

            public Body(boolean forceTTL) {
                this.forceTTL = forceTTL;
            }

            @Override public @Nullable RDFLang feed(byte value) {
                if (begin == '\0') {
                    return feedTermBegin(value);
                } else if (begin == '[') {
                    if      (value == ']'        ) endTerm(true);
                    else if (value == '{'        ) return UNKNOWN;
                    else if (!isWhitespace(value)) {
                        bNodeSubject = false;
                        return TRIG;
                    }
                } else if (begin == '<') {
                    if (xmlFirst) {
                        xmlFirst = false;
                        if (value == '!')
                            return UNKNOWN; // "<!" implies XML
                    }
                    if (value == XML_TAG.charAt(xmlTagIdx)) {
                        if (++xmlTagIdx == XML_TAG.length())
                            return UNKNOWN;
                    } else {
                        xmlTagIdx = value == XML_TAG.charAt(0) ? 1 : 0;
                    }
                    if (value == '>') endTerm(true);
                } else if (begin == '_') {
                    if (buffer.length() == 1) {
                        assert buffer.toString().equals("_");
                        if (value != ':') return UNKNOWN;
                    } else if (isWhitespace(value)) {
                        boolean hasDot = buffer.charAt(buffer.length() - 1) == '.';
                        if (hasDot && termIndex < 2)
                            return UNKNOWN;
                        endTerm(false);
                        return hasDot ? feedTermBegin((byte)'.') : null;
                    } else if (value == ',' || value == ';') {
                        return termIndex == 2 ? TRIG : UNKNOWN;
                    } else if (value >= 0 && !BNODE_CHARS.contains(value)) {
                        return UNKNOWN;
                    }
                    buffer.append((char) value);
                    return null;
                } else if (begin == '"' || begin == '\'') {
                    assert termIndex == 2;
                    if (litParser.feedByte(value)) {
                        if (value == ',' || value == ';') {
                            endTerm(false);
                            return TRIG;
                        } else {
                            boolean hadDot = litParser.endsInDot() || value == '.';
                            Literal literal = litParser.endAndReset();
                            endTerm(!isWhitespace(value));
                            if (literal.isPrefixTyped()) return TRIG;
                            if (hadDot)  {
                                termIndex = 0;
                                ++triples;
                            }
                        }
                    }
                } else if (isWhitespace(value) || value == ',' || value == ';') {
                    int termIndex = this.termIndex;
                    String str = buffer.toString();
                    assert termIndex >= 1;
                    endTerm(false);
                    if (termIndex == 1)
                        return str.equals("a") || str.equals("GRAPH") ? TRIG : UNKNOWN;
                    else if (str.startsWith("f") && !str.equals("false") && !str.equals("false."))
                        return UNKNOWN;
                    else if (str.startsWith("t") && !str.equals("true") && !str.equals("true."))
                        return UNKNOWN;
                    else if (value == ',' || value == ';')
                        return termIndex == 2 ? TRIG : UNKNOWN;
                } else {
                    return feedTermByte(value);
                }
                return null;
            }

            private void endTerm(boolean needsSpace) {
                begin = 0;
                buffer.setLength(0);
                this.needsSpace = needsSpace;
                ++termIndex;
            }

            private @Nullable RDFLang feedTermBegin(byte value) {
                if (isWhitespace(value)) {
                    needsSpace = false;
                    return null; // ignore
                } else if (value == '[') {
                    if (termIndex != 0 && termIndex != 2)
                        return UNKNOWN;
                    bNodeSubject = termIndex == 0;
                    //likely TRIG, but we should continue to catch the '[]' JSON-LD input ambiguity
                } else if (value == ',' || value == ';') {
                    return termIndex == 3 ? TRIG : UNKNOWN;
                } else if (value == '.') {
                    if (termIndex < 3  && ( !bNodeSubject || (termIndex > 0 && begin != 0) ))
                        return UNKNOWN; // incomplete triple or [] subject with something after it
                    else if (termIndex == 4)
                        return NQ;
                    else if (bNodeSubject)
                        return TRIG;
                    assert termIndex == 3;
                    termIndex = 0;
                    ++triples;
                    return null;
                } else if (needsSpace) {
                    return UNKNOWN; // value is not space, ',', '.' nor ';'
                } else if (value == '{') {
                    // { only appears as a term begin in the <graph URI> { ... } form of TriG
                    return termIndex == 1 ? TRIG : UNKNOWN;
                } else if (value == '"' || value == '\'') {
                    if (termIndex != 2) return UNKNOWN;
                    litParser.reset();
                    litParser.feed((char)value);
                } else if (value == '_') {
                    buffer.setLength(0);
                    buffer.append((char) value);
                } else if (value == '<') {
                    xmlTagIdx = 0;
                    xmlFirst = true;
                    if (termIndex == 3)
                        return NQ;
                } else {
                    /* Note: if we got here, no @prefix/PREFIX statement was present
                     * in the preamble. Thus we cannot have "ns:.*" or ":.*" terms */
                    buffer.setLength(0);
                    if (termIndex == 0) {
                        return UNKNOWN;
                    } else if (termIndex == 1) {
                        if (value == 'a' || value == 'G') buffer.append((char) value);
                        else                              return UNKNOWN;
                    } else if (termIndex >= 2) {
                        if (LIT_BEGIN.contains(value)) return TRIG;
                        else                           return UNKNOWN;
                    }
                }
                begin = value;
                return null;
            }

            private @Nullable RDFLang feedTermByte(byte value) {
                buffer.append((char) value);
                if (begin == 'f')
                    return "false.".startsWith(buffer.toString()) ? null : UNKNOWN;
                else if (begin == 't')
                    return "true.".startsWith(buffer.toString()) ? null : UNKNOWN;
                else if (begin == 'G')
                    return "GRAPH".startsWith(buffer.toString()) ? null : UNKNOWN;
                else
                    return LIT_CHARS.contains(value) ? null : UNKNOWN;
            }

            @Override public @Nullable RDFLang end(boolean hardEnd) {
                if (termIndex > 4) {
                    return UNKNOWN; //triple too large for NQ
                } else if (termIndex == 4) {
                    return NQ; // already parsed the graph term, waiting for .
                } else if (termIndex == 3 && (begin != 0)) {
                    return NQ; // stopped while parsing the graph term
                } else if (triples >= 1) {
                    // Got more at least one complete triple and none had TRIG/NQ/TTL features.
                    if (hardEnd) {
                        // since we know the input ended, this is safe. There is the possibility
                        // that  an incomplete triple follows the fully parsed ones (termIndex
                        // != 0 || begin != 0). Nevertheless report the input as NT and let
                        // the actual parsers spew out an error message over invalid input
                        return forceTTL ? TTL : NT;
                    }
                    // Else, could be a TRIG, NT or NQ.
                    // Here we guess TRIG as it is more widespread and NQ files tend to always
                    // have 4 terms (i.e., NQ would have been detected on the few first triples)
                    return TRIG;
                } else if (hardEnd && termIndex == 0 && begin == 0) {
                    return TTL; //empty input
                } else if (hardEnd && termIndex < 2) {
                    // input has ended and we found only two terms or less. This likely is not a
                    // NQ/NT/TTL/TRIG file. Typical scenario for this is "[]" which is an empty
                    // JSON-LD and counts as a single-term incomplete turtle/trig file.
                    return UNKNOWN;
                } else {
                    // took too long to reach first triple (which is not yet finished or had only
                    // 3 terms. This large preambles are a characteristic of Turtle/TriG and
                    // these are more common than NQ, thus guess TriG.
                    return TRIG;
                }
            }
        }

        private @Nonnull SubState subState = new BOM();
        private @Nullable RDFLang detected = null;

        @SuppressWarnings("SameParameterValue")
        private @Nullable RDFLang feedAll(byte[] data, int size, boolean callEnd, boolean hardEnd) {
            for (int i = 0; i < size; i++) {
                RDFLang lang = feedByte(data[i]);
                if (lang != null)
                    return lang;
            }
            if (callEnd)
                return end(hardEnd);
            return null;
        }

        @Override public @Nullable RDFLang feedByte(byte value) {
            if (detected == null)
                detected = subState.feed(value);
            return detected;
        }

        @Override public @Nullable RDFLang end(boolean hardEnd) {
            if (detected == null)
                detected = subState.end(hardEnd);
            /* Invalid input would have had detected set to UNKNOWN in a previous feedByte() call
             * Since the input is valid until this point, assume it is TriG. */
            return detected == null ? TRIG : detected;
        }
    }
}
