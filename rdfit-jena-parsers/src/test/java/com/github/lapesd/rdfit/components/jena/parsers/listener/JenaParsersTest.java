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

package com.github.lapesd.rdfit.components.jena.parsers.listener;

import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.components.jena.JenaParsers;
import com.github.lapesd.rdfit.components.normalizers.CoreSourceNormalizers;
import com.github.lapesd.rdfit.components.normalizers.DefaultSourceNormalizerRegistry;
import com.github.lapesd.rdfit.components.parsers.DefaultParserRegistry;
import com.github.lapesd.rdfit.errors.InconvertibleException;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.impl.DefaultRDFItFactory;
import com.github.lapesd.rdfit.listener.RDFListenerBase;
import com.github.lapesd.rdfit.listener.TripleListenerBase;
import com.github.lapesd.rdfit.source.RDFFile;
import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.RDF;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.lapesd.rdfit.source.syntax.RDFLangs.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.rdf.model.ResourceFactory.*;
import static org.testng.Assert.*;

public class JenaParsersTest {
    private static final String EX = "http://example.org/";
    private static final Triple T1 = new Triple(createURI(EX+"S"), createURI(EX+"P"), createURI(EX+"O"));
    private static final Statement S1 = createStatement(createResource(EX+"S"),
                                                        createProperty(EX+"P"),
                                                        createResource(EX+"O"));
    private static final Quad Q1 = new Quad(Quad.defaultGraphIRI, createURI(EX+"S"),
                                            createURI(EX+"P"), createURI(EX+"O"));
    private static final Quad Q2 = new Quad(createURI(EX), createURI(EX+"S"),
                                            createURI(EX+"P"), createURI(EX+"O"));

    private final @Nonnull List<File> tempFiles = new ArrayList<>();
    private DefaultRDFItFactory factory;

    @BeforeClass
    public void beforeClass() {
        factory = new DefaultRDFItFactory(new DefaultParserRegistry(),
                new DefaultConversionManager(), new DefaultSourceNormalizerRegistry());
        factory.getParserRegistry().setConversionManager(factory.getConversionManager());
        JenaParsers.registerAll(factory);
        CoreSourceNormalizers.registerAll(factory);
    }

    @AfterClass
    public void afterClass() {
        for (File tempFile : tempFiles)
            assertTrue(tempFile.delete());
        tempFiles.clear();
    }

    private @Nonnull File toFile(@Nonnull String rdf) {
        try {
            File file = Files.createTempFile("rdfit", "").toFile();
            file.deleteOnExit();
            tempFiles.add(file);
            try (Writer w = new OutputStreamWriter(new FileOutputStream(file), UTF_8)) {
                w.write(rdf);
            }
            return file;
        } catch (IOException e) {
            throw new AssertionError("createTempFile() failed", e);
        }
    }

    private void addConverted(@Nonnull Collection<?> src, Class<?> dstCls,
                              @Nonnull Collection<Object> dst) {
        for (Object o : src) {
            if (dstCls.isInstance(o)) {
                dst.add(o);
            } else if (dstCls.equals(Quad.class)) {
                if (o instanceof Triple)
                    dst.add(new Quad(Quad.defaultGraphIRI, (Triple) o));
                else if (o instanceof Statement)
                    dst.add(new Quad(Quad.defaultGraphIRI, ((Statement) o).asTriple()));
                else
                    fail("Unexpected object class in src:"+o.getClass());
            } else if (dstCls.equals(Triple.class)) {
                if (o instanceof Statement)
                    dst.add(((Statement)o).asTriple());
                else if (o instanceof Quad)
                    dst.add(((Quad)o).asTriple());
                else
                    fail("Unexpected object class in src:"+o.getClass());
            } else if (dstCls.equals(Statement.class)) {
                if (!(o instanceof Quad) && !(o instanceof Triple))
                    fail("Unexpected object class in src:"+o.getClass());
                Triple t = o instanceof Triple ? (Triple) o : ((Quad)o).asTriple();
                Resource s = new ResourceImpl(t.getSubject(), null);
                Property p = new PropertyImpl(t.getPredicate(), null);
                RDFNode obj = t.getObject().isLiteral() ? new LiteralImpl(t.getObject(), null)
                            : new ResourceImpl(t.getObject(), null);
                dst.add(ResourceFactory.createStatement(s, p, obj));
            } else {
                fail("Unexpected dstCls="+dstCls);
            }
        }
    }

    private static void assertUnorderedEquals(@Nonnull Collection<?> ac, @Nonnull Collection<?> ex) {
        assertEquals(new HashSet<>(ac), new HashSet<>(ex));
        assertEquals(ac.size(), ex.size());
    }

    @DataProvider public @Nonnull Object[][] testData() {
        List<List<Object>> stubs = asList(
                asList("<"+EX+"S> <"+EX+"P> <"+EX+"O>.", NT, singletonList(T1), emptyList()),
                asList("@prefix : <"+EX+">.\n :S :P :O.\n", TTL, singletonList(T1), emptyList()),
                asList("@base <"+EX+">.\n <S> <P> <O>.\n", TTL, singletonList(T1), emptyList()),
                asList("<"+EX+"S> <"+EX+"P> <"+EX+"O> <"+EX+">.", NQ, emptyList(), singletonList(Q2)),
                asList("@prefix e: <"+EX+">.\n GRAPH e: { e:S e:P e:O }.\n", TRIG, emptyList(), singletonList(Q2)),
                asList("@base <"+EX+">.\n GRAPH <> { <S> <P> <O> }.\n", TRIG, emptyList(), singletonList(Q2)),
                asList("<"+EX+"S> <"+EX+"P> <"+EX+"O>.\n" +
                       "<"+EX+"S> <"+EX+"P> <"+EX+"O> <"+EX+">.", NQ,
                       singletonList(T1), singletonList(Q2))
        );
        List<BiFunction<String, RDFLang, Object>> inputFactories = asList(
                (s, l) -> (Supplier<?>)() -> new RDFInputStream(new ByteArrayInputStream(s.getBytes(UTF_8)), l),
                (s, l) -> (Supplier<?>)() -> new RDFInputStream(new ByteArrayInputStream(s.getBytes(UTF_8)), l, EX+"base"),
                (s, l) -> (Supplier<?>)() -> new RDFInputStream(new ByteArrayInputStream(s.getBytes(UTF_8))),
                (s, l) -> (Supplier<?>)() -> new ByteArrayInputStream(s.getBytes(UTF_8)),
                (s, l) -> s.getBytes(UTF_8),
                (s, l) -> toFile(s),
                (s, l) -> (Supplier<?>)() -> new RDFFile(toFile(s)),
                (s, l) -> (Supplier<?>)() -> new RDFFile(toFile(s), l),
                (s, l) -> toFile(s).toPath(),
                (s, l) -> toFile(s).getAbsolutePath(),
                (s, l) -> "file://"+toFile(s).getAbsolutePath().replaceAll(" ", "%20"),
                (s, l) -> toFile(s).toURI(),
                (s, l) -> toFile(s).toURI().toString(),
                (s, l) -> toFile(s).toURI().toASCIIString(),
                (s, l) -> {
                    try {
                        return toFile(s).toURI().toURL();
                    } catch (MalformedURLException e) {
                        throw new AssertionError("unexpected exception", e);
                    }
                }
        );
        Object nil = new Object();
        List<Object> tripleClasses = asList(Triple.class, Statement.class, nil);
        List<Object> quadClasses = asList(Quad.class, nil);

        List<List<Object>> rows = new ArrayList<>();
        for (List<Object> stub : stubs) {
            for (BiFunction<String, RDFLang, Object> inputFactory : inputFactories) {
                Object input = inputFactory.apply((String) stub.get(0), (RDFLang) stub.get(1));
                for (List<Object> ts : Lists.cartesianProduct(tripleClasses, quadClasses)) {
                    Class<?> tCls = nil.equals(ts.get(0)) ? null : (Class<?>) ts.get(0);
                    Class<?> qCls = nil.equals(ts.get(1)) ? null : (Class<?>) ts.get(1);
                    if (tCls == null && qCls == null)
                        continue;
                    List<Object> exTriples = new ArrayList<>(), exQuads = new ArrayList<>();
                    if (tCls == null)
                        addConverted((Collection<?>)stub.get(2), qCls, exQuads);
                    else
                        addConverted((Collection<?>)stub.get(2), tCls, exTriples);
                    if (qCls == null)
                        addConverted((Collection<?>)stub.get(3), tCls, exTriples);
                    else
                        addConverted((Collection<?>)stub.get(3), qCls, exQuads);
                    rows.add(asList(input, tCls, qCls, exTriples, exQuads));
                }
            }
        }
        return rows.stream().map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "testData")
    public void test(@Nonnull Object input, @Nonnull Class<?> tripleCls, @Nonnull Class<?> quadCls,
                     @Nonnull Collection<?> exTriples,
                     @Nonnull Collection<?> exQuads) throws Exception {
        if (input instanceof Supplier) {
            input = ((Supplier<?>) input).get();
            if (!(input instanceof RDFInputStream)) {
                if (input instanceof AutoCloseable)
                    ((AutoCloseable)input).close();
                return;
            }
        } else {
            return;
        }

        JenaInputStreamParser parser = new JenaInputStreamParser();
        assertTrue(parser.canParse(input));
        List<Object> acTriples = new ArrayList<>(), acQuads = new ArrayList<>();
        List<Exception> exceptions = new ArrayList<>();
        List<String> messages = new ArrayList<>();

        //noinspection unchecked
        parser.parse(input, new RDFListenerBase<Object, Object>(
                (Class<Object>) tripleCls, (Class<Object>) quadCls) {
            @Override public void triple(@Nonnull Object triple) {
                acTriples.add(triple);
            }

            @Override public void quad(@Nonnull Object quad) {
                acQuads.add(quad);
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
        });

        assertEquals(exceptions, emptyList());
        assertEquals(messages, emptyList());
        assertUnorderedEquals(acTriples, exTriples);
        assertUnorderedEquals(acQuads, exQuads);
    }


    @DataProvider public @Nonnull Object[][] testParsePrefixData() {
        return Stream.of(
                RDFFormat.TURTLE_PRETTY, RDFFormat.RDFXML_PRETTY, RDFFormat.JSONLD_PRETTY
        ).map(l -> new Object[]{l}).toArray(Object[][]::new);
    }

    @Test(dataProvider = "testParsePrefixData")
    public void testParsePrefix(RDFFormat format) throws IOException {
        Graph g = GraphFactory.createDefaultGraph();
        g.getPrefixMapping().setNsPrefix("ex", EX);
        g.add(T1);

        byte[] data;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            RDFDataMgr.write(out, g, format);
            data = out.toByteArray();
        }
        JenaInputStreamParser parser = new JenaInputStreamParser();
        Map<String, String> actual = new HashMap<>(), expected = new HashMap<>();
        List<Throwable> exceptions = new ArrayList<>();
        expected.put("ex", EX);
        if (Lang.RDFXML.equals(format.getLang()))
            expected.put("rdf", RDF.getURI());

        parser.parse(new RDFInputStream(new ByteArrayInputStream(data)),
                     new TripleListenerBase<Triple>(Triple.class) {
            @Override public void triple(@Nonnull Triple triple) { }

            @Override public void prefix(@Nonnull String label, @Nonnull String iriPrefix) {
                if (actual.put(label, iriPrefix) != null)
                    exceptions.add(new AssertionError("multiple prefixes "+label));
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
                exceptions.add(new AssertionError("Unexpected warning: "+message));
                return super.notifyParseWarning(message);
            }

            @Override public boolean notifyParseError(@Nonnull String message) {
                exceptions.add(new AssertionError("Unexpected error: "+message));
                return super.notifyParseError(message);
            }
        });

        assertEquals(actual, expected);
        assertEquals(exceptions, emptyList());
    }

    @Test(dataProvider = "testData")
    public void testFactory(@Nonnull Object input, @Nonnull Class<?> tCls, @Nonnull Class<?> qCls,
                            @Nonnull Collection<?> exTriples, @Nonnull Collection<?> exQuads) {
        List<Object> acTriples = new ArrayList<>(), acQuads = new ArrayList<>();
        List<Exception> exceptions = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        //noinspection unchecked
        factory.parse(new RDFListenerBase<Object, Object>(
                (Class<Object>) tCls, (Class<Object>) qCls) {
            @Override public void triple(@Nonnull Object triple) {
                acTriples.add(triple);
            }
            @Override public void quad(@Nonnull Object quad) {
                acQuads.add(quad);
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

            @Override public boolean notifyParseWarning(@Nonnull String message) {
                messages.add(message);
                return super.notifyParseWarning(message);
            }

            @Override public boolean notifyParseError(@Nonnull String message) {
                messages.add(message);
                return super.notifyParseError(message);
            }
        }, input);

        assertEquals(exceptions, Collections.emptyList());
        assertEquals(messages, Collections.emptyList());
        assertUnorderedEquals(acTriples, exTriples);
        assertUnorderedEquals(acQuads, exQuads);
    }

    @Test(dataProvider = "testData")
    public void testIterateTriples(@Nonnull Object input, @Nullable Class<?> tripleCls,
                                   @Nullable Class<?> quadCls, @Nonnull Collection<?> exTriples,
                                   @Nonnull Collection<?> exQuads) {
        if (tripleCls == null)
            return;
        List<Object> actual = new ArrayList<>(), expected = new ArrayList<>(exTriples);
        addConverted(exQuads, tripleCls, expected);
        factory.iterateTriples(tripleCls, input).forEachRemaining(actual::add);
        assertUnorderedEquals(actual, expected);
    }

    @Test(dataProvider = "testData")
    public void testIterateQuads(@Nonnull Object input, @Nullable Class<?> tripleCls,
                                 @Nullable Class<?> quadCls, @Nonnull Collection<?> exTriples,
                                 @Nonnull Collection<?> exQuads) {
        if (quadCls == null)
            return;
        List<Object> actual = new ArrayList<>(), expected = new ArrayList<>(exQuads);
        addConverted(exTriples, quadCls, expected);
        factory.iterateQuads(quadCls, input).forEachRemaining(actual::add);
        assertUnorderedEquals(actual, expected);
    }

}