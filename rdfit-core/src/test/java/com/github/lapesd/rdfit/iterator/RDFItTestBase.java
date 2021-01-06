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

package com.github.lapesd.rdfit.iterator;

import com.github.lapesd.rdfit.data.*;
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

    @DataProvider public static Object[][] iterateData() {
        return Stream.of(
                asList("singleTriple",             TripleMock1.class, IterationElement.TRIPLE, singleTriple()),
                asList("twoTriples",               TripleMock1.class, IterationElement.TRIPLE, twoTriples()),
                asList("threeMixedTriples",        TripleMock.class,  IterationElement.TRIPLE, threeMixedTriples()),
                asList("twoQuads",                 QuadMock1.class,   IterationElement.QUAD,   twoQuads()),
                asList("twoMixedQuads",            QuadMock.class,    IterationElement.QUAD,   twoMixedQuads()),
                asList("oneTripleOneQuad",         ElementMock.class, IterationElement.TRIPLE, oneTripleOneQuad()),
                asList("fourMixedTriplesAndQuads", ElementMock.class, IterationElement.TRIPLE, fourMixedTriplesAndQuads()),

                asList("singleTriple",             Object.class, IterationElement.TRIPLE, singleTriple()),
                asList("twoTriples",               Object.class, IterationElement.TRIPLE, twoTriples()),
                asList("threeMixedTriples",        Object.class, IterationElement.TRIPLE, threeMixedTriples()),
                asList("twoQuads",                 Object.class, IterationElement.QUAD,   twoQuads()),
                asList("twoMixedQuads",            Object.class, IterationElement.QUAD,   twoMixedQuads()),
                asList("oneTripleOneQuad",         Object.class, IterationElement.TRIPLE, oneTripleOneQuad()),
                asList("fourMixedTriplesAndQuads", Object.class, IterationElement.TRIPLE, fourMixedTriplesAndQuads())
        ).map(List::toArray).toArray(Object[][]::new);
    }

    protected abstract @Nonnull <T> RDFIt<T>
    createIt(@Nonnull Class<T> valueClass, @Nonnull IterationElement itElement,
             @Nonnull List<?> data);

    protected @Nonnull List<?>
    adjustExpected(@Nonnull List<?> data, @Nonnull Class<?> valueClass,
                   @Nonnull IterationElement itEl) {
        return new ArrayList<>(data);
    }

    @Test(dataProvider = "iterateData")
    public void testIterateNoConversions(@Nonnull String ignored, @Nonnull Class<?> valueClass,
                                         @Nonnull IterationElement itElement,
                                         @Nonnull List<?> data) {
        List<?> expected = adjustExpected(data, valueClass, itElement);
        assertEquals(data.size(), expected.size());
        List<Object> actual = new ArrayList<>();
        try (RDFIt<?> it = createIt(valueClass, itElement, data)) {
            assertEquals(it.valueClass(), valueClass);
            while (it.hasNext()) {
                Object value = it.next();
                assertNotNull(value);
                actual.add(value);
                assertTrue(valueClass.isInstance(value));
            }
        }
        assertEquals(actual, expected);
    }
}