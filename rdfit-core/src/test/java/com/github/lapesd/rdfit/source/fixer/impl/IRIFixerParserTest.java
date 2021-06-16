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
import org.apache.jena.iri.IRIFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.*;

public class IRIFixerParserTest {

    @DataProvider public Object[][] fixData() {
        List<List<Object>> conventionalURIs = Stream.of(
                "http://xmlns.com/foaf/0.1/Person",
                "http://www.w3.org/2001/XMLSchema#integer",
                "http://www.w3.org/2001/XMLSchema#int",
                "http://www.w3.org/2000/01/rdf-schema#Class",
                "http://www.w3.org/1999/02/22-rdf-syntax-ns#nil",
                "http://www.w3.org/2003/01/geo/wgs84_pos#lat"
        ).map(s -> asList((Object) s.getBytes(UTF_8), s)).collect(toList());
        List<List<Object>> validURIs = Stream.of(
                "http://example.org/name",
                "http://example.org/science/publication/book",
                "http://example.org/science/Publication/Book#type",
                "http://example.org/wiki/Category:Books",
                "http://example.org/fold@home",
                "http://example.org/named%20by",
                "https://example.org/Person",
                "http://example~.org/Uncle",
                "file:///absolute.nt#thing",
                "file://relative.nt#other",
                "file://~user/file",
                "file:java.file",
                "file:/java/absolute.file",
                "file:///tmp/abs2.nt",
                "file:///c:/windows/file.nt",
                "coap://example.org/sensor",
                "ftp://example.org/file",
                "http://[::1]/path",
                "http://[::1]",
                "http://[::1]:8080/path",
                "http://[::1]:8080",
                "http://[0:0:0:0:0:0:0:1]/path",
                "http://[0:0:0:0:0:0:0:1]:8080/path",
                "http://[0:0:0:0:0:0:0:1]:8080",
                "http://[0:0:0:0:0:0:0:1]"
        ).map(s -> asList((Object) s.getBytes(UTF_8), s)).collect(toList());
        List<List<Object>> validIRIs = Stream.of(
                "http://example.org/Florianópolis",
                "https://example.org/Sabiá",
                "http://sabiá.org/file#açaí",
                "coap://sabiá.org/file#açaí",
                "file:///tangará.txt",
                "file://tangará.txt",
                "file://tangará.txt",
                "file://tangará.txt",
                "http://example.org/countries/日本",
                "http://日本.jp/voc#jp",
                "http://xx.jp/日本",
                "http://xx.日本/asd",
                "http://user:日本@xx.jp/asd",
                "http://www47.日本.jp/voc#jp",
                "https://www47.日本.jp/voc#jp",
                "file:///日本",
                "file:日本"
                ).map(s -> asList((Object) s.getBytes(UTF_8), s)).collect(toList());
        List<List<Object>> validURNs = Stream.of(
                "urn:plain:freqel",
                "tel:+1-201-555-0123",
                "tel:7042;phone-context=example.com"
        ).map(s -> asList((Object) s.getBytes(UTF_8), s)).collect(toList());

        // ensure all "valid" URIs/IRIs/URNs are valid
        assertTrue(Stream.of(conventionalURIs, validURIs, validIRIs, validURNs)
                .flatMap(List::stream).map(l -> (String)l.get(1))
                .map(IRIFactory.iriImplementation()::create)
                .allMatch(Objects::nonNull));

        List<List<Object>> notPercentEscapedPath = Stream.of(
            asList("http://example.org/~john<doe>", "http://example.org/~john%3Cdoe%3E"),
            asList("http://example.org/~john doe", "http://example.org/~john%20doe"),
            asList("http://example.org/~john+doe`", "http://example.org/~john+doe%60"),
            asList("http://example.org/-`", "http://example.org/-%60"),
            asList("http://example.org/0`", "http://example.org/0%60"),
            asList("http://example.org/9`", "http://example.org/9%60"),
            asList("http://example.org/A`", "http://example.org/A%60"),
            asList("http://example.org/Z`", "http://example.org/Z%60")
        ).map(l -> asList((Object) l.get(0).getBytes(UTF_8), l.get(1))).collect(toList());
        List<List<Object>> badPort = Stream.of(
            asList("http://example.org:http/file", "http://example.org%3Ahttp/file"),
            asList("http://example.org:80x/file", "http://example.org:80/file"),
            asList("http://example.org:80x", "http://example.org:80"),
            asList("http://example.org:/file", "http://example.org%3A/file"),
            asList("http://example.org:", "http://example.org%3A"),
            asList("http://example.org:x", "http://example.org%3Ax")
        ).map(l -> asList((Object) l.get(0).getBytes(UTF_8), l.get(1))).collect(toList());
        List<List<Object>> badUTF8 = asList(
            asList("http://tubarão.com.br/".getBytes(ISO_8859_1), "http://tubaro.com.br/"),
            asList("http://example.org/cão".getBytes(ISO_8859_1), "http://example.org/co"),
            asList("http://example.org/frag#cão".getBytes(ISO_8859_1), "http://example.org/frag#co"),
            asList("http://example.org/query?x=maçã".getBytes(ISO_8859_1), "http://example.org/query?x=ma"),
            asList("http://example.org/query?x=mané".getBytes(ISO_8859_1), "http://example.org/query?x=man")
        );
        List<List<Object>> notPercentEscapedHost = Stream.of(
            asList("http://example<.org/file", "http://example%3C.org/file"),
            asList("http://example<.org/", "http://example%3C.org/"),
            asList("http://example<.org", "http://example%3C.org"),
            asList("http://`example.org{}/", "http://%60example.org%7B%7D/"),
            asList("http://`example.org{}", "http://%60example.org%7B%7D"),
            asList("http://#example.org/", "http://%23example.org/"),
            asList("http://#example.org", "http://%23example.org"),
            asList("http://:example.org/", "http://%3Aexample.org/"),
            asList("http://:example.org", "http://%3Aexample.org")
        ).map(l -> asList((Object) l.get(0).getBytes(UTF_8), l.get(1))).collect(toList());
        List<List<Object>> notPercentEscapedUserInfo = Stream.of(
            asList("http://bob:?@example.org/file", "http://bob:%3F@example.org/file"),
            asList("http://bob:?@example.org/", "http://bob:%3F@example.org/"),
            asList("http://bob:?@example.org", "http://bob:%3F@example.org"),
            asList("http://bob:pwd?x@example.org", "http://bob:pwd%3Fx@example.org"),
            asList("http://bob:pwd?%@example.org/", "http://bob:pwd%3F%25@example.org/"),
            asList("http://bob:pwd?%@example.org", "http://bob:pwd%3F%25@example.org")
        ).map(l -> asList((Object) l.get(0).getBytes(UTF_8), l.get(1))).collect(toList());
        List<List<Object>> badScheme = Stream.of(
                asList("bio2rdf_dataset:bio2rdf-affymetrix-20121004", "bio2rdfdataset:bio2rdf-affymetrix-20121004"),
                asList(" http://example.org/x", "http://example.org/x"),
                asList("some_file", "some_file"),
                asList("  http://example.org/x", "http://example.org/x"),
                asList(" \t\r\n http://example.org/x", "http://example.org/x"),
                asList("%20http://example.org/x", "http://example.org/x"),
                asList("%09%0A%0D %20http://example.org/x", "http://example.org/x")
        ).map(l -> asList((Object) l.get(0).getBytes(UTF_8), l.get(1))).collect(toList());

        // ensure all "fixed" URIs/IRIs/URNs are valid
        assertTrue(
                Stream.of(notPercentEscapedPath, notPercentEscapedHost,
                          notPercentEscapedUserInfo, badPort, badUTF8, badScheme
                ).flatMap(List::stream).map(l -> (String)l.get(1))
                        .map(IRIFactory.iriImplementation()::create)
                        .allMatch(Objects::nonNull)
        );

        return Stream.of(
                conventionalURIs, validURIs, validIRIs, validURNs,
                notPercentEscapedPath, notPercentEscapedHost, notPercentEscapedUserInfo,
                badPort, badUTF8, badScheme
        ).flatMap(List::stream)
                .map(l -> new Object[] {l.get(1), l.get(0)})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "fixData")
    public void testFix(@Nonnull String output, @Nonnull byte[] in) {
        GrowableByteBuffer buffer = new GrowableByteBuffer();
        IRIFixerParser state = new IRIFixerParser(buffer);
        for (byte b : in)
            assertEquals(state.feedByte(b & 0xFF), state);
        state.flush();
        assertEquals(buffer.asString(UTF_8), output);

        // test jena parses the expected IRI
        assertNotNull(IRIFactory.iriImplementation().create(output));
        assertNotNull(ResourceFactory.createResource(output));

        // test Java parses as URI
        assertNotNull(URI.create(output));
    }

}