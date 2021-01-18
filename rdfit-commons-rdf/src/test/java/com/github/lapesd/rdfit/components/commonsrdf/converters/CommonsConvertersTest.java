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

package com.github.lapesd.rdfit.components.commonsrdf.converters;

import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.components.converters.util.ConversionCache;
import com.github.lapesd.rdfit.components.converters.util.ConversionPathSingletonCache;
import com.github.lapesd.rdfit.util.NoSource;
import org.apache.commons.rdf.api.*;
import org.apache.commons.rdf.simple.SimpleRDF;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

public class CommonsConvertersTest {
    private static final SimpleRDF SR = new SimpleRDF();
    private static final String EX = "http://example.org/";
    private static final BlankNode B1 = SR.createBlankNode("blank1");
    private static final Literal i23 = SR.createLiteral("23", SR.createIRI("http://www.w3.org/2001/XMLSchema#integer"));
    private static final IRI S1 = SR.createIRI(EX+"S1");
    private static final IRI P1 = SR.createIRI(EX+"P1");
    private static final IRI O1 = SR.createIRI(EX+"O1");
    private static final IRI G1 = SR.createIRI(EX+"G1");

    @DataProvider public @Nonnull Object[][] testData() {
        return Stream.of(
                asList(SR.createTriple(S1, P1, O1), Quad.class, SR.createQuad(null, S1, P1, O1)),
                asList(SR.createTriple(S1, P1, i23), Quad.class, SR.createQuad(null, S1, P1, i23)),
                asList(SR.createTriple(S1, P1, B1), Quad.class, SR.createQuad(null, S1, P1, B1)),

                asList(SR.createQuad(null, S1, P1, O1), Triple.class, SR.createTriple(S1, P1, O1)),
                asList(SR.createQuad(null, S1, P1, i23), Triple.class, SR.createTriple(S1, P1, i23)),
                asList(SR.createQuad(null, S1, P1, B1), Triple.class, SR.createTriple(S1, P1, B1)),

                asList(SR.createQuad(G1, S1, P1, O1), Triple.class, SR.createTriple(S1, P1, O1)),
                asList(SR.createQuad(G1, S1, P1, i23), Triple.class, SR.createTriple(S1, P1, i23)),
                asList(SR.createQuad(G1, S1, P1, B1), Triple.class, SR.createTriple(S1, P1, B1))
        ).map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "testData")
    public void test(@Nonnull Object in, @Nonnull Class<?> desired, @Nonnull Object expected) {
        DefaultConversionManager manager = new DefaultConversionManager();
        CommonsConverters.registerAll(manager);
        ConversionCache cache = ConversionPathSingletonCache.createCache(manager, desired);
        assertEquals(cache.convert(NoSource.INSTANCE, in), expected);
    }
}