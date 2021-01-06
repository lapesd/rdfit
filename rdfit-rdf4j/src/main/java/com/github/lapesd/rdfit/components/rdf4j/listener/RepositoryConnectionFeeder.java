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
