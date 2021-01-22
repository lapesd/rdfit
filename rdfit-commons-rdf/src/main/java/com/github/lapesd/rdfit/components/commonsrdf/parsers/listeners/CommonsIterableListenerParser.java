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

package com.github.lapesd.rdfit.components.commonsrdf.parsers.listeners;

import com.github.lapesd.rdfit.components.parsers.ListenerFeeder;
import com.github.lapesd.rdfit.components.parsers.impl.listener.IterableListenerParser;
import org.apache.commons.rdf.api.Quad;
import org.apache.commons.rdf.api.Triple;

import javax.annotation.Nonnull;

public class CommonsIterableListenerParser extends IterableListenerParser {
    public CommonsIterableListenerParser() {
        super(Triple.class, Quad.class);
    }

    @Override protected boolean feed(@Nonnull ListenerFeeder feeder, @Nonnull Object element) {
        if (element instanceof Quad) {
            Quad q = (Quad) element;
            if (q.getGraphName().isPresent())
                return feeder.feedQuad(q);
        }
        assert element instanceof Triple || element instanceof Quad;
        return feeder.feedTriple(element);
    }
}
