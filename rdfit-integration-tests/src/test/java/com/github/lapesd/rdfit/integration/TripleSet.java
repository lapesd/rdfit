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

package com.github.lapesd.rdfit.integration;

import com.github.lapesd.rdfit.components.converters.JenaRDF4JConverters;
import com.github.lapesd.rdfit.components.jena.converters.JenaConverters;
import com.github.lapesd.rdfit.errors.ConversionException;
import com.github.lapesd.rdfit.util.Utils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.GraphFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.apache.jena.sparql.core.Quad.defaultGraphIRI;
import static org.apache.jena.sparql.core.Quad.defaultGraphNodeGenerated;

public class TripleSet {
    private List<Quad> quads = new ArrayList<>();

    public TripleSet(@Nonnull Quad... quads) {
        this.quads.addAll(Arrays.asList(quads));
    }

    public TripleSet(@Nonnull Triple... triples) {
        for (Triple t : triples) this.quads.add(new Quad(defaultGraphIRI, t));
    }

    private static boolean inDefaultGraph(@Nonnull Quad q) {
        return defaultGraphIRI.equals(q.getGraph())
                || defaultGraphNodeGenerated.equals(q.getGraph());
    }

    public boolean hasQuads() {
        return quads.stream().anyMatch(q -> !inDefaultGraph(q));
    }

    public @Nonnull DatasetGraph toDatasetGraph() {
        DatasetGraph dsg = DatasetGraphFactory.create();
        quads.forEach(dsg::add);
        return dsg;
    }

    public @Nonnull Graph toGraph() {
        if (hasQuads())
            throw new UnsupportedOperationException();
        Graph graph = GraphFactory.createDefaultGraph();
        for (Quad q : quads) graph.add(q.asTriple());
        return graph;
    }

    private @Nonnull <T> List<T> export(@Nonnull Class<T> cls,
                                        @Nonnull Predicate<Quad> predicate) {
        ArrayList<Object> list = new ArrayList<>();
        try {
            for (Quad q : quads) {
                if (!predicate.test(q)) continue;
                if (cls.equals(Quad.class))
                    list.add(q);
                else if (cls.equals(Triple.class))
                    list.add(q.asTriple());
                else if (cls.equals(Statement.class))
                    list.add(JenaConverters.Quad2Statement.INSTANCE.convert(q));
                else if (cls.equals(org.eclipse.rdf4j.model.Statement.class))
                    list.add(JenaRDF4JConverters.Quad2RDF4J.INSTANCE.convert(q));
                else
                    throw new IllegalArgumentException("Bad Class: "+cls);
            }
        } catch (ConversionException e) {
            throw new RuntimeException(e);
        }
        //noinspection unchecked
        return (List<T>) list;
    }

    public @Nonnull <T> List<T> onlyTriples(@Nonnull Class<T> cls) {
        return export(cls, TripleSet::inDefaultGraph);
    }
    public @Nonnull <T> List<T> onlyQuads(@Nonnull Class<T> cls) {
        return export(cls, q -> !inDefaultGraph(q));
    }
    public @Nonnull <T> List<T> export(@Nonnull Class<T> cls) {
        return export(cls, q -> true);
    }

    @Override public @Nonnull String toString() {
        return Utils.toString(this);
    }
}
