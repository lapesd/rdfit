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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Character.isWhitespace;

/**
 * A byte sequence that identifies a {@link RDFLang} in RDF data.
 */
public class Cookie {
    private final @Nonnull byte[] bytes;
    private final boolean strict, ignoreCase, skipBOM, skipWhitespace;
    private final @Nonnull List<Cookie> successors;

    /**
     * Builder helper
     */
    public static class Builder {
        private final @Nullable Builder parent;
        private final byte[] bytes;
        private boolean strict;
        private boolean ignoreCase;
        private boolean skipWhitespace = false;
        private boolean skipBOM = true;
        private @Nonnull List<Cookie> successors = Collections.emptyList();

        /**
         * Start from a sequence of bytes
         * @param bytes the byte sequence
         * @param parent the parent of this builder, optional
         */
        public Builder(byte[] bytes, @Nullable Builder parent) {
            this.bytes = bytes;
            this.parent = parent;
        }

        /**
         * Sets {@link #strict(boolean)} to true
         * @return this builder
         */
        public @Nonnull Builder strict() {
            return strict(true);
        }

        /**
         * If strict, the input must start with the byte sequence
         * @param value  whether to tolerate leading bytes before the cookie
         * @return this builder
         */
        public @Nonnull Builder strict(boolean value) {
            this.strict = value;
            return this;
        }

        /**
         * Calls {@link #skipWhitespace(boolean)} with true
         * @return this builder
         */
        public @Nonnull Builder skipWhitespace() {
            return skipWhitespace(true);
        }
        /**
         * Skip whitespace before matching the byte sequence, even if non-strict
         * @param value whether to skip whitespace
         * @return this builder
         */
        public @Nonnull Builder skipWhitespace(boolean value) {
            this.skipWhitespace = value;
            return this;
        }

        /**
         * Sets {@link #ignoreCase(boolean)} to true
         * @return this builder
         */
        public @Nonnull Builder ignoreCase() {
            return ignoreCase(true);
        }
        /**
         * Ignores upper/lower case distinction for ASCII characters
         * @param value whether to ignore case
         * @return this builder
         */
        public @Nonnull Builder ignoreCase(boolean value) {
            this.ignoreCase = value;
            return this;
        }

        /**
         * skips the UTF-* BOM bytes, even if in strict mode
         * @param value whether to skip the BOM
         * @return this builder
         */
        public @Nonnull Builder skipBOM(boolean value) {
            skipBOM = value;
            return this;
        }
        public @Nonnull Builder includeBOM() {
            return skipBOM(false);
        }

        /**
         * Adds a cookie that must be matched after this one
         *
         * @param cookie the next cookie
         * @return this builder
         */
        public @Nonnull Builder then(@Nonnull Cookie cookie) {
            if (successors.isEmpty())
                successors = new ArrayList<>();
            successors.add(cookie);
            return this;
        }

        /**
         * Starts building a cookie that must follow this one, with the UTF-8 bytes of the string
         * @param string bytes of the next cookie
         * @return the builder of the new cookie
         */
        public @Nonnull Builder then(@Nonnull String string) {
            return then(string, StandardCharsets.UTF_8);
        }
        /**
         * Starts building a cookie that must follow this one, with the UTF-8 bytes of the string
         * @param string bytes of the next cookie
         * @param cs character set that should be used to convert string into bytes
         * @return the builder of the new cookie
         */
        public @Nonnull Builder then(@Nonnull String string, @Nonnull Charset cs) {
            return then(string.getBytes(cs));
        }
        /**
         * Starts building a cookie that must follow this one, with the UTF-8 bytes of the string
         * @param bytes byte sequence defining the cookie
         * @return the builder of the new cookie
         */
        public @Nonnull Builder then(@Nonnull byte[] bytes) {
            return new Builder(bytes, this);
        }

        /**
         * Saves this cookie continue configuring the preceding cookie
         * @return the builder of the parent cookie.
         */
        public @Nonnull Builder save() {
            if (parent == null)
                throw new UnsupportedOperationException("Cannot save() root builder");
            return parent.then(doBuild());
        }

        /**
         * Create the {@link Cookie} instance with the configuration of this Builder
         * @return the new {@link Cookie}
         */
        public @Nonnull Cookie build() {
            if (parent != null)
                throw new UnsupportedOperationException("Cannot build() child builder");
            return doBuild();
        }

        private @Nonnull Cookie doBuild() {
            return new Cookie(bytes, strict, ignoreCase, skipBOM, skipWhitespace, successors);
        }
    }

    /**
     * Start a Buidler with the UTF-8 encoding of the string
     * @param string the cookie data
     * @return A Builder configuring the new {@link Cookie}
     */
    public static @Nonnull Builder builder(@Nonnull String string) {
        return builder(string, StandardCharsets.UTF_8);
    }

    /**
     * Start configuring a {@link Cookie} with the bytes of the string in the given {@link Charset}
     * @param string the cookie string
     * @param cs the {@link Charset} that must be used to convert the string into bytes
     * @return a {@link Builder} for the new Cookie
     */
    public static @Nonnull Builder builder(@Nonnull String string, @Nonnull Charset cs) {
        return builder(string.getBytes(cs));
    }

    /**
     * Start configuring a Cookie that matches the given byte sequence
     * @param bytes the bytes to match in an input sequence
     * @return A {@link Builder} for the new Cookie
     */
    public static @Nonnull Builder builder(@Nonnull byte[] bytes) {
        return new Builder(bytes, null);
    }

    /**
     * Create a new {@link Cookie}
     * @param bytes the bytes that identify the input
     * @param strict if true, the input must start with the given bytes sequence
     * @param ignoreCase if true, case is ignored for ASCII chars in bytes
     * @param skipBOM if true, BOM for UTF-* is ignored, even if strict
     * @param skipWhitespace if true, leading whitespace is ignored, even if strict
     * @param successors list of Cookies that may succed this one. If non-empty, at least one
     *                   of the successors must match for this cookie to also match.
     */
    public Cookie(@Nonnull byte[] bytes, boolean strict, boolean ignoreCase, boolean skipBOM,
                  boolean skipWhitespace, @Nonnull List<Cookie> successors) {
        if (ignoreCase) {
            byte[] copy = new byte[bytes.length];
            for (int i = 0, size = bytes.length; i < size; i++)
                copy[i] = (byte)Character.toLowerCase((char)bytes[i]);
            bytes = copy;
        }
        this.bytes = bytes;
        this.strict = strict;
        this.ignoreCase = ignoreCase;
        this.skipBOM = skipBOM;
        this.skipWhitespace = skipWhitespace;
        this.successors = successors;
    }

    /**
     * Matcher for a single {@link Cookie} instance
     */
    protected class SingleMatcher {
        private int index = 0;
        private int bomIndex = 0;
        byte[] exBOM;
        byte[] bom;

        private boolean feedBOM(byte value) {
            if (skipBOM && bomIndex >= 0) {
                if (bom == null) {
                    bom = new byte[]{value, 0, 0};
                    if (value == (byte) 0xef) {
                        exBOM = new byte[]{(byte)0xef, (byte)0xbb, (byte)0xbf};
                    } else if (value == (byte)0xfe) {
                        exBOM = new byte[] {(byte)0xfe, (byte)0xff};
                    } else if (value == (byte)0xff) {
                        exBOM = new byte[] {(byte)0xff, (byte)0xfe};
                    } else {
                        bomIndex = -1; //no BOM
                        return false;
                    }
                    bomIndex = 1;
                    return true; // do not process value
                } else {
                    bom[bomIndex] = value;
                    if (exBOM[bomIndex] == value) {
                        ++bomIndex;
                        if (bomIndex == exBOM.length)
                            bomIndex = -2; //BOM completely matched
                        return true; // do not process value
                    } else {
                        for (int i = 0; i < bomIndex; i++)
                            feed(bom[i]);
                        bomIndex = -1; //BOM matching failed
                        return false;// value will be processed normally
                    }
                }
            } else {
                return false; //already processed BOM (or !skipBOM)
            }
        }

        /**
         * Feed a single byte
         *
         * @param value the byte to match
         * @return true if mathcing not (yet) failed.
         */
        public boolean feed(byte value) {
            if (feedBOM(value))
                return true; //value is (likely) part of the BOM
            if (skipWhitespace && index == 0 && isWhitespace(value))
                return true;
            if (ignoreCase)
                value = (byte) Character.toLowerCase((char) value);
            if      (index < 0)             return false;
            else if (index == bytes.length) return true;
            else if (bytes[index] == value) ++index;
            else if (strict)                index = -1;
            else if (bytes[0] == value)     index =  1;
            else                            index =  0;
            return index >= 0;
        }

        /**
         * True if {@link #isMatched()} is conclusive
         * @return true if {@link #isMatched()} will not change with further {@link #feed(byte)} calls
         */
        public boolean isConclusive() {
            return index < 0 || index == bytes.length;
        }

        /**
         * The {@link SingleMatcher} is matched if it has not yet conclusively failed matching
         * or if it has definitely accepted the input
         * @return true if matched
         */
        public boolean isMatched() {
            return index == bytes.length;
        }
    }

    /**
     * Matches multiple Cookies concurrently (handles successors)
     */
    public class Matcher {
        private final @Nonnull SingleMatcher myMatcher = new SingleMatcher();
        private final @Nonnull List<Matcher> matchers;

        /**
         * Constructor
         */
        public Matcher() {
            if (successors.isEmpty()) {
                this.matchers = Collections.emptyList();
            } else {
                this.matchers = new ArrayList<>(successors.size());
                successors.forEach(n -> this.matchers.add(n.createMatcher()));
            }
        }

        /**
         * Feed a byte to the matcher
         * @param value next byte from the input
         * @return false iff the input does not have the cookie, true if matched or inconclusive
         */
        public boolean feed(byte value) {
            if (myMatcher.isConclusive()) {
                if (!myMatcher.isMatched())
                    return false;
                if (matchers.isEmpty()) {
                    assert myMatcher.isMatched();
                    return true;
                }
                boolean ok = false;
                for (Matcher m : matchers)
                    ok |= m.feed(value);
                return ok;
            } else {
                return myMatcher.feed(value);
            }
        }

        /**
         * The match is conclusive if further {@link #feed(byte)} calls will not change the
         * result of {@link #isMatched()}
         *
         * @return true iff conclusive
         */
        public boolean isConclusive() {
            if (!myMatcher.isConclusive()) return false;
            if (!myMatcher.isMatched()) return true;
            boolean conclusive = true, matched = false;
            for (Matcher next : matchers) {
                conclusive &= next.isConclusive();
                matched    |= next.isMatched();
            }
            return matched || conclusive;
        }

        /**
         * The matcher is matched if the input has not yet failed the Cookie.
         *
         * @return true iff matched.
         */
        public boolean isMatched() {
            if      (!myMatcher.isMatched()) return false;
            else if (  matchers.isEmpty()  ) return true;
            boolean is = false;
            for (Matcher next : matchers)
                is |= next.isMatched();
            return is;
        }
    }

    /**
     * Create a new {@link Matcher} for this Cookie.
     * @return a new empty {@link Matcher}
     */
    public @Nonnull Matcher createMatcher() {
        return new Matcher();
    }

    /**
     * Get the defining bytes of this {@link Cookie}
     * @return the byte[]
     */
    public @Nonnull byte[] getBytes() {
        return bytes;
    }

    /**
     * Write a string representation of this {@link Cookie} to the builder applying the
     * number of indent spaces
     *
     * @param b the destination {@link StringBuilder}
     * @param indent how many spaces to add to the left margin
     */
    public void toString(@Nonnull StringBuilder b, int indent) {
        for (int i = 0; i < indent; i++) b.append(' ');
        b.append(new String(bytes, StandardCharsets.UTF_8));
        if (strict) b.append("[strict]");
        for (Cookie next : successors)
            next.toString(b.append("\n  "), indent+2);
    }

    @Override public @Nonnull String toString() {
        StringBuilder b = new StringBuilder();
        toString(b, 0);
        return b.toString();
    }
}
