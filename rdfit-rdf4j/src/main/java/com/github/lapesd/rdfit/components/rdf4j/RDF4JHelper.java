package com.github.lapesd.rdfit.components.rdf4j;

import com.github.lapesd.rdfit.components.rdf4j.listener.ModelFeeder;
import com.github.lapesd.rdfit.components.rdf4j.listener.RepositoryConnectionFeeder;
import com.github.lapesd.rdfit.components.rdf4j.listener.RepositoryFeeder;
import com.github.lapesd.rdfit.iterator.RDFIt;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.DynamicModel;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import javax.annotation.Nonnull;

public class RDF4JHelper {
    public static @Nonnull Model toModel(@Nonnull Model model, @Nonnull RDFIt<Statement> it) {
        try {
            it.forEachRemaining(model::add);
        } finally {
            it.close();
        }
        return model;
    }

    public static @Nonnull Model toModel(@Nonnull RDFIt<Statement> it) {
        DynamicModel model = new DynamicModelFactory().createEmptyModel();
        return toModel(model, it);
    }

    public static @Nonnull ModelFeeder feeder(@Nonnull Model model) {
        return new ModelFeeder(model);
    }
    public static @Nonnull RepositoryConnectionFeeder
    feeder(@Nonnull RepositoryConnection connection) {
        return new RepositoryConnectionFeeder(connection);
    }
    public static @Nonnull RepositoryFeeder feeder(@Nonnull Repository repository) {
        return new RepositoryFeeder(repository);
    }
}
