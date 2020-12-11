package com.github.lapesd.rdfit.components.jena.converters;

import com.github.lapesd.rdfit.components.converters.quad.QuadLifterBase;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;

import javax.annotation.Nonnull;

public class TripleQuadLifter extends QuadLifterBase {
    private final @Nonnull Node graphIRI;

    public TripleQuadLifter() {
        this(Quad.defaultGraphIRI);
    }
    public TripleQuadLifter(@Nonnull String graphIRI) {
        this(NodeFactory.createURI(graphIRI));
    }
    public TripleQuadLifter(@Nonnull Node graphIRI) {
        super(Triple.class);
        if (!graphIRI.isURI())
            throw new IllegalArgumentException("graphIRI="+graphIRI+" is not an IRI Node");
        this.graphIRI = graphIRI;
    }

    @Override public @Nonnull Object lift(@Nonnull Object triple) {
        return new Quad(graphIRI, (Triple) triple);
    }

    @Override public @Nonnull String toString() {
        return "TripleQuadLifter("+graphIRI+")";
    }
}
