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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.testng.Assert.*;

public class EmptyRDFItTest {
    @DataProvider public static Object[][] isEmptyData() {
        return Stream.of(
                asList(String.class, IterationElement.TRIPLE),
                asList(String.class, IterationElement.QUAD)
        ).map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "isEmptyData")
    public void testIsEmpty(@Nonnull Class<?> valueClass, @Nonnull IterationElement itEl) {
        Object source = new Object();
        //noinspection unchecked
        try (EmptyRDFIt<String> it = new EmptyRDFIt<>((Class<String>) valueClass, itEl, source)) {
            assertEquals(it.valueClass(), valueClass);
            assertEquals(it.itElement(), itEl);
            assertFalse(it.hasNext());
            assertSame(it.getSource(), source);
        } //nothing thrown
    }
}