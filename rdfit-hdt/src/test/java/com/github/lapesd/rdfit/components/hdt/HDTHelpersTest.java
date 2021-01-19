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

package com.github.lapesd.rdfit.components.hdt;

import com.github.lapesd.rdfit.RIt;
import com.github.lapesd.rdfit.components.hdt.listeners.HDTBufferFeeder;
import com.github.lapesd.rdfit.components.hdt.listeners.HDTFileFeeder;
import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.iterator.PlainRDFIt;
import com.github.lapesd.rdfit.iterator.RDFIt;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TripleString;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.testng.Assert.*;

public class HDTHelpersTest {
    private static final String EX = "http://example.org/";
    private static final String S1 = EX+"S1";
    private static final String S2 = "_:bnode1";
    private static final String P = EX+"P";
    private static final String O1 = EX+"O1";
    private static final String O2 = "_:bnode2";
    private static final String O3 = "\"1\"^^xsd:int";

    private static final List<TripleString> TRIPLES = asList(
            new TripleString(S1, P, O1),
            new TripleString(S2, P, O1),
            new TripleString(S1, P, O2),
            new TripleString(S1, P, O3)
    );

    private final List<File> tempFiles = new ArrayList<>();


    @AfterClass
    public void afterClass() {
        for (File file : tempFiles)
            assertTrue(!file.exists() || file.delete());
        tempFiles.clear();
    }

    private @Nonnull File createFile() throws IOException {
        File file = Files.createTempFile("rdfit", ".hdt").toFile();
        file.deleteOnExit();
        tempFiles.add(file);
        return file;
    }

    private @Nonnull RDFIt<TripleString> createIt() {
        return new PlainRDFIt<>(TripleString.class, IterationElement.TRIPLE, TRIPLES.iterator(), TRIPLES);
    }

    private void checkHDT(@Nonnull HDT hdt) throws NotFoundException {
        IteratorTripleString it = hdt.search(null, null, null);
        Set<List<String>> actual = new HashSet<>();
        Set<List<String>> expected = new HashSet<>();
        while (it.hasNext()) {
            TripleString ts = it.next();
            actual.add(asList(ts.getSubject().toString(), ts.getPredicate().toString(),
                              ts.getObject().toString()));
        }
        for (TripleString ts : TRIPLES) {
            expected.add(asList(ts.getSubject().toString(), ts.getPredicate().toString(),
                                ts.getObject().toString()));
        }
        assertEquals(actual, expected);
    }

    @Test
    public void testWriteIteratorToFile() throws IOException, NotFoundException {
        File file = createFile();
        File sameFile = HDTHelpers.toHDTFile(file, createIt());
        assertSame(sameFile, file);
        checkHDT(HDTManager.mapHDT(file.getAbsolutePath()));
        assertTrue(file.length() > 0);
    }

    @Test
    public void testListenToFile() throws IOException, NotFoundException {
        File file = createFile();
        HDTFileFeeder listener = HDTHelpers.fileFeeder(file);
        assertSame(listener.getFile(), file);
        RIt.parse(listener, TRIPLES);
        assertTrue(file.length() > 0);
        checkHDT(HDTManager.mapHDT(file.getAbsolutePath()));
    }

    @Test
    public void testWriteIteratorToHDT() throws NotFoundException {
        checkHDT(HDTHelpers.toHDT(createIt()));
    }

    @Test
    public void testListenToBuffer() throws IOException, NotFoundException {
        HDTBufferFeeder listener = HDTHelpers.feeder();
        RIt.parse(listener, TRIPLES);
        HDT hdt = HDTManager.loadHDT(new ByteArrayInputStream(listener.getBuffer()));
        checkHDT(hdt);
    }
}