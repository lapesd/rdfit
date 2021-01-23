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

package com.github.lapesd.rdfit.integration;

import com.github.lapesd.rdfit.RIt;
import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.components.converters.util.ConversionCache;
import com.github.lapesd.rdfit.components.converters.util.ConversionPathSingletonCache;
import com.github.lapesd.rdfit.components.hdt.HDTHelpers;
import com.github.lapesd.rdfit.components.jena.GraphFeeder;
import com.github.lapesd.rdfit.components.jena.JenaHelpers;
import com.github.lapesd.rdfit.components.rdf4j.RDF4JHelper;
import com.github.lapesd.rdfit.iterator.RDFIt;
import com.github.lapesd.rdfit.listener.RDFListener;
import com.github.lapesd.rdfit.source.RDFResource;
import com.github.lapesd.rdfit.util.NoSource;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.eclipse.rdf4j.model.Model;
import org.rdfhdt.hdt.triples.TripleString;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.github.lapesd.rdfit.components.converters.util.ConversionPathSingletonCache.createCache;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apache.jena.sparql.core.Quad.defaultGraphIRI;
import static org.testng.Assert.*;

public class TranscodeTest {
    private ExecutorService exec;

    @BeforeClass
    public void beforeClass() {
        RIt.init();
        int processors = Runtime.getRuntime().availableProcessors();
        exec = Executors.newFixedThreadPool(processors);
    }

    @AfterClass
    public void afterClass() throws Exception {
        List<Runnable> list = exec.shutdownNow();
        assertEquals(list, Collections.emptyList());
        assertTrue(exec.awaitTermination(3, TimeUnit.SECONDS));

    }

    @DataProvider public @Nonnull Object[][] iterateTranscodeData() {
        List<TestData> data = TestDataGenerator.generateTestData().stream()
                .filter(d -> d.getGenerator().isReusable())
                .filter(d -> d.isTripleOnly() || d.isQuadOnly())
                .collect(toList());
        List<List<Object>> rows = new ArrayList<>();
        for (TestData d : data) {
            rows.add(asList(d, (Function<RDFIt<?>, ?>)JenaHelpers::toGraph));
            rows.add(asList(d, (Function<RDFIt<?>, ?>)JenaHelpers::toGraphImporting));
            rows.add(asList(d, (Function<RDFIt<?>, ?>)JenaHelpers::toModel));
            rows.add(asList(d, (Function<RDFIt<?>, ?>)JenaHelpers::toModelImporting));
            rows.add(asList(d, (Function<RDFIt<?>, ?>) RDF4JHelper::toModel));
            rows.add(asList(d, (Function<RDFIt<?>, ?>) RDF4JHelper::toModelImporting));
            rows.add(asList(d, (Function<RDFIt<?>, ?>) HDTHelpers::toHDT));
        }
        return rows.stream().map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "iterateTranscodeData")
    public void testIterateTranscode(@Nonnull TestData testData,
                                     @Nonnull Function<RDFIt<?>, Object> transcoder) throws Exception {
        List<Future<?>> futures = new ArrayList<>();
        try {
            for (Object input : testData.generateInputs()) {
                futures.add(exec.submit(() -> {
                    Set<Object> expected = new HashSet<>(), actual = new HashSet<>();
                    if (testData.isTripleOnly()) {
                        Class<?> valueClass = testData.getTripleClass();
                        RIt.forEachTriple(Triple.class, expected::add, input);
                        Object transcoded = transcoder.apply(RIt.iterateTriples(valueClass, input));
                        RIt.forEachTriple(Triple.class, actual::add, transcoded);
                    } else if (testData.isQuadOnly()) {
                        Class<?> valueClass = testData.getQuadClass();
                        RIt.forEachQuad(Quad.class, expected::add, input);
                        Object transcoded = transcoder.apply(RIt.iterateQuads(valueClass, input));
                        RIt.forEachQuad(Quad.class, actual::add, transcoded);
                    } else {
                        fail("Unexpected TestData");
                    }
                    if (testData.isQuadOnly() && !actual.equals(expected)) {
                        expected = expected.stream()
                                           .map(q -> new Quad(defaultGraphIRI, ((Quad)q).asTriple()))
                                           .collect(Collectors.toSet());
                    }
                    assertEquals(actual.size(), expected.size());
                    assertEquals(actual, expected);
                    return null;
                }));
            }
            for (Future<?> f : futures)
                f.get(); //will rethrow AssertionErrors
        } finally {
            for (Future<?> f : futures) f.cancel(true);
            testData.cleanUp();
        }
    }

    @DataProvider public @Nonnull Object[][] listenerTranscodeData() {
        List<TestData> data = TestDataGenerator.generateTestData().stream()
                .filter(d -> d.getGenerator().isReusable())
                .filter(d -> d.isTripleOnly() || d.isQuadOnly())
                .collect(toList());
        List<List<Object>> rows = new ArrayList<>();
        for (TestData d : data) {
            rows.add(asList(d,
                    (Supplier<? extends RDFListener<?,?>>)GraphFeeder::new,
                    (Function<RDFListener<?,?>, Object>)o -> ((GraphFeeder)o).getGraph()));
        }
        return rows.stream().map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "listenerTranscodeData")
    public void testListenerTranscode(@Nonnull TestData testData,
                                      @Nonnull Supplier<RDFListener<?,?>> listenerSupplier,
                                      @Nonnull Function<RDFListener<?,?>, Object> resultGetter) throws Exception {
        List<Future<?>> futures = new ArrayList<>();
        try {
            for (Object input : testData.generateInputs()) {
                futures.add(exec.submit(() -> {
                    Set<Triple> expected = new HashSet<>(), actual = new HashSet<>();
                    RIt.forEachTriple(Triple.class, expected::add, input);

                    RDFListener<?, ?> listener = listenerSupplier.get();
                    RIt.parse(listener, input);
                    Object transcoded = resultGetter.apply(listener);

                    CollectingListener transL = new CollectingListener();
                    RIt.parse(transL, transcoded);
                    assertEquals(transL.exceptions, Collections.emptyList());
                    assertEquals(transL.messages, Collections.emptyList());

                    RIt.forEachTriple(Triple.class, actual::add, transL.acTriples, transL.acQuads);
                    assertEquals(actual, expected);
                    return null;
                }));
            }
            for (Future<?> f : futures)
                f.get(); //throws AssertionErrors

        } finally {
            for (Future<?> f : futures) {
                try { f.get(); } catch (Throwable ignored) {}
            }
            for (Future<?> f : futures) f.cancel(true);
            testData.cleanUp();
        }
    }

    @Test
    public void testRegressionHDT() {
        String path = "LargeRDFBench-tbox.hdt";
        List<TripleString> original = new ArrayList<>();
        RIt.forEachTriple(TripleString.class, original::add, new RDFResource(getClass(), path));
        Graph graph = JenaHelpers.toGraph(new RDFResource(getClass(), path));
        Model model = RDF4JHelper.toModel(new RDFResource(getClass(), path));

        assertTrue(graph.size() > 0);
        assertTrue(graph.size() > 100);
        assertEquals(graph.size(), model.size());
        assertEquals(original.size(), graph.size());

        ConversionCache jenaCache = createCache(DefaultConversionManager.get(), Triple.class);
        for (TripleString tripleString : original) {
            Triple t = (Triple) jenaCache.convert(NoSource.INSTANCE, tripleString);
            if (!t.getSubject().isBlank() && !t.getObject().isBlank())
                assertTrue(graph.contains(t));
        }
    }
}
