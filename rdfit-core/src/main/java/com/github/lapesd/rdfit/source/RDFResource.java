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

package com.github.lapesd.rdfit.source;

import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import com.github.lapesd.rdfit.util.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;

/**
 * An {@link RDFInputStream} over java resources in the classpath.
 */
public class RDFResource extends RDFInputStream {
    private final @Nonnull String fallbackBaseIRI;

    private static @Nonnull String computeFallbackBaseIRI(@Nonnull Class<?> refClass,
                                                          @Nonnull String resourcePath) {
        return "file:"+Utils.toFullResourcePath(resourcePath, refClass);
    }

    private static @Nonnull String computeFallbackBaseIRI(@Nonnull String resourcePath) {
        return "file:"+Utils.toFullResourcePath(resourcePath, null);
    }

    public RDFResource(@Nonnull Class<?> refClass, @Nonnull String resourcePath) {
        this(refClass, resourcePath, null);
    }
    public RDFResource(@Nonnull Class<?> refClass, @Nonnull String resourcePath,
                       @Nullable RDFLang lang) {
        this(refClass, resourcePath, lang, null);
    }
    public RDFResource(@Nonnull Class<?> refClass, @Nonnull String resourcePath,
                       @Nullable RDFLang lang, @Nullable String baseIRI) {
        this(Utils.openResource(refClass, resourcePath),
             computeFallbackBaseIRI(refClass, resourcePath), lang, baseIRI);
    }

    public RDFResource(@Nonnull String resourcePath) {
        this(resourcePath, null);
    }
    public RDFResource(@Nonnull String resourcePath, @Nullable RDFLang lang) {
        this(resourcePath, lang, null);
    }
    public RDFResource(@Nonnull String resourcePath,
                       @Nullable RDFLang lang, @Nullable String baseIRI) {
        this(Utils.openResource(resourcePath),
             computeFallbackBaseIRI(resourcePath), lang, baseIRI);
    }
    public RDFResource(@Nonnull InputStream inputStream, @Nonnull String fallbackBaseIRI,
                       @Nullable RDFLang lang, @Nullable String baseIRI) {
        super(inputStream, lang, baseIRI);
        this.fallbackBaseIRI = fallbackBaseIRI;
    }

    @Override public @Nonnull String getBaseIRI() {
        if (hasBaseIRI())
            return super.getBaseIRI();
        return fallbackBaseIRI;
    }

    @Override public @Nonnull String toString() {
        return String.format("%s{%s, syntax=%s, baseIRI=%s}",
                             Utils.toString(this),
                             fallbackBaseIRI.replaceFirst("^file:/*", ""),
                             getLang(), getBaseIRI());
    }
}
