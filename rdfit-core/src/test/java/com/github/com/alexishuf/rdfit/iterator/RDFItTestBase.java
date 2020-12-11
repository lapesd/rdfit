package com.github.com.alexishuf.rdfit.iterator;

import com.github.com.alexishuf.rdfit.data.*;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.testng.Assert.*;

public abstract class RDFItTestBase {

    public static @Nonnull List<TripleMock1> singleTriple() {
        return Collections.singletonList(Ex.T1);
    }
    public static @Nonnull List<TripleMock1> twoTriples() {
        return asList(Ex.T1, Ex.T2);
    }
    public static @Nonnull List<? extends TripleMock> threeMixedTriples() {
        return asList(Ex.T1, Ex.U2, Ex.T3);
    }
    public static @Nonnull List<QuadMock1> twoQuads() {
        return asList(Ex.Q1, Ex.Q2);
    }
    public static @Nonnull List<? extends QuadMock> twoMixedQuads() {
        return asList(Ex.Q1, Ex.R2);
    }
    public static @Nonnull List<?> oneTripleOneQuad() {
        return asList(Ex.T1, Ex.Q1);
    }
    public static @Nonnull List<?> fourMixedTriplesAndQuads() {
        return asList(Ex.T1, Ex.Q2, Ex.U3, Ex.R4);
    }

    protected abstract @Nonnull <T> RDFIt<T> createIt(@Nonnull Class<T> valueClass,
                                                      @Nonnull List<?> data);

    @DataProvider public static Object[][] iterateData() {
        return Stream.of(
                asList("singleTriple",             TripleMock1.class, singleTriple()),
                asList("twoTriples",               TripleMock1.class, twoTriples()),
                asList("threeMixedTriples",        TripleMock.class, threeMixedTriples()),
                asList("twoQuads",                 QuadMock1.class, twoQuads()),
                asList("twoMixedQuads",            QuadMock.class, twoMixedQuads()),
                asList("oneTripleOneQuad",         ElementMock.class, oneTripleOneQuad()),
                asList("fourMixedTriplesAndQuads", ElementMock.class, fourMixedTriplesAndQuads()),

                asList("singleTriple",             Object.class, singleTriple()),
                asList("twoTriples",               Object.class, twoTriples()),
                asList("threeMixedTriples",        Object.class, threeMixedTriples()),
                asList("twoQuads",                 Object.class, twoQuads()),
                asList("twoMixedQuads",            Object.class, twoMixedQuads()),
                asList("oneTripleOneQuad",         Object.class, oneTripleOneQuad()),
                asList("fourMixedTriplesAndQuads", Object.class, fourMixedTriplesAndQuads())
        ).map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "iterateData")
    public void testIterate(@Nonnull String ignored, @Nonnull Class<?> valueClass,
                            @Nonnull List<?> data) {
        List<Object> actual = new ArrayList<>();
        try (RDFIt<?> it = createIt(valueClass, data)) {
            assertEquals(it.valueClass(), valueClass);
            while (it.hasNext()) {
                Object value = it.next();
                assertNotNull(value);
                actual.add(value);
                assertTrue(valueClass.isInstance(value));
            }
        }
        assertEquals(actual, data);
    }

}