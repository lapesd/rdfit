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
