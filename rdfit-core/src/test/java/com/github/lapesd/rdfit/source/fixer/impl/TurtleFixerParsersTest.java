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

package com.github.lapesd.rdfit.source.fixer.impl;

import com.github.lapesd.rdfit.util.GrowableByteBuffer;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

public class TurtleFixerParsersTest {

    @DataProvider public @Nonnull Object[][] uCharStateData() {
        return Stream.of(
                /* all input is rejected */
                asList("0", ""),
                asList("u00C3", ""),

                /* input is rejected midway, but no complete UCHAR  */
                asList("\\", "\\"),
                asList("\\\\", "\\"),
                asList("\\x", "\\"),
                asList("\\x20", "\\"),

                /* valid UCHAR sequences */
                asList("\\u01E9", "\\u01E9"),
                asList("\\u01e9", "\\u01e9"),
                asList("\\u007E", "\\u007E"),
                asList("\\u007e", "\\u007e"),
                asList("\\u66A0", "\\u66A0"),
                asList("\\u66a0", "\\u66a0"),
                asList("\\u71F0", "\\u71F0"),
                asList("\\U0001FA04", "\\U0001FA04"),
                asList("\\U0001FA04", "\\U0001FA04"),
                asList("\\U0001FA04\\u0020",               "\\U0001FA04\\u0020"),
                asList("\\U0001FA04\\u0020\\u71F0",        "\\U0001FA04\\u0020\\u71F0"),
                asList("\\U0001FA04\\u0020\\u71F0\\u01e9", "\\U0001FA04\\u0020\\u71F0\\u01e9"),

                /* UCHARs with UTF-8 encoding */
                asList("\\u00C3\\u0083", "\\u00C3"), // valid UTF-8 and U+0083 is control
                asList("\\u00c3\\u008f", "\\u00CF"), // valid UTF-8 and U+008F is control
                asList("\\u00c3\\u008f", "\\u00CF"), // valid UTF-8 and U+008F is control
                asList("\\u00c3\\u00A0", "\\u00E0"), // valid UTF-8 and U+00A0 is last control
                asList("\\u00c3\\u00be", "\\u00C3\\u00BE"), // ambiguous, assume input is correct
                asList("\\u00c3\\u00AF", "\\u00C3\\u00AF"), // ambiguous, assume input is correct
                asList("\\u00c3\\u00Af", "\\u00C3\\u00AF"), // ambiguous, assume input is correct
                asList("\\u00c3\\u00a1", "\\u00C3\\u00A1")  // ambiguous, assume input is correct
        ).map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "uCharStateData")
    private void testUCharState(@Nonnull String in, @Nonnull String expected) {
        GrowableByteBuffer out = new GrowableByteBuffer();
        TurtleFixerParsers.UCharFixer state = new TurtleFixerParsers.UCharFixer(out) {
            @Override
            protected void handleInvalid(@Nonnull GrowableByteBuffer buffer, int begin, int end) {
                output.add('\\').add(buffer.getArray(), begin, end - begin);
            }
        };

        for (int round = 0; round < 2; round++) {
            out.clear();
            boolean rejected = false;
            for (byte b : in.getBytes(UTF_8)) {
                if ((rejected = !state.feedByte(b & 0xFF)))
                    break;
            }
            if (!rejected)
                state.flush();
            assertEquals(out.asString(UTF_8), expected, "round="+round);
            state.flush();
            assertEquals(out.asString(UTF_8), expected, "round="+round+", after flush()");
            state.reset();
            assertEquals(out.asString(UTF_8), expected, "round="+round+", after reset()");
        }
    }

}