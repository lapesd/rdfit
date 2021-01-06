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
import com.github.lapesd.rdfit.errors.InconvertibleException;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.listener.RDFListener;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class QueryExecutionListenerParser extends BaseListenerParser {
    private final @Nonnull DatasetListenerParser dsParser = new DatasetListenerParser();

    public QueryExecutionListenerParser() {
        super(Collections.singleton(QueryExecution.class), Triple.class, Quad.class);
    }

    @Override public boolean canParse(@Nonnull Object source) {
        if (!super.canParse(source)) return false;
        Query q = ((QueryExecution) source).getQuery();
        return q.isSelectType() || q.isDescribeType() || q.isConstructType();
    }

    private @Nonnull Triple toTriple(@Nonnull Object source, @Nonnull List<Var> vars,
                                     @Nonnull Binding binding) {
        Node s = binding.get(vars.get(0));
        Node p = binding.get(vars.get(1));
        Node o = binding.get(vars.get(2));
        String msg = null;
        if (s == null || p == null || o == null) {
            msg = "Null subject, predicate or object in "+binding;
        } else if (!s.isConcrete() || !p.isConcrete() || !o.isConcrete()) {
            msg = "Variable subject, predicate or object in "+binding;
        } else if (s.isLiteral()) {
            msg = "Subject ("+vars.get(0)+") is not a resource in "+binding;
        } else if (!s.isURI()) {
            msg = "Predicate ("+vars.get(1)+") is not an IRI in "+binding;
        }
        if (msg != null)
            throw new InconvertibleException(source, binding, Triple.class, msg);
        return new Triple(s, p, o);
    }

    @Override
    public void parse(@Nonnull Object source,
                      @Nonnull RDFListener<?, ?> listener) throws InterruptParsingException {
        try (QueryExecution exec = (QueryExecution) source) {
            Query query = exec.getQuery();
            if (query.isConstructType()) {
                // this is not the best performance, but is the only API-exposed way to get
                // both triples and quads
                dsParser.parse(source, exec.execConstructDataset().asDatasetGraph(), listener);
                return;
            }

            try (ListenerFeeder feeder = createListenerFeeder(listener, source)) {
                if (query.isDescribeType()) {
                    exec.execDescribeTriples().forEachRemaining(feeder::feedTriple);
                } else if (query.isSelectType()) {
                    List<Var> vs = query.getProjectVars();
                    if (vs.size() == 3 || vs.size() == 4) {
                        parseBindings(source, listener, exec, feeder, vs);
                    } else {
                        throw new RDFItException(source, "Cannot parse triples/quads from SELECT " +
                                                         "with "+vs.size()+" vs");
                    }
                } else {
                    throw new RDFItException(source, "Unsupported query type: "+query);
                }
            } catch (InterruptParsingException e) {
                throw e;
            } catch (Throwable t) {
                if (!listener.notifySourceError(RDFItException.wrap(source, t)))
                    throw new InterruptParsingException();
            }
        }
    }

    private void parseBindings(@Nonnull Object source, @Nonnull RDFListener<?, ?> listener,
                               @Nonnull QueryExecution exec, @Nonnull ListenerFeeder feeder,
                               @Nonnull List<Var> vars) {
        ResultSet rs = exec.execSelect();
        boolean hasGraphVar = vars.size() > 3;
        while (rs.hasNext()) {
            Binding binding = rs.nextBinding();
            Node graph = hasGraphVar ? binding.get(vars.get(3)) : null;
            try {
                Triple triple = toTriple(source, vars, binding);
                if (graph == null) {
                    feeder.feedTriple(triple);
                } else {
                    if (!graph.isURI()) {
                        throw new InconvertibleException(source, binding, Quad.class,
                                "variable "+ vars.get(3)+" in "+binding+" does not " +
                                        "contain a graph IRI");
                    }
                    feeder.feedQuad(new Quad(graph, triple));
                }
            } catch (InconvertibleException e) {
                if (graph != null) {
                    if (!listener.notifyInconvertibleQuad(e)) break;
                } else if (!listener.notifyInconvertibleTriple(e)) {
                    break;
                }
            }
        }
    }
}
