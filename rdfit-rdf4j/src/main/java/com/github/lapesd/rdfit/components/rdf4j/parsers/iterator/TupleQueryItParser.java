package com.github.lapesd.rdfit.components.rdf4j.parsers.iterator;

import com.github.lapesd.rdfit.components.rdf4j.parsers.iterator.impl.TupleQueryResultRDFIt;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.iterator.RDFIt;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collections;

public class TupleQueryItParser extends AbstractRDF4JItParser {
    private static final Logger logger = LoggerFactory.getLogger(TupleQueryItParser.class);

    public TupleQueryItParser(@Nonnull IterationElement itElement) {
        super(Collections.singleton(TupleQuery.class), itElement);
    }

    @Override protected @Nonnull RDFIt<Statement> doParse(@Nonnull Object source) {
        TupleQuery q = (TupleQuery) source;
        TupleQueryResult result = q.evaluate();
        int size = result.getBindingNames().size();
        if (size != 3 && size != 4) {
            try {
                result.close();
            } catch (Throwable t) {
                logger.error("Ignoring failed TupleQueryResult.close() from query {}.", q, t);
            }
            throw new RDFItException(source, "TupleQuery has "+size+" variables. " +
                                             "Triples/quads required 3/4.");
        }
        return new TupleQueryResultRDFIt(iterationElement(), source, result);
    }
}
