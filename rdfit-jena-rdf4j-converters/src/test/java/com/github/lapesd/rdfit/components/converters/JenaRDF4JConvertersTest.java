package com.github.lapesd.rdfit.components.converters;

import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.errors.ConversionException;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.sparql.core.Quad;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class JenaRDF4JConvertersTest {
    private ConversionManager mgr;
    private static final String EX = "http://example.org/";
    private static final Node S1 = NodeFactory.createURI(EX+"S1");
    private static final Node S2 = NodeFactory.createBlankNode("S2");
    private static final Node P1 = NodeFactory.createURI(EX+"P1");
    private static final Node O1 = NodeFactory.createURI(EX+"O1");
    private static final Node O2 = NodeFactory.createBlankNode("O2");
    private static final Node O3 = NodeFactory.createLiteral("O3");
    private static final Node O4 = NodeFactory.createLiteral("O4", "en");
    private static final Node O5 = NodeFactory.createLiteral("O5", XSDDatatype.XSDstring);
    private static final Node G1 = NodeFactory.createURI(EX+"G1");

    private static final ValueFactory VF = SimpleValueFactory.getInstance();
    private static final Value rS1 = VF.createIRI(EX+"S1");
    private static final Value rS2 = VF.createBNode("S2");
    private static final IRI   rP1 = VF.createIRI(EX+"P1");
    private static final Value rO1 = VF.createIRI(EX+"O1");
    private static final Value rO2 = VF.createBNode("O2");
    private static final Value rO3 = VF.createLiteral("O3");
    private static final Value rO4 = VF.createLiteral("O4", "en");
    private static final Value rO5 = VF.createLiteral("O5", VF.createIRI(XSDDatatype.XSDstring.getURI()));
    private static final IRI   rG1 = VF.createIRI(EX+"G1");

    @BeforeClass
    public void beforeClass() {
        mgr = new DefaultConversionManager();
        JenaRDF4JConverters.registerAll(mgr);
    }

    @DataProvider public static Object[][] testData() {
        SimpleValueFactory VF = SimpleValueFactory.getInstance();
        List<Node> jSubjects = asList(S1, S2), jObjects = asList(O1, O2, O3, O4, O5);
        List<Value> rSubjects = asList(rS1, rS2), rObjects = asList(rO1, rO2, rO3, rO4, rO5);
        List<List<Object>> rows = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 5; j++) {
                Node  js = jSubjects.get(i), jo = jObjects.get(j);
                Resource rs = (Resource) rSubjects.get(i);
                Value ro = rObjects.get(j);
                Statement rStmt = VF.createStatement(rs, rP1, ro);
                Statement rQuad = VF.createStatement(rs, rP1, ro, rG1);
                Triple triple = new Triple(js, P1, jo);
                Quad quad = new Quad(Quad.defaultGraphIRI, js, P1, jo);
                Quad quad2 = new Quad(G1, js, P1, jo);
                org.apache.jena.rdf.model.Statement stmt = ResourceFactory.createStatement(
                        new ResourceImpl(js, null),
                        new PropertyImpl(P1, null),
                        jo.isLiteral() ? new LiteralImpl(jo, null)
                                : new ResourceImpl(jo, null));

                rows.add(asList(triple, Statement.class, rStmt));
                rows.add(asList(quad, Statement.class, rStmt));
                rows.add(asList(quad2, Statement.class, rQuad));
                rows.add(asList(stmt, Statement.class, rStmt));

                rows.add(asList(rStmt, Triple.class, triple));
                rows.add(asList(rStmt, Quad.class, quad));
                rows.add(asList(rQuad, Quad.class, quad2));
                rows.add(asList(rStmt, org.apache.jena.rdf.model.Statement.class, stmt));
            }
        }
        return rows.stream().map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "testData")
    public void test(@Nonnull Object in, @Nonnull Class<?> desired,
                     @Nonnull Object expected) throws ConversionException {
        ConversionFinder finder = mgr.findPath(in, desired);
        assertTrue(finder.hasNext());
        Object actual = finder.convert(in);
        assertEquals(actual, expected);
    }

}