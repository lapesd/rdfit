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

package com.github.lapesd.rdfit.components.hdt.parsers.iterator;

import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.components.hdt.HDTParsers;
import com.github.lapesd.rdfit.components.normalizers.CoreSourceNormalizers;
import com.github.lapesd.rdfit.components.normalizers.DefaultSourceNormalizerRegistry;
import com.github.lapesd.rdfit.components.parsers.DefaultParserRegistry;
import com.github.lapesd.rdfit.errors.InconvertibleException;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.impl.DefaultRDFItFactory;
import com.github.lapesd.rdfit.iterator.RDFIt;
import com.github.lapesd.rdfit.listener.TripleListenerBase;
import com.github.lapesd.rdfit.source.RDFFile;
import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.source.syntax.RDFLangs;
import com.github.lapesd.rdfit.util.Utils;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.rdf.TripleWriter;
import org.rdfhdt.hdt.triples.TripleString;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class HDTItParserTest {
    private static final String EX = "http://example.org/";
    private static final String S1 = EX+"S1";
    private static final String P1 = EX+"P1";
    private static final String O1 = EX+"O1";
    private static final String S2 = EX+"S2";
    private static final String P2 = EX+"P2";
    private static final String O2 = EX+"O2";

    private static final List<File> tempFiles = new ArrayList<>();
    private DefaultRDFItFactory factory;

    @BeforeClass
    public void beforeClass() {
        factory = new DefaultRDFItFactory(new DefaultParserRegistry(),
                new DefaultConversionManager(),
                new DefaultSourceNormalizerRegistry());
        CoreSourceNormalizers.registerAll(factory);
        HDTParsers.registerAll(factory);
    }

    @AfterClass
    public void afterClass() {
        for (File file : tempFiles) {
            if (file.exists() && !file.delete())
                fail("Failed to delete file "+file);
        }
        tempFiles.clear();
    }

    private @Nonnull File createFile() throws IOException {
        File file = Files.createTempFile("rdfit-hdt", ".hdt").toFile();
        file.deleteOnExit();
        tempFiles.add(file);
        return file;
    }

    @DataProvider public Object[][] testData() throws Exception {
        File f = createFile();
        TripleWriter writer = HDTManager.getHDTWriter(f.getAbsolutePath(), f.toURI().toString(),
                                                      new HDTSpecification());
        writer.addTriple(new TripleString(S1, P1, O1));
        writer.addTriple(new TripleString(S2, P2, O2));
        writer.close();

        HDT hdt = HDTManager.generateHDT(asList(new TripleString(S1, P1, O1),
                                         new TripleString(S1, P2, O2)).iterator(),
                                         "file://nowhere.hdt", new HDTSpecification(),
                                         (level, message) -> {});
        List<TripleString> ex1 = asList(new TripleString(S1, P1, O1), new TripleString(S2, P2, O2));
        List<TripleString> ex2 = asList(new TripleString(S1, P1, O1), new TripleString(S1, P2, O2));

        return Stream.of(
                asList(f, ex1),
                asList(new RDFFile(f, RDFLangs.HDT), ex1),
                asList(new RDFFile(f), ex1),
                asList(f.toPath(), ex1),
                asList(f.toURI(), ex1),
                asList(f.toURI().toURL(), ex1),
                asList(f.toURI().toString(), ex1),
                asList(f.getAbsolutePath(), ex1),
                asList("file://"+f.getAbsolutePath().replaceAll(" ", "%20"), ex1),

                asList(hdt, ex2),

                asList(ex1, ex1),
                asList(new LinkedList<>(ex1), ex1),
                asList(ex1.toArray(new TripleString[0]), ex1)
        ).map(List::toArray).toArray(Object[][]::new);
    }

    private void assertTripleStringsEqual(@Nonnull List<TripleString> expected, List<TripleString> actual) {
        assertEquals(actual.size(), expected.size());
        for (int i = 0, size = actual.size(); i < size; i++) {
            TripleString a = actual.get(i), e = expected.get(i);
            assertEquals(a.getSubject().toString(), e.getSubject().toString());
            assertEquals(a.getPredicate().toString(), e.getPredicate().toString());
            assertEquals(a.getObject().toString(), e.getObject().toString());
        }
    }

    @Test(dataProvider = "testData")
    public void testIterate(Object source, @Nonnull List<TripleString> expected) {
        List<TripleString> actual = new ArrayList<>();
        try (RDFIt<TripleString> it = factory.iterateTriples(TripleString.class, source)) {
            while (it.hasNext())
                actual.add(it.next());
        }
        assertTripleStringsEqual(expected, actual);
    }

    @Test(dataProvider = "testData")
    public void testParse(Object source, @Nonnull List<TripleString> expected) {
        String expectedBaseIRI;
        if (source instanceof RDFInputStream) {
            expectedBaseIRI = ((RDFInputStream) source).getBaseIRI();
        } else if (source instanceof HDT || source instanceof Collection
                   || Object[].class.isAssignableFrom(source.getClass()))  {
            expectedBaseIRI = null;
        } else if (source instanceof URL) {
            expectedBaseIRI = Utils.toASCIIString((URL) source);
        } else if (source instanceof URI) {
            expectedBaseIRI = Utils.toASCIIString((URI) source);
        } else if (source instanceof String) {
            String s = source.toString();
            if (s.startsWith("file:"))
                expectedBaseIRI = s.replaceFirst("^file:/([^/])", "file:///$1");
            else
                expectedBaseIRI = "file://" + s;
        } else {
            expectedBaseIRI = source.toString();
            if (!expectedBaseIRI.matches("^(file|https?):.*"))
                expectedBaseIRI = "file://"+expectedBaseIRI;
        }


        List<TripleString> actual = new ArrayList<>();
        List<Throwable> exceptions = new ArrayList<>();
        List<String> baseIRIs = new ArrayList<>(), messages = new ArrayList<>();
        factory.parse(new TripleListenerBase<TripleString>(TripleString.class) {
            @Override public void triple(@Nonnull TripleString triple) {
                actual.add(triple);
            }

            @Override
            public boolean notifyInconvertibleTriple(@Nonnull InconvertibleException e) throws InterruptParsingException {
                exceptions.add(e);
                return true;
            }

            @Override
            public boolean notifyInconvertibleQuad(@Nonnull InconvertibleException e) throws InterruptParsingException {
                exceptions.add(e);
                return true;
            }

            @Override
            public boolean notifySourceError(@Nonnull RDFItException e) {
                exceptions.add(e);
                return true;
            }

            @Override public void start(@Nonnull Object source) {
                super.start(source);
                if (baseIRI != null)
                    exceptions.add(new AssertionError("baseIRI != null after start()"));
            }

            @Override public void baseIRI(@Nonnull String baseIRI) {
                super.baseIRI(baseIRI);
                baseIRIs.add(baseIRI);
            }

            @Override public void finish(@Nonnull Object source) {
                super.finish(source);
                if (baseIRI != null)
                    exceptions.add(new AssertionError("baseIRI != null after finish()"));
            }

            @Override public boolean notifyParseWarning(@Nonnull String message) {
                messages.add(message);
                return super.notifyParseWarning(message);
            }

            @Override public boolean notifyParseError(@Nonnull String message) {
                messages.add(message);
                return super.notifyParseError(message);
            }
        }, source);

        assertTripleStringsEqual(actual, expected);
        assertEquals(exceptions, emptyList());
        assertEquals(messages, emptyList());
        assertEquals(baseIRIs,
                     expectedBaseIRI == null ? emptyList() : singletonList(expectedBaseIRI));
    }

}