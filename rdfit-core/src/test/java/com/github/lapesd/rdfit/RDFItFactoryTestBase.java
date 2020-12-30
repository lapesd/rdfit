package com.github.lapesd.rdfit;

import com.github.lapesd.rdfit.components.converters.quad.QuadLifter;
import com.github.lapesd.rdfit.data.ModelLib;
import com.github.lapesd.rdfit.data.QuadMock1;
import com.github.lapesd.rdfit.data.QuadMock3;
import com.github.lapesd.rdfit.data.TripleMock1;
import com.github.lapesd.rdfit.errors.InconvertibleException;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.NoParserException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.iterator.Ex;
import com.github.lapesd.rdfit.iterator.RDFIt;
import com.github.lapesd.rdfit.listener.RDFListenerBase;
import com.github.lapesd.rdfit.listener.TripleListenerBase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.github.lapesd.rdfit.data.MockFactory.*;
import static com.github.lapesd.rdfit.data.MockHelpers.quadLifter;
import static java.util.Arrays.asList;
import static org.testng.Assert.*;

public abstract class RDFItFactoryTestBase {
    protected abstract @Nonnull RDFItFactory createFactoryOnlyCbParsers();
    protected abstract @Nonnull RDFItFactory createFactoryOnlyItParsers();
    protected abstract @Nonnull RDFItFactory createFactoryAllParsers();

    @DataProvider public @Nonnull Object[][] elementClassPairs() {
        return TRIPLE_CLASSES.stream().flatMap(t ->
                QUAD_CLASSES.stream().map(q -> asList(t, q))
        ).map(List::toArray).toArray(Object[][]::new);
    }

    private void runTestIterateTriplesNoConversion(@Nonnull RDFItFactory factory,
                                                  @Nonnull Class<?> tripleClass) {
        Object t1 = createTriple(tripleClass, Ex.S1, Ex.P1, Ex.O1);
        Object t2 = createTriple(tripleClass, Ex.S2, Ex.P2, Ex.O2);
        List<Object> expected = asList(t1, t2);
        ModelLib.Model source = ModelLib.getModel(expected);

        ArrayList<Object> actual = new ArrayList<>();
        try (RDFIt<?> it = factory.iterateTriples(tripleClass, source)) {
            while (it.hasNext())
                actual.add(it.next());
        }
        assertEquals(actual, expected);
    }

    @Test(dataProvider = "elementClassPairs")
    public void testIterateTriplesAllParsersNoConversion(@Nonnull Class<?> tripleClass, @Nonnull Class<?> ignored) {
        runTestIterateTriplesNoConversion(createFactoryAllParsers(), tripleClass);
    }
    @Test(dataProvider = "elementClassPairs")
    public void testIterateTriplesItParsersNoConversion(@Nonnull Class<?> tripleClass, @Nonnull Class<?> ignored) {
        runTestIterateTriplesNoConversion(createFactoryOnlyItParsers(), tripleClass);
    }
    @Test(dataProvider = "elementClassPairs")
    public void testIterateTriplesCbParsersNoConversion(@Nonnull Class<?> tripleClass, @Nonnull Class<?> ignored) {
        runTestIterateTriplesNoConversion(createFactoryOnlyCbParsers(), tripleClass);
    }

    private void runTestIterateTriplesDowngrading(@Nonnull RDFItFactory factory,
                                                  @Nonnull Class<?> tripleClass,
                                                  @Nonnull Class<?> quadClass) {
        ModelLib.Model source = ModelLib.getModel(asList(
                createQuad(quadClass, Ex.G1, Ex.S1, Ex.P1, Ex.O1),
                createQuad(quadClass, Ex.G2, Ex.S2, Ex.P2, Ex.O2)));

        List<Object> expected = asList(createTriple(tripleClass, Ex.S1, Ex.P1, Ex.O1),
                                       createTriple(tripleClass, Ex.S2, Ex.P2, Ex.O2));

        ArrayList<Object> actual = new ArrayList<>();
        try (RDFIt<?> it = factory.iterateTriples(tripleClass, source)) {
            if (quadClass.equals(QuadMock3.class)) {
                // QuadMock3 is a dead-end
                expectThrows(InconvertibleException.class, () -> assertFalse(it.hasNext()));
                return;
            } else {
                while (it.hasNext())
                    actual.add(it.next());
            }
        }
        assertEquals(actual, expected);
    }

    @Test(dataProvider = "elementClassPairs")
    public void testIterateTriplesAllParsersDowngrading(@Nonnull Class<?> tripleClass, @Nonnull Class<?> quadClass) {
        runTestIterateTriplesDowngrading(createFactoryAllParsers(), tripleClass, quadClass);
    }
    @Test(dataProvider = "elementClassPairs")
    public void testIterateTriplesItParsersDowngrading(@Nonnull Class<?> tripleClass, @Nonnull Class<?> quadClass) {
        runTestIterateTriplesDowngrading(createFactoryOnlyItParsers(), tripleClass, quadClass);
    }
    @Test(dataProvider = "elementClassPairs")
    public void testIterateTriplesCbParsersDowngrading(@Nonnull Class<?> tripleClass, @Nonnull Class<?> quadClass) {
        runTestIterateTriplesDowngrading(createFactoryOnlyCbParsers(), tripleClass, quadClass);
    }

    private void runTestIterateTriplesMixed(@Nonnull RDFItFactory factory,
                                            @Nonnull Class<?> tripleClass,
                                            @Nonnull Class<?> quadClass) {
        ModelLib.HybridModel source = new ModelLib.HybridModel(tripleClass, quadClass,
                asList(createTriple(tripleClass,        Ex.S1, Ex.P1, Ex.O1),
                         createQuad(  quadClass, Ex.G2, Ex.S2, Ex.P2, Ex.O2)));

        List<Object> expected = asList(createTriple(tripleClass, Ex.S1, Ex.P1, Ex.O1),
                                       createTriple(tripleClass, Ex.S2, Ex.P2, Ex.O2));

        ArrayList<Object> actual = new ArrayList<>();
        try (RDFIt<?> it = factory.iterateTriples(tripleClass, source)) {
            if (quadClass.equals(QuadMock3.class)) {
                assertTrue(it.hasNext());
                assertEquals(it.next(), expected.get(0));
                // QuadMock3 is a dead-end
                expectThrows(InconvertibleException.class, () -> assertFalse(it.hasNext()));
                return;
            } else {
                while (it.hasNext())
                    actual.add(it.next());
            }
        }
        assertEquals(actual, expected);
    }

    @Test(dataProvider = "elementClassPairs", invocationCount = 10)
    public void testIterateTriplesAllParsersMixed(@Nonnull Class<?> tripleClass, @Nonnull Class<?> quadClass) {
        runTestIterateTriplesMixed(createFactoryAllParsers(), tripleClass, quadClass);
    }
    @Test(dataProvider = "elementClassPairs", invocationCount = 10)
    public void testIterateTriplesCbParsersMixed(@Nonnull Class<?> tripleClass, @Nonnull Class<?> quadClass) {
        runTestIterateTriplesMixed(createFactoryOnlyCbParsers(), tripleClass, quadClass);
    }


    private void runTestIterateQuadsNoConversion(@Nonnull RDFItFactory factory, @Nonnull Class<?> quadClass) {
        List<Object> quads = asList(createQuad(quadClass, Ex.G1, Ex.S1, Ex.P1, Ex.O1),
                                    createQuad(quadClass, Ex.G2, Ex.S2, Ex.P2, Ex.O2));
        ModelLib.Model source = ModelLib.getModel(quads);

        ArrayList<Object> actual = new ArrayList<>();
        QuadLifter quadLifter = quadLifter(TripleMock1.class, quadClass, Ex.G6);
        try (RDFIt<?> it = factory.iterateQuads(quadClass, quadLifter, source)) {
            while (it.hasNext())
                actual.add(it.next());
        }
        assertEquals(actual, quads);
    }

    @Test(dataProvider = "elementClassPairs")
    public void testIterateQuadsAllParsersNoConversion(@Nonnull Class<?> ignored,
                                                       @Nonnull Class<?> quadClass) {
        runTestIterateQuadsNoConversion(createFactoryOnlyCbParsers(), quadClass);
    }
    @Test(dataProvider = "elementClassPairs")
    public void testIterateQuadsItParsersNoConversion(@Nonnull Class<?> ignored,
                                                       @Nonnull Class<?> quadClass) {
        runTestIterateQuadsNoConversion(createFactoryOnlyItParsers(), quadClass);
    }
    @Test(dataProvider = "elementClassPairs")
    public void testIterateQuadsCbParsersNoConversion(@Nonnull Class<?> ignored,
                                                      @Nonnull Class<?> quadClass) {
        runTestIterateQuadsNoConversion(createFactoryOnlyCbParsers(), quadClass);
    }

    private void runTestIterateQuadsConverting(@Nonnull RDFItFactory factory,
                                                           @Nonnull Class<?> quadClass) {
        ModelLib.Model source = ModelLib.getModel(asList(new QuadMock1(Ex.G1, Ex.S1, Ex.P1, Ex.O1),
                                                         new QuadMock1(Ex.G2, Ex.S2, Ex.P2, Ex.O2)));
        List<Object> expected = asList(createQuad(quadClass, Ex.G1, Ex.S1, Ex.P1, Ex.O1),
                                       createQuad(quadClass, Ex.G2, Ex.S2, Ex.P2, Ex.O2));

        ArrayList<Object> actual = new ArrayList<>();
        QuadLifter quadLifter = quadLifter(TripleMock1.class, quadClass, Ex.G6);
        try (RDFIt<?> it = factory.iterateQuads(quadClass, quadLifter, source)) {
            while (it.hasNext())
                actual.add(it.next());
        }
        assertEquals(actual, expected);
    }
    @Test(dataProvider = "elementClassPairs")
    public void testIterateQuadsAllParsersConverting(@Nonnull Class<?> ignored, @Nonnull Class<?> quadClass) {
        runTestIterateQuadsConverting(createFactoryAllParsers(), quadClass);
    }
    @Test(dataProvider = "elementClassPairs")
    public void testIterateQuadsItParsersConverting(@Nonnull Class<?> ignored, @Nonnull Class<?> quadClass) {
        runTestIterateQuadsConverting(createFactoryOnlyItParsers(), quadClass);
    }
    @Test(dataProvider = "elementClassPairs")
    public void testIterateQuadsCbParsersConverting(@Nonnull Class<?> ignored, @Nonnull Class<?> quadClass) {
        runTestIterateQuadsConverting(createFactoryOnlyCbParsers(), quadClass);
    }

    private void runTestIterateQuadsConvertingAndUpgrading(@Nonnull RDFItFactory factory,
                                                           @Nonnull Class<?> tripleClass,
                                                           @Nonnull Class<?> quadClass) {
        ModelLib.Model source = ModelLib.getModel(asList(
                createTriple(tripleClass, Ex.S1, Ex.P1, Ex.O1),
                createTriple(tripleClass, Ex.S2, Ex.P2, Ex.O2)));
        List<Object> expected = asList(createQuad(quadClass, Ex.G1, Ex.S1, Ex.P1, Ex.O1),
                                       createQuad(quadClass, Ex.G1, Ex.S2, Ex.P2, Ex.O2));

        ArrayList<Object> actual = new ArrayList<>();
        QuadLifter quadLifter = quadLifter(TripleMock1.class, quadClass, Ex.G1);
        try (RDFIt<?> it = factory.iterateQuads(quadClass, quadLifter, source)) {
            while (it.hasNext())
                actual.add(it.next());
        }
        assertEquals(actual, expected);
    }
    @Test(dataProvider = "elementClassPairs")
    public void testIterateQuadsAllParsersConvertingAndUpgrading(@Nonnull Class<?> tripleClass,
                                                                 @Nonnull Class<?> quadClass) {
        runTestIterateQuadsConvertingAndUpgrading(createFactoryAllParsers(), tripleClass, quadClass);
    }
    @Test(dataProvider = "elementClassPairs")
    public void testIterateQuadsItParsersConvertingAndUpgrading(@Nonnull Class<?> tripleClass,
                                                                 @Nonnull Class<?> quadClass) {
        runTestIterateQuadsConvertingAndUpgrading(createFactoryOnlyItParsers(), tripleClass, quadClass);
    }
    @Test(dataProvider = "elementClassPairs")
    public void testIterateQuadsCbParsersConvertingAndUpgrading(@Nonnull Class<?> tripleClass,
                                                                @Nonnull Class<?> quadClass) {
        runTestIterateQuadsConvertingAndUpgrading(createFactoryOnlyCbParsers(), tripleClass, quadClass);
    }

    private void runTestIterateQuadsFromMixed(@Nonnull RDFItFactory factory,
                                              @Nonnull Class<?> tripleClass,
                                              @Nonnull Class<?> quadClass) {
        ModelLib.Model source = ModelLib.getModel(asList(Ex.T1, Ex.Q2));
        List<Object> expected = asList(createQuad(quadClass, Ex.G1, Ex.S1, Ex.P1, Ex.O1),
                                       createQuad(quadClass, Ex.G1, Ex.S2, Ex.P2, Ex.O2));
        ArrayList<Object> actual = new ArrayList<>();

        try (RDFIt<?> it = factory.iterateQuads(quadClass,
                quadLifter(TripleMock1.class, quadClass, Ex.G1), source)) {
            while (it.hasNext())
                actual.add(it.next());
        }
        assertEquals(actual, expected);
    }

    @Test(dataProvider = "elementClassPairs")
    public void testIterateQuadsAllParsersFromMixed(Class<?> triple, Class<?> quad) {
        runTestIterateQuadsFromMixed(createFactoryAllParsers(), triple, quad);
    }
    @Test(dataProvider = "elementClassPairs")
    public void testIterateQuadsCbParsersFromMixed(Class<?> triple, Class<?> quad) {
        runTestIterateQuadsFromMixed(createFactoryOnlyCbParsers(), triple, quad);
    }

    private enum FactoryMethod {
        ALL_PARSERS,
        ONLY_IT,
        ONLY_CB;

        public @Nonnull RDFItFactory invoke(@Nonnull RDFItFactoryTestBase obj) {
            switch (this) {
                case ONLY_IT: return obj.createFactoryOnlyItParsers();
                case ONLY_CB: return obj.createFactoryOnlyCbParsers();
                case ALL_PARSERS: return obj.createFactoryAllParsers();
                default: break;
            }
            throw new UnsupportedOperationException();
        }
    }


    @DataProvider public @Nonnull Object[][] callbackData() {
        List<FactoryMethod> suppliers = asList(
                FactoryMethod.ALL_PARSERS,
                FactoryMethod.ONLY_IT,
                FactoryMethod.ONLY_CB);
        ArrayList<Class<?>> triples = new ArrayList<>(TRIPLE_CLASSES);
        triples.add(null);
        ArrayList<Class<?>> quads = new ArrayList<>(QUAD_CLASSES);
        quads.add(null);

        List<List<Object>> rows = new ArrayList<>();

        for (Class<?> dataTriple : triples) {
            for (Class<?> dataQuad : quads) {
                ArrayList<Object> data = new ArrayList<>();
                if (dataTriple != null)
                    data.add(createTriple(dataTriple, Ex.S1, Ex.P1, Ex.O1));
                if (dataQuad != null)
                    data.add(createQuad(dataQuad, Ex.G2, Ex.S2, Ex.P2, Ex.O2));
                ModelLib.Model source = ModelLib.getModel(data);
                for (Class<?> desTriple : TRIPLE_CLASSES) {
                    for (Class<?> desQuad : QUAD_CLASSES) {
                        if (QuadMock3.class.equals(dataQuad) && !desQuad.equals(QuadMock3.class))
                            continue; // QM3 cannot be converted into anything else
                        ArrayList<Object> exTriples = new ArrayList<>();
                        if (dataTriple != null)
                            exTriples.add(createTriple(desTriple, Ex.S1, Ex.P1, Ex.O1));
                        ArrayList<Object> exQuads = new ArrayList<>();
                        if (dataQuad != null)
                            exQuads.add(createQuad(desQuad, Ex.G2, Ex.S2, Ex.P2, Ex.O2));
                        for (FactoryMethod m : suppliers)
                            rows.add(asList(source, desTriple, desQuad, m, exTriples, exQuads));
                    }
                }
            }
        }
        return rows.stream().map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "callbackData")
    public void testAbleCallback(Object data, Class<?> desiredTripleClass,
                                 Class<?> desiredQuadClass, FactoryMethod factoryMethod,
                                 Collection<?> exTriples, Collection<?> exQuads) {
        ArrayList<Object> acTriples = new ArrayList<>(), acQuads = new ArrayList<>();
        RDFItFactory factory = factoryMethod.invoke(this);
        List<RDFItException> sourceExceptions = new ArrayList<>();
        List<InconvertibleException> tripleExceptions = new ArrayList<>();
        List<InconvertibleException> quadException = new ArrayList<>();
        //noinspection unchecked
        factory.parse(new RDFListenerBase<Object,Object>((Class<Object>) desiredTripleClass, (Class<Object>) desiredQuadClass) {
            @Override public void triple(@Nonnull Object triple) {
                acTriples.add(triple);
            }
            @Override public void quad(@Nonnull Object quad) {
                acQuads.add(quad);
            }
            @Override
            public boolean notifySourceError(@Nonnull RDFItException e) {
                sourceExceptions.add(e);
                return true;
            }

            @Override
            public boolean notifyInconvertibleTriple(@Nonnull InconvertibleException e) {
                tripleExceptions.add(e);
                return true;
            }

            @Override
            public boolean notifyInconvertibleQuad(@Nonnull InconvertibleException e) {
                quadException.add(e);
                return true;
            }
        }, data);

        if (sourceExceptions.size() == 1 && sourceExceptions.get(0) instanceof NoParserException) {
            // tolerate only if all parsers are ItParsers and the input has both triples and quads
            assertEquals(factoryMethod, FactoryMethod.ONLY_IT);
            assertFalse(exTriples.isEmpty());
            assertFalse(exQuads.isEmpty());
            return;
        }
        //no exceptions
        assertTrue(sourceExceptions.isEmpty());
        assertTrue(tripleExceptions.isEmpty());
        assertTrue(   quadException.isEmpty());

        //expected triples and quads
        assertEquals(acTriples, exTriples);
        assertEquals(acQuads  , exQuads  );
    }

    @DataProvider public @Nonnull Object[][] downgradingCallbackData() {
        List<FactoryMethod> suppliers = asList(
                FactoryMethod.ALL_PARSERS,
                FactoryMethod.ONLY_IT,
                FactoryMethod.ONLY_CB);
        ArrayList<Class<?>> triples = new ArrayList<>(TRIPLE_CLASSES);
        triples.add(null);
        ArrayList<Class<?>> quads = new ArrayList<>(QUAD_CLASSES);
        quads.remove(QuadMock3.class);
        quads.add(null);

        List<List<Object>> rows = new ArrayList<>();
        for (Class<?> dataTriple : triples) {
            for (Class<?> dataQuad : quads) {
                ArrayList<Object> data = new ArrayList<>();
                if (dataTriple != null)
                    data.add(createTriple(dataTriple, Ex.S1, Ex.P1, Ex.O1));
                if (dataQuad != null)
                    data.add(createQuad(dataQuad, Ex.G2, Ex.S2, Ex.P2, Ex.O2));
                ModelLib.Model source = ModelLib.getModel(data);
                for (Class<?> desTriple : TRIPLE_CLASSES) {
                    ArrayList<Object> exTriples = new ArrayList<>();
                    if (dataTriple != null)
                        exTriples.add(createTriple(desTriple, Ex.S1, Ex.P1, Ex.O1));
                    if (dataQuad != null)
                        exTriples.add(createTriple(desTriple, Ex.S2, Ex.P2, Ex.O2));
                    for (FactoryMethod method : suppliers)
                        rows.add(asList(source, desTriple, method, exTriples));
                }
            }
        }

        return rows.stream().map(List::toArray).toArray(Object[][]::new);

    }

    @Test(dataProvider = "downgradingCallbackData")
    public void testDowngradingCallback(Object data, Class<?> desiredTripleClass,
                                        FactoryMethod factoryMethod, Collection<?> exTriples) {
        RDFItFactory factory = factoryMethod.invoke(this);
        List<InconvertibleException> tripleConversionExceptions = new ArrayList<>();
        List<InconvertibleException> quadConversionExceptions = new ArrayList<>();
        List<RDFItException> sourceExceptions = new ArrayList<>();
        List<Object> acTriples = new ArrayList<>();
        //noinspection unchecked
        factory.parse(new TripleListenerBase<Object>((Class<Object>) desiredTripleClass) {
            @Override public void triple(@Nonnull Object triple) {
                acTriples.add(triple);
            }

            @Override
            public boolean notifyInconvertibleTriple(@Nonnull InconvertibleException e) throws InterruptParsingException {
                e.printStackTrace();
                tripleConversionExceptions.add(e);
                return true;
            }

            @Override
            public boolean notifyInconvertibleQuad(@Nonnull InconvertibleException e) throws InterruptParsingException {
                e.printStackTrace();
                quadConversionExceptions.add(e);
                return true;
            }

            @Override
            public boolean notifySourceError(@Nonnull RDFItException e) {
                e.printStackTrace();
                sourceExceptions.add(e);
                return true;
            }
        }, data);

        if (sourceExceptions.size() == 1 && sourceExceptions.get(0) instanceof NoParserException) {
            // tolerate only if all parsers are ItParsers and the input has both triples and quads
            assertEquals(factoryMethod, FactoryMethod.ONLY_IT);
            return;
        }

        assertEquals(tripleConversionExceptions, Collections.emptyList());
        assertEquals(quadConversionExceptions, Collections.emptyList());
        assertEquals(sourceExceptions, Collections.emptyList());
        assertEquals(acTriples, exTriples);
    }
}