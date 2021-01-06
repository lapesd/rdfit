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

import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.listener.RDFListenerBase;
import com.github.lapesd.rdfit.util.Utils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.GraphFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class DatasetGraphFeeder extends RDFListenerBase<Triple, Quad> {
    private static final Logger logger = LoggerFactory.getLogger(DatasetGraphFeeder.class);
    private final @Nonnull DatasetGraph dsg;

    public DatasetGraphFeeder(@Nonnull DatasetGraph dsg) {
        super(Triple.class, Quad.class);
        this.dsg = dsg;
    }

    @Override public void triple(@Nonnull Triple triple) {
        dsg.getDefaultGraph().add(triple);
    }

    @Override public void quad(@Nonnull Quad quad) {
        if (quad.isDefaultGraph()) {
            triple(quad.asTriple());
            return;
        }
        Node name = quad.getGraph();
        if (!name.isURI())
            throw new RDFItException(source, "Graph name "+name+" in quad "+quad+" is not an IRI");
        Graph graph = dsg.getGraph(name);
        if (graph == null) {
            logger.debug("Creating new graph {} in dataset", name);
            dsg.addGraph(name, GraphFactory.createDefaultGraph());
            graph = dsg.getGraph(name);
        }
        graph.add(quad.asTriple());
    }

    @Override public @Nonnull String toString() {
        return Utils.toString(this);
    }
}
