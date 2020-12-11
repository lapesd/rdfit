package com.github.lapesd.rdfit.source.syntax.impl;

import com.github.lapesd.rdfit.source.syntax.LangDetector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static com.github.lapesd.rdfit.source.syntax.RDFLangs.*;
import static java.lang.Character.isSpaceChar;
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
            public abstract @Nullable RDFLang end();
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
                    return feedAll(fed, index+1, false);
                } else if (ex == 0) { // advance state and forget consumed BOM bytes
                    subState = new Preamble();
                    return null;
                } else { // expect next BOM byte
                    ++index;
                    return null;
                }
            }

            @Override public @Nullable RDFLang end() {
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
                            subState = new Body();
                            return feedAll(data, data.length, false);
                        }
                    }
                } else if (value == '@') {
                    return TRIG;
                } else if (value == 'B' || value == 'P' || value == '#') {
                    buffer.write(value);
                    active = true;
                    hadComment |= value == '#';
                } else if (!isSpaceChar(value)) { //body has started
                    subState = new Body();
                    return feedByte(value);
                }
                return null;
            }

            @Override public @Nullable RDFLang end() {
                byte[] data = buffer.toByteArray();
                if (data.length > 0) {
                    String str = new String(data, StandardCharsets.UTF_8);
                    if (str.startsWith("PREFIX ") || str.startsWith("BASE "))
                        return TRIG;
                    // try to parse a body (no preamble)
                    return feedAll(data, data.length, true);
                }
                /* if a comment was read, guess TRIG. If no comment was read and we are still
                 * on this SubState, return UNKNOWN since the input is likely empty.*/
                return hadComment ? TRIG : UNKNOWN;
            }
        }

        protected static class Body extends SubState {
            private static final Set<Byte> LIT_CHARS;
            private static final Set<Byte> LIT_BEGIN;
            private static final Set<Byte> BNODE_CHARS;
            private final StringBuilder buffer = new StringBuilder();
            private byte begin = 0;
            boolean needsSpace = false;
            int termIndex = 0;

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

            @Override public @Nullable RDFLang feed(byte value) {
                if (begin == '\0') {
                    return feedTermBegin(value);
                } else if (begin == '<') {
                    if      (value == ' ') return UNKNOWN;
                    else if (value == '>') endTerm(true);
                } else if (begin == '_') {
                    if (buffer.length() == 1) {
                        assert buffer.toString().equals("_");
                        if (value != ':') return UNKNOWN;
                    } else if (isWhitespace(value)) {
                        if (buffer.toString().endsWith(".")) {
                            if      (termIndex == 2) return TRIG;
                            else if (termIndex == 3) return NQ;
                            else return UNKNOWN;
                        }
                        endTerm(false);
                        return null;
                    } else if (value == ',' || value == ';') {
                        return termIndex == 2 ? TRIG : UNKNOWN;
                    } else if (value >= 0 && !BNODE_CHARS.contains(value)) {
                        return UNKNOWN;
                    }
                    buffer.append((char)value);
                    return null;
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
                    return termIndex == 0 || termIndex == 2 ? TRIG : UNKNOWN;
                } else if (value == ',' || value == ';') {
                    return termIndex == 3 ? TRIG : UNKNOWN;
                } else if (value == '.') {
                    if      (termIndex == 3) return TRIG;
                    else if (termIndex == 4) return NQ;
                    else                     return UNKNOWN;
                } else if (needsSpace) {
                    return UNKNOWN; // value is not space, ',', '.' nor ';'
                } else if (value == '{') {
                    // { only appears as a term begin in the <graph URI> { ... } form of TriG
                    return termIndex == 1 ? TRIG : UNKNOWN;
                } else if (value == '"' || value == '\'') {
                    if      (termIndex == 3) return NQ;
                    else if (termIndex == 2) return TRIG;
                    else                     return UNKNOWN; //non-object predicate or too many terms
                } else if (value == '_') {
                    buffer.setLength(0);
                    buffer.append((char)value);
                } else if (value != '<') {
                    /* Note: if we got here, no @prefix/PREFIX statement was present
                     * in the preamble. Thus we cannot have "ns:.*" or ":.*" terms */
                    buffer.setLength(0);
                    if (termIndex == 0) {
                        return UNKNOWN;
                    } else if (termIndex == 1) {
                        if (value == 'a' || value == 'G') buffer.append((char) value);
                        else                              return UNKNOWN;
                    } else if (termIndex >= 2) {
                        if (LIT_BEGIN.contains(value)) buffer.append((char) value);
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

            @Override public @Nullable RDFLang end() {
                if      (termIndex > 4) return UNKNOWN;
                else if (termIndex == 4) return buffer.length() == 0 ? NQ : UNKNOWN;
                else if (termIndex == 3) return buffer.length() == 0 ? TRIG : NQ;
                else                     return TRIG;
            }
        }

        private @Nonnull SubState subState = new BOM();
        private @Nullable RDFLang detected = null;

        private @Nullable RDFLang feedAll(byte[] data, int size, boolean callEnd) {
            for (int i = 0; i < size; i++) {
                RDFLang lang = feedByte(data[i]);
                if (lang != null)
                    return lang;
            }
            if (callEnd)
                return end();
            return null;
        }

        @Override public @Nullable RDFLang feedByte(byte value) {
            if (detected == null)
                detected = subState.feed(value);
            return detected;
        }

        @Override public @Nullable RDFLang end() {
            if (detected == null)
                detected = subState.end();
            /* Invalid input would have had detected set to UNKNOWN in a previous feedByte() call
             * Since the input is valid until this point, assume it is TriG. */
            return detected == null ? TRIG : detected;
        }
    }
}
