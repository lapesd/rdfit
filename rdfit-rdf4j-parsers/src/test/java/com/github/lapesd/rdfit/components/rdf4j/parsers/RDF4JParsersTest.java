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

package com.github.lapesd.rdfit.components.rdf4j.parsers;

import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.components.normalizers.DefaultSourceNormalizerRegistry;
import com.github.lapesd.rdfit.components.parsers.DefaultParserRegistry;
import com.github.lapesd.rdfit.components.rdf4j.RDF4JParsers;
import com.github.lapesd.rdfit.errors.InconvertibleException;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.impl.DefaultRDFItFactory;
import com.github.lapesd.rdfit.iterator.RDFIt;
import com.github.lapesd.rdfit.listener.RDFListenerBase;
import com.github.lapesd.rdfit.listener.TripleListenerBase;
import com.github.lapesd.rdfit.source.RDFBytesInputStream;
import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.source.syntax.RDFLangs;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.*;

public class RDF4JParsersTest {
    private static final String EX = "http://example.org/";
    private static final SimpleValueFactory VF = SimpleValueFactory.getInstance();
    private static final Resource S1 = VF.createIRI(EX+"S1");
    private static final IRI P1 = VF.createIRI(EX+"P1");
    private static final Resource O1 = VF.createIRI(EX+"O1");
    private static final Resource G1 = VF.createIRI(EX+"G1");
    private static final Statement T1 = VF.createStatement(S1, P1, O1);
    private static final Statement Q1 = VF.createStatement(S1, P1, O1, G1);
    private final RDFItFactory factory;

    public RDF4JParsersTest() {
        factory = new DefaultRDFItFactory(new DefaultParserRegistry(),
                                          new DefaultConversionManager(),
                                          new DefaultSourceNormalizerRegistry());
        factory.getParserRegistry().setConversionManager(factory.getConversionManager());
    }

    @BeforeClass
    public void beforeClass() {
        RDF4JParsers.registerAll(factory);
    }

    private static void split(@Nonnull Collection<Statement> collection,
                              @Nonnull Collection<Statement> triples,
                              @Nonnull Collection<Statement> quads) {
        for (Statement s : collection)
            (s.getContext() == null ? triples : quads).add(s);
    }

    private void assertEqualTriples(@Nonnull Collection<Statement> actual,
                                   @Nonnull Collection<Statement> expected) {
        ArrayList<Statement> ac = new ArrayList<>(), ex = new ArrayList<>();
        SimpleValueFactory vf = SimpleValueFactory.getInstance();
        for (Statement s : actual)
            ac.add(vf.createStatement(s.getSubject(), s.getPredicate(), s.getObject()));
        for (Statement s : expected)
            ex.add(vf.createStatement(s.getSubject(), s.getPredicate(), s.getObject()));
        assertEquals(new HashSet<>(ac), new HashSet<>(ex));
        assertEquals(ac.size(), ex.size());
    }

    private void assertEqualStatements(@Nonnull Collection<Statement> actual,
                                       @Nonnull Collection<Statement> expected) {
        assertEquals(new HashSet<>(actual), new HashSet<>(expected));
        assertEquals(actual.size(), expected.size());
    }

    @DataProvider public static Object[][] testData() {
        List<List<Object>> rows = new ArrayList<>();
        for (RDFLang lang : RDFLangs.getLangs()) {
            RDFFormat format = RDF4JFormat.toRDF4J(lang);
            if (format == null) continue;
            List<Statement> ex = new ArrayList<>(singletonList(T1));
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                RDFWriter w = Rio.createWriter(format, out, EX + "base");
                Rio.createParser(format); // will throw if cannot parse lang
                w.startRDF();
                w.handleStatement(T1);
                if (format.supportsContexts()) {
                    w.handleStatement(Q1);
                    ex.add(Q1);
                }
                w.endRDF();
                out.flush();
                byte[] bytes = out.toByteArray();
                RDFBytesInputStream ris = new RDFBytesInputStream(bytes, lang, EX + "base");
                rows.add(asList(ris, ex));
            } catch (IOException|URISyntaxException e) {
                fail("Unexpected exception", e);
            } catch (UnsupportedRDFormatException ignored) { }
        }

        //sanity: check presence of most important languages
        List<RDFLang> langs = rows.stream().map(r -> ((RDFInputStream) r.get(0)).getLang())
                                           .collect(toList());
        assertFalse(langs.contains(RDFLangs.UNKNOWN));
        assertFalse(langs.contains(null));
        assertTrue(langs.contains(RDFLangs.RDFXML));
        assertTrue(langs.contains(RDFLangs.JSONLD));
        assertTrue(langs.contains(RDFLangs.TRIG));
        assertTrue(langs.contains(RDFLangs.NQ));

        List<List<Object>> more = new ArrayList<>();
        for (List<Object> row : rows) {
            RDFBytesInputStream ris = (RDFBytesInputStream) row.get(0);
            RDFLang lang = ris.getLang();
            more.add(asList(new RDFBytesInputStream(ris.getData(), lang), row.get(1)));
            if (RDFLangs.RDFJSON.equals(lang) || RDFLangs.BRF.equals(lang))
                continue;
            more.add(asList(new RDFBytesInputStream(ris.getData()      ), row.get(1)));
        }
        rows.addAll(more);

        return rows.stream().map(List::toArray).toArray(Object[][]::new);
    }

    @Test
    public void testParsePrefixes() throws IOException {
        byte[] data;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
                writer.write("@prefix ex: <" + EX + ">.\nex:S1 ex:P1 ex:O1.\n");
            }
            data = out.toByteArray();
        }
        List<Statement> triples = new ArrayList<>();
        List<Throwable> exceptions = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        Map<String, String> actual = new HashMap<>(), expected = new HashMap<>();
        expected.put("ex", EX);
        factory.parse(new TripleListenerBase<Statement>(Statement.class) {
            @Override public void triple(@Nonnull Statement triple) {
                triples.add(triple);
            }

            @Override public void prefix(@Nonnull String prefixLabel, @Nonnull String iriPrefix) {
                if (actual.put(prefixLabel, iriPrefix) != null)
                    exceptions.add(new AssertionError("Already had "+prefixLabel));
            }

            @Override
            public boolean notifyInconvertibleTriple(@Nonnull InconvertibleException e) throws InterruptParsingException {
                exceptions.add(e);
                return super.notifyInconvertibleTriple(e);
            }

            @Override
            public boolean notifyInconvertibleQuad(@Nonnull InconvertibleException e) throws InterruptParsingException {
                exceptions.add(e);
                return super.notifyInconvertibleQuad(e);
            }

            @Override public boolean notifySourceError(@Nonnull RDFItException e) {
                exceptions.add(e);
                return super.notifySourceError(e);
            }

            @Override public boolean notifyParseWarning(@Nonnull String message) {
                messages.add(message);
                return super.notifyParseWarning(message);
            }

            @Override public boolean notifyParseError(@Nonnull String message) {
                messages.add(message);
                return super.notifyParseError(message);
            }
        }, new RDFInputStream(new ByteArrayInputStream(data)));

        assertEquals(triples, singletonList(T1));
        assertEquals(exceptions, emptyList());
        assertEquals(messages, emptyList());
        assertEquals(actual, expected);
    }

    @Test(dataProvider = "testData")
    public void testIterateTriples(@Nonnull Object source,
                                   @Nonnull Collection<Statement> expected) {
        List<Statement> ac = new ArrayList<>();
        try (RDFIt<Statement> it = factory.iterateTriples(Statement.class, source)) {
            it.forEachRemaining(ac::add);
        }
        assertEqualTriples(ac, expected);
    }

    @Test(dataProvider = "testData")
    public void testIterateQuads(@Nonnull Object source,
                                 @Nonnull Collection<Statement> expected) {
        List<Statement> ac = new ArrayList<>();
        try (RDFIt<Statement> it = factory.iterateQuads(Statement.class, source)) {
            it.forEachRemaining(ac::add);
        }
        assertEqualStatements(ac, expected);
    }

    @Test(dataProvider = "testData")
    public void testParse(@Nonnull Object source,
                          @Nonnull Collection<Statement> expected) {
        List<Statement> triples = new ArrayList<>(), quads = new ArrayList<>();
        List<Throwable> exceptions = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<String> baseIRIs = new ArrayList<>();
        factory.parse(new RDFListenerBase<Statement, Statement>(Statement.class, Statement.class) {
            @Override public void triple(@Nonnull Statement triple) {
                triples.add(triple);
            }

            @Override public void quad(@Nonnull Statement quad) {
                quads.add(quad);
            }

            @Override public void baseIRI(@Nonnull String baseIRI) {
                super.baseIRI(baseIRI);
                baseIRIs.add(baseIRI);
            }

            @Override public void finish(@Nonnull Object source) {
                super.finish(source);
                if (baseIRI != null)
                    exceptions.add(new AssertionError("baseIRI != null after finish"));
            }

            @Override
            public boolean notifyInconvertibleTriple(@Nonnull InconvertibleException e) throws InterruptParsingException {
                exceptions.add(e);
                return super.notifyInconvertibleTriple(e);
            }

            @Override
            public boolean notifyInconvertibleQuad(@Nonnull InconvertibleException e) throws InterruptParsingException {
                exceptions.add(e);
                return super.notifyInconvertibleQuad(e);
            }

            @Override public boolean notifyParseWarning(@Nonnull String message) {
                errors.add(message);
                return super.notifyParseWarning(message);
            }

            @Override public boolean notifyParseError(@Nonnull String message) {
                errors.add(message);
                return super.notifyParseError(message);
            }

            @Override public boolean notifySourceError(@Nonnull RDFItException e) {
                exceptions.add(e);
                return super.notifySourceError(e);
            }
        }, source);

        assertEquals(exceptions, emptyList());
        assertEquals(errors, emptyList());
        List<Statement> exTriples = new ArrayList<>(), exQuads = new ArrayList<>();
        split(expected, exTriples, exQuads);
        assertEqualStatements(triples, exTriples);
        assertEqualStatements(quads, exQuads);

        String expectedBase = null;
        if (source instanceof RDFInputStream)
            expectedBase = ((RDFInputStream)source).getBaseIRI();
        assertEquals(baseIRIs, expectedBase == null ? emptyList() : singletonList(expectedBase));
    }

    @Test(dataProvider = "testData")
    public void testParseTriples(@Nonnull Object source,
                                 @Nonnull Collection<Statement> expected) {
        List<Statement> triples = new ArrayList<>();
        List<Exception> exceptions = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        factory.parse(new TripleListenerBase<Statement>(Statement.class) {
            @Override public void triple(@Nonnull Statement triple) {
                triples.add(triple);
            }

            @Override
            public boolean notifyInconvertibleTriple(@Nonnull InconvertibleException e) throws InterruptParsingException {
                exceptions.add(e);
                return super.notifyInconvertibleTriple(e);
            }

            @Override
            public boolean notifyInconvertibleQuad(@Nonnull InconvertibleException e) throws InterruptParsingException {
                exceptions.add(e);
                return super.notifyInconvertibleQuad(e);
            }

            @Override public boolean notifyParseWarning(@Nonnull String message) {
                errors.add(message);
                return super.notifyParseWarning(message);
            }

            @Override public boolean notifyParseError(@Nonnull String message) {
                errors.add(message);
                return super.notifyParseError(message);
            }

            @Override public boolean notifySourceError(@Nonnull RDFItException e) {
                exceptions.add(e);
                return super.notifySourceError(e);
            }
        }, source);

        assertEquals(exceptions, emptyList());
        assertEquals(errors, emptyList());
        assertEqualStatements(triples, expected);
    }

}