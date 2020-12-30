package com.github.lapesd.rdfit.components.rdf4j.parsers.listener;

import com.github.lapesd.rdfit.components.rdf4j.listener.RDFListenerHandler;
import org.eclipse.rdf4j.query.TupleQuery;

import javax.annotation.Nonnull;
import java.util.Collections;

public class TupleQueryListenerParser extends RDFHandlerParser {
    private final @Nonnull RDFHandlerParser resultParser = new TupleQueryResultListenerParser();

    public TupleQueryListenerParser() {
        super(Collections.singleton(TupleQuery.class));
    }

    @Override protected void parse(@Nonnull Object source,
                                   @Nonnull RDFListenerHandler handler) {
        resultParser.parse(((TupleQuery)source).evaluate(), handler);
    }
}
