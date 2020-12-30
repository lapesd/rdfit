package com.github.lapesd.rdfit.components.rdf4j.listener;

import org.eclipse.rdf4j.repository.Repository;

import javax.annotation.Nonnull;

public class RepositoryFeeder extends RepositoryConnectionFeeder {
    private final @Nonnull Repository repository;

    public RepositoryFeeder(@Nonnull Repository repository) {
        super(repository.getConnection());
        this.repository = repository;
    }

    public @Nonnull Repository getRepository() {
        return repository;
    }
}
