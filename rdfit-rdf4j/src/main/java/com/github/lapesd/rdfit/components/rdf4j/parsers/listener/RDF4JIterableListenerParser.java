package com.github.lapesd.rdfit.components.rdf4j.parsers.listener;

import com.github.lapesd.rdfit.components.parsers.ListenerFeeder;
import com.github.lapesd.rdfit.components.parsers.impl.listener.IterableListenerParser;
import org.eclipse.rdf4j.model.Statement;

import javax.annotation.Nonnull;

public class RDF4JIterableListenerParser extends IterableListenerParser {
    public RDF4JIterableListenerParser() {
        super(Statement.class, Statement.class);
    }

    @Override protected boolean feed(@Nonnull ListenerFeeder feeder, @Nonnull Object element) {
        Statement stmt = (Statement) element;
        if (stmt.getContext() == null)
            return feeder.feedTriple(stmt);
        else
            return feeder.feedQuad(stmt);
    }
}
