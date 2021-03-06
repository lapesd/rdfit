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

package com.github.lapesd.rdfit.util;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static com.github.lapesd.rdfit.util.Literal.QuotationStyle.*;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

public class LiteralParserTest {
    @DataProvider public static Object[][] testData() {
        //noinspection JavacQuirks
        return Stream.of(
                //unquoted literals
                asList("1", Literal.unquoted("1")),
                asList("false", Literal.unquoted("false")),

                //quote variants with simple contents
                asList("\"asd\"", Literal.plain(SINGLE_DOUBLE, "asd")),
                asList("'asd'", Literal.plain(SINGLE_SINGLE, "asd")),
                asList("'''asd'''", Literal.plain(MULTI_SINGLE, "asd")),
                asList("\"\"\"asd\"\"\"", Literal.plain(MULTI_DOUBLE, "asd")),

                //quote variants with simple contents + UTF-8
                asList("'Süd'", Literal.plain(SINGLE_SINGLE, "Süd")), // ü = c3 bc
                asList("'€'", Literal.plain(SINGLE_SINGLE, "€")), // € = e2 82 ac
                asList("'a€b'", Literal.plain(SINGLE_SINGLE, "a€b")), // € = e2 82 ac
                asList("\"Süd\"", Literal.plain(SINGLE_DOUBLE, "Süd")), // ü = c3 bc
                asList("\"€\"", Literal.plain(SINGLE_DOUBLE, "€")), // € = e2 82 ac
                asList("\"a€b\"", Literal.plain(SINGLE_DOUBLE, "a€b")), // € = e2 82 ac

                // quote variants + lang tag
                asList("'asd'@en", Literal.lang(SINGLE_SINGLE, "asd", "en")),
                asList("'asd'@en-US", Literal.lang(SINGLE_SINGLE, "asd", "en-US")),
                asList("\"asd\"@en", Literal.lang(SINGLE_DOUBLE, "asd", "en")),
                asList("\"asd\"@en-US", Literal.lang(SINGLE_DOUBLE, "asd", "en-US")),
                asList("'''asd'''@en", Literal.lang(MULTI_SINGLE, "asd", "en")),
                asList("'''asd'''@en-US", Literal.lang(MULTI_SINGLE, "asd", "en-US")),
                asList("\"\"\"asd\"\"\"@en", Literal.lang(MULTI_DOUBLE, "asd", "en")),
                asList("\"\"\"asd\"\"\"@en-US", Literal.lang(MULTI_DOUBLE, "asd", "en-US")),

                // quote variants + type
                asList("'asd'^^xsd:string", Literal.prefixTyped(SINGLE_SINGLE, "asd", "xsd", "string")),
                asList("'asd'^^<http://www.w3.org/2001/XMLSchema#string>", Literal.iriTyped(SINGLE_SINGLE, "asd", "http://www.w3.org/2001/XMLSchema#string")),
                asList("\"asd\"^^xsd:string", Literal.prefixTyped(SINGLE_DOUBLE, "asd", "xsd", "string")),
                asList("\"asd\"^^<http://www.w3.org/2001/XMLSchema#string>", Literal.iriTyped(SINGLE_DOUBLE, "asd", "http://www.w3.org/2001/XMLSchema#string")),
                asList("'''asd'''^^xsd:string", Literal.prefixTyped(MULTI_SINGLE, "asd", "xsd", "string")),
                asList("'''asd'''^^<http://www.w3.org/2001/XMLSchema#string>", Literal.iriTyped(MULTI_SINGLE, "asd", "http://www.w3.org/2001/XMLSchema#string")),
                asList("\"\"\"asd\"\"\"^^xsd:string", Literal.prefixTyped(MULTI_DOUBLE, "asd", "xsd", "string")),
                asList("\"\"\"asd\"\"\"^^<http://www.w3.org/2001/XMLSchema#string>", Literal.iriTyped(MULTI_DOUBLE, "asd", "http://www.w3.org/2001/XMLSchema#string")),

                // escaped chars
                asList("\"a \\\" b \"@en", Literal.lang(SINGLE_DOUBLE, "a \\\" b ", "en")),
                asList("'a\"b'@en", Literal.lang(SINGLE_SINGLE, "a\"b", "en")),
                asList("'a\"b@'@en", Literal.lang(SINGLE_SINGLE, "a\"b@", "en")),
                asList("'a\"b^^'@en", Literal.lang(SINGLE_SINGLE, "a\"b^^", "en")),
                asList("'a\"b^^\\''@en", Literal.lang(SINGLE_SINGLE, "a\"b^^\\'", "en")),
                asList("'''a\"b'''@en", Literal.lang(MULTI_SINGLE, "a\"b", "en")),
                asList("'''a\"b@'''@en", Literal.lang(MULTI_SINGLE, "a\"b@", "en")),
                asList("'''a\"b^^'''@en", Literal.lang(MULTI_SINGLE, "a\"b^^", "en")),
                asList("'''a\"b^^\\''''@en", Literal.lang(MULTI_SINGLE, "a\"b^^\\'", "en")),

                // ignore leading/trailing spaces
                asList("1  ", Literal.unquoted("1")),
                asList("  1", Literal.unquoted("1")),
                asList("  1 ", Literal.unquoted("1")),
                asList("\n1", Literal.unquoted("1")),
                asList("\n1\t", Literal.unquoted("1")),
                asList("'asd'  ", Literal.plain(SINGLE_SINGLE, "asd")),
                asList("  'asd'  ", Literal.plain(SINGLE_SINGLE, "asd")),
                asList("\n 'asd'  ", Literal.plain(SINGLE_SINGLE, "asd")),
                asList("\n 'asd'\t ", Literal.plain(SINGLE_SINGLE, "asd")),
                asList("  'a \"\\' ^^ @ '@en  ", Literal.lang(SINGLE_SINGLE, "a \"\\' ^^ @ ", "en")),
                asList(" \n \"a \\\"' ^^ @ \"@en-US\t  ", Literal.lang(SINGLE_DOUBLE, "a \\\"' ^^ @ ", "en-US")),
                asList(" \n \"\"\"a \\\"' ^^ @ \"\"\"@en-US\t  ", Literal.lang(MULTI_DOUBLE, "a \\\"' ^^ @ ", "en-US")),

                // escape badly escaped inner quotes
                asList("\"a\"\"@en", Literal.lang(SINGLE_DOUBLE, "a\\\"", "en")),

                //empty quoted lexical forms
                asList("\"\"", Literal.plain(SINGLE_DOUBLE, "")),
                asList("''", Literal.plain(SINGLE_SINGLE, "")),
                asList("\"\"\"\"\"\"", Literal.plain(MULTI_DOUBLE, "")),
                asList("''''''", Literal.plain(MULTI_SINGLE, "")),

                //empty quoted lexical forms + string typed
                asList("\"\"^^<http://www.w3.org/2001/XMLSchema#string>", Literal.iriTyped(SINGLE_DOUBLE, "", "http://www.w3.org/2001/XMLSchema#string")),
                asList("''^^<http://www.w3.org/2001/XMLSchema#string>", Literal.iriTyped(SINGLE_SINGLE, "", "http://www.w3.org/2001/XMLSchema#string")),
                asList("\"\"\"\"\"\"^^<http://www.w3.org/2001/XMLSchema#string>", Literal.iriTyped(MULTI_DOUBLE, "", "http://www.w3.org/2001/XMLSchema#string")),
                asList("''''''^^<http://www.w3.org/2001/XMLSchema#string>", Literal.iriTyped(MULTI_SINGLE, "", "http://www.w3.org/2001/XMLSchema#string")),

                //empty quoted lexical forms + langtag
                asList("\"\"@en", Literal.lang(SINGLE_DOUBLE, "", "en")),
                asList("''@en", Literal.lang(SINGLE_SINGLE, "", "en")),
                asList("\"\"\"\"\"\"@en", Literal.lang(MULTI_DOUBLE, "", "en")),
                asList("''''''@en", Literal.lang(MULTI_SINGLE, "", "en"))
        ).map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "testData")
    public void test(@Nonnull String in, @Nonnull Literal expected) {
        LiteralParser parser = new LiteralParser();
        Literal full = parser.parse(in);
        Literal first = parser.parseFirst(in);
        assertEquals(full, expected);
        assertEquals(first, expected);
    }

    @Test(dataProvider = "testData")
    public void testUtf8(@Nonnull String in, @Nonnull Literal expected) {
        LiteralParser parser = new LiteralParser();
        Literal first = null, second = null;
        for (byte b : in.getBytes(StandardCharsets.UTF_8)) {
            if (parser.feedByte(b)) {
                first = parser.endAndReset();
                break;
            }
        }
        if (first == null)
            first = parser.endAndReset();
        for (byte b : in.getBytes(StandardCharsets.UTF_8)) {
            if (parser.feedByte(b)) {
                second = parser.endAndReset();
                break;
            }
        }
        if (second == null)
            second = parser.endAndReset();
        assertEquals(first, expected);
        assertEquals(second, expected);
    }

    @Test(dataProvider = "testData")
    public void testReused(@Nonnull String in, @Nonnull Literal expected) {
        LiteralParser parser = new LiteralParser();
        for (char c : "\"a\"^^xsd:string".toCharArray())
            parser.feed(c);
        Literal full = parser.parse(in);
        for (char c : "\"a\"@en".toCharArray())
            parser.feed(c);
        Literal first = parser.parseFirst(in);
        assertEquals(full, expected);
        assertEquals(first, expected);
    }

}