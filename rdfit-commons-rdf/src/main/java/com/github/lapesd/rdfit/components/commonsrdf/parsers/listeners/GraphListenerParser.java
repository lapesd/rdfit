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

import com.github.lapesd.rdfit.components.parsers.BaseListenerParser;
import com.github.lapesd.rdfit.components.parsers.ListenerFeeder;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.listener.RDFListener;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.Triple;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Iterator;

/**
 * A {@link com.github.lapesd.rdfit.components.ListenerParser} for commons-rdf {@link Graph}s
 */
public class GraphListenerParser extends BaseListenerParser {
    /**
     * Default constructor
     */
    public GraphListenerParser() {
        super(Collections.singleton(Graph.class), Triple.class);
    }

    @Override
    public void parse(@Nonnull Object source,
                      @Nonnull RDFListener<?, ?> listener) throws InterruptParsingException {
        try (ListenerFeeder feeder = createListenerFeeder(listener, source)) {
            Iterator<Triple> it = ((Graph) source).iterate().iterator();
            try {
                while (it.hasNext())
                    feeder.feedTriple(it.next());
            }catch (InterruptParsingException e) {
                throw e;
            } catch (Throwable t) {
                if (!listener.notifySourceError(RDFItException.wrap(source, t)))
                    throw new InterruptParsingException();
            }
        }
    }
}
