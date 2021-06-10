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

package com.github.lapesd.rdfit.source.syntax;

import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import com.github.lapesd.rdfit.source.syntax.impl.TurtleFamilyDetector;
import com.google.common.collect.Lists;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.rdf.TripleWriter;
import org.rdfhdt.hdt.triples.TripleString;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static com.github.lapesd.rdfit.source.syntax.RDFLangs.*;
import static com.github.lapesd.rdfit.util.Utils.openResource;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.testng.Assert.*;

public class RDFLangsTest {
    static {
        JenaSystem.init();
    }

    private static final @Nonnull String EX = "http://example.org/";
    private static final @Nonnull Resource S = ResourceFactory.createResource(EX+"S1");
    private static final @Nonnull Resource B = ResourceFactory.createResource();
    private static final @Nonnull Property P = ResourceFactory.createProperty(EX+"P1");
    private static final @Nonnull Resource O = ResourceFactory.createResource(EX+"O1");
    private static final @Nonnull Literal PL = ResourceFactory.createPlainLiteral("\"pl\"a\"in@");
    private static final @Nonnull Literal SL = ResourceFactory.createTypedLiteral(" \" st@r\"^^\"");
    private static final @Nonnull Literal LL = ResourceFactory.createLangLiteral("'l \"a\"n@g \"", "en");
    private static final @Nonnull Literal IL = ResourceFactory.createTypedLiteral(1);
    private static final @Nonnull Literal BL = ResourceFactory.createTypedLiteral(false);



    private static final List<RDFFormat> JENA_LANGS = asList(
            RDFFormat.NT, RDFFormat.TTL, RDFFormat.TURTLE_PRETTY,
            RDFFormat.TRIG, RDFFormat.TRIG_PRETTY,
            RDFFormat.JSONLD, RDFFormat.JSONLD_COMPACT_PRETTY, RDFFormat.JSONLD_FLATTEN_FLAT,
            RDFFormat.RDFXML, RDFFormat.TRIX
    );
    private static final List<RDFLang> RDFIT_LANGS = asList(
            TRIG, TRIG, TRIG,
            TRIG, TRIG,
            RDFLangs.JSONLD, RDFLangs.JSONLD, RDFLangs.JSONLD,
            RDFLangs.RDFXML, RDFLangs.TRIX
    );

    @DataProvider public @Nonnull Object[][] guessData() throws Exception {
        List<List<Object>> rows = new ArrayList<>();

        List<Boolean> booleans = asList(false, true);
        List<RDFNode> subs = asList(S, B);
        List<RDFNode> preds = asList(P, RDF.type);
        List<RDFNode> objs = asList(O, PL, SL, LL, IL, BL);
        for (List<Object> nodes : Lists.cartesianProduct(booleans, subs, preds, objs)) {
            Model m = ModelFactory.createDefaultModel();
            if (nodes.get(0).equals(Boolean.TRUE))
                m.setNsPrefix("ex", EX);
            m.add((Resource)nodes.get(1), (Property)nodes.get(2), (RDFNode)nodes.get(3));
            for (int i = 0, size = JENA_LANGS.size(); i < size; i++) {
                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    RDFDataMgr.write(out, m, JENA_LANGS.get(i));
                    String input = new String(out.toByteArray(), UTF_8);
                    rows.add(asList(input, RDFIT_LANGS.get(i)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        for (List<RDFNode> nodes : Lists.cartesianProduct(subs, preds, objs)) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                HDTSpecification hdtSpecification = new HDTSpecification();
                try (TripleWriter writer = HDTManager.getHDTWriter(out, EX, hdtSpecification)) {
                    String sub = nodes.get(0).toString(), pred = nodes.get(1).toString();
                    Node o = nodes.get(2).asNode();
                    String obj = o.toString(true);

                    if (o.isLiteral() && o.getLiteralLanguage().isEmpty()
                                      && o.getLiteralDatatypeURI() != null) {
                        obj = obj.replaceAll("([^\\\\]\")\\^\\^([^^]+)$", "$1^^<$2>");
                    }
                    TripleString tripleString = new TripleString(sub, pred, obj);
                    writer.addTriple(tripleString);
                }
                rows.add(asList(out.toByteArray(), RDFLangs.HDT));
            }
        }

        return rows.stream().map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "guessData")
    public void testGuess(@Nonnull Object input, @Nonnull RDFLang expected) throws IOException {
        byte[] bs = input instanceof byte[] ? (byte[]) input : input.toString().getBytes(UTF_8);
        try (ByteArrayInputStream is = new ByteArrayInputStream(bs)) {
            RDFLang lang = RDFLangs.guess(is, Integer.MAX_VALUE);
            if (expected.equals(TRIG))
                assertTrue(asList(RDFLangs.NT, TTL, TRIG).contains(lang));
            else if (expected.equals(RDFLangs.NQ))
                assertTrue(asList(RDFLangs.NT, RDFLangs.NQ).contains(lang));
            else
                assertEquals(lang, expected);
        }
    }

    @DataProvider public Object[][] testGuessTurtleFamilyDetectorData() {
        Object[][] data = new TurtleFamilyDetectorTest().testData();
        Object[][] filtered = new Object[data.length][];
        int i = 0;
        for (Object[] row : data) {
            if (RDFLangs.isKnown((RDFLang) row[2]))
                filtered[i++] = row;
        }
        return filtered;
    }

    @Test(dataProvider = "testGuessTurtleFamilyDetectorData")
    public void testGuessTurtleFamilyDetector(@Nullable byte[] prepend,
                                              @Nonnull Object input,
                                              @Nonnull RDFLang expected) throws IOException {
        byte[] bytes = input instanceof byte[] ? (byte[]) input : input.toString().getBytes(UTF_8);
        int allowedRead = bytes.length;
        if (prepend != null) {
            allowedRead += prepend.length;
            byte[] utf8 = bytes;
            bytes = new byte[prepend.length + utf8.length];
            System.arraycopy(prepend, 0, bytes, 0, prepend.length);
            System.arraycopy(utf8, 0, bytes, prepend.length, utf8.length);
        }
        bytes = Arrays.copyOf(bytes, allowedRead + 2);
        for (int i = allowedRead; i < bytes.length; i++)
            bytes[i] = ' ';
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            RDFLang lang = RDFLangs.guess(is, allowedRead);
            if (expected.equals(TRIG))
                assertTrue(asList(TTL, TRIG).contains(lang));
            else if (expected.equals(RDFLangs.NQ))
                assertTrue(asList(TTL, RDFLangs.NQ).contains(lang));
            else
                assertEquals(lang, expected);
        }
    }

    @Test(dataProvider = "testGuessTurtleFamilyDetectorData")
    public void testGuessTurtleFamilyDetectorHardEnd(@Nullable byte[] prepend,
                                                     @Nonnull Object input,
                                                     @Nonnull RDFLang expected) throws IOException {
        String inputString = input instanceof byte[] ? new String((byte[]) input, UTF_8)
                : input.toString();
        boolean isValid = Pattern.compile("\\.\\s*$").matcher(inputString).find();
        if (!isValid)
            return;

        byte[] bytes = input instanceof byte[] ? (byte[]) input : input.toString().getBytes(UTF_8);
        if (prepend != null) {
            byte[] utf8 = bytes;
            bytes = new byte[prepend.length + utf8.length];
            System.arraycopy(prepend, 0, bytes, 0, prepend.length);
            System.arraycopy(utf8, 0, bytes, prepend.length, utf8.length);
        }
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            RDFLang lang = RDFLangs.guess(is, Integer.MAX_VALUE);
            if (expected.equals(TRIG))
                assertTrue(asList(NT, TTL, TRIG).contains(lang));
            else if (expected.equals(RDFLangs.NQ))
                assertTrue(asList(NT, TTL, RDFLangs.NQ).contains(lang));
            else
                assertEquals(lang, expected);
        }
    }

    @DataProvider public @Nonnull Object[][] fromExtensionData() throws Exception {
        List<List<Object>> rows = asList(
                asList(".ttl", TTL),
                asList("file.ttl", TTL),
                asList("file.nq", RDFLangs.NQ),
                asList("file.nquads", RDFLangs.NQ),
                asList("file.nt", RDFLangs.NT),
                asList("file.ntriples", RDFLangs.NT),
                asList("file.jsonld", RDFLangs.JSONLD),
                asList("file.rj", RDFLangs.RDFJSON),
                asList("file.trig", TRIG),
                asList("file.rdf", RDFLangs.RDFXML),
                asList("file.xml", RDFLangs.RDFXML), //RDFXML takes precedence over OWL
                asList("file.ttl", TTL),
                asList("/tmp/file.ttl", TTL),
                asList("../file.ttl", TTL),
                asList("../file.ttl.gz", RDFLangs.UNKNOWN),
                asList("../ttl.ttl.gz", RDFLangs.UNKNOWN),
                asList("../ttl.ttl.gz-ttl", RDFLangs.UNKNOWN),
                asList("../ttl.ttl.gz ttl", RDFLangs.UNKNOWN),
                asList("http://example.org/data.ttl", TTL),
                asList("http://example.org/data.ttl?param=1", TTL),
                asList("http://example.org/data.ttl?param=1&other=2", TTL),
                asList("http://example.org/data.ttl?param=1&other=2#title", TTL),
                asList("http://example.org/data.ttl?param=1&other=2#nt", TTL),
                asList("http://example.org/data.ttl?ttl=1&other=2#nt", TTL),
                asList("data.ttl?ttl=1&other=2#nt", TTL),
                asList("/path/to/data.ttl?ttl=1&other=2#nt", TTL),
                asList("..//path/to/data.ttl?ttl=1&other=2#nt", TTL)
        );
        List<List<Object>> expanded = new ArrayList<>(rows);
        for (List<Object> row : rows) {
            URI uri = new URI(row.get(0).toString().replace(" ", "%20"));
            expanded.add(asList(uri, row.get(1)));
            if (uri.isAbsolute()) {
                expanded.add(asList(uri.toURL(), row.get(1)));
            } else {
                expanded.add(asList(new File(row.get(0).toString()), row.get(1)));
                expanded.add(asList(new File(row.get(0).toString()).toPath(), row.get(1)));
            }
        }
        return expanded.stream().map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "fromExtensionData")
    public void testFromExtension(@Nonnull Object in, @Nonnull RDFLang expected) {
        RDFLang actual = null;
        if (in instanceof Path)
            actual = RDFLangs.fromExtension((Path) in);
        else if (in instanceof File)
            actual = RDFLangs.fromExtension((File) in);
        else if (in instanceof URI)
            actual = RDFLangs.fromExtension((URI) in);
        else if (in instanceof URL)
            actual = RDFLangs.fromExtension((URL) in);
        else if (in instanceof String)
            actual = RDFLangs.fromExtension(in.toString());
        else
            fail();
        assertEquals(actual, expected);
    }

    @Test
    public void testDetectEmptyInput() throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(new byte[0]);
        assertSame(RDFLangs.guess(is, Integer.MAX_VALUE), RDFLangs.NT);
    }

    @Test
    public void testDetectOnlySpaces() throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(" \t\n   ".getBytes(UTF_8));
        assertSame(RDFLangs.guess(is, Integer.MAX_VALUE), RDFLangs.NT);
    }

    @Test
    public void testDetectOnlyComments() throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream("#...\n\n".getBytes(UTF_8));
        assertSame(RDFLangs.guess(is, Integer.MAX_VALUE), TTL);
    }

    @Test
    public void testRegressionEmptyJSONLD() throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream("[]\n".getBytes(UTF_8));
        assertSame(RDFLangs.guess(is, Integer.MAX_VALUE), RDFLangs.JSONLD);
    }

    @Test
    public void testLMDBRegression() throws IOException {
        String path = "com/github/lapesd/rdfit/source/fixer/lmdb-subset.nt";
        for (Integer bytes : asList(160, 161, 180, 321, 8192)) {
            RDFLang expected = bytes == 8192 ? NT : bytes < 321 ? TRIG : NQ;
            try (InputStream in = openResource(path)) {
                RDFLang guess = RDFLangs.guess(in, bytes);
                assertEquals(guess, expected);
            }
        }
    }

    @Test
    public void testDOCTYPE() throws IOException {
        String path = "com/github/lapesd/rdfit/source/fixer/eswc-2009-complete+DOCTYPE.rdf";
        try (InputStream in = openResource(path)) {
            assertEquals(RDFLangs.guess(in, Integer.MAX_VALUE), RDFXML);
        }
        try (InputStream in = openResource(path)) {
            assertEquals(RDFLangs.guess(in, 550), RDFXML);
        }
    }

}