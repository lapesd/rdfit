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

package com.github.lapesd.rdfit.components.rdf4j.parsers.listener;

import com.github.lapesd.rdfit.components.rdf4j.listener.RDFListenerHandler;
import com.github.lapesd.rdfit.errors.InconvertibleException;
import com.github.lapesd.rdfit.errors.RDFItException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class TupleQueryResultListenerParser extends RDFHandlerParser {
    private static final SimpleValueFactory FACTORY = SimpleValueFactory.getInstance();

    public TupleQueryResultListenerParser() {
        super(Collections.singleton(TupleQueryResult.class));
    }

    private @Nonnull Statement parse(@Nonnull Object source, @Nonnull List<String> vars,
                                     @Nonnull BindingSet set) {
        Value sv = set.getValue(vars.get(0));
        Value pv = set.getValue(vars.get(1));
        Value ov = set.getValue(vars.get(2));
        Value gv = vars.size() > 3 ? set.getValue(vars.get(3)) : null;
        String type = gv == null ? "triple" : "quad";
        if (sv == null || pv == null || ov == null) {
            throw new RDFItException(source, "Cannot convert BindingSet"+set+
                    " to "+type+": null S/P/O");
        } else if (!sv.isResource()) {
            throw new RDFItException(source, "Cannot convert BindingSet"+set+" to "+type+
                    ": subject is not a Resource");
        } else if (!pv.isIRI()) {
            throw new RDFItException(source, "Cannot convert BindingSet"+set+" to "+type+
                    ": predicate is not an IRI");
        } else if (gv != null && !gv.isResource()) {
            throw new RDFItException(source, "Cannot convert BindingSet"+set+" to quad: "+
                    "graph IRI "+gv+" is not a Resource");
        } else if (gv != null) {
            return FACTORY.createStatement((Resource) sv, (IRI) pv, ov, (Resource) gv);
        } else {
            return FACTORY.createStatement((Resource) sv, (IRI) pv, ov);
        }
    }

    @Override protected void parse(@Nonnull Object source, @Nonnull RDFListenerHandler handler) {
        try (TupleQueryResult result = (TupleQueryResult) source) {
            List<String> vars = result.getBindingNames();
            if (vars.size() != 3 && vars.size() != 4) {
                throw new RDFItException(source, "Cannot interpret TupleQueryResult as " +
                        "triples/quads. result table has "+vars.size()+" variables: "+vars);
            }
            handler.startRDF();
            for (boolean stop = false; !stop && result.hasNext(); ) {
                try {
                    handler.handleStatement(parse(source, vars, result.next()));
                } catch (InconvertibleException e) {
                    stop = vars.size() == 4
                            ? !handler.getTarget().notifyInconvertibleQuad(e)
                            : !handler.getTarget().notifyInconvertibleTriple(e);
                }
            }
            handler.endRDF();
        }
    }
}
