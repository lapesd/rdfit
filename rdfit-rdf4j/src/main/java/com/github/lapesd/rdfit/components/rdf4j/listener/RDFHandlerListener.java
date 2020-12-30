package com.github.lapesd.rdfit.components.rdf4j.listener;

import com.github.lapesd.rdfit.listener.RDFListener;
import com.github.lapesd.rdfit.listener.RDFListenerBase;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFHandler;

import javax.annotation.Nonnull;

/**
 * A {@link RDFListener} that forwards triples and statements to an {@link RDFHandler}.
 */
public class RDFHandlerListener extends RDFListenerBase<Statement, Statement> {
    private final @Nonnull RDFHandler handler;

    public RDFHandlerListener(@Nonnull RDFHandler handler) {
        super(Statement.class, Statement.class);
        this.handler = handler;
    }

    @Override public void triple(@Nonnull Statement triple) {
        handler.handleStatement(triple);
    }

    @Override public void quad(@Nonnull Statement quad) {
        handler.handleStatement(quad);
    }

    @Override public void start(@Nonnull Object source) {
        handler.startRDF();
        super.start(source);
    }

    @Override public void finish(@Nonnull Object source) {
        handler.endRDF();
        super.finish(source);
    }

    @Override public @Nonnull String toString() {
        return "RDFListener2RDFHandler";
    }
}
