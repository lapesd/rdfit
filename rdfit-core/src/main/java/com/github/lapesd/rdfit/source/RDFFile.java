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

import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import com.github.lapesd.rdfit.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public class RDFFile extends RDFInputStream {
    private static final Logger logger = LoggerFactory.getLogger(RDFFile.class);

    private final @Nonnull File file;
    private boolean deleteOnClose;

    private static @Nonnull String computeBaseIRI(@Nullable String offeredBaseIRI,
                                                  @Nonnull File file) {
        if (offeredBaseIRI != null && !offeredBaseIRI.isEmpty())
            return offeredBaseIRI;
        return Utils.toASCIIString(file.toURI());
    }

    public static class Builder {
        private final @Nonnull File file;
        private @Nullable RDFLang lang;
        private @Nullable String baseIRI;
        private boolean deleteOnClose;
        private @Nullable RDFInputStreamDecorator decorator;

        public Builder(@Nonnull File file) {
            this.file = file;
        }

        public @Nonnull Builder lang(@Nullable RDFLang lang) {
            this.lang = lang;
            return this;
        }

        public @Nonnull Builder baseIRI(@Nullable String baseIRI) {
            this.baseIRI = baseIRI;
            return this;
        }

        public @Nonnull Builder deleteOnClose() { return deleteOnClose(true); }

        public @Nonnull Builder deleteOnClose(boolean deleteOnClose) {
            this.deleteOnClose = deleteOnClose;
            return this;
        }

        public @Nonnull Builder decorator(@Nullable RDFInputStreamDecorator decorator) {
            this.decorator = decorator;
            return this;
        }

        public @Nonnull RDFFile build() {
            return new RDFFile(file, lang, baseIRI, decorator, deleteOnClose);
        }
    }

    public static @Nonnull Builder builder(@Nonnull File file) {
        return new Builder(file);
    }
    public static @Nonnull Builder builder(@Nonnull Path file) {
        return new Builder(file.toFile());
    }
    public static @Nonnull Builder builder(@Nonnull String path) {
        return new Builder(new File(path));
    }

    public RDFFile(@Nonnull File file) {
        this(file, false);
    }
    public RDFFile(@Nonnull File file, @Nullable RDFLang lang) {
        this(file, lang, false);
    }
    public RDFFile(@Nonnull File file, boolean deleteOnClose) {
        this(file, null, deleteOnClose);
    }
    public RDFFile(@Nonnull File file, @Nullable RDFLang lang, boolean deleteOnClose) {
        this(file, lang, null, deleteOnClose);
    }
    public RDFFile(@Nonnull File file, @Nullable RDFLang lang, @Nullable String baseIRI) {
        this(file, lang, baseIRI, false);
    }
    public RDFFile(@Nonnull File file, @Nullable RDFLang lang, @Nullable String baseIRI,
                   @Nullable RDFInputStreamDecorator decorator) {
        this(file, lang, baseIRI, decorator, false);
    }

    public RDFFile(@Nonnull File file, @Nullable RDFLang lang, @Nullable String baseIRI,
                   boolean deleteOnClose) {
        this(file, lang, baseIRI, null, deleteOnClose);
    }
    public RDFFile(@Nonnull File file, @Nullable RDFLang lang, @Nullable String baseIRI,
                   @Nullable RDFInputStreamDecorator decorator, boolean deleteOnClose) {
        super(null, lang, computeBaseIRI(baseIRI, file), file.getPath(), decorator, true);
        this.file = file;
        this.deleteOnClose = deleteOnClose;
    }

    public static @Nonnull RDFFile createTemp() throws IOException {
        return createTemp(new ByteArrayInputStream(new byte[0]));
    }
    public static @Nonnull RDFFile
    createTemp(@Nonnull Supplier<RDFInputStream> supplier) throws IOException {
        return createTemp(supplier.get());
    }
    public static @Nonnull RDFFile createTemp(@Nonnull RDFInputStream ris) throws IOException {
        String hint = ris.getBaseIRI();
        String prefix = hint.substring(hint.lastIndexOf('/') + 1);
        int extBegin = prefix.indexOf('.') + 1;
        String ext = "." + prefix.substring(extBegin);
        prefix = prefix.substring(0, extBegin);
        return createTemp(ris.getInputStream(), prefix, ext, ris.getLang(),
                          ris.hasBaseIRI() ? ris.getBaseIRI() : null);

    }
    public static @Nonnull RDFFile createTemp(@Nonnull InputStream is) throws IOException {
        return createTemp(is, "rdfit", "", null, null);
    }
    public static @Nonnull RDFFile createTemp(@Nonnull InputStream is, @Nonnull String prefix,
                                              @Nonnull String suffix, @Nullable RDFLang lang,
                                              @Nullable String baseIRI)  throws IOException{
        File file = Files.createTempFile(prefix, suffix).toFile();
        file.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(file)) {
            byte[] buf = new byte[256];
            for (int n = is.read(buf); n >= 0; n = is.read(buf))
                out.write(buf, 0, n);
        }
        return new RDFFile(file, lang, baseIRI, true);
    }

    public @Nonnull RDFFile setDeleteOnClose() {
        return setDeleteOnClose(true);
    }

    public @Nonnull RDFFile setDeleteOnClose(boolean value) {
        deleteOnClose = value;
        return this;
    }

    public boolean getDeleteOnClose() {
        return deleteOnClose;
    }

    public @Nonnull File getFile() {
        return file;
    }

    @Override public @Nonnull InputStream getRawInputStream() {
        if (inputStream == null) {
            try {
                inputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new RDFItException(file, e);
            }
        }
        return inputStream;
    }

    @Override public @Nonnull String toString() {
        return String.format("%s{syntax=%s,file=%s}", Utils.toString(this), lang, file);
    }

    @Override public void close() {
        try {
            super.close();
        } finally {
            if (deleteOnClose) {
                if (!file.delete())
                    logger.error("{}.close(): failed to delete {}", this, file.getAbsolutePath());
            }
        }
    }
}
