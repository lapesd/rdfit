package com.github.lapesd.rdfit.components.rdf4j.parsers.listener;

import com.github.lapesd.rdfit.components.rdf4j.listener.RDFListenerHandler;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import javax.annotation.Nonnull;
import java.util.Collections;

public class RepositoryListenerParser extends RDFHandlerParser {
    public RepositoryListenerParser() {
        super(Collections.singleton(Repository.class));
    }

    @Override protected void parse(@Nonnull Object source, @Nonnull RDFListenerHandler handler) {
        try (RepositoryConnection conn = ((Repository) source).getConnection()) {
            conn.export(handler);
        }
    }
}
