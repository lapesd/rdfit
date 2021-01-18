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

package com.github.lapesd.rdfit.util.impl;

import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.source.syntax.RDFLangs;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import com.github.lapesd.rdfit.util.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Supplier;

public class RDFBlob implements Supplier<RDFInputStream> {
    private final byte[] data;
    private final @Nullable RDFLang lang;
    private final @Nullable String baseIRI;

    public RDFBlob(byte[] data, @Nullable RDFLang lang, @Nullable String baseIRI) {
        this.data = data;
        this.lang = lang;
        this.baseIRI = baseIRI;
    }

    public RDFBlob(@Nonnull RDFInputStream ris) throws IOException {
        this(Utils.toBytes(ris.getInputStream()), ris.getLang(),
                           ris.hasBaseIri() ? ris.getBaseIRI() : null);
    }

    public static @Nonnull RDFBlob
    fromResource(@Nonnull Class<?> refClass, @Nonnull String resourcePath,
                 @Nonnull String baseIRI) throws IOException {
        try (InputStream in = Utils.openResource(refClass, resourcePath)) {
            RDFLang lang = RDFLangs.fromExtension(resourcePath);
            return new RDFBlob(Utils.toBytes(in), RDFLangs.isKnown(lang) ? lang : null, baseIRI);
        }
    }

    public static @Nonnull RDFBlob
    fromSupplier(@Nonnull Supplier<RDFInputStream> supplier) throws IOException {
        if (supplier instanceof RDFBlob) return (RDFBlob) supplier;
        return new RDFBlob(supplier.get());
    }

    public int getLength() {
        return data.length;
    }

    @Override public @Nonnull RDFInputStream get() {
        return new RDFInputStream(new ByteArrayInputStream(data), lang, baseIRI);
    }

    @Override public @Nonnull String toString() {
        RDFInputStream ris = get();
        RDFLang lang;
        try {
            lang = ris.getOrDetectLang(Integer.MAX_VALUE);
        } catch (IOException e) {
            return String.format("%s{lang=null,baseIRI=%s}", Utils.toString(this), baseIRI);
        }
        String string = String.format("%s{lang=%s,baseIRI=%s,data=",
                                       Utils.toString(this), lang, baseIRI);
        ByteBuffer byteBuffer;
        try (InputStream is = ris.getInputStream()) {
            byte[] buf = new byte[80];
            byteBuffer = ByteBuffer.wrap(buf, 0, is.read(buf));
        } catch (IOException e) {
            return string + "# unexpected IOException #}";
        }
        if (!lang.isBinary())
            return string + StandardCharsets.UTF_8.decode(byteBuffer).toString() + "}";
        else
            return string + Base64.getEncoder().encode(byteBuffer) + "}";
    }
}
