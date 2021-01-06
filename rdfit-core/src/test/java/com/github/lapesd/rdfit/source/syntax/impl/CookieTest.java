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

package com.github.lapesd.rdfit.source.syntax.impl;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.testng.Assert.*;

public class CookieTest {
    @DataProvider public @Nonnull Object[][] singleCookieData() {
        return Stream.of(
                asList("ABC", true, "ABC", true),
                asList("ABC", true, "ABCDE", true),
                asList("ABC", true, "XABC", false),
                asList("ABC", true, "AB ABC", false),
                asList("X", false, "X", true),
                asList("X", false, "XABC", true),
                asList("X", false, "ABCX", true),
                asList("XY", false, "XXYAB", true),
                asList("XY", false, "AXXYAB", true),
                asList("XY", false, "X Y", false),
                asList("XY", false, "YXXAYX", false)
        ).map(List::toArray).toArray(Object[][]::new);

    }

    @Test(dataProvider = "singleCookieData")
    public void testSingleCookie(String cookie, boolean strict, String input, boolean expected) {
        Cookie obj = Cookie.builder(cookie).strict(strict).build();
        Cookie.Matcher matcher = obj.createMatcher();
        doTest(matcher, input.getBytes(UTF_8), expected);
    }

    @DataProvider public @Nonnull Object[][] skipBOMData() {
        return Stream.of(
                asList(true, true, "XYZ".getBytes(UTF_8)), //no BOM
                asList(true, true, new byte[] {0x58, 0x59}), // sanity: no BOM UTF-8/ASCII
                asList(true, true, new byte[] {(byte)0xFF, (byte)0xFE, 0x58, 0x59}), // UTF-8 with 16LE BOM
                asList(true, true, new byte[] {(byte)0xFE, (byte)0xFF, 0x58, 0x59}), // UTF-8 with 16BE BOM
                asList(true, true, new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF, 0x58, 0x59}), // UTF-8 with BOM

                asList(true, false, "XYZ".getBytes(UTF_8)), //no BOM
                asList(true, false, new byte[] {0x58, 0x59}), // sanity: no BOM UTF-8/ASCII
                asList(false, false, new byte[] {(byte)0xFF, (byte)0xFE, 0x58, 0x59}), // UTF-8 with 16LE BOM
                asList(false, false, new byte[] {(byte)0xFE, (byte)0xFF, 0x58, 0x59}), // UTF-8 with 16BE BOM
                asList(false, false, new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF, 0x58, 0x59}) // UTF-8 with BOM
        ).map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "skipBOMData")
    public void testSkipBOM(boolean expected, boolean skipBOM, byte[] input) {
        Cookie cookie = Cookie.builder("XY").skipBOM(skipBOM).strict().build();
        Cookie.Matcher matcher = cookie.createMatcher();
        doTest(matcher, input, expected);
    }

    private void doTest(@Nonnull Cookie.Matcher matcher, @Nonnull byte[] bytes, boolean expected) {
        boolean wasMatched = false;
        for (byte value : bytes) {
            if (!matcher.feed(value)) {
                assertTrue(matcher.isConclusive());
                assertFalse(matcher.isMatched());
            } else {
                if (matcher.isMatched())
                    wasMatched = true;
                else
                    assertFalse(wasMatched); // feed() after match never un-matches
                assertEquals(matcher.isMatched(), matcher.isConclusive());
            }
        }
        assertEquals(matcher.isMatched(), expected);
        if (expected)
            assertTrue(matcher.isConclusive());
    }

    @DataProvider public @Nonnull Object[][] alternativeSuccessorsData() {
        Cookie c1 = Cookie.builder("XY").strict()
                          .then("AB").strict().save()
                          .build();
        Cookie c2 = Cookie.builder("XY").strict()
                          .then("AB").strict().save()
                          .then("CD").then("EF").strict().ignoreCase().save().save()
                          .build();
        Cookie c3 = Cookie.builder("XY").strict()
                          .then("AB").strict().skipWhitespace().save()
                          .build();
        return Stream.of(
                asList(c1, "XYAB", true),
                asList(c1, " XY", false),
                asList(c1, "XY", false),
                asList(c1, "XYA", false),
                asList(c1, "XYAAB", false),
                asList(c2, "XYAB", true),
                asList(c2, "XYAb", false),
                asList(c2, "XYab", false),
                asList(c2, "XyAB", false),
                asList(c2, "xYAB", false),
                asList(c2, "XYAAB", false),
                asList(c2, "XYABCD", true),
                asList(c2, "XYCD", false),
                asList(c2, "XYCDEF", true),
                asList(c2, "XY CDEF", true),
                asList(c2, "XYCCDEF", true),
                asList(c2, "XYcCDEF", true),
                asList(c2, "XYCcDEF", false),
                asList(c2, "XYCCDEf", true),
                asList(c2, "XYCCDef", true),
                asList(c2, "XYEFCDEF", true),
                asList(c2, "XYEFCD EF", false),
                asList(c2, "XYEFCDEEF", false),
                asList(c3, "XYAB", true),
                asList(c3, "XY|AB", false),
                asList(c3, "XYAAB", false),
                asList(c3, "XY AB", true),
                asList(c3, "XY  AB", true),
                asList(c3, "XY \t\r\n AB", true),
                asList(c3, "XY \t\r\n ABX", true)
        ).map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "alternativeSuccessorsData")
    public void testAlternativeSuccessors(@Nonnull Cookie cookie, String input, boolean expected) {
        doTest(cookie.createMatcher(), input.getBytes(UTF_8), expected);
    }

    @Test(dataProvider = "alternativeSuccessorsData")
    public void testConcurrentMatchers(@Nonnull Cookie cookie, String input,
                                       boolean expected) throws Exception{
        byte[] bytes = input.getBytes(UTF_8);
        ExecutorService exec = Executors.newCachedThreadPool();
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < Runtime.getRuntime().availableProcessors() * 100; i++)
            futures.add(exec.submit(() -> doTest(cookie.createMatcher(), bytes, expected)));
        for (Future<?> f : futures)
            f.get();
        exec.shutdown();
        assertTrue(exec.awaitTermination(3, TimeUnit.SECONDS));
    }
}