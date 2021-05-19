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

package com.github.lapesd.rdfit.components.compress.normalizers.impl;

import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.source.RDFInputStreamDecorator;
import com.github.lapesd.rdfit.source.SourcesIterator;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

/**
 * A {@link SourcesIterator} over entries in a {@link SevenZFile}.
 */
public class SevenZSourceIterator implements SourcesIterator {
    private static final Logger logger = LoggerFactory.getLogger(SevenZSourceIterator.class);
    private final @Nonnull Object source;
    private final @Nullable RDFInputStreamDecorator decorator;
    private final @Nonnull SevenZFile file;
    private @Nullable File deleteOnClose;
    private boolean exhausted;
    private @Nullable Object current;

    /**
     * Constructor
     * @param source the source that yielded the {@link SevenZFile}
     * @param file the open and readable {@link SevenZFile}
     */
    public SevenZSourceIterator(@Nonnull Object source, @Nonnull SevenZFile file) {
        this.source = source;
        this.decorator = source instanceof RDFInputStream
                       ? ((RDFInputStream)source).getDecorator() : null;
        this.file = file;
    }

    /**
     * Causes the given file to be deleted when this {@link SourcesIterator} is closed.
     * @param file {@link File} to delete on {@link #close()}
     * @return this instance
     */
    public @Nonnull SevenZSourceIterator deleteOnClose(@Nonnull File file) {
        deleteOnClose = file;
        return this;
    }

    @Override public boolean hasNext() {
        while (!exhausted && current == null)
            advance();
        return current != null;
    }

    private static class SZInputStream extends InputStream {
        private final @Nonnull SevenZFile sz; // do not close on close()

        private SZInputStream(@Nonnull SevenZFile sz) {
            this.sz = sz;
        }

        @Override public int read() throws IOException {
            return sz.read();
        }

        @Override public int read(@Nonnull byte[] b) throws IOException {
            return sz.read(b);
        }

        @Override public int read(@Nonnull byte[] b, int off, int len) throws IOException {
            return sz.read(b, off, len);
        }

    }

    private void advance() {
        try {
            SevenZArchiveEntry e = file.getNextEntry();
            if (e == null) {
                exhausted = true;
                close();
            } else if (!e.isDirectory() && !e.isAntiItem()) {
                String name = null;
                if (source instanceof RDFInputStream)
                    name = ((RDFInputStream)source).getName();
                if (name == null || name.isEmpty())
                    name = source.toString();
                current = RDFInputStream.builder(new SZInputStream(file))
                                        .name(name+"["+e.getName()+"]")
                                        .decorator(decorator).build();
            }
        } catch (IOException e) {
            current = new RDFItException(source, "Problem reading 7z contents from "+source, e);
        }
    }

    @Override public Object next() {
        if (!hasNext()) throw new NoSuchElementException();
        Object current = this.current;
        this.current = null;
        assert current != null;
        return current;
    }

    @Override public void close() {
        try {
            file.close();
        } catch (Throwable e) {
            logger.error("Ignoring SevenZFile.close() failure for {}.", source, e);
        }
        if (deleteOnClose != null && deleteOnClose.exists() && !deleteOnClose.delete())
            logger.error("Failed to delete temp file {}", deleteOnClose);
    }
}
