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

package com.github.lapesd.rdfit.components.hdt.iterator;

import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.RIt;
import com.github.lapesd.rdfit.SourceQueue;
import com.github.lapesd.rdfit.impl.DefaultSourceQueue;
import com.github.lapesd.rdfit.iterator.FlatMapRDFIt;
import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.util.Utils;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.triples.TripleString;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class HDTImportingRDFItTest {
    private static final String EX = "http://example.org/";
    private static final String imports = "http://www.w3.org/2002/07/owl#imports";
    private static final String S = EX+"S";
    private static final String P = EX+"P";
    private static final String O = EX+"O";
    private static final String S1 = EX+"S1";
    private static final String P1 = EX+"P1";
    private static final String O1 = EX+"O1";
    private static final String S2 = EX+"S2";
    private static final String P2 = EX+"P2";
    private static final String O2 = EX+"O2";

    @DataProvider public @Nonnull Object[][] getImportIRIData() {
        return Stream.of(
                asList(new TripleString(S, P, O), null),
                asList(new TripleString(S, imports, O), O),
                asList(new TripleString(S, imports, "false"), null),
                asList(new TripleString(S, imports, "http:Request"), null),
                asList(new TripleString(S, imports, "file:///tmp/file.ttl"), "file:///tmp/file.ttl")
        ).map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "getImportIRIData")
    public void testGetImportIRI(@Nonnull TripleString in, @Nullable String expected) {
        assertEquals(HDTImportingRDFIt.getImportIRI(in), expected);
    }

    @Test
    public void testImport() throws Exception {
        HDT hdt = HDTManager.generateHDT(asList(new TripleString(S1, P1, O1),
                new TripleString(S2, P2, O2)).iterator(),
                "file://nowhere.hdt", new HDTSpecification(), (level, message) -> {});
        File hdtFile = Files.createTempFile("rdfit", ".hdt").toFile();
        hdtFile.deleteOnExit();
        String hdtFileURI = Utils.toASCIIString(hdtFile.toURI());
        HDT hdtMain = HDTManager.generateHDT(
                singletonList(new TripleString(S, imports, hdtFileURI)).iterator(),
                "file://nowhere.hdt", new HDTSpecification(), (level, message) -> {});
        try {
            hdt.saveToHDT(hdtFile.getAbsolutePath(), (l, m) -> {});
            RDFItFactory factory = RIt.createFactory();
            DefaultSourceQueue queue = new DefaultSourceQueue();
            queue.add(SourceQueue.When.Soon, hdtMain);
            FlatMapRDFIt<TripleString> fmIt = new FlatMapRDFIt<>(TripleString.class,
                    IterationElement.TRIPLE, queue,
                    s -> factory.iterateTriples(TripleString.class, s), queue);
            List<TripleString> actual = new ArrayList<>();
            try (HDTImportingRDFIt<TripleString> iit = new HDTImportingRDFIt<>(fmIt)) {
                while (iit.hasNext())
                    actual.add(iit.next());
            }
            assertTrue(queue.isClosed());

            List<List<String>> actualLists = actual.stream().map(ts -> asList(
                    ts.getSubject().toString(), ts.getPredicate().toString(),
                    ts.getObject().toString()
            )).collect(Collectors.toList());
            assertEquals(actualLists, asList(
                    asList(S, imports, hdtFileURI),
                    asList(S1, P1, O1),
                    asList(S2, P2, O2)
            ));
        } finally {
            assertTrue(hdtFile.delete());
        }


    }

}