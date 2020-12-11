package com.github.lapesd.rdfit.source.syntax.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cookie {
    private final @Nonnull byte[] bytes;
    private final boolean strict, ignoreCase, skipBOM;
    private final @Nonnull List<Cookie> successors;

    public static class Builder {
        private final @Nullable Builder parent;
        private final byte[] bytes;
        private boolean strict;
        private boolean ignoreCase;
        private boolean skipBOM = true;
        private @Nonnull List<Cookie> successors = Collections.emptyList();

        public Builder(byte[] bytes, @Nullable Builder parent) {
            this.bytes = bytes;
            this.parent = parent;
        }

        public @Nonnull Builder strict() {
            return strict(true);
        }
        public @Nonnull Builder strict(boolean value) {
            this.strict = value;
            return this;
        }

        public @Nonnull Builder ignoreCase() {
            return ignoreCase(true);
        }
        public @Nonnull Builder ignoreCase(boolean value) {
            this.ignoreCase = value;
            return this;
        }

        public @Nonnull Builder skipBOM(boolean value) {
            skipBOM = value;
            return this;
        }
        public @Nonnull Builder includeBOM() {
            return skipBOM(false);
        }

        public @Nonnull Builder then(@Nonnull Cookie cookie) {
            if (successors.isEmpty())
                successors = new ArrayList<>();
            successors.add(cookie);
            return this;
        }

        public @Nonnull Builder then(@Nonnull String string) {
            return then(string, StandardCharsets.UTF_8);
        }
        public @Nonnull Builder then(@Nonnull String string, @Nonnull Charset cs) {
            return then(string.getBytes(cs));
        }
        public @Nonnull Builder then(@Nonnull byte[] bytes) {
            return new Builder(bytes, this);
        }
        public @Nonnull Builder save() {
            if (parent == null)
                throw new UnsupportedOperationException("Cannot save() root builder");
            return parent.then(doBuild());
        }
        public @Nonnull Cookie build() {
            if (parent != null)
                throw new UnsupportedOperationException("Cannot build() child builder");
            return doBuild();
        }
        public @Nonnull Cookie doBuild() {
            return new Cookie(bytes, strict, ignoreCase, skipBOM, successors);
        }
    }

    public static @Nonnull Builder builder(@Nonnull String string) {
        return builder(string, StandardCharsets.UTF_8);
    }
    public static @Nonnull Builder builder(@Nonnull String string, @Nonnull Charset cs) {
        return builder(string.getBytes(cs));
    }
    public static @Nonnull Builder builder(@Nonnull byte[] bytes) {
        return new Builder(bytes, null);
    }

    public Cookie(@Nonnull byte[] bytes, boolean strict, boolean ignoreCase, boolean skipBOM,
                  @Nonnull List<Cookie> successors) {
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
        this.successors = successors;
    }

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

        public boolean feed(byte value) {
            if (feedBOM(value))
                return true; //value is (likely) part of the BOM
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

        public boolean isConclusive() {
            return index < 0 || index == bytes.length;
        }

        public boolean isMatched() {
            return index == bytes.length;
        }
    }

    public class Matcher {
        private final @Nonnull SingleMatcher myMatcher = new SingleMatcher();
        private final @Nonnull List<Matcher> matchers;

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

        public boolean isMatched() {
            if      (!myMatcher.isMatched()) return false;
            else if (  matchers.isEmpty()  ) return true;
            boolean is = false;
            for (Matcher next : matchers)
                is |= next.isMatched();
            return is;
        }
    }

    public @Nonnull Matcher createMatcher() {
        return new Matcher();
    }


    public @Nonnull byte[] getBytes() {
        return bytes;
    }

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
