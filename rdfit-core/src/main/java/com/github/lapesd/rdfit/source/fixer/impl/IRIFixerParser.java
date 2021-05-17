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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.Arrays;
import java.util.regex.Pattern;

import static com.github.lapesd.rdfit.util.Utils.isInSmall;
import static java.nio.charset.CodingErrorAction.IGNORE;
import static java.nio.charset.StandardCharsets.UTF_8;

public class IRIFixerParser implements FixerParser {
    private static final Logger logger = LoggerFactory.getLogger(IRIFixerParser.class);

    private static final Pattern IP_FUTURE = Pattern.compile("(?i)v[0-9a-f]\\.(.+)");

    private static final byte[] URN = "urn".getBytes(UTF_8);
    private static final byte[] FILE_SCHEME = "file:".getBytes(UTF_8);
    private static final byte[] HEX_DIGITS = "0123456789ABCDEF".getBytes(UTF_8);
    private static final byte[] SUBDELIMS = "!$&'()*+,;=".getBytes(UTF_8); //sorted
    private static final byte[] PATHSUBDELIMS = "!$&'()*+,/:;=@".getBytes(UTF_8); //sorted
    private static final byte[] FRAGMENTCHARS = "!$&'()*+,/:;=?@".getBytes(UTF_8); //sorted
    private static final byte[] USERINFOSUBDELIMS = "!$&'()*+,:;=".getBytes(UTF_8); //sorted
    private static final int[] UNRESERVED_BEGINS = {
            '-', '0', 'A', '_', 'a', '~',
            0xA0, 0xF900, 0xFDF0, 0x10000, 0x20000, 0x30000, 0x40000, 0x50000,
            0x60000, 0x70000, 0x80000, 0x90000, 0xA0000, 0xB0000, 0xC0000, 0xD0000, 0xE1000
    };
    private static final int[] UNRESERVED_ENDS = {
            '.', '9', 'Z', '_', 'z', '~',
            0xD7FF, 0xFDCF, 0xFFEF, 0x1FFFD, 0x2FFFD, 0x3FFFD, 0x4FFFD, 0x5FFFD,
            0x6FFFD, 0x7FFFD, 0x8FFFD, 0x9FFFD, 0xAFFFD, 0xBFFFD, 0xCFFFD, 0xDFFFD, 0xEFFFD
    };
    private static final int[] PRIVATE_BEGINS = {
            0xE000, 0xF0000, 0x100000
    };
    private static final int[] PRIVATE_ENDS = {
            0xF8FF, 0xFFFFD, 0x10FFFD
    };

    public static boolean isAlpha(int code) {
        return (code >= 'A' && code <= 'Z') || (code >= 'a' && code <= 'z');
    }

    public static boolean isDigit(int code) {
        return code >= '0' && code <= '9';
    }

    public static boolean isUnreserved(int codePoint) {
        int idx = Arrays.binarySearch(UNRESERVED_BEGINS, codePoint);
        if (idx < 0)
            idx = -idx - 2;
        return idx >= 0 && codePoint <= UNRESERVED_ENDS[idx];
    }

    private static @Nonnull ComponentState runFrom(@Nonnull ComponentState state,
                                                   @Nonnull GrowableByteBuffer input, int from) {
        for (int i = from, size = input.size(); i < size; i++)
            state = state.feedByte(input.get(i) & 0xFF);

        return state;
    }

    @SuppressWarnings("SameParameterValue")
    private static int runUntil(@Nonnull ComponentState state, @Nonnull GrowableByteBuffer input,
                                int stop) {
        for (int i = 0, size = input.size(); i < size; i++) {
            byte value = input.get(i);
            if (value == stop)
                return i;
            state = state.feedByte(value & 0xFF);
        }
        return input.size();
    }

    /* --- --- ComponentState instances --- --- */

    private final @Nonnull Scheme scheme = new Scheme();
    private final @Nonnull HierPart hierPart = new HierPart();
    private final @Nonnull Authority authority = new Authority();
    private final @Nonnull UserInfo userInfo = new UserInfo();
    private final @Nonnull RegName regName = new RegName();
    private final @Nonnull Path path = new Path();
    private final @Nonnull Query query = new Query();
    private final @Nonnull Fragment fragment = new Fragment();

    /* --- --- Parser state --- --- */

    protected final @Nonnull GrowableByteBuffer output;
    protected @Nullable String context;
    private @Nonnull ComponentState state = scheme;

    /* --- --- UTF-8 decode/encode state & buffers --- --- */

    private final CharsetEncoder u8Encoder = UTF_8.newEncoder().onMalformedInput(IGNORE)
                                                               .onUnmappableCharacter(IGNORE);
    private final CharsetDecoder u8Decoder = UTF_8.newDecoder().onMalformedInput(IGNORE)
                                                                    .onUnmappableCharacter(IGNORE);
    private final @Nonnull ByteBuffer u8DecoderIn = ByteBuffer.allocate(4);
    private final @Nonnull CharBuffer u8DecoderOut = CharBuffer.allocate(2);
    private final @Nonnull ByteBuffer u8EncoderOut = ByteBuffer.allocate(4);
    private final @Nonnull CharBuffer u8EncoderIn = CharBuffer.allocate(2);

    protected @Nonnull String getContextPrefix(@Nonnull String prefix) {
        return context != null ? prefix+context+": " : "";
    }

    private int decodeUTF8(int byteValue) {
        assert (byteValue & ~0xFF) == 0 : "value is too large for a byte";
        if ((byteValue & ~0x7f) == 0) // fast path
            return byteValue;
        u8DecoderIn.put((byte) byteValue).limit(u8DecoderIn.position()).position(0);
        u8DecoderOut.clear();
        CoderResult result = u8Decoder.decode(u8DecoderIn, u8DecoderOut, false);
        u8DecoderIn.position(u8DecoderIn.limit()).limit(u8DecoderIn.capacity());
        if (result.isOverflow()) {
            throw new RuntimeException("Unexpected overflow of u8Out");
        } else if (result.isUnderflow()) {
            int nChars = u8DecoderOut.position();
            if      (nChars == 0) {
                return -1;
            } else if (nChars == 1) {
                u8DecoderIn.clear();
                return u8DecoderOut.get(0);
            } else if (nChars == 2) {
                u8DecoderIn.clear();
                char high = u8DecoderOut.get(0), low = u8DecoderOut.get(1);
                if (Character.isHighSurrogate(high) && Character.isLowerCase(low))
                    return Character.toCodePoint(high, low);
                else if (Character.isHighSurrogate(low) && Character.isLowerCase(high))
                    return Character.toCodePoint(low, high);
                else // impossible, if thrown is a bug in this class
                    throw new IllegalStateException("two Unicode chars decoded at once.");
            } else { // impossible
                throw new IllegalStateException("Too many chars in buffer!");
            }
        } else {
            throw new RuntimeException("Unexpected CoderResult "+result);
        }
    }

    /* --- --- ComponentState implementations --- --- */

    private abstract class ComponentState {
        /**
         * Appends the UTF-8 encoding of the given character to {@link IRIFixerParser#output}.
         *
         * @param codePoint a unicode character code
         */
        protected void writeCodePoint(int codePoint) {
            if (codePoint < 128) {
                output.add(codePoint);
            } else {
                u8EncoderIn.clear();
                u8EncoderIn.limit(Character.toChars(codePoint, u8EncoderIn.array(), 0));
                CoderResult result = u8Encoder.encode(u8EncoderIn, u8EncoderOut, true);
                if (result.isOverflow()) {
                    throw new RuntimeException("One codePoint yielded more than 4 UTF-8 bytes");
                } else if (result.isUnderflow()) {
                    output.add(u8EncoderOut.limit(u8EncoderOut.position()).position(0));
                    u8EncoderOut.clear();
                } else {
                    throw new RuntimeException("Unexpected "+result+" for codePoint "+codePoint);
                }
            }
        }

        public @Nonnull ComponentState reset() { return this; }
        public void flush() { }
        public @Nonnull ComponentState feedByte(int byteValue) {
            int code = decodeUTF8(byteValue);
            if (code >= 0)
                return feedCode(code);
            return this;
        }
        public @Nonnull ComponentState feedCode(int codePoint) {
            return this;
        }
    }

    private abstract class BufferingComponentState extends ComponentState {
        /** Internal buffer, independent from {@link IRIFixerParser#output} */
        protected @Nonnull GrowableByteBuffer internal = new GrowableByteBuffer();

        @Override public @Nonnull ComponentState reset() {
            internal.clear();
            return this;
        }

        @Override public void flush() {
            output.add(internal);
        }

        @Override public @Nonnull String toString() {
            return getClass().getSimpleName() + "{internal=\"" + internal.asString(UTF_8) + "\"}";
        }
    }

    private abstract class PercentEncoder extends ComponentState {
        private byte first = 0;
        private boolean active = false;
        private final @Nonnull String ruleName;

        public PercentEncoder(@Nonnull String ruleName) {
            this.ruleName = ruleName;
        }

        @Override public @Nonnull ComponentState reset() {
            first = 0;
            active = false;
            return super.reset();
        }

        private void handleNotEncoded(int codePoint) {
            if (isAllowed(codePoint)) {
                writeCodePoint(codePoint);
            } else {
                if (codePoint < 128) {
                    output.add('%').add(HEX_DIGITS[codePoint/16]).add(HEX_DIGITS[codePoint%16]);
                } else {
                    logger.warn("{}Erasing char '{}' (codePoint={}): not allowed in RFC 3987 {}",
                                getContextPrefix("Unexpected non-ASCII char "),
                                new String(Character.toChars(codePoint)), codePoint, ruleName);
                }
            }
        }

        private void flushPercent() {
            if (active) {
                output.add('%').add('2').add('5');
                if (first > 0)
                    output.add(first);
                reset();
            }
        }

        @Override public void flush() {
            flushPercent();
        }

        @Override public @Nonnull ComponentState feedCode(int codePoint) {
            if (active) {
                if (!Utils.isHexDigit(codePoint)) {
                    flushPercent();
                    if (codePoint == '%') active = true;
                    else                  handleNotEncoded(codePoint);
                } else if (first == 0) {
                    first = (byte) codePoint;
                } else {
                    output.add('%').add(first).add(codePoint);
                    reset();
                }
            } else if (codePoint == '%') {
                active = true;
                first = 0;
            } else {
                handleNotEncoded(codePoint);
            }
            return this;
        }

        @Override public @Nonnull String toString() {
            return String.format("%s{active=%b, first=0x%x, ruleName=%s}",
                                 getClass().getSimpleName(), active, first, ruleName);
        }

        protected abstract boolean isAllowed(int codePoint);
    }

    /**
     * See <code>scheme</code> in
     * <a href="https://datatracker.ietf.org/doc/html/rfc3987#section-2.2">RFC 3987</a>.
     */
    private class Scheme extends BufferingComponentState {
        @Override public @Nonnull ComponentState feedByte(int byteValue) {
            if (isAlpha(byteValue) || (isDigit(byteValue) && !internal.isEmpty())) {
                internal.add(byteValue); //looking good...
                return this;
            } else if (byteValue == ':' && !internal.isEmpty()) {
                internal.add(byteValue);
                if (internal.startsWith(URN)) {
                    return this; // tolerate urn: prefixes, read actual schema after it
                } else {
                    output.add(internal); //valid & complete scheme
                    return (internal.endsWith(FILE_SCHEME) ? path : hierPart).reset();
                }
            } else { // bad scheme, assume relative IRI
                return runFrom(path.reset(), internal, 0).feedByte(byteValue);
            }
        }
    }

    /**
     * See <code>ihier-part</code> in
     * <a href="https://datatracker.ietf.org/doc/html/rfc3987#section-2.2">RFC 3987</a>.
     */
    private class HierPart extends ComponentState {
        private int slashes = 0;

        @Override public @Nonnull ComponentState reset() {
            slashes = 0;
            return super.reset();
        }

        @Override public @Nonnull ComponentState feedByte(int byteValue) {
            assert slashes < 2;
            if (byteValue == '/') {
                if (++slashes == 2) {
                    output.add(byteValue).add(byteValue);
                    return authority.reset();
                }
                return this;
            } else {
                path.reset();
                return (slashes > 0 ? path.feedByte('/') : path).feedByte(byteValue);
            }
        }
    }

    private class UserInfo extends PercentEncoder {
        public UserInfo() {
            super("iuserinfo");
        }

        @Override protected boolean isAllowed(int codePoint) {
            return isUnreserved(codePoint) || isInSmall(codePoint, USERINFOSUBDELIMS);
        }
    }

    /**
     * See <code>iauthority</code> in
     * <a href="https://datatracker.ietf.org/doc/html/rfc3987#section-2.2">RFC 3987</a>.
     */
    private class Authority extends BufferingComponentState {
        private boolean validateIP() {
            final String tail = "Will percent-encode into an ireg-name from RFC 3987.";
            int end = internal.indexOf(']');
            String string = internal.asString(0, end < 0 ? internal.size() : end+1);
            if (end < 0) {
                logger.warn("{}IPv6+ address '{}' is missing closing ']', {}",
                            getContextPrefix("Bad IPv6 address "), string, tail);
                return false;
            } else if (internal.has('v', 0)) {
                if (!IP_FUTURE.matcher(string).matches()) {
                    logger.warn("{}\"{}\" does not match IPvFuture rule from RFC 3987. {}",
                                getContextPrefix("Bad IP address at "), string, tail);
                    return false;
                }
            } else {
                try {
                    return InetAddress.getByName(string) != null;
                } catch (UnknownHostException e) {
                    logger.warn("{}\"{}\" is not a valid IP v6 address: {}",
                                getContextPrefix("Bad IP address at "), string, e);
                    return false;
                }
            }
            return true; // valid
        }

        private void dumpPort(int from) {
            int size = internal.size(), p = from + (from<size && internal.get(from) == ':' ? 1:0);
            while (p < size && isDigit(internal.get(p))) ++p;
            if (p > from+1) {
                output.add(internal.getArray(), from, p - from);
                if (p != size) {
                    logger.warn("{}Erasing \"{}\" from \"{}\", as the extra does not match " +
                                "RFC 3987 iport rule ^:[0-9]+", getContextPrefix("Bad port at "),
                                internal.asString(UTF_8, from, p), internal.asString(from, size));
                }
            } else {
                logger.warn("{}\"{}\" in \"{}\" does not match RFC 3987 iport rule ^:[0-9]. " +
                            "Percent-encoding it into ireg-name", getContextPrefix("Bad port at "),
                            internal.asString(UTF_8, from, size), internal.asString());
                runFrom(regName.reset(), internal, from);
                regName.flush();
            }
        }

        @Override public void flush() {
            int ipEnd;
            if (internal.has('[', 0)) { // handle ip-literal
                if (validateIP()) {
                    output.add(internal.getArray(), 0, ipEnd = internal.indexOf(']')+1);
                } else {
                    ipEnd = runUntil(regName.reset(), internal, ':');
                }
            } else { // ireg-name or IPv4address
                ipEnd = runUntil(regName.reset(), internal, ':');
            }
            if (ipEnd < internal.size())
                dumpPort(ipEnd);
        }

        @Override public @Nonnull ComponentState feedByte(int byteValue) {
            if (byteValue == '@') {
                runFrom(userInfo.reset(), internal, 0);
                userInfo.flush();
                output.add(byteValue);
                internal.clear(); // save only ihost [":" iport]
            } else if (byteValue == '/') {
                flush();
                return path.feedByte(byteValue);
            } else {
                internal.add(byteValue);
            }
            return this;
        }
    }

    /**
     * See <code>ireg-name</code> in
     * <a href="https://datatracker.ietf.org/doc/html/rfc3987#section-2.2">RFC 3987</a>, which
     * subsumes <code>IPv4address</code>.
     */
    private class RegName extends PercentEncoder {
        public RegName() { super("ireg-name"); }
        @Override protected boolean isAllowed(int codePoint) {
            return isUnreserved(codePoint) || isInSmall(codePoint, SUBDELIMS);
        }
    }

    /**
     * See <code>ipath-abempty</code>, <code>ipath-absolute</code>, <code>ipath-rootless</code>
     * and <code>ipath-empty</code> in
     * <a href="https://datatracker.ietf.org/doc/html/rfc3987#section-2.2">RFC 3987</a>.
     */
    private class Path extends PercentEncoder {
        public Path() { super("ipath"); }
        @Override protected boolean isAllowed(int codePoint) {
            return isUnreserved(codePoint) || isInSmall(codePoint, PATHSUBDELIMS);
        }

        @Override public @Nonnull ComponentState feedByte(int byteValue) {
            if (byteValue == '#') {
                output.add(byteValue);
                return fragment;
            } else if (byteValue == '?') {
                output.add(byteValue);
                return query;
            }
            return super.feedByte(byteValue);
        }
    }

    /**
     * See <code>ifragment</code> in
     * <a href="https://datatracker.ietf.org/doc/html/rfc3987#section-2.2">RFC 3987</a>.
     */
    private class Fragment extends PercentEncoder {
        public Fragment() {
            super("ifragment");
        }

        @Override protected boolean isAllowed(int codePoint) {
            return isUnreserved(codePoint) || isInSmall(codePoint, FRAGMENTCHARS);
        }
    }

    /**
     * See <code>iquery</code> in
     * <a href="https://datatracker.ietf.org/doc/html/rfc3987#section-2.2">RFC 3987</a>.
     */
    private class Query extends PercentEncoder {
        public Query() {
            super("iquery");
        }

        @Override public @Nonnull ComponentState feedByte(int byteValue) {
            if (byteValue == '#') {
                output.add(byteValue);
                return fragment;
            } else {
                return super.feedByte(byteValue);
            }
        }

        @Override protected boolean isAllowed(int codePoint) {
            if (isUnreserved(codePoint) || isInSmall(codePoint, FRAGMENTCHARS))
                return true;
            // more efficient to unroll than to invoke binarySearch()
            if (codePoint <  0xE000) return false;
            if (codePoint <= 0xF8FF) return true;
            if (codePoint <  0xF0000) return false;
            if (codePoint <= 0xFFFFD) return true;
            return codePoint >= 0x100000 && codePoint <= 0x10FFFD;
        }
    }

    /**
     * Create a new {@link IRIFixerParser}
     *  @param output Where to write the validated/fixed IRI bytes (UTF-8 encoded)
     * 
     */
    public IRIFixerParser(@Nonnull GrowableByteBuffer output) {
        this(output, null);
    }

    /**
     * Create a new {@link IRIFixerParser}
     *
     * @param output Where to write the validated/fixed IRI bytes (UTF-8 encoded)
     * @param context If an error is found/fixed, use this string as a identification of the source
     */
    public IRIFixerParser(@Nonnull GrowableByteBuffer output, @Nullable String context) {
        this.output = output;
        this.context = context;
    }

    public @Nonnull IRIFixerParser setContext(@Nullable String context) {
        this.context = context;
        return this;
    }

    public @Nullable String getContext() {
        return context;
    }

    @Override public @Nonnull FixerParser reset() {
        state = scheme.reset();
        return this;
    }

    @Override public void flush() {
        state.flush();
    }

    @Override public @Nonnull FixerParser feedByte(int value) {
        state = state.feedByte(value);
        return this;
    }

    @Override public @Nonnull String toString() {
        return getClass().getSimpleName() + "{state=" + state + ", override=" + output + '}';
    }
}
