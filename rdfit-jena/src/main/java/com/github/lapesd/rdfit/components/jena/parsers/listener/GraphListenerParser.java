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

import com.github.lapesd.rdfit.components.parsers.BaseListenerParser;
import com.github.lapesd.rdfit.components.parsers.ListenerFeeder;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.listener.RDFListener;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;

import javax.annotation.Nonnull;
import java.util.Collections;

public class GraphListenerParser extends BaseListenerParser {

    public GraphListenerParser() {
        super(Collections.singleton(Graph.class), Triple.class);
    }

    @Override
    public void parse(@Nonnull Object source,
                      @Nonnull RDFListener<?, ?> listener) throws InterruptParsingException {
        try (ListenerFeeder feeder = createListenerFeeder(listener, source)) {
            ExtendedIterator<Triple> it = ((Graph) source).find();
            try {
                while (it.hasNext()) {
                    if (!feeder.feedTriple(it.next()))
                        break;
                }
            } catch (InterruptParsingException e) {
                throw e;
            } catch (Throwable t) {
                if (!listener.notifySourceError(RDFItException.wrap(source, t)))
                    throw new InterruptParsingException();
            }
        }
    }
}
