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

package com.github.lapesd.rdfit.integration.generators;

import com.github.lapesd.rdfit.integration.TripleSet;
import com.github.lapesd.rdfit.source.syntax.RDFLangs;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.*;

import static java.nio.file.Files.createTempFile;
import static org.apache.commons.compress.archivers.ArchiveStreamFactory.TAR;
import static org.apache.commons.compress.archivers.ArchiveStreamFactory.ZIP;
import static org.apache.commons.compress.compressors.CompressorStreamFactory.GZIP;
import static org.apache.commons.compress.compressors.CompressorStreamFactory.ZSTANDARD;

@SuppressWarnings("SameParameterValue")
public class CompressedBytesGenerator implements SourceGenerator {
    private static final Logger logger = LoggerFactory.getLogger(CompressedBytesGenerator.class);
    private final CompressorStreamFactory CSF = new CompressorStreamFactory();
    private final ArchiveStreamFactory ASF = new ArchiveStreamFactory();

    @Override
    public @Nonnull List<?> generate(@Nonnull TripleSet tripleSet, @Nonnull File tempDir) {
        Set<Integer> doneHashes = new HashSet<>();
        List<byte[]> list = new ArrayList<>();
        for (ImmutablePair<byte[], RDFLang> p : new ByteGenerator().generateWithLang(tripleSet)) {
            if (!ByteGenerator.CAN_GUESS.contains(p.right))
                continue;
            list.add(createCompressed(p.left, GZIP));
            list.add(createCompressedTAR(tempDir, p.left, GZIP));
            list.add(createArchive(tempDir, p.left, ZIP));
            if (p.right.equals(RDFLangs.NT) && doneHashes.add(Arrays.hashCode(p.left))) {
                list.add(createSevenZip(tempDir, p.left));
                list.add(createCompressedTAR(tempDir, p.left, ZSTANDARD));
            }
        }
        return list;
    }

    private @Nonnull byte[] createCompressed(byte[] data, String name) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CompressorOutputStream cout = CSF.createCompressorOutputStream(name, out);
             ByteArrayInputStream in = new ByteArrayInputStream(data)) {
            IOUtils.copy(in, cout);
        } catch (IOException | CompressorException e) {
            throw new RuntimeException(e);
        }
        return out.toByteArray();
    }

    private @Nonnull byte[] createCompressedTAR(@Nonnull File tempDir, byte[] data,
                                                @Nonnull String name) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        File file = null;
        try (CompressorOutputStream cout = CSF.createCompressorOutputStream(name, out);
             ArchiveOutputStream aout = ASF.createArchiveOutputStream(TAR, cout);
             ByteArrayInputStream in = new ByteArrayInputStream(data)) {
            file = FileGenerator.extractFile(tempDir, data);
            aout.putArchiveEntry(aout.createArchiveEntry(file, "file"));
            IOUtils.copy(in, aout);
            aout.closeArchiveEntry();
        } catch (IOException|ArchiveException|CompressorException e) {
            throw new RuntimeException(e);
        } finally {
            if (file != null && file.exists() && !file.delete())
                logger.warn("Ignoring failure to delete {}", file);
        }
        return out.toByteArray();
    }

    private @Nonnull byte[] createArchive(@Nonnull File tempDir, byte[] data,
                                          @Nonnull String name) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        File file = null;
        try (ByteArrayInputStream in = new ByteArrayInputStream(data);
             ArchiveOutputStream cout = ASF.createArchiveOutputStream(name, out)) {
            file = FileGenerator.extractFile(tempDir, data);
            cout.putArchiveEntry(cout.createArchiveEntry(file, "file"));
            IOUtils.copy(in, cout);
            cout.closeArchiveEntry();
        } catch (IOException| ArchiveException e) {
            throw new RuntimeException(e);
        } finally {
            if (file != null && file.exists() && !file.delete())
                logger.warn("Ignoring failure to delete {}", file);
        }
        return out.toByteArray();
    }

    private byte[] createSevenZip(@Nonnull File tempDir, byte[] data) {
        File szf = null, file = null;
        try {
            szf = createTempFile(tempDir.toPath(), "rdfit", ".7z").toFile();
            try (ByteArrayInputStream in = new ByteArrayInputStream(data);
                 SevenZOutputFile szof = new SevenZOutputFile(szf)) {
                file = FileGenerator.extractFile(tempDir, data);
                szof.putArchiveEntry(szof.createArchiveEntry(file, "file"));
                IOUtils.copy(in, new OutputStream() {
                    @Override public void write(int b) throws IOException {
                        szof.write(b);
                    }
                });
                szof.closeArchiveEntry();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (FileInputStream in = new FileInputStream(szf)) {
                IOUtils.copy(in, out);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (szf != null && szf.exists() && !szf.delete())
                logger.warn("Failed to delete file szf={}", szf);
            if (file != null && file.exists() && !file.delete())
                logger.warn("Failed to delete file file={}", file);
        }
    }

    @Override public boolean isReusable() {
        return true;
    }
}
