package com.github.lapesd.rdfit.components.jena;

import com.github.lapesd.rdfit.DefaultRDFItFactory;
import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.components.parsers.DefaultParserRegistry;
import com.github.lapesd.rdfit.listener.RDFListenerBase;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.GraphFactory;
import org.testng.annotations.BeforeClass;
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
import static java.util.Collections.*;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.rdf.model.ResourceFactory.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class JenaModelParsersTest {
    private static final @Nonnull String EX = "http://example.org/";
    private RDFItFactory factory;
    private Model model;
    private Graph graph;
    private Dataset modelDs, namedDs;
    private DatasetGraph graphDs, namedGraphDs;
    private Statement stmt;
    private Triple triple;
    private Quad defQuad, exQuad;

    @BeforeClass
    public void setUp() {
        factory = new DefaultRDFItFactory(new DefaultParserRegistry(),
                new DefaultConversionManager());
        JenaHelpers.registerAll(factory);
        model = ModelFactory.createDefaultModel();
        model.add(createResource(EX+"S"), createProperty(EX+"P"), createResource(EX+"O"));
        stmt = createStatement(createResource(EX + "S"),
                createProperty(EX + "P"),
                createResource(EX + "O"));

        graph = GraphFactory.createDefaultGraph();
        graph.add(new Triple(createURI(EX+"S"), createURI(EX+"P"), createURI(EX+"O")));
        triple = new Triple(createURI(EX + "S"), createURI(EX + "P"), createURI(EX + "O"));

        modelDs = DatasetFactory.wrap(model);
        namedDs = DatasetFactory.create();
        namedDs.addNamedModel(EX, model);

        graphDs = DatasetGraphFactory.wrap(graph);
        namedGraphDs = DatasetGraphFactory.create();
        namedGraphDs.addGraph(createURI(EX), graph);

        defQuad = new Quad(Quad.defaultGraphIRI, triple);
        exQuad = new Quad(createURI(EX), triple);
    }

    @DataProvider public Object[][] iterateData() {
        return Stream.of(
                asList(model, Statement.class, singletonList(stmt)),
                asList(graph, Statement.class, singletonList(stmt)),
                asList(modelDs, Statement.class, singletonList(stmt)),
                asList(namedDs, Statement.class, singletonList(stmt)),
                asList(graphDs, Statement.class, singletonList(stmt)),
                asList(namedGraphDs, Statement.class, singletonList(stmt)),

                asList(model, Triple.class, singletonList(triple)),
                asList(graph, Triple.class, singletonList(triple)),
                asList(modelDs, Triple.class, singletonList(triple)),
                asList(namedDs, Triple.class, singletonList(triple)),
                asList(graphDs, Triple.class, singletonList(triple)),
                asList(namedGraphDs, Triple.class, singletonList(triple)),

                asList(model, Quad.class, singletonList(defQuad)),
                asList(graph, Quad.class, singletonList(defQuad)),
                asList(modelDs, Quad.class, singletonList(defQuad)),
                asList(namedDs, Quad.class, singletonList(exQuad)),
                asList(graphDs, Quad.class, singletonList(defQuad)),
                asList(namedGraphDs, Quad.class, singletonList(exQuad))
        ).map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "iterateData")
    public void testIterate(@Nonnull Object in,
                            @Nonnull Class<?> valueClass,
                            @Nonnull List<?> expected) {
        assertNotNull(factory);
        List<Object> actual = new ArrayList<>();
        if (valueClass.equals(Quad.class))
            factory.iterateQuads(valueClass, in).forEachRemaining(actual::add);
        else
            factory.iterateTriples(valueClass, in).forEachRemaining(actual::add);
        assertEquals(new HashSet<>(actual), new HashSet<>(expected));
        assertEquals(actual.size(), expected.size());
    }

    @DataProvider public Object[][] parseData() {
        return Stream.of(
                asList(model, Statement.class, null, singleton(stmt), emptyList()),
                asList(model, Triple.class, null, singleton(triple), emptyList()),
                asList(model, Statement.class, Quad.class, singleton(stmt), emptyList()),
                asList(model, Triple.class, Quad.class, singleton(triple), emptyList()),

                asList(graph, Statement.class, null, singleton(stmt), emptyList()),
                asList(graph, Triple.class, null, singleton(triple), emptyList()),
                asList(graph, Statement.class, Quad.class, singleton(stmt), emptyList()),
                asList(graph, Triple.class, Quad.class, singleton(triple), emptyList()),

                asList(modelDs, Statement.class, null, singleton(stmt), emptyList()),
                asList(modelDs, Triple.class, null, singleton(triple), emptyList()),
                asList(modelDs, Statement.class, Quad.class, emptyList(), singletonList(defQuad)),
                asList(modelDs, Triple.class, Quad.class, emptyList(), singletonList(defQuad)),

                asList(namedDs, Statement.class, null, singleton(stmt), emptyList()),
                asList(namedDs, Triple.class, null, singleton(triple), emptyList()),
                asList(namedDs, Statement.class, Quad.class, emptyList(), singletonList(exQuad)),
                asList(namedDs, Triple.class, Quad.class, emptyList(), singletonList(exQuad)),

                asList(graphDs, Statement.class, null, singleton(stmt), emptyList()),
                asList(graphDs, Triple.class, null, singleton(triple), emptyList()),
                asList(graphDs, Statement.class, Quad.class, emptyList(), singletonList(defQuad)),
                asList(graphDs, Triple.class, Quad.class, emptyList(), singletonList(defQuad)),

                asList(namedGraphDs, Statement.class, null, singleton(stmt), emptyList()),
                asList(namedGraphDs, Triple.class, null, singleton(triple), emptyList()),
                asList(namedGraphDs, Statement.class, Quad.class, emptyList(), singletonList(exQuad)),
                asList(namedGraphDs, Triple.class, Quad.class, emptyList(), singletonList(exQuad))
        ).map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "parseData")
    public void testParse(@Nonnull Object input, @Nullable Class<?> tripleClass,
                          @Nullable Class<?> quadClass, @Nonnull Collection<?> expectedTriples,
                          @Nonnull Collection<?> expectedQuads) {
        List<Object> acTriples = new ArrayList<>(), acQuads = new ArrayList<>();

        //noinspection unchecked
        factory.parse(new RDFListenerBase<Object, Object>(
                (Class<Object>) tripleClass, (Class<Object>) quadClass) {
            @Override public void triple(@Nonnull Object triple) {
                acTriples.add(triple);
            }

            @Override public void quad(@Nonnull Object quad) {
                acQuads.add(quad);
            }
        }, input);
        assertEquals(new HashSet<>(acTriples), new HashSet<>(expectedTriples));
        assertEquals(new HashSet<>(acQuads), new HashSet<>(expectedQuads));
        assertEquals(acTriples.size(), expectedTriples.size());
        assertEquals(acQuads.size(), expectedQuads.size());
    }
}