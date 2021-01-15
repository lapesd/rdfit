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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private static final @Nonnull Pattern UP_STEP_RX = Pattern.compile("\\.\\./");

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
        String fullPath = toFullPath(relativeResourcePath, tl);
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

    private static @Nonnull String toFullPath(@Nonnull String relativeResourcePath, Class<?> cls) {
        String fullPath; // absolute path that does not start with '/'
        if (relativeResourcePath.startsWith("/")) {
            fullPath = relativeResourcePath.replaceAll("^/+", "");
        } else {
            Matcher matcher = UP_STEP_RX.matcher(relativeResourcePath);
            int stepsUp = 0;
            while (matcher.find())
                ++stepsUp;
            StringBuilder fullPathBuilder = new StringBuilder();
            String[] segments = cls.getName().split("\\.");
            for (int i = 0, size = segments.length - stepsUp - 1; i < size; i++)
                fullPathBuilder.append(segments[i]).append('/');
            int filenameIdx = relativeResourcePath.lastIndexOf('/') + 1;
            fullPathBuilder.append(relativeResourcePath.substring(filenameIdx));
            fullPath = fullPathBuilder.toString();
        }
        return fullPath;
    }
}
