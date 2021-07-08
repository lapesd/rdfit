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
import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.testng.Assert.*;

public class UtilsTest {
    private static final String EX = "http://example.org";

    @DataProvider public @Nonnull Object[][] createURIOrFixData() {
        return Stream.of(
                asList(EX, URI.create(EX)),
                asList(EX+"/a-_,X?query[]=1#fragment", URI.create(EX+"/a-_,X?query[]=1#fragment")),
                asList(EX+"/a b", URI.create(EX+"/a%20b")),
                asList(EX+"/a b?query=`", URI.create(EX+"/a%20b?query=%60")),
                asList(EX+"/a|b", URI.create(EX+"/a%7Cb")),
                asList(EX+"/a%20|b", URI.create(EX+"/a%20%7Cb")),
                asList("", URI.create("")),
                asList(":", null)
        ).map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "createURIOrFixData")
    public void testCreateURIOrFix(@Nonnull String in, @Nullable URI expected) throws URISyntaxException {
        try {
            URI uri = Utils.createURIOrFix(in);
            if (expected == null)
                fail("Expected an URISyntaxException to be thrown for in="+in);
            assertEquals(uri, expected);
        } catch (URISyntaxException e) {
            if (expected != null)
                throw e;
        }
    }

    @DataProvider public @Nonnull Object[][] asciiLowerData() {
        return Stream.of(
                asList('a', 'a'),
                asList('z', 'z'),
                asList('0', '0'),
                asList('`', '`'),
                asList('{', '{'),
                asList('A', 'a'),
                asList('D', 'd'),
                asList('Z', 'z'),
                asList('@', '@'),
                asList('[', '[')
        ).map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "asciiLowerData")
    public void testAsciiLower(int in, int expected) {
        assertEquals(Utils.asciiLower(in), expected);
    }

    @DataProvider public @Nonnull Object[][] asciiUpperData() {
        return Stream.of(
                asList('0', '0'),
                asList('A', 'A'),
                asList('Z', 'Z'),
                asList('@', '@'),
                asList('[', '['),
                asList('a', 'A'),
                asList('z', 'Z'),
                asList('`', '`'),
                asList('{', '{')
        ).map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "asciiUpperData")
    public void testAsciiUpper(int in, int expected) {
        assertEquals(Utils.asciiUpper(in), expected);
    }

    @DataProvider public @Nonnull Object[][] isInSmallData() {
        String digits     = "0123456789";
        String evenDigits = "02468";
        String oddDigits  = "13579";
        String singleton  = "0";
        String pair       = "12";
        String extremes   = "09";

        List<List<Object>> rows = new ArrayList<>();
        for (String string : asList(digits, evenDigits, oddDigits, singleton, pair, extremes)) {
            byte[] bytes = string.getBytes(UTF_8);
            for (byte b : digits.getBytes(UTF_8))
                rows.add(asList((int) b, bytes, string.indexOf(b) >= 0));
        }
        return rows.stream().map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "isInSmallData")
    public void testIsInSmall(int value, byte[] a, boolean expected) {
        assertEquals(Utils.isInSmall(value, a), expected);
    }

    @DataProvider public @Nonnull Object[][] indexInSmallData() {
        List<Object[]> rows = new ArrayList<>();
        for (String string : asList("0123456789", "0248", "1359", "12", "09", "123", "135", "1357")) {
            byte[] bytes = string.getBytes();
            for (byte b : "0123456789".getBytes())
                rows.add(new Object[]{(int) b, bytes, string.indexOf(b)});
        }
        return rows.toArray(new Object[0][]);
    }

    @Test(dataProvider = "indexInSmallData")
    public void testIndexInSmall(int value, byte[] a, int expected) {
        assertEquals(Utils.indexInSmall(value, a), expected);
    }

    @DataProvider public @Nonnull Object[][] isAlphaNumData() {
        String yes = "0123456789ABCXYZabcxyz";
        String  no = "\t\n\r !()./:;?@[\\_`{|}~";
        List<Object[]> list = new ArrayList<>();
        for (byte b : yes.getBytes(UTF_8))
            list.add(new Object[]{(int)b, true});
        for (byte b : no.getBytes(UTF_8))
            list.add(new Object[]{(int)b, false});
        return list.toArray(new Object[0][]);
    }

    @Test(dataProvider = "isAlphaNumData")
    public void testIsAlphaNum(int codePoint, boolean expected) {
        assertEquals(Utils.isAsciiAlphaNum(codePoint), expected);
    }

    @Test(dataProvider = "isAlphaNumData")
    public void testIsAlpha(int codePoint, boolean expected) {
        assertEquals(Utils.isAsciiAlpha(codePoint), expected && "0123456789".indexOf(codePoint) < 0);
    }

    @DataProvider public @Nonnull Object[][] isSpaceData() {
        String spaces = "\n\r\t ";
        List<Object[]> rows = new ArrayList<>();
        for (byte b : "\n\r\t 0123abcABC{}`_~[]()!.;:,+-/$%&*@#".getBytes(UTF_8))
            rows.add(new Object[] {b, spaces.indexOf(b) >= 0});
        return rows.toArray(new Object[0][]);
    }

    @Test(dataProvider = "isSpaceData")
    public void testIsNum(int codePoint, boolean expected) {
        assertEquals(Utils.isAsciiSpace(codePoint), expected);
    }

    @DataProvider public @Nonnull Object[][] writeHexByteData() {
        List<Object[]> rows = new ArrayList<>();
        for (int i = 0; i < 256; i++)
            rows.add(new Object[]{i, String.format("%02X", i)});
        return rows.toArray(new Object[0][]);
    }

    @Test(dataProvider = "writeHexByteData")
    public void testWriteHexByte(int value, @Nonnull String expected) {
        GrowableByteBuffer buffer = new GrowableByteBuffer().add('%');
        assertEquals(Utils.writeHexByte(buffer, value).asString(), "%"+expected);
    }

    @Test(dataProvider = "writeHexByteData")
    public void testParseHexByte(int expected, @Nonnull String in) {
        assertFalse(in.isEmpty());
        if (in.length() == 1)
            assertEquals(Utils.parseHexByte('0', in.codePointAt(0)), expected);
        else
            assertEquals(Utils.parseHexByte(in.codePointAt(0), in.codePointAt(1)), expected);
    }

    @DataProvider public @Nonnull Object[][] parseHexByteBadInputsData() {
        return Stream.of("xx", "A ", " F", ".7", "FG", "fg", "0x", "x0")
                .map(s -> new Object[]{s})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "parseHexByteBadInputsData")
    public void testParseHexByteBadInputs(@Nonnull String in) {
        assertFalse(in.isEmpty());
        if (in.length() == 1)
            assertEquals(Utils.parseHexByte('0', in.codePointAt(0)), -1);
        else
            assertEquals(Utils.parseHexByte(in.codePointAt(0), in.codePointAt(1)), -1);
    }

    public static class Inner {
        public static class Innermost {

        }
    }

    @DataProvider public static @Nonnull Object[][] toFullResourcePathData() {
        String root = "com/github/lapesd/rdfit/";
        return Stream.of(
                asList(Utils.class, "file.txt", root+"util/file.txt"),
                asList(Utils.class, "sub/file.txt", root+"util/sub/file.txt"),
                asList(Utils.class, "../file.txt", root+"file.txt"),
                asList(Utils.class, "../sub/file.txt", root+"sub/file.txt"),
                asList(Inner.class, "file.txt", root+"util/file.txt"),
                asList(Inner.Innermost.class, "file.txt", root+"util/file.txt"),
                asList(Inner.class, "../file.txt", root+"file.txt"),
                asList(Inner.Innermost.class, "../file.txt", root+"file.txt")
        ).map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "toFullResourcePathData")
    public void testToFullResourcePath(@Nonnull Class<?> reference, @Nonnull String relative,
                                       @Nonnull String expected) {
        String actual = Utils.toFullResourcePath(relative, reference);
        assertEquals(actual, expected);
    }
}