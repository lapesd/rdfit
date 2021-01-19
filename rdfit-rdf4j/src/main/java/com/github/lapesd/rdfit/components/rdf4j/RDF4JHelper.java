/*
 *    Copyright 2021 Alexis Armin Huf
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.lapesd.rdfit.components.rdf4j;

import com.github.lapesd.rdfit.RIt;
import com.github.lapesd.rdfit.components.rdf4j.iterators.RDF4JImportingRDFIt;
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
    public static @Nonnull Model toModel(@Nonnull Model model, @Nonnull RDFIt<Statement> inIt,
                                         boolean fetchImports) {
        try (RDFIt<Statement> it = fetchImports ? new RDF4JImportingRDFIt<>(inIt) : inIt) {
            it.forEachRemaining(model::add);
        }
        return model;
    }
    public static @Nonnull Model toModelImporting(@Nonnull Model m, @Nonnull RDFIt<Statement> it) {
        return toModel(m, it, true);
    }
    public static @Nonnull Model toModelImporting(@Nonnull Model m, @Nonnull Object... sources) {
        return toModel(m, RIt.iterateQuads(Statement.class, sources), true);
    }
    public static @Nonnull Model toModel(@Nonnull Model model, @Nonnull RDFIt<Statement> it) {
        return toModel(model, it, false);
    }
    public static @Nonnull Model toModel(@Nonnull Model model, @Nonnull Object... sources) {
        return toModel(model, RIt.iterateQuads(Statement.class, sources), false);
    }

    public static @Nonnull Model toModel(@Nonnull RDFIt<Statement> it, boolean fetchImports) {
        DynamicModel model = new DynamicModelFactory().createEmptyModel();
        return toModel(model, it, fetchImports);
    }
    public static @Nonnull Model toModelImporting(@Nonnull RDFIt<Statement> it) {
        return toModel(it, true);
    }
    public static @Nonnull Model toModelImporting(@Nonnull Object... sources) {
        return toModel(RIt.iterateQuads(Statement.class, sources), true);
    }
    public static @Nonnull Model toModel(@Nonnull RDFIt<Statement> it) {
        return toModel(it, false);
    }
    public static @Nonnull Model toModel(@Nonnull Object... sources) {
        return toModel(RIt.iterateQuads(Statement.class, sources), false);
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
