package com.github.lapesd.rdfit.components.jena;

import com.github.lapesd.rdfit.listener.TripleListenerBase;
import com.github.lapesd.rdfit.util.Utils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.GraphFactory;

import javax.annotation.Nonnull;

public class GraphFeeder extends TripleListenerBase<Triple> {
    private @Nonnull final Graph destination;

    public GraphFeeder() {
        this(GraphFactory.createDefaultGraph());
    }

    public GraphFeeder(@Nonnull Graph destination) {
        super(Triple.class);
        this.destination = destination;
    }

    @Override public void triple(@Nonnull Triple triple) {
        destination.add(triple);
    }

    public @Nonnull Graph getGraph() {
        return destination;
    }

    @Override public @Nonnull String toString() {
        return Utils.toString(this);
    }
}
