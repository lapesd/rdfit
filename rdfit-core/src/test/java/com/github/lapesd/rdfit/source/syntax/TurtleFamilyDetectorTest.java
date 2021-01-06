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
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.lapesd.rdfit.source.syntax.RDFLangs.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

public class TurtleFamilyDetectorTest {
    private static final @Nonnull String EX = "http://example.org/";

    private static @Nonnull Object trigToLang(@Nonnull String trig, @Nonnull Lang lang) {
        Model m = ModelFactory.createDefaultModel();
        RDFDataMgr.read(m, new ByteArrayInputStream(trig.getBytes(UTF_8)), Lang.TRIG);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            RDFDataMgr.write(out, m, lang);
            if (lang.equals(Lang.RDFTHRIFT))
                return out.toByteArray();
            else
                return out.toString("UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @DataProvider public Object[][] testData() {
        List<List<Object>> rows = new ArrayList<>();
        List<String> subjects = asList("<a>", "<"+EX+"s>",  "<"+EX+"ns#s>", "_:sub");
        List<String> predicates = asList("<p>", "<"+EX+"p>", "a");
        List<String> objects = asList(
                "<o>",
                "<"+EX+"o>",
                "<"+EX+"ns#o>",
                "_:obj",
                "1",
                "false",
                "\"asd\"", "\"asd\"@en", "\"asd\"@en-US", "\"asd\"@en-US",
                "\"asd\"^^<http://www.w3.org/2001/XMLSchema#string>",
                "\" asd\"",
                "\"a sd\"",
                "\"asd \"",
                "'asd'^^<http://www.w3.org/2001/XMLSchema#string>",
                "'''asd'''^^<http://www.w3.org/2001/XMLSchema#string>",
                "\"\"\"asd\"\"\"^^<http://www.w3.org/2001/XMLSchema#string>",
                "\"asd\\\"\"^^<http://www.w3.org/2001/XMLSchema#string>",
                "\"\\\"asd\\\"\"^^<http://www.w3.org/2001/XMLSchema#string>",
                "\"\\\"asd\"^^<http://www.w3.org/2001/XMLSchema#string>",

                "\"<c> <d>\"",
                "\"\"\"<c> <d>\"\"\"",
                "'<c> <d>'",
                "'''<c> <d>'''",

                "\"<c>,<d>\"",
                "\"\"\"<c>,<d>\"\"\"",
                "'<c>,<d>'",
                "'''<c>,<d>'''",

                "\"<c>, <d>\"",
                "\"\"\"<c>, <d>\"\"\"",
                "'<c>, <d>'",
                "'''<c>, <d>'''",

                "\"<c> , <d>\"",
                "\"\"\"<c> , <d>\"\"\"",
                "'<c> , <d>'",
                "'''<c> , <d>'''",

                "\"<c> <d>\"@en",
                "\"\"\"<c> <d>\"\"\"@en",
                "'<c> <d>'@en",
                "'''<c> <d>'''@en",
                "\"<c> <d>\"^^<http://www.w3.org/2001/XMLSchema#int>"
        );
        // positive examples
        for (List<String> ss : Lists.cartesianProduct(subjects, predicates, objects)) {
            String line = ss.get(0) + " " + ss.get(1) + " " + ss.get(2) + ".";
            rows.add(asList(null, line, TRIG));
            line = ss.get(0) + "\n\t" + ss.get(1) + "\n\t" + ss.get(2) + ".";
            rows.add(asList(null, line, TRIG));
            // although invalid, the detector cannot assume all input will be fed
            line = ss.get(0) + " " + ss.get(1) + " " + ss.get(2) + ";";
            rows.add(asList(null, line, TRIG));
            line = ss.get(0) + " " + ss.get(1) + " " + ss.get(2) + " \n\t , ";
            rows.add(asList(null, line, TRIG));
            // end() should settle for TRIG
            line = ss.get(0) + " " + ss.get(1) + " " + ss.get(2);
            rows.add(asList(null, line, TRIG));
            // ,-separated objects
            line = ss.get(0) + " " + ss.get(1) + " " + ss.get(2) + "," + ss.get(0) + ".\n";
            rows.add(asList(null, line, TRIG));
            line = ss.get(0) + " " + ss.get(1) + " " + ss.get(2) + " ," + ss.get(0) + " .\n";
            rows.add(asList(null, line, TRIG));
            line = ss.get(0) + " " + ss.get(1) + " " + ss.get(2) + " , " + ss.get(0) + " . \n";
            rows.add(asList(null, line, TRIG));

            // [] in subject position
            line = "[ "+ ss.get(1) + " " + ss.get(2) +" ].";
            rows.add(asList(null, line, TRIG));
            line = "["+ ss.get(1) + " " + ss.get(2) +"]";
            rows.add(asList(null, line, TRIG));
            line = "[] " + ss.get(1) + " " + ss.get(2);
            rows.add(asList(null, line, TRIG));
            line = "[ ] " + ss.get(1) + " " + ss.get(2);
            rows.add(asList(null, line, TRIG));

            // [] in object position
            line = ss.get(0) + " " + ss.get(1) + " [ " + ss.get(1) + " " + ss.get(2) + "];";
            rows.add(asList(null, line, TRIG));
            line = ss.get(0) + " " + ss.get(1) + " []";
            rows.add(asList(null, line, TRIG));

            // NQ examples
            if (!ss.get(0).startsWith("_") && !ss.get(1).equals("a")) {
                line = ss.get(0) + " " + ss.get(1) + " " + ss.get(2) + " " + ss.get(0) + ".\n";
                rows.add(asList(null, line, NQ));
                line = ss.get(0) + "\t" + ss.get(1) + "\t" + ss.get(2) + "\n\t" + ss.get(0) + ".";
                rows.add(asList(null, line, NQ));
                line = ss.get(0) + "\t" + ss.get(1) + "\t" + ss.get(2) + "\r\n\t" + ss.get(0) + ".";
                rows.add(asList(null, line, NQ));
                line = ss.get(0) + " " + ss.get(1) + " " + ss.get(2) +  " " + ss.get(0) +"\n";
                rows.add(asList(null, line, NQ));

                line = ss.get(0) + " " + ss.get(1) + " " + ss.get(0) + ".\n" +
                       ss.get(0) + " " + ss.get(1) + " " + ss.get(2) + " " + ss.get(0);
                rows.add(asList(null, line, NQ));

                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < 3; i++) {
                    builder.append(ss.get(0)).append(' ').append(ss.get(1)).append(' ')
                           .append(ss.get(2)).append(".\n");
                }
                rows.add(asList(null, builder.toString(), NQ));
                builder.setLength(builder.length()-2);
                rows.add(asList(null, builder.toString(), NQ));
            }
        }

        rows.add(asList(null, "@base <"+EX+">.\n", TRIG));
        rows.add(asList(null, "@base <"+EX+">.", TRIG));
        rows.add(asList(null, "@prefix x: <"+EX+">.", TRIG));
        rows.add(asList(null, "@prefix x: <"+EX+">.\n", TRIG));
        rows.add(asList(null, "@prefix : <"+EX+">.", TRIG));
        rows.add(asList(null, "# comment\n@prefix : <"+EX+">.", TRIG));
        rows.add(asList(null, "# comment\n@prefix : <"+EX+">. # comment 2", TRIG));
        rows.add(asList(null, "BASE <"+EX+">\n", TRIG));
        rows.add(asList(null, "BASE <"+EX+">", TRIG));
        rows.add(asList(null, "PREFIX x: <"+EX+">", TRIG));
        rows.add(asList(null, "PREFIX x: <"+EX+">\n", TRIG));
        rows.add(asList(null, "PREFIX : <"+EX+">", TRIG));
        rows.add(asList(null, "BASE ", TRIG));
        rows.add(asList(null, "BASE <", TRIG));
        rows.add(asList(null, "PREFIX ", TRIG));

        //negative examples
        for (List<String> ss : Lists.cartesianProduct(subjects, predicates, objects)) {
            String line;
            //literal in subject position
            if (!ss.get(2).startsWith("<") && !ss.get(2).startsWith("_")) {
                line = ss.get(2) + " " + ss.get(1) + " " + ss.get(0) + " .\n";
                rows.add(asList(null, line, UNKNOWN));
            }

            //no separators
            if (!( ss.get(0).startsWith("_") &&
                   !ss.get(1).contains(":") && !ss.get(1).startsWith("<") &&
                   !ss.get(2).startsWith("\"") && !ss.get(2).startsWith("<") &&
                   !ss.get(2).contains(":"))) {
                line = ss.get(0) + ss.get(1) + ss.get(2) + "\n";
                rows.add(asList(null, line, UNKNOWN));
            }

            String ok = ss.get(0) + " " + ss.get(1) + " " + ss.get(2) + " .\n";
            rows.add(asList(null, trigToLang(ok, Lang.JSONLD), UNKNOWN));
            rows.add(asList(null, trigToLang(ok, Lang.RDFJSON), UNKNOWN));
            rows.add(asList(null, trigToLang(ok, Lang.RDFTHRIFT), UNKNOWN));
            rows.add(asList(null, trigToLang(ok, Lang.RDFXML), UNKNOWN));
        }

        rows.add(asList(null, "{\"@graph\": [ {", UNKNOWN));
        rows.add(asList(null, "{ \"@graph\": [ {", UNKNOWN));
        rows.add(asList(null, " { \"@graph\": [ {", UNKNOWN));

        // create variations with BOMs
        List<List<Object>> expanded = new ArrayList<>(rows);
        for (List<Object> row : rows) {
            ArrayList<Object> prefixed = new ArrayList<>(row);
            prefixed.set(0, new byte[]{(byte)0xef, (byte)0xbb, (byte)0xbf});
            expanded.add(prefixed);
            prefixed = new ArrayList<>(row);
            prefixed.set(0, new byte[]{(byte)0xfe, (byte)0xff});
            expanded.add(prefixed);
            prefixed = new ArrayList<>(row);
            prefixed.set(0, new byte[]{(byte)0xff, (byte)0xfe});
            expanded.add(prefixed);
        }
        return expanded.stream().map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "testData")
    public void test(@Nullable byte[] prepend, @Nonnull Object input, @Nonnull RDFLang expected) {
        doTest(prepend, input, expected);
    }

    private void doTest(@Nullable byte [] prepend, @Nonnull Object input, @Nonnull RDFLang expected) {
        byte[] bytes = input instanceof byte[] ? (byte[]) input : input.toString().getBytes(UTF_8);
        if (prepend != null) {
            byte[] utf8 = bytes;
            bytes = new byte[prepend.length + utf8.length];
            System.arraycopy(prepend, 0, bytes, 0, prepend.length);
            System.arraycopy(utf8, 0, bytes, prepend.length, utf8.length);
        }
        TurtleFamilyDetector detector = new TurtleFamilyDetector();
        LangDetector.State state = detector.createState();
        RDFLang detected = null;
        for (byte value : bytes) {
            RDFLang lang = state.feedByte(value);
            if (detected != null)
                assertEquals(lang, detected);
            if (lang != null)
                detected = lang;
        }
        RDFLang lang = state.end();
        if (detected != null)
            assertEquals(lang, detected);
        detected = lang;
        assertEquals(detected, expected);
    }

}