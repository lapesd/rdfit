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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

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
                   boolean deleteOnClose) {
        super(null, lang, computeBaseIRI(baseIRI, file), true);
        this.file = file;
        this.deleteOnClose = deleteOnClose;
    }

    public @Nonnull RDFFile setDeleteOnClose() {
        return setDeleteOnClose(true);
    }

    public @Nonnull RDFFile setDeleteOnClose(boolean value) {
        deleteOnClose = value;
        return this;
    }

    public @Nonnull File getFile() {
        return file;
    }

    @Override public @Nonnull InputStream getInputStream() {
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
