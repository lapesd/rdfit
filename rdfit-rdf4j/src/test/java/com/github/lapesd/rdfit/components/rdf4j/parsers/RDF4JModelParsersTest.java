package com.github.lapesd.rdfit.components.rdf4j.parsers;

import com.github.lapesd.rdfit.DefaultRDFItFactory;
import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.errors.InconvertibleException;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.iterator.RDFIt;
import com.github.lapesd.rdfit.listener.RDFListenerBase;
import com.github.lapesd.rdfit.listener.TripleListenerBase;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.DynamicModel;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

public class RDF4JModelParsersTest {
    private static final String EX = "http://example.org/";
    private static final IRI S1 = SimpleValueFactory.getInstance().createIRI(EX+"S1");
    private static final IRI P1 = SimpleValueFactory.getInstance().createIRI(EX+"P1");
    private static final IRI O1 = SimpleValueFactory.getInstance().createIRI(EX+"O1");
    private static final IRI G1 = SimpleValueFactory.getInstance().createIRI(EX+"G1");
    private static final BNode    S2 = SimpleValueFactory.getInstance().createBNode();
    private static final Literal  O2 = SimpleValueFactory.getInstance().createLiteral("O2");
    private RDFItFactory factory;

    @BeforeMethod
    public void setUp() {
        factory = new DefaultRDFItFactory();
        RDF4JModelParsers.registerAll(factory);
    }

    @DataProvider public Object[][] testData() {
        SimpleValueFactory f = SimpleValueFactory.getInstance();
        DynamicModel model = new DynamicModelFactory().createEmptyModel();
        model.add(S1, P1, O1);
        model.add(S1, P1, O2);

        List<Statement> list = singletonList(f.createStatement(S2, P1, O1));

        SailRepository repo = new SailRepository(new MemoryStore());
        try (SailRepositoryConnection conn = repo.getConnection()) {
            conn.add(f.createStatement(S1, P1, O1));
            conn.add(f.createStatement(S1, P1, O2), G1);
        }
        List<Statement> repoExpected = asList(f.createStatement(S1, P1, O1),
                                              f.createStatement(S1, P1, O2, G1));
        List<Statement> repoTriples = asList(f.createStatement(S1, P1, O1),
                                             f.createStatement(S1, P1, O2));
        List<Statement> repoQuads = singletonList(f.createStatement(S1, P1, O2, G1));
        TupleQuery query1 = repo.getConnection()
                .prepareTupleQuery("SELECT ?s ?p ?o WHERE {?s ?p ?o.}");
        TupleQueryResult result1 = repo.getConnection()
                .prepareTupleQuery("SELECT ?s ?p ?o WHERE {?s ?p ?o.}")
                .evaluate();
        TupleQuery query2 = repo.getConnection()
                .prepareTupleQuery("SELECT ?s ?p ?o ?g WHERE { GRAPH ?g {?s ?p ?o.}.}");
        TupleQueryResult result2 = repo.getConnection()
                .prepareTupleQuery("SELECT ?s ?p ?o ?g WHERE { GRAPH ?g {?s ?p ?o.}.}")
                .evaluate();

        return Stream.of(
                asList(model, asList(f.createStatement(S1, P1, O1), f.createStatement(S1, P1, O2))),
                asList(list, singletonList(f.createStatement(S2, P1, O1))),
                asList(repo, repoExpected),
                asList(repo.getConnection(), repoExpected),
                asList(query1, repoTriples),
                asList(result1, repoTriples),
                asList(query2, repoQuads),
                asList(result2, repoQuads)
        ).map(List::toArray).toArray(Object[][]::new);

    }

    @Test(dataProvider = "testData")
    public void testIterateTriples(@Nonnull Object in, @Nonnull Collection<Statement> expected) {
        List<Statement> actual;
        try (RDFIt<Statement> it = factory.iterateTriples(Statement.class, in)) {
            actual = new ArrayList<>();
            while (it.hasNext())
                actual.add(it.next());
        }

        HashSet<Statement> expectedSet = new HashSet<>();
        SimpleValueFactory f = SimpleValueFactory.getInstance();
        for (Statement s : expected)
            expectedSet.add(f.createStatement(s.getSubject(), s.getPredicate(), s.getObject()));
        for (int i = 0, size = actual.size(); i < size; i++) {
            Statement s = actual.get(i);
            actual.set(i, f.createStatement(s.getSubject(), s.getPredicate(), s.getObject()));
        }

        assertEquals(new HashSet<>(actual), expectedSet);
        assertEquals(actual.size(), expected.size());
    }

    @Test(dataProvider = "testData")
    public void testIterateQuads(@Nonnull Object in, @Nonnull Collection<Statement> expected) {
        List<Statement> actual = new ArrayList<>();
        try (RDFIt<Statement> it = factory.iterateQuads(Statement.class, in)) {
            it.forEachRemaining(actual::add);
        }

        assertEquals(new HashSet<>(actual), new HashSet<>(expected));
        assertEquals(actual.size(), expected.size());
    }

    @Test(dataProvider = "testData")
    public void testParse(@Nonnull Object in, @Nonnull Collection<Statement> expected) {
        List<Statement> exTriples = new ArrayList<>(), exQuads = new ArrayList<>();
        for (Statement s : expected)
            (s.getContext() == null ? exTriples : exQuads).add(s);

        List<Exception> exceptions = new ArrayList<>();
        List<Statement> triples = new ArrayList<>(), quads = new ArrayList<>();
        factory.parse(new RDFListenerBase<Statement, Statement>(Statement.class, Statement.class) {
            @Override public void triple(@Nonnull Statement triple) {
                triples.add(triple);
            }

            @Override public void quad(@Nonnull Statement quad) {
                quads.add(quad);
            }

            @Override
            public boolean notifyInconvertibleQuad(@Nonnull InconvertibleException e) throws InterruptParsingException {
                exceptions.add(e);
                return true;
            }

            @Override
            public boolean notifyInconvertibleTriple(@Nonnull InconvertibleException e) throws InterruptParsingException {
                exceptions.add(e);
                return true;
            }

            @Override
            public boolean notifySourceError(@Nonnull RDFItException e) {
                exceptions.add(e);
                return true;
            }
        }, in);

        assertEquals(new HashSet<>(triples), new HashSet<>(exTriples));
        assertEquals(new HashSet<>(quads), new HashSet<>(exQuads));
        assertEquals(triples.size(), exTriples.size());
        assertEquals(quads.size(), exQuads.size());
        assertEquals(exceptions, Collections.emptyList());
    }

    @Test(dataProvider = "testData")
    public void testParseTriples(@Nonnull Object in, @Nonnull Collection<Statement> expected) {
        List<Statement> exTriples = new ArrayList<>(), exQuads = new ArrayList<>();
        SimpleValueFactory f = SimpleValueFactory.getInstance();
        for (Statement s : expected)
            (s.getContext() == null ? exTriples : exQuads).add(s);

        List<Exception> exceptions = new ArrayList<>();
        List<Statement> triples = new ArrayList<>(), quads = new ArrayList<>();
        factory.parse(new TripleListenerBase<Statement>(Statement.class) {
            @Override public void triple(@Nonnull Statement t) {
                triples.add(f.createStatement(t.getSubject(), t.getPredicate(), t.getObject()));
            }

            @Override public void quad(@Nonnull String graph, @Nonnull Statement triple) {
                quads.add(triple);
            }

            @Override
            public boolean notifyInconvertibleTriple(@Nonnull InconvertibleException e) throws InterruptParsingException {
                exceptions.add(e);
                return true;
            }

            @Override
            public boolean notifyInconvertibleQuad(@Nonnull InconvertibleException e) throws InterruptParsingException {
                exceptions.add(e);
                return true;
            }

            @Override
            public boolean notifySourceError(@Nonnull RDFItException e) {
                exceptions.add(e);
                return true;
            }
        }, in);

        assertEquals(new HashSet<>(triples), new HashSet<>(exTriples));
        assertEquals(new HashSet<>(quads), new HashSet<>(exQuads));
        assertEquals(triples.size(), exTriples.size());
        assertEquals(quads.size(), exQuads.size());
        assertEquals(exceptions, Collections.emptyList());
    }

}