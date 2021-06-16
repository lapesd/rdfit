/*
 * Copyright 2021 Alexis Armin Huf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.lapesd.rdfit.util;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.testng.Assert.*;

public class GrowableByteBufferTest {

    @DataProvider public @Nonnull Object[][] indexOfArrayData() {
        return Stream.of(
                asList("",    "",     0),
                asList("asd", "",     0),
                asList("asd", "a",    0),
                asList("asd", "s",    1),
                asList("asd", "d",    2),
                asList("asd", "e",   -1),
                asList("asd", "as",   0),
                asList("asd", "sd",   1),
                asList("asd", "asd",  0),
                asList("asdqwe", "asd",    0),
                asList("asdqwe", "qwe",    3),
                asList("asdqwe", "sdq",    1),
                asList("asdqwe", "sq",    -1),
                asList("asdqwe", "qe",    -1),
                asList("asdqwe", "adqwe", -1),
                asList("asdqwe", "awe",   -1),
                asList("asdqwe", "adq",   -1)
        ).map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "indexOfArrayData")
    public void testIndexOfArray(@Nonnull String buffer, @Nonnull String sequence, int expected) {
        GrowableByteBuffer bb = new GrowableByteBuffer();
        assertEquals(bb.add(buffer.getBytes(UTF_8)).indexOf(sequence.getBytes(UTF_8)), expected);
    }

    @DataProvider public @Nonnull Object[][] removeAllData() {
        return Stream.of(
                asList("",     'a', "",    false),
                asList("a",    'x', "a",   false),
                asList("a",    'a', "",    true),
                asList("asd",  'x', "asd", false),
                asList("asd",  'd', "as",  true),
                asList("asd",  's', "ad",  true),
                asList("assd", 's', "ad",  true),
                asList("asd",  'a', "sd",  true)
        ).map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "removeAllData")
    public void testRemoveAll(@Nonnull String buffer, char target,  @Nonnull String after,
                              boolean expected) {
        GrowableByteBuffer bb = new GrowableByteBuffer();
        assertEquals(bb.add(buffer.getBytes(UTF_8)).removeAll(target), expected);
        assertEquals(bb.asString(UTF_8), after);
    }
}