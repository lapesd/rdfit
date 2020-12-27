package com.github.lapesd.rdfit.components.jena.converters;

import com.github.lapesd.rdfit.components.converters.ConversionFinder;
import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.errors.ConversionException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.Quad;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class JenaConvertersTest {
    private static final @Nonnull String EX = "http://example.org/";
    private @Nonnull ConversionManager mgr;

    @BeforeMethod
    public void setUp() {
        mgr = new DefaultConversionManager();
        JenaConverters.registerAll(mgr);
    }

    @DataProvider public Object[][] testData() {
        Triple triple = new Triple(NodeFactory.createURI(EX + "S"), NodeFactory.createURI(EX + "P"),
                                   NodeFactory.createLiteral("asd", "en"));
        Statement stmt = ResourceFactory.createStatement(
                ResourceFactory.createResource(EX+"S"),
                ResourceFactory.createProperty(EX+"P"),
                ResourceFactory.createLangLiteral("asd", "en"));
        Node graphIRI=  NodeFactory.createURI(EX+"G");
        Quad quadIn = new Quad(graphIRI, NodeFactory.createURI(EX + "S"),
                             NodeFactory.createURI(EX + "P"),
                             NodeFactory.createLiteral("asd", "en"));
        Quad quadOut = new Quad(Quad.defaultGraphIRI, NodeFactory.createURI(EX + "S"),
                                NodeFactory.createURI(EX + "P"),
                                NodeFactory.createLiteral("asd", "en"));

        return Stream.of(
                asList(triple, Statement.class, stmt),
                asList(quadIn, Statement.class, stmt),
                asList(stmt, Triple.class, triple),
                asList(quadIn, Triple.class, triple),
                asList(triple, Quad.class, quadOut),
                asList(stmt, Quad.class, quadOut),

                asList(triple, Triple.class, triple),
                asList(stmt, Statement.class, stmt),
                asList(quadIn, Quad.class, quadIn)
        ).map(List::toArray).toArray(Object[][]::new);

    }

    @Test(dataProvider = "testData")
    public void test(@Nonnull Object in, @Nonnull Class<?> desired, @Nonnull Object expected)
            throws ConversionException {
        ConversionFinder path = mgr.findPath(in, desired);
        assertTrue(path.hasNext());
        assertEquals(path.convert(in), expected);
    }

}