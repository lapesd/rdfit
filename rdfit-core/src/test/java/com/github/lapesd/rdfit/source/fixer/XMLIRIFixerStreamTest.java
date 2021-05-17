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

package com.github.lapesd.rdfit.source.fixer;

import com.github.lapesd.rdfit.RIt;
import com.github.lapesd.rdfit.source.RDFInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.lapesd.rdfit.util.Utils.openResource;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

public class XMLIRIFixerStreamTest {
    @DataProvider public static Object[][] testData() {
        List<String> filenames = asList(
                "eswc-2006-complete.bad-host.rdf",
                "eswc-2006-complete.rdf",
                "eswc-2009-complete+comment.rdf",
                "eswc-2009-complete+DOCTYPE.rdf",
                "eswc-2009-complete+encoding.rdf",
                "eswc-2009-complete+everything.rdf",
                "eswc-2009-complete.rdf",
                "LDOW-2008-complete.rdf",
                "foaf.rdf",
                "iswc-2008-complete.bad-space.rdf",
                "iswc-2008-complete.rdf",
                "owled-2007-complete+everything.rdf",
                "owled-2007-complete.rdf",
                "time.rdf"
        );
        List<List<String>> data = filenames.stream().filter(n -> !n.contains(".bad-"))
                                            .map(n -> asList(n, n)).collect(Collectors.toList());
        Pattern badPattern = Pattern.compile("\\.bad-.+(\\.[^.]+)$");
        filenames.stream().map(s -> ImmutablePair.of(s, badPattern.matcher(s)))
                          .filter(p -> p.right.find())
                          .map(p -> asList(p.left, p.right.replaceFirst("$1")))
                          .forEach(data::add);
        return data.stream().map(List::toArray).toArray(Object[][]::new);
    }

    private byte[] readResource(@Nonnull String file) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (InputStream in = openResource(getClass(), file)) {
            IOUtils.copy(in, out);
        }
        return out.toByteArray();
    }

    @Test(dataProvider = "testData")
    public void test(@Nonnull String badFile, @Nonnull String fixedFile) throws IOException {
        ByteArrayOutputStream actualOut = new ByteArrayOutputStream();
        try (XMLIRIFixerStream fixer = new XMLIRIFixerStream(openResource(getClass(), badFile))) {
            for (int b = fixer.read(); b >= 0; b = fixer.read())
                actualOut.write(b);
        }
        byte[] expected = readResource(fixedFile);
        assertEquals(new String(actualOut.toByteArray(), UTF_8), new String(expected, UTF_8));
        assertEquals(actualOut.toByteArray(), expected);
    }

    @Test(dataProvider = "testData")
    public void testBigChunks(@Nonnull String badFile, @Nonnull String fixedFile) throws IOException {
        ByteArrayOutputStream actualOut = new ByteArrayOutputStream();
        try (XMLIRIFixerStream in = new XMLIRIFixerStream(openResource(getClass(), badFile))) {
            IOUtils.copy(in, actualOut);
        }
        byte[] expected = readResource(fixedFile);
        assertEquals(new String(actualOut.toByteArray(), UTF_8), new String(expected, UTF_8));
        assertEquals(actualOut.toByteArray(), expected);
    }

    @Test(dataProvider = "testData")
    public void testSmallChunks(@Nonnull String badFile,
                                @Nonnull String fixedFile) throws IOException {
        ByteArrayOutputStream actualOut = new ByteArrayOutputStream();
        try (XMLIRIFixerStream in = new XMLIRIFixerStream(openResource(getClass(), badFile))) {
            byte[] buf = {0, 0, 0, 23};
            for (int n = in.read(buf, 0, 3); n >= 0; n = in.read(buf, 0, 3))
                actualOut.write(buf, 0, n);
        }
        ByteArrayOutputStream expectedOut = new ByteArrayOutputStream();
        try (InputStream in = openResource(getClass(), fixedFile)) {
            IOUtils.copy(in, expectedOut);
        }
        byte[] expected = readResource(fixedFile);
        assertEquals(new String(actualOut.toByteArray(), UTF_8), new String(expected, UTF_8));
        assertEquals(actualOut.toByteArray(), expected);
    }

    @Test(dataProvider = "testData")
    public void testTolerateViaRIt(@Nonnull String badFile,
                                    @Nonnull String fixedFile) throws IOException {
        ByteArrayOutputStream actualOut = new ByteArrayOutputStream();
        try (RDFInputStream in = (RDFInputStream) RIt.tolerant(openResource(getClass(), badFile))) {
            IOUtils.copy(in.getInputStream(), actualOut);
        }
        byte[] expected = readResource(fixedFile);
        assertEquals(new String(actualOut.toByteArray(), UTF_8), new String(expected, UTF_8));
        assertEquals(actualOut.toByteArray(), expected);

    }
}