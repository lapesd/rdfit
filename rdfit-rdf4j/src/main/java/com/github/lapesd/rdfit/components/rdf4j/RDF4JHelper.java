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

import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.RIt;
import com.github.lapesd.rdfit.components.rdf4j.iterators.RDF4JImportingRDFIt;
import com.github.lapesd.rdfit.components.rdf4j.listener.ModelFeeder;
import com.github.lapesd.rdfit.components.rdf4j.listener.RepositoryConnectionFeeder;
import com.github.lapesd.rdfit.components.rdf4j.listener.RepositoryFeeder;
import com.github.lapesd.rdfit.impl.DefaultRDFItFactory;
import com.github.lapesd.rdfit.iterator.ConvertingRDFIt;
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
        return toModelImporting(DefaultRDFItFactory.get(), m, sources);
    }
    public static @Nonnull Model toModelImporting(@Nonnull RDFItFactory factory,
                                                  @Nonnull Model m, @Nonnull Object... sources) {
        if (sources.length == 1 && sources[0] instanceof RDFIt) {
            RDFIt<Statement> it = ConvertingRDFIt.createIf(Statement.class, (RDFIt<?>) sources[0]);
            return toModelImporting(m, it);
        }
        return toModel(m, factory.iterateQuads(Statement.class, sources), true);
    }
    public static @Nonnull Model toModel(@Nonnull Model model, @Nonnull RDFIt<Statement> it) {
        return toModel(model, it, false);
    }
    public static @Nonnull Model toModel(@Nonnull Model model, @Nonnull Object... sources) {
        return toModel(DefaultRDFItFactory.get(), model, sources);
    }
    public static @Nonnull Model toModel(@Nonnull RDFItFactory factory,
                                         @Nonnull Model model, @Nonnull Object... sources) {
        if (sources.length == 1 && sources[0] instanceof RDFIt)
            return toModel(model, ConvertingRDFIt.createIf(Statement.class, (RDFIt<?>) sources[0]));
        return toModel(model, factory.iterateQuads(Statement.class, sources), false);
    }

    public static @Nonnull Model toModel(@Nonnull RDFIt<Statement> it, boolean fetchImports) {
        DynamicModel model = new DynamicModelFactory().createEmptyModel();
        return toModel(model, it, fetchImports);
    }
    public static @Nonnull Model toModelImporting(@Nonnull RDFIt<Statement> it) {
        return toModel(it, true);
    }
    public static @Nonnull Model toModelImporting(@Nonnull Object... srcs) {
        return toModelImporting(DefaultRDFItFactory.get(), srcs);
    }
    public static @Nonnull Model toModelImporting(@Nonnull RDFItFactory factory,
                                                  @Nonnull Object... srcs) {
        if (srcs.length == 1 && srcs[0] instanceof RDFIt)
            return toModelImporting(ConvertingRDFIt.createIf(Statement.class, (RDFIt<?>) srcs[0]));
        return toModel(factory.iterateQuads(Statement.class, srcs), true);
    }
    public static @Nonnull Model toModel(@Nonnull RDFIt<Statement> it) {
        return toModel(it, false);
    }
    public static @Nonnull Model toModel(@Nonnull Object... sources) {
        return toModel(DefaultRDFItFactory.get(), sources);
    }
    public static @Nonnull Model toModel(@Nonnull RDFItFactory factory,
                                         @Nonnull Object... sources) {
        if (sources.length == 1 && sources[0] instanceof RDFIt)
            return toModel(ConvertingRDFIt.createIf(Statement.class, (RDFIt<?>) sources[0]));
        return toModel(factory.iterateQuads(Statement.class, sources), false);
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
