package com.github.lapesd.rdfit.components.jena.parsers.listener;

import com.github.lapesd.rdfit.DefaultRDFItFactory;
import com.github.lapesd.rdfit.components.jena.JenaParsers;
import com.github.lapesd.rdfit.errors.InconvertibleException;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.listener.RDFListenerBase;
import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.source.syntax.RDFLangs;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.Quad;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.rdf.model.ResourceFactory.*;
import static org.testng.Assert.*;

public class JenaRDFInputStreamParserTest {
    private static final String EX = "http://example.org/";
    private static final Triple T1 = new Triple(createURI(EX+"S"), createURI(EX+"P"), createURI(EX+"O"));
    private static final Statement S1 = createStatement(createResource(EX+"S"),
                                                        createProperty(EX+"P"),
                                                        createResource(EX+"O"));
    private static final Quad Q1 = new Quad(Quad.defaultGraphIRI, createURI(EX+"S"),
                                            createURI(EX+"P"), createURI(EX+"O"));
    private static final Quad Q2 = new Quad(createURI(EX), createURI(EX+"S"),
                                            createURI(EX+"P"), createURI(EX+"O"));

    private @Nonnull RDFInputStream createRIS(@Nonnull Object input, @Nullable RDFLang lang,
                                              @Nullable String baseURI) {
        RDFInputStream ris;
        if (input instanceof String) {
            ByteArrayInputStream is = new ByteArrayInputStream(input.toString().getBytes(UTF_8));
            ris = new RDFInputStream(is, lang, baseURI);
        } else if (input instanceof InputStream) {
            ris = new RDFInputStream((InputStream) input, lang, baseURI);
        } else {
            ris = null;
            fail("Unexpected input type: "+ input.getClass());
        } return ris;
    }

    @DataProvider public @Nonnull Object[][] testData() {
        List<List<Object>> singleTriple = asList(
                asList("<"+EX+"S> <"+EX+"P> <"+EX+"O>.", RDFLangs.NT),
                asList("@prefix : <"+EX+">.\n :S :P :O.\n", RDFLangs.TTL),
                asList("@base <"+EX+">.\n <S> <P> <O>.\n", RDFLangs.TTL)
        );
        List<List<Object>> singleQuad = asList(
                asList("<"+EX+"S> <"+EX+"P> <"+EX+"O> <"+EX+">.", RDFLangs.NQ),
                asList("@prefix e: <"+EX+">.\n GRAPH e: { e:S e:P e:O }.\n", RDFLangs.TRIG),
                asList("@base <"+EX+">.\n GRAPH <> { <S> <P> <O> }.\n", RDFLangs.TRIG)
        );
        Object nil = new Object();
        List<List<Object>> rows = new ArrayList<>();
        for (List<Object> base : singleTriple) {
            List<Object> langs = asList(base.get(1), nil);
            List<Object> bases = asList(EX + "base", nil);
            List<Object> tripleClasses = asList(Triple.class, Statement.class, nil);
            for (List<Object> vs : Lists.cartesianProduct(langs, bases, tripleClasses)) {
                List<Object> row = new ArrayList<>(base);
                row.set(1, vs.get(0));
                row.add(vs.get(1));
                row.add(vs.get(2));
                row.add(Quad.class);
                if (vs.get(2) == nil) {
                    row.add(Collections.emptyList());
                    row.add(Collections.singleton(Q1));
                } else if (vs.get(2).equals(Triple.class)) {
                    row.add(Collections.singleton(T1));
                    row.add(Collections.emptyList());
                } else if (vs.get(2).equals(Statement.class)) {
                    row.add(Collections.singleton(S1));
                    row.add(Collections.emptyList());
                }
                rows.add(row);
            }
        }

        for (List<Object> base : singleQuad) {
            List<Object> langs = asList(base.get(1), nil);
            List<Object> bases = asList(EX + "base", nil);
            List<Object> tripleClasses = asList(Triple.class, Statement.class, nil);
            List<Object> quadClasses = asList(Quad.class, nil);
            for (List<Object> vs : Lists.cartesianProduct(langs, bases, tripleClasses, quadClasses)) {
                if (vs.get(2) == nil && vs.get(3) == nil) continue;
                List<Object> row = new ArrayList<>(base);
                row.set(1, vs.get(0));
                for (int i = 1; i < 4; i++) row.add(vs.get(i));

                if (vs.get(3) == nil && Triple.class.equals(vs.get(2)))
                    row.add(Collections.singleton(T1));
                else if (vs.get(3) == nil && Statement.class.equals(vs.get(2)))
                    row.add(Collections.singleton(S1));
                else
                    row.add(Collections.emptyList());

                if (vs.get(3) == nil)
                    row.add(Collections.emptyList());
                else
                    row.add(Collections.singleton(Q2));
                rows.add(row);
            }
        }

        return rows.stream()
                .map(l -> l.stream().map(o -> nil.equals(o) ? null : o).collect(toList()))
                .map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "testData")
    public void test(@Nonnull Object input, @Nullable RDFLang lang, @Nullable String baseURI,
                     @Nonnull Class<?> tripleClass, @Nonnull Class<?> quadClass,
                     @Nonnull Collection<?> exTriples, @Nonnull Collection<?> exQuads) {
        RDFInputStream ris = createRIS(input, lang, baseURI);

        JenaRDFInputStreamParser parser = new JenaRDFInputStreamParser();
        assertTrue(parser.canParse(ris));
        List<Object> acTriples = new ArrayList<>(), acQuads = new ArrayList<>();

        //noinspection unchecked
        parser.parse(ris, new RDFListenerBase<Object, Object>(
                (Class<Object>) tripleClass, (Class<Object>) quadClass) {
            @Override public void triple(@Nonnull Object triple) {
                acTriples.add(triple);
            }

            @Override public void quad(@Nonnull Object quad) {
                acQuads.add(quad);
            }
        });

        assertEquals(new HashSet<>(acTriples), new HashSet<>(exTriples));
        assertEquals(new HashSet<>(acQuads), new HashSet<>(exQuads));
        assertEquals(acTriples.size(), exTriples.size());
        assertEquals(acQuads.size(), exQuads.size());
    }

    @Test(dataProvider = "testData")
    public void testFactory(@Nonnull Object input, @Nullable RDFLang lang, @Nullable String baseURI,
                            @Nonnull Class<?> tripleClass, @Nonnull Class<?> quadClass,
                            @Nonnull Collection<?> exTriples, @Nonnull Collection<?> exQuads) {
        DefaultRDFItFactory factory = new DefaultRDFItFactory();
        JenaParsers.registerAll(factory);
        List<Object> acTriples = new ArrayList<>(), acQuads = new ArrayList<>();
        List<Exception> exceptions = new ArrayList<>();
        //noinspection unchecked
        factory.parse(new RDFListenerBase<Object, Object>(
                (Class<Object>) tripleClass, (Class<Object>) quadClass) {
            @Override public void triple(@Nonnull Object triple) {
                acTriples.add(triple);
            }
            @Override public void quad(@Nonnull Object quad) {
                acQuads.add(quad);
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
        }, createRIS(input, lang, baseURI));

        assertEquals(new HashSet<>(acTriples), new HashSet<>(exTriples));
        assertEquals(new HashSet<>(acQuads), new HashSet<>(exQuads));
        assertEquals(acTriples.size(), exTriples.size());
        assertEquals(acQuads.size(), exQuads.size());
        assertEquals(exceptions, Collections.emptyList());
    }

}