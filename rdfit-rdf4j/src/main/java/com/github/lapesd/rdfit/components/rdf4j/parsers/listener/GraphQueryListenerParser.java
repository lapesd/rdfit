package com.github.lapesd.rdfit.components.rdf4j.parsers.listener;

import com.github.lapesd.rdfit.components.rdf4j.listener.RDFListenerHandler;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;

import javax.annotation.Nonnull;
import java.util.Collections;

public class GraphQueryListenerParser extends RDFHandlerParser {
    private final @Nonnull RDFHandlerParser resultParser = new GraphQueryResultListenerParser();

    public GraphQueryListenerParser() {
        super(Collections.singleton(GraphQuery.class));
    }

    @Override protected void parse(@Nonnull Object source, @Nonnull RDFListenerHandler handler) {
        try (GraphQueryResult result = ((GraphQuery) source).evaluate()) {
            resultParser.parse(result, handler);
        }
    }
}
