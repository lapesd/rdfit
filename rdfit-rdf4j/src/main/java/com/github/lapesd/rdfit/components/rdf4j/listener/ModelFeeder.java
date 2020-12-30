package com.github.lapesd.rdfit.components.rdf4j.listener;

import com.github.lapesd.rdfit.listener.RDFListenerBase;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;

import javax.annotation.Nonnull;

public class ModelFeeder extends RDFListenerBase<Statement, Statement> {
    private final @Nonnull Model model;

    public ModelFeeder() {
        this(new DynamicModelFactory().createEmptyModel());
    }

    public ModelFeeder(@Nonnull Model model) {
        super(Statement.class, Statement.class);
        this.model = model;
    }

    public @Nonnull Model getModel() {
        return model;
    }

    @Override public void triple(@Nonnull Statement triple) {
        model.add(triple);
    }

    @Override public void quad(@Nonnull Statement quad) {
        model.add(quad);
    }
}
