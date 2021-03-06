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

package com.github.lapesd.rdfit.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Utils {
    private static final @Nonnull Pattern ANY_UP_STEP_RX = Pattern.compile("(^|/)\\.\\.(/|$)");
    private static final @Nonnull Pattern UP_STEP_RX = Pattern.compile("\\.\\./");
    public static final byte[] HEX_DIGITS = "0123456789ABCDEF".getBytes(UTF_8);
    public static final byte[] ASCII_WS = new byte[] {'\t', '\n', '\r', ' '};

    public static boolean isHexDigit(int c) {
        if ((c & ~0x7F) != 0)
            return false;
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    public static @Nonnull GrowableByteBuffer writeHexByte(@Nonnull GrowableByteBuffer out, int value) {
        assert (value & ~0xFF) == 0;
        return out.add(HEX_DIGITS[value/16]).add(HEX_DIGITS[value%16]);
    }

    public static int parseHexByte(int hiCodePoint, int loCodePoint) {
        int hiIdx = indexInSmall(asciiUpper(hiCodePoint), HEX_DIGITS);
        int loIdx = indexInSmall(asciiUpper(loCodePoint), HEX_DIGITS);
        return (hiIdx < 0 || loIdx < 0) ? -1 : hiIdx * 16 + loIdx;
    }


    /**
     * Given a {@link CharBuffer} just written to by a {@link CharsetDecoder},
     * return the code point of the single decoded character.
     *
     * @param singleCodePointOutBuffer the buffer written by a {@link CharsetDecoder}.
     * @return -1 if the buffer is empty or the code point (either the value of the single char
     *         or the combination of a high and low surrogate java chars.
     * @throws IllegalStateException if the given buffer contains more than one unicode character
     *                               (i.e., more than 2 java chars)
     */
    public static int toCodePoint(@Nonnull CharBuffer singleCodePointOutBuffer) {
        int nChars = singleCodePointOutBuffer.position();
        if      (nChars == 0) {
            return -1;
        } else if (nChars == 1) {
            return singleCodePointOutBuffer.get(0);
        } else if (nChars == 2) {
            char high = singleCodePointOutBuffer.get(0), low = singleCodePointOutBuffer.get(1);
            if (Character.isHighSurrogate(high) && Character.isLowerCase(low))
                return Character.toCodePoint(high, low);
            else if (Character.isHighSurrogate(low) && Character.isLowerCase(high))
                return Character.toCodePoint(low, high);
            else
                throw new IllegalStateException("two Unicode chars decoded at once.");
        } else {
            throw new IllegalStateException("Too many chars in buffer!");
        }
    }

    public static boolean isAsciiSpace(int value) {
        return isInSmall(value, ASCII_WS);
    }

    public static boolean isAsciiAlpha(int v) {
        return (v >= 'A' && v <= 'Z') || (v >= 'a' && v <= 'z');
    }

    public static boolean isAsciiAlphaNum(int v) {
        return (v >= 'A' && v <= 'Z') || (v >= 'a' && v <= 'z') || (v >= '0' && v <= '9');
    }

    public static int asciiLower(int value) {
        return value + (value >= 'A' && value <= 'Z' ? 'a'-'A' : 0);
    }

    public static int asciiUpper(int value) {
        return value - (value >= 'a' && value <= 'z' ? 'a'-'A' : 0);
    }

    public static boolean isInSmall(int value, byte[] a) { return indexInSmall(value, a) >= 0; }

    public static int indexInSmall(int value, byte[] a) {
        int m = a.length>>1, i = (value < a[m] ? 0 : m)-1, diff = 1;
        for (int last = a.length-1; diff > 0 && i < last; )
            diff = value - a[++i];
        return diff == 0 ? i : -1;
    }

    public static @Nonnull String compactClass(@Nullable Class<?> cls) {
        return cls == null ? "null" : cls.getName().replaceAll("(\\w)[^.]+\\.", "$1.");
    }

    public static @Nonnull String genericToString(@Nonnull Object instance,
                                           @Nonnull Class<?>... params) {
        StringBuilder builder = new StringBuilder(compactClass(instance.getClass()));
        builder.append('<');
        for (Class<?> param : params)
            builder.append(", ").append(compactClass(param));
        if (params.length > 0)
            builder.setLength(builder.length()-2);
        builder.append(String.format(">@%x", System.identityHashCode(instance)));
        return builder.append('>').toString();
    }

    public static @Nonnull String toString(@Nonnull Object instance) {
        return String.format("%s@%x", compactClass(instance.getClass()),
                                      System.identityHashCode(instance));
    }

    /**
     * Parse the given string into an URI instance. If the string is invalid, will try to
     * escape reserved and non-ASCII characters before throwing an {@link URISyntaxException}.
     *
     * @param string the URI string to parse
     * @return an URI instance
     * @throws URISyntaxException If the given URI string is invalid and escaping unescaped
     *                            characters does not solve the issue
     */
    public static @Nonnull URI createURIOrFix(@Nonnull String string) throws URISyntaxException {
        try {
            return new URI(string);
        } catch (URISyntaxException e) {
            StringBuilder b = new StringBuilder(string.length()+20);
            char[] buf = new char[2];
            string.codePoints().forEach(c -> {
                if (Character.isISOControl(c) || c >= 0x7f || "<>{}|\\ \"'` ".indexOf(c) >= 0) {
                    b.append(String.format("%%%X", c));
                } else {
                    int nChars = Character.toChars(c, buf, 0);
                    for (int i = 0; i < nChars; i++)
                        b.append(buf[i]);
                }
            });
            try {
                return new URI(b.toString());
            } catch (URISyntaxException e2) {
                throw e; // blame the original bad input
            }
        }
    }

    /**
     * Convert the uri to its ASCII representation, but for "file:" URIs generated by Java,
     * replace the "file:" with "file://".
     *
     * @param uri the URI to convert to a string
     * @return a String representation of the URI containing only ASCII characters
     */
    public static @Nonnull String toASCIIString(@Nonnull URI uri) {
        String authority = uri.getAuthority();
        if ((authority == null || authority.isEmpty()) && uri.getScheme().startsWith("file"))
            return uri.toASCIIString().replaceFirst("^file:", "file://");
        return uri.toASCIIString();
    }

    /**
     * Same as {@link #toASCIIString(URI)}, but for URLs
     *
     * @param url the URL object to be converted
     * @return a String representation of url containing only ASCII chars
     */
    public static @Nonnull String toASCIIString(@Nonnull URL url) {
        String authority = url.getAuthority();
        if ((authority == null || authority.isEmpty()) && url.getProtocol().startsWith("file"))
            return url.toString().replaceFirst("^file:", "file://");
        return url.toString();
    }

    /**
     * Normalize https to http and remove URL fragment. Variants of those components should
     * result in the same response from a remote server.
     *
     * @param url the URL to normalize
     * @return a non-null and non-empty URL string.
     */
    public static @Nonnull String toCacheKey(@Nonnull URL url) {
        String s;
        return url.getProtocol().replace("https", "http") + "://"
                + ((s = url.getAuthority()) == null ? "" : s)
                + ((s = url.getPath()) == null ? "" : s)
                + ((s = url.getQuery()) == null ? "" : "?"+s);
    }

    /**
     * Read the given InputStream to completion into a byte array and close the stream.
     *
     * @param stream stream to read and close
     * @return byte array with all bytes read
     * @throws IOException if thrown from {@link InputStream#read()} or {@link InputStream#close()}.
     */
    public static @Nonnull byte[] toBytes(@Nonnull InputStream stream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[64];
        try (InputStream in = stream) {
            for (int nBytes = in.read(buf); nBytes > 0; nBytes = in.read(buf))
                out.write(buf, 0, nBytes);
        }
        return out.toByteArray();
    }

    /**
     * Get an {@link InputStream} for a resource file whose path is relative to the given class
     *
     * @param refClass Class that serves as reference to the resource file, will try to open
     *                 the file using {@link Class#getResourceAsStream(String)}, but will
     *                 try using a {@link ClassLoader} before giving up.
     * @param relativeResourcePath path to the resource file
     * @return a non-null {@link InputStream}
     * @throws IllegalArgumentException if the resource could not be found
     */
    public static @Nonnull InputStream
    openResource(@Nonnull Class<?> refClass,
                 @Nonnull String relativeResourcePath) throws IllegalArgumentException {
        InputStream is = refClass.getResourceAsStream(relativeResourcePath);
        if (is != null) return is;
        Class<?> tl = refClass;
        while (tl.getEnclosingClass() != null)
            tl = tl.getEnclosingClass();
        if (!tl.equals(refClass)) {
            is = tl.getResourceAsStream(relativeResourcePath);
            if (is != null) return is;
        }

        //try the system class loader
        String fullPath = toFullResourcePath(relativeResourcePath, tl);
        String absPath = "/" + fullPath;
        is = ClassLoader.getSystemResourceAsStream(fullPath);
        if (is != null) return is;

        // retry refClass, now with absolute path
        is = refClass.getResourceAsStream(absPath);
        if (is != null) return is;

        //try absolute path on top-level class (if refClass was not top-level)
        if (!tl.equals(refClass)) {
            is = tl.getResourceAsStream(absPath);
            if (is != null) return is;
        }

        //try fullPath on current thread ClassLoader
        ClassLoader threadLoader = Thread.currentThread().getContextClassLoader();
        is = threadLoader.getResourceAsStream(fullPath);
        if (is != null) return is;

        //try fullPath on the ClassLoader of the refClass
        ClassLoader refLoader = refClass.getClassLoader();
        is = refLoader.getResourceAsStream(fullPath);
        if (is != null) return is;

        // try absolute paths on ClassLoaders
        is = ClassLoader.getSystemResourceAsStream(absPath);
        if (is != null) return is;
        is = threadLoader.getResourceAsStream(absPath);
        if (is != null) return is;
        is = refLoader.getResourceAsStream(absPath);
        if (is != null) return is;

        // resource does not exist, this is likely the programmers fault
        throw new IllegalArgumentException("Could not find resource file "+
                                            relativeResourcePath+" relative to "+refClass);
    }

    /**
     * Opens an InputStream reading the resource at the given absolute or full path (no leading '/').
     *
     * @param resourcePath the path to the resource. Will be interpreted as an absolute path
     *                    regardless of a leading '/'. For tolerance, will try loading both
     *                     as an absolute path (leading '/') and as a full path.
     * @return A non-null InputStream reading the resource.
     * @throws IllegalArgumentException if the resource could not be found.
     */
    public static @Nonnull InputStream openResource(@Nonnull String resourcePath) {
        String fullPath = resourcePath.replaceFirst("^/+", "");
        String absPath = "/" + fullPath;

        InputStream is = ClassLoader.getSystemResourceAsStream(fullPath);
        if (is != null) return is;

        //try fullPath on current thread ClassLoader
        ClassLoader threadLoader = Thread.currentThread().getContextClassLoader();
        is = threadLoader.getResourceAsStream(fullPath);
        if (is != null) return is;

        // try absolute paths on ClassLoaders
        is = ClassLoader.getSystemResourceAsStream(absPath);
        if (is != null) return is;
        is = threadLoader.getResourceAsStream(absPath);
        if (is != null) return is;

        // resource does not exist, this is likely the programmers fault
        throw new IllegalArgumentException("Could not find resource file "+resourcePath);
    }

    /**
     * Createa a full path (no leading '/') to a resource that may be relative to the given class.
     *
     * @param resourcePath the resource path. If cls != null, this is considered relative,
     *                     unless it starts with '/'. If cls == null, it is assumed to be absolute
     *                     but must not contain '..' steps
     * @param cls the class to which the resourcePath is relative.
     * @return A full path (an absolute path that does not start with '/').
     * @throws IllegalArgumentException if resourcePath contains '..' steps in the path
     *                                  and cls == null.
     */
    public static @Nonnull String toFullResourcePath(@Nonnull String resourcePath,
                                                     @Nullable Class<?> cls) {
        String fullPath; // absolute path that does not start with '/'
        if (resourcePath.startsWith("/")) {
            fullPath = resourcePath.replaceAll("^/+", "");
        } else if (cls == null) {
            if (ANY_UP_STEP_RX.matcher(resourcePath).find())
                throw new IllegalArgumentException("cls == null with relative path (has ..)");
            fullPath = resourcePath;
        } else {
            for (Class<?> tl = cls.getEnclosingClass(); tl != null; tl = tl.getEnclosingClass())
                cls = tl;
            Matcher matcher = UP_STEP_RX.matcher(resourcePath);
            int stepsUp = 0, relativeStart = 0;
            while (matcher.find()) {
                relativeStart = matcher.end();
                ++stepsUp;
            }
            StringBuilder fullPathBuilder = new StringBuilder();
            String[] segments = cls.getName().split("\\.");
            for (int i = 0, size = segments.length - stepsUp - 1; i < size; i++)
                fullPathBuilder.append(segments[i]).append('/');
            fullPathBuilder.append(resourcePath.substring(relativeStart));
            fullPath = fullPathBuilder.toString();
        }
        return fullPath;
    }

    public static void extractResource(@Nonnull File file, @Nonnull Class<?> refClass,
                                       @Nonnull String path) throws IOException {
        byte[] buf = new byte[8192];
        try (InputStream in = openResource(refClass, path);
             FileOutputStream out = new FileOutputStream(file)) {
            for (int n = in.read(buf); n >= 0; n = in.read(buf))
                out.write(buf, 0, n);
        }
    }
}
