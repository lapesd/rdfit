package com.github.lapesd.rdfit.components.rdf4j.listener;

import com.github.lapesd.rdfit.listener.RDFListenerBase;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import javax.annotation.Nonnull;

public class RepositoryConnectionFeeder extends RDFListenerBase<Statement, Statement> {
    private final @Nonnull RepositoryConnection connection;

    public RepositoryConnectionFeeder(@Nonnull RepositoryConnection connection) {
        super(Statement.class, Statement.class);
        this.connection = connection;
    }

    public @Nonnull RepositoryConnection getConnection() {
        return connection;
    }

    @Override public void triple(@Nonnull Statement triple) {
        connection.add(triple);
    }

    @Override public void quad(@Nonnull Statement quad) {
        if (quad.getContext() != null)
            connection.add(quad, quad.getContext());
        else
            connection.add(quad);
    }


}
