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

package com.github.lapesd.rdfit.components.jena.parsers.iterator;

import com.github.lapesd.rdfit.components.parsers.BaseItParser;
import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.iterator.PlainRDFIt;
import com.github.lapesd.rdfit.iterator.RDFIt;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;

import javax.annotation.Nonnull;
import java.util.Collections;

public class GraphItParser extends BaseItParser {
    public GraphItParser() {
        super(Collections.singleton(Graph.class), Triple.class, IterationElement.TRIPLE);
    }

    @Override public @Nonnull <T> RDFIt<T> parse(@Nonnull Object source) {
        ExtendedIterator<Triple> it = ((Graph) source).find();
        return new PlainRDFIt<>(Triple.class, IterationElement.TRIPLE, it, source);
    }
}
