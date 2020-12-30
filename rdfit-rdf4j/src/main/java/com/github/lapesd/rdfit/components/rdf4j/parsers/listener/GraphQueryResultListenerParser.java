package com.github.lapesd.rdfit.components.rdf4j.parsers.listener;

import com.github.lapesd.rdfit.components.rdf4j.listener.RDFListenerHandler;
import org.eclipse.rdf4j.query.GraphQueryResult;

import javax.annotation.Nonnull;
import java.util.Collections;

public class GraphQueryResultListenerParser extends RDFHandlerParser {
    public GraphQueryResultListenerParser() {
        super(Collections.singleton(GraphQueryResult.class));
    }

    @Override protected void parse(@Nonnull Object source, @Nonnull RDFListenerHandler handler) {
        try (GraphQueryResult result = (GraphQueryResult) source) {
            handler.startRDF();
            while (result.hasNext())
                handler.handleStatement(result.next());
            handler.endRDF();
        }
    }
}
