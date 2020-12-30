package com.github.lapesd.rdfit.components.rdf4j.parsers.iterator;

import com.github.lapesd.rdfit.components.rdf4j.parsers.iterator.impl.TupleQueryResultRDFIt;
import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.iterator.RDFIt;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collections;

public class TupleQueryResultItParser extends AbstractRDF4JItParser {
    private static final Logger logger = LoggerFactory.getLogger(TupleQueryResultItParser.class);

    public TupleQueryResultItParser(@Nonnull IterationElement itElement) {
        super(Collections.singleton(TupleQueryResult.class), itElement);
    }

    @Override public boolean canParse(@Nonnull Object source) {
        if (!super.canParse(source))
            return false;
        TupleQueryResult r = (TupleQueryResult) source;
        int s = r.getBindingNames().size();
        if (s != 3 && s != 4) {
            logger.warn("TupleQueryResult {} has {} vars, cannot parse as triple nor quad", r, s);
            return false;
        }
        return true;
    }

    @Override protected @Nonnull RDFIt<Statement> doParse(@Nonnull Object source) {
        return new TupleQueryResultRDFIt(iterationElement(), source, (TupleQueryResult) source);
    }
}
