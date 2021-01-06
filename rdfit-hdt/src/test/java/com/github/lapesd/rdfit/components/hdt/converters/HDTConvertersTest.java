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

package com.github.lapesd.rdfit.components.hdt.converters;

import com.github.lapesd.rdfit.components.converters.ConversionFinder;
import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.errors.ConversionException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.sparql.core.Quad;
import org.rdfhdt.hdt.triples.TripleString;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.tuple.ImmutablePair.of;
import static org.apache.jena.graph.NodeFactory.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class HDTConvertersTest {
    private static final String EX = "http://example.org/";
    private @Nonnull ConversionManager mgr;

    @BeforeMethod
    public void setUp() {
        mgr = new DefaultConversionManager();
        HDTConverters.registerAll(mgr);
    }

    @DataProvider public Object[][] testData() {
        List<ImmutablePair<String, Node>> subjects = asList(
                of(EX+"a", createURI(EX+"a")),
                of("_:blank1", createBlankNode("blank1"))
        );
        List<ImmutablePair<String, Node>> objects = asList(
                of("1", createLiteral("1", XSDDatatype.XSDinteger)),
                of("true", createLiteral("true", XSDDatatype.XSDboolean)),
                of("\"a\"", createLiteral("a", XSDDatatype.XSDstring)),
                of("\"b\"@en", createLiteral("b", "en")),
                of("'c'@en-US", createLiteral("c", "en-US")),
                of("\"\"\"d\nd\"\"\"@pt", createLiteral("d\nd", "pt")),
                of("''' e\ne '''^^<http://www.w3.org/2001/XMLSchema#string>", createLiteral(" e\ne ", XSDDatatype.XSDstring)),
                of("'123'^^xsd:int", createLiteral("123", XSDDatatype.XSDint))
        );
        String predStr = EX+"p";
        Node pred = createURI(predStr);
        List<List<Object>> rows = new ArrayList<>();
        for (List<ImmutablePair<String, Node>> ss : Lists.cartesianProduct(subjects, objects)) {
            TripleString ts = new TripleString(ss.get(0).left, EX + "p", ss.get(1).left);
            Triple triple = new Triple(ss.get(0).right, pred, ss.get(1).right);
            rows.add(asList(ts, Triple.class, triple));

            Statement stmt = ResourceFactory.createStatement(
                    new ResourceImpl(ss.get(0).right, null),
                    new PropertyImpl(pred, null),
                    new LiteralImpl(ss.get(1).right, null));
            rows.add(asList(ts, Statement.class, stmt));

            rows.add(asList(ts, Quad.class, new Quad(Quad.defaultGraphIRI, triple)));
        }
        return rows.stream().map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "testData")
    public void test(@Nonnull Object in, @Nonnull Class<?> desired,
                     @Nonnull Object expected) throws ConversionException {
        ConversionFinder finder = mgr.findPath(in, desired);
        assertTrue(finder.hasNext());
        assertEquals(finder.getConversionPath().convert(in), expected);
    }

}