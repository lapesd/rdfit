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
import com.github.lapesd.rdfit.components.parsers.impl.listener.BaseJavaListenerParser;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;

import static org.apache.jena.sparql.core.Quad.defaultGraphIRI;
import static org.apache.jena.sparql.core.Quad.defaultGraphNodeGenerated;

public class JenaArrayListenerParser extends BaseJavaListenerParser {
    public JenaArrayListenerParser(@Nullable Class<?> tripleClass, @Nullable Class<?> quadClass) {
        super(Array.newInstance(quadClass != null ? quadClass : tripleClass, 0).getClass(),
              tripleClass, quadClass);
    }

    @Override protected boolean feed(@Nonnull ListenerFeeder feeder, @Nonnull Object element) {
        if (element instanceof Quad) {
            Quad q = (Quad) element;
            Node g = q.getGraph();
            if (!defaultGraphIRI.equals(g) && !defaultGraphNodeGenerated.equals(g))
                return feeder.feedQuad(q);
        }
        return feeder.feedTriple(element);
    }

    @Override protected @Nonnull Iterator<?> createIterator(@Nonnull Object source) {
        return Arrays.asList((Object[])source).iterator();
    }
}
