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

package com.github.lapesd.rdfit.components.converters;

import com.github.lapesd.rdfit.components.Converter;
import com.github.lapesd.rdfit.data.*;
import com.github.lapesd.rdfit.errors.ConversionException;
import com.github.lapesd.rdfit.iterator.Ex;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.testng.Assert.assertEquals;

public abstract class ConversionManagerTestBase {
    protected abstract @Nonnull ConversionManager createManager();

    @DataProvider public Object[][] testData() {
        return Stream.of(
                asList("No",          emptyList(), Ex.T1, TripleMock1.class, Ex.T1),

                // triple convserions
                asList("T1 -> U1",                  ConverterLib.TRIPLE_CONVERTERS, Ex.T1, TripleMock2.class, Ex.U1),
                asList("T1 -> U1 (all converters)", ConverterLib.ALL_CONVERTERS,    Ex.T1, TripleMock2.class, Ex.U1),
                asList("U1 -> T1",                  ConverterLib.TRIPLE_CONVERTERS, Ex.U1, TripleMock1.class, Ex.T1),
                asList("U1 -> T1 (all converters)", ConverterLib.ALL_CONVERTERS,    Ex.U1, TripleMock1.class, Ex.T1),

                // quad conversion
                asList("Q1 -> R1",                  ConverterLib.QUAD_CONVERTERS, Ex.Q1, QuadMock2.class, Ex.R1),
                asList("Q1 -> R1 (all converters)", ConverterLib.ALL_CONVERTERS,  Ex.Q1, QuadMock2.class, Ex.R1),
                asList("R1 -> Q1",                  ConverterLib.QUAD_CONVERTERS, Ex.R1, QuadMock1.class, Ex.Q1),
                asList("R1 -> Q1 (all converters)", ConverterLib.ALL_CONVERTERS,  Ex.R1, QuadMock1.class, Ex.Q1),

                // quad -> triple downgrades with no path or singleton paths
                asList("Q1 -> T1 (triple converters)", ConverterLib.TRIPLE_CONVERTERS,  Ex.Q1, TripleMock1.class, null),
                asList("Q1 -> T1 (quad converters)",   ConverterLib.QUAD_CONVERTERS,    Ex.Q1, TripleMock1.class, null),
                asList("Q1 -> T1 (downgraders)",       ConverterLib.TRIPLE_DOWNGRADERS, Ex.Q1, TripleMock1.class, Ex.T1),
                asList("Q1 -> T1 (all converters)",    ConverterLib.ALL_CONVERTERS,     Ex.Q1, TripleMock1.class, Ex.T1),
                asList("R1 -> U1 (triple converters)", ConverterLib.TRIPLE_CONVERTERS,  Ex.R1, TripleMock2.class, null),
                asList("R1 -> U1 (quad converters)",   ConverterLib.QUAD_CONVERTERS,    Ex.R1, TripleMock2.class, null),
                asList("R1 -> U1 (downgraders)",       ConverterLib.TRIPLE_DOWNGRADERS, Ex.R1, TripleMock2.class, Ex.U1),
                asList("R1 -> U1 (all converters)",    ConverterLib.ALL_CONVERTERS,     Ex.R1, TripleMock2.class, Ex.U1),

                // multi-step quad -> triple conversions
                asList("Q1 -> U1", ConverterLib.ALL_CONVERTERS, Ex.Q1, TripleMock2.class, Ex.U1),
                asList("R1 -> T1", ConverterLib.ALL_CONVERTERS, Ex.R1, TripleMock1.class, Ex.T1),
                asList("Q1 -> V1", ConverterLib.ALL_CONVERTERS, Ex.Q1, TripleMock3.class, Ex.V1),
                asList("R1 -> V1", ConverterLib.ALL_CONVERTERS, Ex.R1, TripleMock3.class, Ex.V1),
                // can't convert triples into quads
                asList("V1 -> Q1", ConverterLib.ALL_CONVERTERS, Ex.V1, QuadMock1.class,   null),
                asList("V1 -> R1", ConverterLib.ALL_CONVERTERS, Ex.V1, QuadMock2.class,   null),

                // triple conversions, but needed converter is missing
                asList("T1 -> U1",                   emptyList(),                     Ex.T1, TripleMock2.class, null),
                asList("T1 -> U1 (quad converters)", ConverterLib.QUAD_CONVERTERS,    Ex.T1, TripleMock2.class, null),
                asList("T1 -> U1 (downgraders)",     ConverterLib.TRIPLE_DOWNGRADERS, Ex.T1, TripleMock2.class, null),
                asList("U1 -> T1",                   emptyList(),                     Ex.U1, TripleMock1.class, null),
                asList("U1 -> T1 (quad converters)", ConverterLib.QUAD_CONVERTERS,    Ex.U1, TripleMock1.class, null),
                asList("U1 -> T1 (downgraders)",     ConverterLib.TRIPLE_DOWNGRADERS, Ex.U1, TripleMock1.class, null),

                // quad conversions, but needed converter is missing
                asList("Q1 -> R1 (empty)",             emptyList(),                     Ex.Q1, QuadMock2.class, null),
                asList("Q1 -> R1 (triple converters)", ConverterLib.TRIPLE_CONVERTERS,  Ex.Q1, QuadMock2.class, null),
                asList("Q1 -> R1 (downgraders)",       ConverterLib.TRIPLE_DOWNGRADERS, Ex.Q1, QuadMock2.class, null),
                asList("R1 -> Q1 (empty)",             emptyList(),                     Ex.R1, QuadMock1.class, null),
                asList("R1 -> Q1 (triple converters)", ConverterLib.TRIPLE_CONVERTERS,  Ex.R1, QuadMock1.class, null),
                asList("R1 -> Q1 (downgraders)",       ConverterLib.TRIPLE_DOWNGRADERS, Ex.R1, QuadMock1.class, null),

                // convert to and from QuadMock3
                asList("QM3 -> Q1 (all converters)",  ConverterLib.ALL_CONVERTERS, new QuadMock3(Ex.G1, Ex.V1), QuadMock1.class,   null),
                asList("QM3 -> R1 (all converters)",  ConverterLib.ALL_CONVERTERS, new QuadMock3(Ex.G1, Ex.V1), QuadMock2.class,   null),
                asList("QM3 -> T1 (all converters)",  ConverterLib.ALL_CONVERTERS, new QuadMock3(Ex.G1, Ex.V1), TripleMock1.class, null),
                asList("Q1 -> QM3 (all converters)",  ConverterLib.ALL_CONVERTERS, Ex.Q1, QuadMock3.class, new QuadMock3(Ex.G1, Ex.V1)),
                asList("R1 -> QM3 (all converters)",  ConverterLib.ALL_CONVERTERS, Ex.R1, QuadMock3.class, new QuadMock3(Ex.G1, Ex.V1)),
                asList("T1 -> QM3 (all converters)",  ConverterLib.ALL_CONVERTERS, Ex.T1, QuadMock3.class, null)
        ).map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "testData")
    public void test(@Nonnull String testName, @Nonnull Collection<? extends Converter> converters,
                     @Nonnull Object input, @Nonnull Class<?> desired, @Nullable Object expected) {
        ConversionManager mgr = createManager();
        for (Converter c : converters)
            mgr.register(c);
        ConversionFinder finder = mgr.findPath(input, desired);
        Object result = null;
        while (finder.hasNext() && result == null) {
            try {
                result = finder.convert(input);
            } catch (ConversionException ignored) {  }
        }
        assertEquals(result, expected);
    }

}