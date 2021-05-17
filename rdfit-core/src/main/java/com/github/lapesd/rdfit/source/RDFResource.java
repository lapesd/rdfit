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

import static com.github.lapesd.rdfit.util.Utils.openResource;

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

    public static class Builder {
        private final @Nonnull InputStream inputStream;
        private final @Nonnull String fallbackBaseIRI;
        private @Nullable RDFLang lang;
        private @Nullable String baseIRI;
        private @Nullable RDFInputStreamDecorator decorator;

        public Builder(@Nonnull InputStream inputStream, @Nonnull String fallbackBaseIRI) {
            this.inputStream = inputStream;
            this.fallbackBaseIRI = fallbackBaseIRI;
        }

        public @Nonnull Builder lang(@Nullable RDFLang lang) {
            this.lang = lang;
            return this;
        }

        public @Nonnull Builder baseIRI(@Nullable String baseIRI) {
            this.baseIRI = baseIRI;
            return this;
        }

        public @Nonnull Builder decorator(@Nullable RDFInputStreamDecorator decorator) {
            this.decorator = decorator;
            return this;
        }

        public @Nonnull RDFResource build() {
            return new RDFResource(inputStream, fallbackBaseIRI, lang, baseIRI, decorator);
        }
    }

    public static @Nonnull Builder builder(@Nonnull Class<?> refClass, @Nonnull String resourcePath) {
        return new Builder(openResource(refClass, resourcePath),
                          computeFallbackBaseIRI(refClass, resourcePath));
    }
    public static @Nonnull Builder builder(@Nonnull String resourcePath) {
        return new Builder(openResource(resourcePath), computeFallbackBaseIRI(resourcePath));
    }

    public RDFResource(@Nonnull Class<?> refClass, @Nonnull String resourcePath) {
        this(refClass, resourcePath, (RDFInputStreamDecorator) null);
    }
    public RDFResource(@Nonnull Class<?> refClass, @Nonnull String resourcePath,
                       @Nullable RDFInputStreamDecorator decorator) {
        this(refClass, resourcePath, null, null, decorator);
    }
    public RDFResource(@Nonnull Class<?> refClass, @Nonnull String resourcePath,
                       @Nullable RDFLang lang) {
        this(refClass, resourcePath, lang, (RDFInputStreamDecorator)null);
    }
    public RDFResource(@Nonnull Class<?> refClass, @Nonnull String resourcePath,
                       @Nullable RDFLang lang, @Nullable RDFInputStreamDecorator decorator) {
        this(refClass, resourcePath, lang, null, decorator);
    }
    public RDFResource(@Nonnull Class<?> refClass, @Nonnull String resourcePath,
                       @Nullable RDFLang lang, @Nullable String baseIRI) {
        this(refClass, resourcePath, lang, baseIRI, null);
    }
    public RDFResource(@Nonnull Class<?> refClass, @Nonnull String resourcePath,
                       @Nullable RDFLang lang, @Nullable String baseIRI,
                       @Nullable RDFInputStreamDecorator decorator) {
        this(openResource(refClass, resourcePath),
             computeFallbackBaseIRI(refClass, resourcePath), lang, baseIRI, decorator);
    }

    public RDFResource(@Nonnull String resourcePath) {
        this(resourcePath, null, null, null);
    }
    public RDFResource(@Nonnull String resourcePath, @Nullable RDFInputStreamDecorator decorator) {
        this(resourcePath, null, null, decorator);
    }
    public RDFResource(@Nonnull String resourcePath, @Nullable RDFLang lang) {
        this(resourcePath, lang, null, null);
    }
    public RDFResource(@Nonnull String resourcePath, @Nullable RDFLang lang,
                       @Nullable RDFInputStreamDecorator decorator) {
        this(resourcePath, lang, null, decorator);
    }
    public RDFResource(@Nonnull String resourcePath,
                       @Nullable RDFLang lang, @Nullable String baseIRI) {
        this(resourcePath, lang, baseIRI, null);
    }
    public RDFResource(@Nonnull String resourcePath,
                       @Nullable RDFLang lang, @Nullable String baseIRI,
                       @Nullable RDFInputStreamDecorator decorator) {
        this(openResource(resourcePath),
             computeFallbackBaseIRI(resourcePath), lang, baseIRI, decorator);
    }
    public RDFResource(@Nonnull InputStream inputStream, @Nonnull String fallbackBaseIRI,
                       @Nullable RDFLang lang, @Nullable String baseIRI) {
        this(inputStream, fallbackBaseIRI, lang, baseIRI, null);
    }
    public RDFResource(@Nonnull InputStream inputStream, @Nonnull String fallbackBaseIRI,
                       @Nullable RDFLang lang, @Nullable String baseIRI,
                       @Nullable RDFInputStreamDecorator decorator) {
        super(inputStream, lang, baseIRI,  fallbackBaseIRI, decorator);
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
