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

package com.github.lapesd.rdfit.components.hdt;

import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.RIt;
import com.github.lapesd.rdfit.components.hdt.listeners.HDTBufferFeeder;
import com.github.lapesd.rdfit.components.hdt.listeners.HDTFileFeeder;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.impl.DefaultRDFItFactory;
import com.github.lapesd.rdfit.iterator.ConvertingRDFIt;
import com.github.lapesd.rdfit.iterator.RDFIt;
import com.github.lapesd.rdfit.util.NoSource;
import com.github.lapesd.rdfit.util.Utils;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.rdf.TripleWriter;
import org.rdfhdt.hdt.triples.TripleString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class HDTHelpers {
    private static final Logger logger = LoggerFactory.getLogger(HDTHelpers.class);
    private static final HDTSpecification SPEC = new HDTSpecification();
    private static final Pattern INDEX_SUFFIX = Pattern.compile("\\.index(\\.v\\d+-\\d+)?$");

    public static @Nonnull File toHDTFile(@Nonnull File file,
                                   @Nonnull RDFIt<TripleString> rdfIt) throws RDFItException {
        String baseURI = Utils.toASCIIString(file.toURI());
        try (RDFIt<TripleString> it = rdfIt;
             TripleWriter w = HDTManager.getHDTWriter(file.getAbsolutePath(), baseURI, SPEC)) {
            while (it.hasNext())
                w.addTriple(it.next());
        } catch (IOException|RuntimeException|Error e) {
            if (file.exists() && !file.delete())
                logger.warn("Failed to delete {} after {}({})", file, e.getClass(), e.getMessage());
            throw new RDFItException(NoSource.INSTANCE, e);
        } catch (Exception e) {
            String message = "Failed to close TripleWriter over " + file;
            throw new RDFItException(NoSource.INSTANCE, message, e);
        }
        return file;
    }
    public static @Nonnull File toHDTFile(@Nonnull File file,
                                          @Nonnull Object... srcs) throws RDFItException {
        return toHDTFile(DefaultRDFItFactory.get(), file, srcs);
    }
    public static @Nonnull File toHDTFile(@Nonnull RDFItFactory factory, @Nonnull File file,
                                          @Nonnull Object... srcs) throws RDFItException {
        if (srcs.length == 1 && srcs[0] instanceof RDFIt)
            return toHDTFile(file, ConvertingRDFIt.createIf(TripleString.class, (RDFIt<?>)srcs[0]));
        return toHDTFile(file, factory.iterateTriples(TripleString.class, srcs));
    }

    public static @Nonnull HDT
    toHDT(@Nonnull RDFIt<TripleString> it) throws RDFItException {
        String baseURI = "file:hdt-in-memory-"+ UUID.randomUUID();
        try {
            return HDTManager.generateHDT(it, baseURI, SPEC,
                                          (l, m) -> logger.debug("{}: {}: {}", baseURI, l, m));
        } catch (IOException e) {
            throw new RDFItException(NoSource.INSTANCE, "IOException writing to memory", e);
        } catch (ParserException e) {
            throw new RDFItException(NoSource.INSTANCE, "HDT refused some parsed triples", e);
        }
    }
    public static @Nonnull HDT toHDT(@Nonnull Object... sources) throws RDFItException {
        return toHDT(DefaultRDFItFactory.get(), sources);
    }
    public static @Nonnull HDT toHDT(@Nonnull RDFItFactory factory,
                                     @Nonnull Object... sources) throws RDFItException {
        if (sources.length == 1 && sources[0] instanceof RDFIt)
            return toHDT(ConvertingRDFIt.createIf(TripleString.class, (RDFIt<?>) sources[0]));
        return toHDT(factory.iterateTriples(TripleString.class, sources));
    }

    public static @Nonnull HDTFileFeeder fileFeeder(@Nonnull File file) throws IOException {
        return new HDTFileFeeder(file);
    }
    public static @Nonnull HDTBufferFeeder feeder() throws IOException {
        return new HDTBufferFeeder();
    }

    public static boolean deleteWithIndex(@Nonnull File hdtFile) {
        try {
            forceDeleteWithIndex(hdtFile);
            return true;
        } catch (IOException e) { return false; }
    }

    public static void forceDeleteWithIndex(@Nonnull File hdtFile) throws IOException {
        List<File> failed = null;
        if (hdtFile.exists() && !hdtFile.delete())
            (failed = new ArrayList<>()).add(hdtFile);
        for (File index : getIndexFiles(hdtFile)) {
            if (!index.delete())
                (failed == null ? failed = new ArrayList<>() : failed).add(index);
        }
        if (failed != null)
            throw new IOException("Failed to remove these files "+failed);
    }

    private static @Nonnull File[] getIndexFiles(@Nonnull File hdtFile) {
        File[] files = hdtFile.getParentFile().listFiles((d, n) -> n.startsWith(hdtFile.getName())
                && INDEX_SUFFIX.matcher(n).find());
        return files == null ? new File[0] : files;
    }

}
