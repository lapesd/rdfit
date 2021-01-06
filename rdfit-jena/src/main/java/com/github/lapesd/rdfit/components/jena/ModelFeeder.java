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

package com.github.lapesd.rdfit.components.jena;

import com.github.lapesd.rdfit.listener.TripleListenerBase;
import com.github.lapesd.rdfit.util.Utils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;

import javax.annotation.Nonnull;

public class ModelFeeder extends TripleListenerBase<Statement> {
    private final @Nonnull Model destination;

    public ModelFeeder() {
        this(ModelFactory.createDefaultModel());
    }

    public ModelFeeder(@Nonnull Model destination) {
        super(Statement.class);
        this.destination = destination;
    }

    @Override public void triple(@Nonnull Statement triple) {
        destination.add(triple);
    }

    public @Nonnull Model getModel() {
        return destination;
    }

    @Override public @Nonnull String toString() {
        return Utils.toString(this);
    }
}
