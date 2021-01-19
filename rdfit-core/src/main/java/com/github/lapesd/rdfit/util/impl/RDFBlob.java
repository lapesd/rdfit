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
    private final @Nullable byte[] data;
    private final @Nullable Class<?> refClass;
    private final @Nullable String resourcePath;
    private final @Nullable RDFLang lang;
    private final @Nullable String baseIRI;
    private @Nullable Integer length;

    public RDFBlob(@Nonnull byte[] data, @Nullable RDFLang lang, @Nullable String baseIRI) {
        this.data = data;
        this.refClass = null;
        this.resourcePath = null;
        this.lang = lang;
        this.baseIRI = baseIRI;
    }

    public RDFBlob(@Nonnull Class<?> refClass, @Nonnull String resourcePath,
               @Nullable String baseIRI) throws IllegalArgumentException {
        this(refClass, resourcePath, null, baseIRI);
    }

    public RDFBlob(@Nonnull Class<?> refClass, @Nonnull String resourcePath,
                   @Nullable RDFLang lang,
                   @Nullable String baseIRI) throws IllegalArgumentException {
        this.data = null;
        this.refClass = refClass;
        this.resourcePath = resourcePath;
        if (lang == null)
            lang = RDFLangs.fromExtension(resourcePath);
        if (!RDFLangs.isKnown(lang))
            lang = null;
        this.lang = lang;
        this.baseIRI = baseIRI;
        try (RDFInputStream ris = get()) {
            @SuppressWarnings("unused") InputStream ignored = ris.getInputStream();
            if (!RDFLangs.isKnown(ris.getOrDetectLang())) {
                throw new IllegalArgumentException("Could not determine RDFLang for resource "+
                                                   resourcePath+" relative to "+refClass);
            }
        } catch (IOException|RuntimeException|Error exception) {
            Throwable t = exception;
            if (t.getCause() instanceof IOException)
                t = exception.getCause();
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error)t;
            } else  {
                throw new IllegalArgumentException("IOException reading from resource "+
                                                   resourcePath+" relative to "+refClass, t);
            }
        }
    }

    public RDFBlob(@Nonnull RDFInputStream ris) throws IOException {
        this(Utils.toBytes(ris.getInputStream()), ris.getLang(),
                           ris.hasBaseIRI() ? ris.getBaseIRI() : null);
    }

    public static @Nonnull RDFBlob
    fromSupplier(@Nonnull Supplier<RDFInputStream> supplier) throws IOException {
        if (supplier instanceof RDFBlob) return (RDFBlob) supplier;
        return new RDFBlob(supplier.get());
    }

    public int getLength() {
        if (data != null) {
            return data.length;
        } else if (length == null) {
            try {
                length = Utils.toBytes(get().getInputStream()).length;
            } catch (IOException e) {
                throw new RuntimeException("Unexpected IOException", e);
            }
        }
        return length;
    }

    @Override public @Nonnull RDFInputStream get() {
        if (data != null) {
            return new RDFInputStream(new ByteArrayInputStream(data), lang, baseIRI);
        } else {
            assert refClass != null;
            assert resourcePath != null;
            InputStream is = Utils.openResource(refClass, resourcePath);
            return new RDFInputStream(is, lang, baseIRI);
        }
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
