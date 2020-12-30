package com.github.lapesd.rdfit.components.rdf4j.parsers.listener;

import com.github.lapesd.rdfit.components.rdf4j.listener.RDFListenerHandler;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import javax.annotation.Nonnull;
import java.util.Collections;

public class RepositoryConnectionListenerParser extends RDFHandlerParser {
    public RepositoryConnectionListenerParser() {
        super(Collections.singleton(RepositoryConnection.class));
    }

    @Override protected void parse(@Nonnull Object source, @Nonnull RDFListenerHandler handler) {
        ((RepositoryConnection)source).export(handler);
    }
}
