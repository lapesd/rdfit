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

package com.github.lapesd.rdfit.components.jena.parsers.listener;

import com.github.lapesd.rdfit.components.parsers.ListenerFeeder;
import com.github.lapesd.rdfit.components.parsers.impl.listener.IterableListenerParser;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.Quad;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.apache.jena.sparql.core.Quad.defaultGraphIRI;
import static org.apache.jena.sparql.core.Quad.defaultGraphNodeGenerated;

public class JenaIterableListenerParser extends IterableListenerParser {
    public JenaIterableListenerParser(@Nullable Class<?> tripleClass, @Nullable Class<?> quadClass) {
        super(tripleClass, quadClass);
    }

    @Override protected boolean feed(@Nonnull ListenerFeeder feeder, @Nonnull Object el) {
        if (el instanceof Quad) {
            Quad q = (Quad) el;
            Node graph = q.getGraph();
            if (!defaultGraphIRI.equals(graph) && !defaultGraphNodeGenerated.equals(graph))
                return feeder.feedQuad(q);
        }
        assert el instanceof Triple || el instanceof Statement || el instanceof Quad
                : "Unexpected element class at JenaIterableListenerParser";
        return feeder.feedTriple(el);
    }
}
