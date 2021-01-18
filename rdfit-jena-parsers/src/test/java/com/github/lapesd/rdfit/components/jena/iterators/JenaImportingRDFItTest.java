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

package com.github.lapesd.rdfit.components.jena.iterators;

import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.RIt;
import com.github.lapesd.rdfit.SourceQueue;
import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.components.converters.util.ConversionCache;
import com.github.lapesd.rdfit.components.converters.util.ConversionPathSingletonCache;
import com.github.lapesd.rdfit.impl.DefaultSourceQueue;
import com.github.lapesd.rdfit.iterator.FlatMapRDFIt;
import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.iterator.RDFIt;
import com.github.lapesd.rdfit.util.NoSource;
import com.github.lapesd.rdfit.util.Utils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.OWL2;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class JenaImportingRDFItTest {
    private static final String EX = "http://example.org/";
    private static final Node S1 = NodeFactory.createURI(EX+"S1");
    private static final Node S2 = NodeFactory.createURI(EX+"S2");
    private static final Node P2 = NodeFactory.createURI(EX+"P2");
    private static final Node O2 = NodeFactory.createURI(EX+"O2");
    private static final Node S3 = NodeFactory.createURI(EX+"S3");
    private static final Node P3 = NodeFactory.createURI(EX+"P3");
    private static final Node O3 = NodeFactory.createURI(EX+"O3");


    @DataProvider public @Nonnull Object[] tripleClassesData() {
        return Stream.of(Triple.class, Statement.class, Quad.class).map(c -> new Object[] {c})
                     .toArray(Object[][]::new);
    }

    @Test(dataProvider = "tripleClassesData")
    public void test(Class<?> tripleClass) throws Exception {
        String prefixes = "@prefix ex: <"+EX+">.\n@prefix owl: <"+ OWL2.getURI() +">.\n";
        File file = Files.createTempFile("rdfit", ".ttl").toFile();
        String fileURI = Utils.toASCIIString(file.toURI());
        String mainTTL = prefixes+"ex:S1 owl:imports <"+fileURI+">.\n" +
                "ex:S2 ex:P2 ex:O2.\n";
        String secondTTL = prefixes+"ex:S3 ex:P3 ex:O3.\n";
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), UTF_8)) {
            w.write(secondTTL);
        }

        List<Object> actual = new ArrayList<>();
        RDFItFactory factory = RIt.createFactory();
        DefaultSourceQueue queue = new DefaultSourceQueue();
        queue.add(SourceQueue.When.Soon, mainTTL);
        boolean[] wasClosed = {false};
        //noinspection unchecked,RedundantCast
        FlatMapRDFIt<Object> fmIt = new FlatMapRDFIt<Object>(tripleClass, IterationElement.TRIPLE,
                        queue, s -> (RDFIt<Object>) factory.iterateTriples(tripleClass, s), queue) {
            @Override public void close() {
                super.close();
                wasClosed[0] = true;
            }
        };
        try (JenaImportingRDFIt<?> iit = new JenaImportingRDFIt<>(fmIt)) {
            while (iit.hasNext())
                actual.add(iit.next());
        }
        assertTrue(wasClosed[0]);

        Node imports = OWL2.imports.asNode();
        List<Triple> expectedTriples = asList(new Triple(S1, imports, NodeFactory.createURI(fileURI)),
                new Triple(S2, P2, O2),
                new Triple(S3, P3, O3));
        DefaultConversionManager mgr = DefaultConversionManager.get();
        ConversionCache cache = ConversionPathSingletonCache.createCache(mgr, tripleClass);
        List<Object> expected = expectedTriples.stream()
                                .map(t -> cache.convert(NoSource.INSTANCE, t)).collect(toList());
        assertEquals(actual, expected);
    }

}