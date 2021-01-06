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
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.GraphFactory;

import javax.annotation.Nonnull;

public class GraphFeeder extends TripleListenerBase<Triple> {
    private @Nonnull final Graph destination;

    public GraphFeeder() {
        this(GraphFactory.createDefaultGraph());
    }

    public GraphFeeder(@Nonnull Graph destination) {
        super(Triple.class);
        this.destination = destination;
    }

    @Override public void triple(@Nonnull Triple triple) {
        destination.add(triple);
    }

    public @Nonnull Graph getGraph() {
        return destination;
    }

    @Override public @Nonnull String toString() {
        return Utils.toString(this);
    }
}
