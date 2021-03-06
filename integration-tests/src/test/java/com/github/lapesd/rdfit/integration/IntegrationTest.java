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

import com.github.lapesd.rdfit.components.hdt.listeners.HDTImportingRDFListener;
import com.github.lapesd.rdfit.components.jena.listener.JenaImportingRDFListener;
import com.github.lapesd.rdfit.components.rdf4j.listener.RDF4JImportingRDFListener;
import com.github.lapesd.rdfit.impl.DefaultRDFItFactory;
import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.RIt;
import com.github.lapesd.rdfit.errors.InconvertibleException;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.integration.generators.FileGenerator;
import com.github.lapesd.rdfit.integration.generators.ListGenerator;
import com.github.lapesd.rdfit.iterator.RDFIt;
import com.github.lapesd.rdfit.listener.BaseImportingRDFListener;
import com.github.lapesd.rdfit.listener.RDFListener;
import com.github.lapesd.rdfit.listener.RDFListenerBase;
import com.github.lapesd.rdfit.util.NoSource;
import com.github.lapesd.rdfit.util.Utils;
import org.apache.jena.vocabulary.OWL2;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.*;

@SuppressWarnings("rawtypes")
public class IntegrationTest {
    private static final String EX = "http://example.org/";
    private static final String ONTO = EX+"onto.ttl";
    private List<RDFItFactory> factories;
    private List<Class<? extends BaseImportingRDFListener>> importingListenerClasses;
    private ExecutorService exec;

    @BeforeClass
    public void beforeClass() {
        RIt.init();
        factories = asList(DefaultRDFItFactory.get(), RIt.createFactory());
        importingListenerClasses = asList(JenaImportingRDFListener.class,
                HDTImportingRDFListener.class, RDF4JImportingRDFListener.class);
        int processors = Runtime.getRuntime().availableProcessors();
        exec = Executors.newFixedThreadPool(processors);
    }

    @AfterClass
    public void afterClass() throws InterruptedException {
        List<Runnable> list = exec.shutdownNow();
        assertEquals(list, Collections.emptyList());
        assertTrue(exec.awaitTermination(3, TimeUnit.SECONDS));
    }

    @DataProvider public static Object[][] testData() {
        return TestDataGenerator.generateTestData().stream().map(d -> new Object[]{d})
                                .toArray(Object[][]::new);
    }

    @DataProvider public static Object[][] fileTestData() {
        return TestDataGenerator.generateTestData(new FileGenerator()).stream()
                                .map(d -> new Object[]{d})
                                .toArray(Object[][]::new);
    }


    public static void check(@Nonnull Collection<?> actual, @Nonnull Collection<?> expected) {
        assertEquals(new HashSet<>(actual), new HashSet<>(expected));
        assertEquals(actual.size(), expected.size());
    }

    public void iterateTriplesTest(@Nonnull TestData data,
                                   @Nonnull BiFunction<Class<?>, Object, RDFIt<?>> function) {
        if (!data.isTripleOnly())
            return;
        List<?> expected = data.expectedTriples();
        List<Future<?>> futures = new ArrayList<>();
        for (Object input : data.generateInputs()) {
            futures.add(exec.submit(() -> {
                List<Object> actual = new ArrayList<>();
                function.apply(data.getTripleClass(), input).forEachRemaining(actual::add);
                check(actual, expected);
                return null;
            }));
        }
        try {
            for (Future<?> f : futures)
                f.get();
        } catch (Error|RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionError(e);
        } finally {
            data.cleanUp();
        }
    }
    public void iterateQuadsTest(@Nonnull TestData data,
                                 @Nonnull BiFunction<Class<?>, Object, RDFIt<?>> function) {
        if (!data.isQuadOnly())
            return;
        List<?> expected = data.expectedQuads();
        List<Future<?>> futures = new ArrayList<>();
        for (Object input : data.generateInputs()) {
            futures.add(exec.submit(() -> {
                List<Object> actual = new ArrayList<>();
                function.apply(data.getQuadClass(), input).forEachRemaining(actual::add);
                check(actual, expected);
                return null;
            }));
        }
        try {
            for (Future<?> f : futures) f.get();
        } catch (Error|RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionError(e);
        } finally {
            data.cleanUp();
        }
    }

    @Test(dataProvider = "testData")
    public void testFactoryIterateTriples(@Nonnull TestData data) {
        for (RDFItFactory factory : factories)
            iterateTriplesTest(data, (cls, in) -> factory.iterateTriples(cls, in));
    }
    @Test(dataProvider = "testData")
    public void testFactoryIterateQuads(@Nonnull TestData data) {
        for (RDFItFactory factory : factories)
            iterateQuadsTest(data, (cls, in) -> factory.iterateQuads(cls, in));
    }
    @Test(dataProvider = "testData")
    public void testRItIterateTriples(@Nonnull TestData data) {
        iterateTriplesTest(data, RIt::iterateTriples);
    }
    @Test(dataProvider = "testData")
    public void testRItIterateQuads(@Nonnull TestData data) {
        iterateQuadsTest(data, RIt::iterateQuads);
    }

    private static class Listener extends CollectingListener {
        public final TestData testData;

        public Listener(@Nonnull TestData testData) {
            //noinspection unchecked
            super((Class<Object>) testData.tripleClass, (Class<Object>) testData.quadClass);
            this.testData = testData;
        }

        public void check() {
            assertEquals(exceptions, Collections.emptyList());
            assertEquals(messages, Collections.emptyList());
            assertEquals(badCalls, Collections.emptyList());
            IntegrationTest.check(acTriples, testData.expectedTriples());
            IntegrationTest.check(acQuads, testData.expectedQuads());
        }

    }

    private void testParse(@Nonnull TestData data,
                           @Nonnull BiConsumer<RDFListener<?,?>, Object> parse) {
        List<Future<?>> futures = new ArrayList<>();
        for (Object input : data.generateInputs()) {
            futures.add(exec.submit(() -> {
                Listener listener = new Listener(data);
                parse.accept(listener, input);
                listener.check();
                return null;
            }));
        }
        try {
            for (Future<?> f : futures) f.get();
        } catch (Error|RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionError(e);
        } finally {
            data.cleanUp();
        }
    }

    @Test(dataProvider = "testData")
    public void testFactoryParse(@Nonnull TestData data) {
        for (RDFItFactory factory : factories)
            testParse(data, factory::parse);
    }

    @Test(dataProvider = "testData")
    public void testRItParse(@Nonnull TestData data) {
        testParse(data, RIt::parse);
    }

    private void testImportingParse(@Nonnull Class<? extends BaseImportingRDFListener> importingListenerClass,
                                    @Nonnull TestData data,
                                    @Nonnull BiConsumer<RDFListener<?,?>, Object> parse) {
        List<Future<?>> futures = new ArrayList<>();
        assertTrue(data.getGenerator() instanceof FileGenerator);
        for (Object input : data.generateInputs()) {
            String uri = null;
            if (input instanceof File)
                uri = ((File)input).toURI().toASCIIString();
            else if (input instanceof Path)
                uri = ((Path)input).toUri().toASCIIString();
            else if (input instanceof URL)
                uri = Utils.toASCIIString((URL) input);
            else if (input instanceof URI)
                uri = Utils.toASCIIString((URI) input);
            else if (input instanceof String && input.toString().startsWith("file:"))
                uri = (String)input;
            else if (input instanceof String)
                uri = new File((String) input).toURI().toASCIIString();
            else
                fail("Unexpected input type");
            String nt = "<"+ONTO+"> <"+ OWL2.imports.getURI() +"> <"+uri+">.\n";
            futures.add(exec.submit(() -> {
                Listener listener = new Listener(data);
                Constructor<? extends BaseImportingRDFListener> ct;
                ct = importingListenerClass.getConstructor(RDFListener.class);
                BaseImportingRDFListener importingListener = ct.newInstance(listener);
                parse.accept(importingListener, nt);

                //handle the extra triple that caused the import
                List<?> exTriples = data.expectedTriples();
                List<?> exQuads = data.expectedQuads();
                List<Object> extraTriples = listener.acTriples.stream()
                        .filter(t -> !exTriples.contains(t)).collect(toList());
                List<Object> extraQuads = listener.acQuads.stream()
                        .filter(t -> !exQuads.contains(t)).collect(toList());
                assertEquals(extraTriples.size() + extraQuads.size(), 1);
                assertTrue(Stream.concat(extraTriples.stream(), extraQuads.stream())
                                 .allMatch(t -> t.toString().contains("imports")));
                listener.acTriples.removeAll(extraTriples);
                listener.acQuads.removeAll(extraQuads);

                listener.check();
                return null;
            }));
        }
        try {
            for (Future<?> f : futures) f.get();
        } catch (Error|RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionError(e);
        } finally {
            data.cleanUp();
        }
    }

    @Test(dataProvider = "fileTestData")
    public void testFactoryImportingParse(@Nonnull TestData data) {
        for (Class<? extends BaseImportingRDFListener> importingListenerClass
                : importingListenerClasses) {
            for (RDFItFactory factory : factories)
                testImportingParse(importingListenerClass, data, factory::parse);
        }
    }

    @Test(dataProvider = "fileTestData")
    public void testRItImportingParse(@Nonnull TestData data) {
        for (Class<? extends BaseImportingRDFListener> importingListenerClass
                : importingListenerClasses) {
            testImportingParse(importingListenerClass, data, RIt::parse);
        }
    }

}
