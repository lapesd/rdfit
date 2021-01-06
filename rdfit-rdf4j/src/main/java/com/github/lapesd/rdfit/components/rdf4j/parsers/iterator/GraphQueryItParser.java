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

package com.github.lapesd.rdfit.components.rdf4j.parsers.iterator;

import com.github.lapesd.rdfit.components.rdf4j.parsers.iterator.impl.GraphQueryResultRDFIt;
import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.iterator.RDFIt;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;

import javax.annotation.Nonnull;
import java.util.Collections;

public class GraphQueryItParser extends AbstractRDF4JItParser {
    public GraphQueryItParser(@Nonnull IterationElement itElement) {
        super(Collections.singleton(GraphQuery.class), itElement);
    }

    @Override protected @Nonnull RDFIt<Statement> doParse(@Nonnull Object source) {
        GraphQuery q = (GraphQuery) source;
        GraphQueryResult result = q.evaluate();
        return new GraphQueryResultRDFIt(source, null, result);
    }
}
