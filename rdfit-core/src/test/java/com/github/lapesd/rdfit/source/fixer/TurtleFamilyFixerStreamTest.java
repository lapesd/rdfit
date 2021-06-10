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
import com.github.lapesd.rdfit.source.syntax.RDFLangs;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.github.lapesd.rdfit.util.Utils.openResource;
import static com.google.common.collect.Lists.cartesianProduct;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TurtleFamilyFixerStreamTest {

    @DataProvider public static Object[][] testData() {
        List<String> preambles = asList("", "  \n\t\n ",
                "@base <http://example.org/> .\n",
                "@prefix ex: <http://example.org/ns>.\n@base <http://example.org>\n . \n");
        List<String> subs = asList("<a>", "_:a", "[]", "ex:a");
        List<String> preds = asList("<p>", "ex:p");
        List<String> singleObjects = asList("<c>", "_:c", "[]", "ex:c",
                /* string literals */
                "\"plain\"",
                "\"lang\"@en",
                "\"typed\"^^<http://www.w3.org/2001/XMLSchema#string>",

                /* empty string literals  */
                "\"\"",
                "\"\"\"\"\"\"",
                "''",
                "''''''",
                "\"\"@en",
                "\"\"^^<http://www.w3.org/2001/XMLSchema#string>",

                /* valid unquoted literals */
                "2",
                "false",
                "2.34",
                "+2.34",
                "-2.34",
                "-2.34e2",
                ".34E2",
                "2e-3",

                /* valid non-string quoted literals */
                "\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>",
                "\"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>",
                "\"2.34\"^^<http://www.w3.org/2001/XMLSchema#decimal>"
        );
        List<String> objs = cartesianProduct(singleObjects, singleObjects).stream()
                .map(l -> String.join(", ", l)).collect(toList());
        List<List<Object>> validData = cartesianProduct(preambles, subs, preds, objs).stream()
                .map(strings -> strings.get(0) + String.join(" ", strings.subList(1, 4)))
                .map(string -> asList((Object) string, string))
                .collect(toList());
        List<List<Object>> validTurtle = Stream.of("<a> a <Class>.",
                "_:bnode a <Class>",
                "<x> a _:bClass",
                "<http://example.org/object> a ex:Class.",
                "<x> a [rdfs:subClassOf ex:Class].",
                "<x> a  [ rdfs:subClassOf ex:Class ] .",
                "<x> a  [ rdfs:subClassOf ex:Class, <other> ] .",
                "<x> a  [ rdfs:subClassOf ex:Class, <other>; rdfs:label \"asd\" ] .",
                "<x> a  [ rdfs:subClassOf ex:Class, <other> ; rdfs:label \"asd\" ] .",
                "<x> a  [ rdfs:subClassOf ex:Class, <other>;rdfs:label \"asd\" ] .",
                "@prefix a:<http://example.org/a>.\n<s> a a: .",
                "@prefix a:<http://example.org/a>.\n<s> a a: ; a <C>."
        ).map(s -> asList((Object) s, s)).collect(toList());

        List<List<String>> fixedPairs = asList(
                asList("<asd qwe>", "<asd%20qwe>"),
                asList("<asd >", "<asd%20>"),
                asList("<as d>", "<as%20d>"),
                asList("<a sd>", "<a%20sd>"),
                asList("< asd>", "<%20asd>"),
                asList("<asd{}>", "<asd%7B%7D>"),
                asList("<asd%3E.`>", "<asd%3E.%60>"),
                asList("\"fr-1\"@fr_123", "\"fr-1\"@fr"),
                asList("\"fr-2\"@fr-xx", "\"fr-2\"@fr"),
                asList("\"en-1\"@en_US", "\"en-1\"@en"),
                asList("\"line\nbreak\"", "\"line\\nbreak\""),
                asList("'line\n\r break'", "'line\\n\\r break'"),
                asList("'\tallow\ttab and\b'", "'\tallow\ttab and\b'"),
                asList("\"\"\"valid\nbreak\"\"\"@pt_BR", "\"\"\"valid\nbreak\"\"\"@pt"),
                asList("\"allow\\\"escapes\"^^<http://www.w3.org/2001/XMLSchema#>", "\"allow\\\"escapes\"^^<http://www.w3.org/2001/XMLSchema#>"),
                asList("\"fix\\ non-escapes\\x\\y\\W\\N\"", "\"fix\\\\ non-escapes\\\\x\\\\y\\\\W\\\\N\""),
                asList("'preserve\\'valid\\t escapes\\\"'", "'preserve\\'valid\\t escapes\\\"'"),
                asList("<http://www.mon deca.com>", "<http://www.mon%20deca.com>"),
                asList("<http://www.mon~deca.com>", "<http://www.mon~deca.com>"),
                asList("<http://www.mon ~ deca.com>", "<http://www.mon%20~%20deca.com>"),
                asList("<http://www.mon:deca.com>", "<http://www.mon%3Adeca.com>"),
                asList("<http://www.mondeca.com ~ http://www.lalic.paris4.sorbonne.fr/>", "<http://www.mondeca.com%20~%20http%3A//www.lalic.paris4.sorbonne.fr/>"),
                asList("\"missing hat\"^<http://www.w3.org/2001/XMLSchema#string>", "\"missing hat\"^^<http://www.w3.org/2001/XMLSchema#string>"),
                asList("\"extra hat\"^^^<http://www.w3.org/2001/XMLSchema#string>", "\"extra hat\"^^<http://www.w3.org/2001/XMLSchema#string>"),
                asList("X", "\"X\""),
                asList("FALSE", "false"),
                asList("True", "true"),
                asList("true1", "\"true1\""),
                asList("b-c", "\"b-c\"")
        );
        List<List<Object>> invalidData = new ArrayList<>();
        for (String preamble : preambles) {
            for (String validObj : singleObjects) {
                for (String subj : subs) {
                    List<List<String>> base = fixedPairs.stream().map(pair -> asList(
                            (subj + " ex:p " + pair.get(0)),
                            (subj + " ex:p " + pair.get(1)))).collect(toList());
                    for (List<String> pair : base) {
                        invalidData.add(asList(
                                preamble+pair.get(0)+".\n"+subj+" ex:p "+validObj,
                                preamble+pair.get(1)+".\n"+subj+" ex:p "+validObj
                        ));
                    }
                }
            }
        }
        return concat(concat(validData.stream(), validTurtle.stream()), invalidData.stream())
                     .map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "testData")
    public void test(@Nonnull String in, @Nonnull String expected) throws Exception {
        boolean[] closed = {false};
        ByteArrayInputStream bis = new ByteArrayInputStream(in.getBytes(UTF_8)) {
            @Override public void close() throws IOException {
                super.close();
                closed[0] = true;
            }
        };
        try (TurtleFamilyFixerStream upgrader = new TurtleFamilyFixerStream(bis)) {
            assertEquals(IOUtils.toString(upgrader, UTF_8), expected);
        }
        assertTrue(closed[0]);
    }

    @Test(dataProvider = "testData")
    public void testReadSmallChunks(@Nonnull String in, @Nonnull String expected) throws Exception {
        byte[] utfBytes = in.getBytes(UTF_8);
        try (TurtleFamilyFixerStream fixer = new TurtleFamilyFixerStream(new ByteArrayInputStream(utfBytes))) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[3];
            int n;
            while ((n = fixer.read(buf, 1, 2)) > -1) {
                out.write(buf, 1, n);
            }
            assertEquals(new String(out.toByteArray(), UTF_8), expected);
        }
    }

    @Test
    public void testRegressionOpenLexicalForm() throws IOException {
        String string = "<a> <p> \"\\\"g\" ;\n  <q> <o> .";
        byte[] bytes = string.getBytes(UTF_8);
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        try (InputStream in = new TurtleFamilyFixerStream(new ByteArrayInputStream(bytes))) {
            IOUtils.copy(in, actual);
        }
        assertEquals(new String(actual.toByteArray(), UTF_8), string);
        assertEquals(actual.toByteArray(), bytes);
    }

    @DataProvider public @Nonnull Object[][] resourceFilesData() {
        return Stream.of(
                asList("lmdb-subset.bad-space.nt", "lmdb-subset.nt"),
                asList("linkedtcga-a-expression_gene_Lookup.unquoted.nt",
                       "linkedtcga-a-expression_gene_Lookup.nt")
        ).map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "resourceFilesData")
    public void testResourceFiles(@Nonnull String badFile,
                                  @Nonnull String expectedFile) throws IOException {
        ByteArrayOutputStream ac = new ByteArrayOutputStream(), ex = new ByteArrayOutputStream();
        try (InputStream in = new TurtleFamilyFixerStream(openResource(getClass(), badFile))) {
            IOUtils.copy(in, ac);
        }
        try (InputStream in = openResource(getClass(), expectedFile)) {
            IOUtils.copy(in, ex);
        }
        assertEquals(new String(ac.toByteArray(), UTF_8), new String(ex.toByteArray(), UTF_8));
        assertEquals(ac.toByteArray(), ex.toByteArray());
    }

    @DataProvider public @Nonnull Object[][] validFilesData() {
        return Stream.of(
                "geonames-1.n3",
                "geonames-2.n3",
                "skos_categories_en.uchar.nt",
                "TCGA-BF-A1PU-01A-11D-A18Z-02_BC0VYMACXX---TCGA-BF-A1PU-10A-01D-A18Z-02_BC0VYMACXX---Segment.tsv.n3",
                "nationwidechildrens.org_biospecimen_tumor_sample_lgg.nt"
        ).map(s -> new Object[]{s}).toArray(Object[][]::new);
    }

    @Test(dataProvider = "validFilesData")
    public void testValidFiles(@Nonnull String filename) throws IOException {
        ByteArrayOutputStream ex = new ByteArrayOutputStream();
        try (InputStream in = openResource(getClass(), filename)) {
            IOUtils.copy(in, ex);
        }
        ByteArrayOutputStream ac = new ByteArrayOutputStream();
        try (InputStream fixer = new TurtleFamilyFixerStream(openResource(getClass(), filename))) {
            IOUtils.copy(fixer, ac);
        }
        assertEquals(new String(ac.toByteArray(), UTF_8), new String(ex.toByteArray(), UTF_8));
        assertEquals(ac.toByteArray(), ex.toByteArray());

        Model acModel = createDefaultModel(), exModel = createDefaultModel();
        RDFDataMgr.read(acModel, new ByteArrayInputStream(ac.toByteArray()), Lang.TTL);
        RDFDataMgr.read(exModel, new ByteArrayInputStream(ex.toByteArray()), Lang.TTL);
        assertTrue(acModel.isIsomorphicWith(exModel));
    }

    @Test(dataProvider = "testData")
    public void testTolerateViaRIt(@Nonnull String in, @Nonnull String expected) throws Exception {
        RDFLang lang = RDFLangs.guess(new ByteArrayInputStream(in.getBytes(UTF_8)), 8192);
        if (lang != RDFLangs.UNKNOWN) {
            Object tol = RIt.tolerant(in);
            assertTrue(tol instanceof RDFInputStream);
            assertEquals(IOUtils.toString(((RDFInputStream) tol).getInputStream(), UTF_8), expected);
        }
    }

    @Test
    public void testBadUtf8() throws IOException {
        //           <     a     >           <     p     >           "     c     ?            ?            "
        byte[] in = {0x3c, 0x61, 0x3e, 0x20, 0x3c, 0x70, 0x3e, 0x20, 0x22, 0x63, (byte) 0xc3, (byte) 0xa7, 0x22};
        TurtleFamilyFixerStream fixer = new TurtleFamilyFixerStream(new ByteArrayInputStream(in));
        assertEquals(IOUtils.toString(fixer, UTF_8), "<a> <p> \"cç\"");
    }

}