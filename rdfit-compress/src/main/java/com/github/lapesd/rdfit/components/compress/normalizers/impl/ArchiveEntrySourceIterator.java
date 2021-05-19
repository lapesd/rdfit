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
import com.github.lapesd.rdfit.source.impl.CloseShield;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * A {@link SourcesIterator} over {@link ArchiveInputStream} entries.
 */
public class ArchiveEntrySourceIterator implements SourcesIterator {
    private static final Logger logger = LoggerFactory.getLogger(ArchiveEntrySourceIterator.class);

    private final @Nonnull Object source;
    private final @Nonnull ArchiveInputStream archive;
    private final @Nullable RDFInputStreamDecorator decorator;
    private @Nullable Object current = null;
    private boolean exhausted = false;

    /**
     * Constructor
     *
     * @param source the source which yielded the {@link ArchiveInputStream}
     * @param archive a stream of archive entries
     */
    public ArchiveEntrySourceIterator(@Nonnull Object source,
                                      @Nonnull ArchiveInputStream archive) {
        this.source = source;
        this.decorator = source instanceof RDFInputStream
                       ? ((RDFInputStream)source).getDecorator() : null;
        this.archive = archive;
    }

    @Override public boolean hasNext() {
        while (!exhausted && current == null)
            advance();
        return !exhausted;
    }

    private void advance() {
        try {
            ArchiveEntry e = archive.getNextEntry();
            if (e == null) {
                exhausted = true;
                close();
            } else if (!e.isDirectory()) {
                String name = null;
                if (source instanceof RDFInputStream)
                    name = ((RDFInputStream) source).getName();
                if (name == null || name.isEmpty())
                    name = source.toString();
                current = RDFInputStream.builder(new CloseShield(archive))
                                        .name(name+"["+e.getName()+"]")
                                        .decorator(decorator).build();
            }
        } catch (Throwable e1) {
            exhausted = true;
            try {
                archive.close();
            } catch (IOException e2) {
                logger.error("archive.close() failed after {} on getNextEntry()for source {}",
                             e1, source, e2);
            }
            current = new RDFItException(source, e1);
        }
    }

    @Override public @Nonnull Object next() {
        if (!hasNext())
            throw new NoSuchElementException();
        assert this.current != null;
        Object current = this.current;
        this.current = null;
        return current;
    }

    @Override public void close() {
        try {
            archive.close();
        } catch (Throwable t) {
            logger.error("Ignoring failure to archive.close() source {}", source, t);
        }
    }
}
