package com.github.lapesd.rdfit;

import com.github.lapesd.rdfit.errors.InconvertibleException;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.iterator.RDFIt;
import com.github.lapesd.rdfit.listener.RDFListener;
import com.github.lapesd.rdfit.listener.RDFListenerBase;
import com.github.lapesd.rdfit.util.NoSource;
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
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class IntegrationTest {
    private DefaultRDFItFactory factory;
    private ExecutorService exec;

    @BeforeClass
    public void beforeClass() {
        RIt.init();
        factory = DefaultRDFItFactory.get();
        exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
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
        iterateTriplesTest(data, (cls, in) -> factory.iterateTriples(cls, in));
    }
    @Test(dataProvider = "testData")
    public void testFactoryIterateQuads(@Nonnull TestData data) {
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

    private static class Listener extends RDFListenerBase<Object, Object> {
        public final List<Exception> exceptions = new ArrayList<>();
        public final List<String> messages = new ArrayList<>();
        public final List<String> badCalls = new ArrayList<>();
        public final List<Object> acTriples = new ArrayList<>(), acQuads = new ArrayList<>();
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

        @Override public void triple(@Nonnull Object triple) {
            if (tripleType() == null)
                badCalls.add("triple() called with null tripleClass");
            acTriples.add(triple);
        }

        @Override public void quad(@Nonnull Object quad) {
            if (quadType() == null)
                badCalls.add("quad() called with null quadClass");
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

        @Override public void finish(@Nonnull Object source) {
            if (!Objects.equals(this.source, source))
                badCalls.add("Mismatched finish("+source+"): expected "+this.source);
            super.finish(source);
        }

        @Override public void start(@Nonnull Object source) {
            if (!this.source.equals(NoSource.INSTANCE))
                badCalls.add("start("+source+"): did not receive finish("+this.source+")");
            super.start(source);
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
        testParse(data, (l, i) -> factory.parse(l, i));
    }

    @Test(dataProvider = "testData")
    public void testRItParse(@Nonnull TestData data) {
        testParse(data, RIt::parse);
    }
}
