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

import com.github.lapesd.rdfit.source.syntax.RDFLangs;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import com.github.lapesd.rdfit.util.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static java.lang.String.format;

/**
 * An {@link RDFInputStream} over a byte array
 */
public class RDFBytesInputStream extends RDFInputStream {
    private final @Nonnull byte[] data;

    public static class Builder {
        private final @Nonnull byte[] data;
        private @Nullable RDFLang lang;
        private @Nullable String baseIRI;
        private @Nullable String name;
        private @Nullable RDFInputStreamDecorator decorator;

        public Builder(@Nonnull byte[] data) {
            this.data = data;
        }

        public @Nonnull Builder lang(@Nullable RDFLang lang) {
            this.lang = lang;
            return this;
        }

        public @Nonnull Builder baseIRI(@Nullable String baseIRI) {
            this.baseIRI = baseIRI;
            return this;
        }

        public @Nonnull Builder name(@Nullable String name) {
            this.name = name;
            return this;
        }

        public @Nonnull Builder decorator(@Nullable RDFInputStreamDecorator decorator) {
            this.decorator = decorator;
            return this;
        }

        public @Nonnull RDFBytesInputStream build() {
            return new RDFBytesInputStream(data, lang, baseIRI, name, decorator);
        }
    }

    public static @Nonnull Builder builder(@Nonnull byte[] data) {
        return new Builder(data);
    }

    public RDFBytesInputStream(@Nonnull byte[] data) {
        this(data, null, null, null, null);
    }

    public RDFBytesInputStream(@Nonnull byte[] data, @Nullable RDFLang lang) {
        this(data, lang, null, null, null);
    }

    public RDFBytesInputStream(@Nonnull byte[] data, @Nullable RDFLang lang,
                               @Nullable String baseIRI) {
        super(new ByteArrayInputStream(data), lang, baseIRI);
        this.data = data;
    }

    public RDFBytesInputStream(@Nonnull byte[] data, @Nullable RDFLang lang,
                           @Nullable String baseIRI, @Nullable String name) {
        this(data, lang, baseIRI, name, null);
    }

    public RDFBytesInputStream(@Nonnull byte[] data, @Nullable RDFLang lang,
                               @Nullable String baseIRI, @Nullable String name,
                               @Nullable RDFInputStreamDecorator decorator) {
        super(new ByteArrayInputStream(data), lang, baseIRI, name, decorator);
        this.data = data;
    }

    public @Nonnull byte[] getData() {
        return data;
    }

    @Override public @Nonnull String toString() {
        RDFLang lang = getLang();
        byte[] d = getData();
        if (lang == null) {
            try {
                lang = RDFLangs.guess(new ByteArrayInputStream(d), d.length);
            } catch (IOException ignored) {}
        }
        String data = null;
        if (lang != null) {
            byte[] start = new byte[Math.min(40, d.length)];
            System.arraycopy(d, 0, start, 0, start.length);
            if (lang.isBinary())
                start = Base64.getEncoder().encode(start);
            data = new String(start, StandardCharsets.UTF_8);
        }
        return format("%s{lang=%s,base=%s,data=%s}",
                      Utils.toString(this), getLang(), baseIRI, data);
    }
}
