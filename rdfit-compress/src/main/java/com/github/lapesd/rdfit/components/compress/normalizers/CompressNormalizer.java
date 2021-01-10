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

package com.github.lapesd.rdfit.components.compress.normalizers;

import com.github.lapesd.rdfit.components.annotations.Accepts;
import com.github.lapesd.rdfit.components.compress.normalizers.impl.ArchiveEntrySourceIterator;
import com.github.lapesd.rdfit.components.compress.normalizers.impl.SevenZSourceIterator;
import com.github.lapesd.rdfit.components.normalizers.BaseSourceNormalizer;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.source.RDFFile;
import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.source.impl.EmptySourcesIterator;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.BufferUnderflowException;
import java.nio.file.Files;

@Accepts(RDFInputStream.class)
public class CompressNormalizer extends BaseSourceNormalizer {
    private static final Logger logger = LoggerFactory.getLogger(CompressNormalizer.class);
    private ArchiveStreamFactory archiveFactory;
    private CompressorStreamFactory compressedFactory;

    @Override public @Nonnull Object normalize(@Nonnull Object source) {
        if (!(source instanceof RDFInputStream))
            return source;
        RDFInputStream ris = (RDFInputStream) source;
        BufferedInputStream is = ris.getBufferedInputStream();
        try {
            return openArchive(ArchiveStreamFactory.detect(is), ris, source);
        } catch (ArchiveException e) {
            try {
                return openCompressed(CompressorStreamFactory.detect(is), is, source);
            } catch (CompressorException compressorException) {
                return source; //does not appear to be compressed
            }
        } catch (RDFItException e) {
            return e;
        }
    }

    protected @Nonnull Object openArchive(@Nonnull String format, @Nonnull RDFInputStream ris,
                                          @Nonnull Object source) {
        if (archiveFactory == null)
            archiveFactory = new ArchiveStreamFactory();
        try {
            if (ArchiveStreamFactory.SEVEN_Z.equalsIgnoreCase(format)) {
                if (ris instanceof RDFFile) {
                    File file = ((RDFFile) ris).getFile();
                    try {
                        SevenZFile sz = new SevenZFile(file);
                        return new SevenZSourceIterator(source, sz);
                    } catch (BufferUnderflowException e) {
                        if (file.length() <= 64) {
                            logger.info("7z file at source {} has no entries", source);
                            return new EmptySourcesIterator(); //7z file has no entries
                        }
                        return new RDFItException(source, "BufferUnderflowException on 7z " +
                                                          "file with "+file.length()+" bytes", e);
                    }
                } else {
                    File temp = Files.createTempFile("rdfit", ".7z").toFile();
                    temp.deleteOnExit();
                    boolean failed = false;
                    try (FileOutputStream out = new FileOutputStream(temp);
                         InputStream in = ris.getInputStream()) {
                        IOUtils.copy(in, out);
                        SevenZFile sz = new SevenZFile(temp);
                        return new SevenZSourceIterator(source, sz).deleteOnClose(temp);
                    } catch (Throwable e) {
                        failed = true;
                        if (temp.exists() && !temp.delete())
                            logger.debug("Failed first deletion of temp 7z file {}.", temp);
                        return new RDFItException(source, "Cannot open 7z archives from " +
                                "sequential streams. Tried copying stream to a temp file"+temp+
                                ", but failed. (Try again providing a File or Path object " +
                                "instead)", e);
                    } finally {
                        if (failed && temp.exists() && !temp.delete())
                            logger.error("Failed to delete temp file {}", temp);
                    }
                }
            } else {
                BufferedInputStream bis = ris.getBufferedInputStream();
                ArchiveInputStream stream = archiveFactory.createArchiveInputStream(format, bis);
                return new ArchiveEntrySourceIterator(source, stream);
            }
        } catch (ArchiveException | IOException e) {
            return new RDFItException(source, e);
        }
    }

    protected @Nonnull Object openCompressed(@Nonnull String format,
                                             @Nonnull BufferedInputStream bis,
                                             @Nonnull Object source) {
        if (compressedFactory == null)
            compressedFactory = new CompressorStreamFactory();
        try {
            CompressorInputStream is = compressedFactory.createCompressorInputStream(format, bis);
            return new RDFInputStream(is);
        } catch (CompressorException e) {
            return new RDFItException(source, e);
        }
    }
}
