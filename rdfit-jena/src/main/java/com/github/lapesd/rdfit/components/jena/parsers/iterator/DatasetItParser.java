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
import com.github.lapesd.rdfit.iterator.PlainRDFIt;
import com.github.lapesd.rdfit.iterator.RDFIt;
import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

import static com.github.lapesd.rdfit.iterator.IterationElement.QUAD;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

public class DatasetItParser extends BaseItParser {
    private static final @Nonnull Set<Class<?>> CLASSES = unmodifiableSet(new HashSet<>(asList(
            Dataset.class, DatasetGraph.class
    )));

    public DatasetItParser() {
        super(CLASSES, Quad.class, QUAD);
    }

    @Override public @Nonnull <T> RDFIt<T> parse(@Nonnull Object source) {
        DatasetGraph dsg = source instanceof DatasetGraph ? (DatasetGraph) source
                                                          : ((Dataset) source).asDatasetGraph();
        return new PlainRDFIt<>(Quad.class, QUAD, dsg.find(), source);
    }
}
