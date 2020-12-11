package com.github.lapesd.rdfit.components.jena;

import com.github.lapesd.rdfit.listener.TripleListenerBase;
import com.github.lapesd.rdfit.util.Utils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;

import javax.annotation.Nonnull;

public class ModelFeeder extends TripleListenerBase<Statement> {
    private final @Nonnull Model destination;

    public ModelFeeder() {
        this(ModelFactory.createDefaultModel());
    }

    public ModelFeeder(@Nonnull Model destination) {
        super(Statement.class);
        this.destination = destination;
    }

    @Override public void triple(@Nonnull Statement triple) {
        destination.add(triple);
    }

    public @Nonnull Model getModel() {
        return destination;
    }

    @Override public @Nonnull String toString() {
        return Utils.toString(this);
    }
}
