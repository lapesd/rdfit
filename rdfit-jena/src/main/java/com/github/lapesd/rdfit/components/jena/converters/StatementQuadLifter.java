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

package com.github.lapesd.rdfit.components.jena.converters;

import com.github.lapesd.rdfit.components.converters.quad.QuadLifterBase;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.Quad;

import javax.annotation.Nonnull;

public class StatementQuadLifter extends QuadLifterBase {

    private final @Nonnull Node graphIRI;

    public StatementQuadLifter() {
        this(Quad.defaultGraphIRI);
    }
    public StatementQuadLifter(@Nonnull String graphIRI) {
        this(NodeFactory.createURI(graphIRI));
    }
    public StatementQuadLifter(@Nonnull Node graphIRI) {
        super(Statement.class);
        if (!graphIRI.isURI())
            throw new IllegalArgumentException("graphIRI="+graphIRI+" is not an IRI Node");
        this.graphIRI = graphIRI;
    }
    public StatementQuadLifter(@Nonnull Resource graphIRI) {
        this(graphIRI.asNode());
    }

    @Override public @Nonnull Object lift(@Nonnull Object triple) {
        return new Quad(graphIRI, ((Statement)triple).asTriple());
    }

    @Override public @Nonnull String toString() {
        return "StatementQuadLifter("+graphIRI+")";
    }
}
