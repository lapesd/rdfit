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
