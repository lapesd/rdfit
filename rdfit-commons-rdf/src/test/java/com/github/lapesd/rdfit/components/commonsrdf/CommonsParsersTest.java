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

package com.github.lapesd.rdfit.components.commonsrdf;

import com.github.lapesd.rdfit.components.ItParser;
import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.components.parsers.DefaultParserRegistry;
import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.iterator.RDFIt;
import org.apache.commons.rdf.api.*;
import org.apache.commons.rdf.simple.SimpleRDF;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.testng.Assert.*;

public class CommonsParsersTest {
    private static final String EX = "http://example.org/";
    private static final SimpleRDF SR = new SimpleRDF();
    private static final IRI G1 = SR.createIRI(EX+"G1");
    private static final IRI S1 = SR.createIRI(EX+"S1");
    private static final IRI P1 = SR.createIRI(EX+"P1");
    private static final IRI O1 = SR.createIRI(EX+"O1");
    private static final BlankNode B1 = SR.createBlankNode("blank1");
    private static final Literal L1 = SR.createLiteral("john", "en");

    private static final Triple T1 = SR.createTriple(S1, P1, O1);
    private static final Triple T2 = SR.createTriple(S1, P1, L1);
    private static final Triple T3 = SR.createTriple(B1, P1, L1);

    private static final Quad Q1 = SR.createQuad(null, S1, P1, O1);
    private static final Quad Q2 = SR.createQuad(null, S1, P1, L1);
    private static final Quad Q3 = SR.createQuad(null, B1, P1, L1);

    private static final Quad R1 = SR.createQuad(G1, S1, P1, O1);
    private static final Quad R2 = SR.createQuad(G1, S1, P1, L1);
    private static final Quad R3 = SR.createQuad(G1, B1, P1, L1);

    private static final Graph graph;
    private static final Dataset ds1, ds2;

    static {
        graph = SR.createGraph();
        graph.add(T1);
        graph.add(T2);
        graph.add(T3);

        ds1 = SR.createDataset();
        ds1.add(R1);
        ds1.add(R2);
        ds1.add(R3);

        ds2 = SR.createDataset();
        ds2.add(Q1);
        ds2.add(Q2);
        ds2.add(R1);
        ds2.add(R3);
    }

    @DataProvider public @Nonnull Object[][] iterateData() {
        return Stream.of(
                asList(graph, IterationElement.TRIPLE, asList(T1, T2, T3)),
                asList(graph, IterationElement.QUAD, null),
                asList(ds1, IterationElement.TRIPLE, asList(T1, T2, T3)),
                asList(ds1, IterationElement.QUAD, asList(R1, R2, R3)),
                asList(ds2, IterationElement.TRIPLE, asList(T1, T2, T1, T3)),
                asList(ds2, IterationElement.QUAD, asList(Q1, Q2, R1, R3))
        ).map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "iterateData")
    public void testIterate(@Nonnull Object source, @Nonnull IterationElement itEl,
                            @Nullable Collection<?> expected) {
        DefaultParserRegistry registry = new DefaultParserRegistry();
        DefaultConversionManager convMgr = new DefaultConversionManager();
        registry.setConversionManager(convMgr);
        CommonsParsers.registerAll(registry);

        ItParser parser = registry.getItParser(source, itEl, null);
        if (expected == null) {
            assertNull(parser);
            return;
        } else {
            assertNotNull(parser);
        }

        assertNotNull(expected);
        List<Object> actual = new ArrayList<>();
        try (RDFIt<Object> it = parser.parse(source)) {
            it.forEachRemaining(actual::add);
        }
        assertEquals(actual.size(), expected.size());
        assertEquals(new HashSet<>(actual), new HashSet<>(expected));
    }

}