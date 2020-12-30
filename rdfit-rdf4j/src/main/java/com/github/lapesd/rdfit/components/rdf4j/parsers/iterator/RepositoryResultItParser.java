package com.github.lapesd.rdfit.components.rdf4j.parsers.iterator;

import com.github.lapesd.rdfit.components.rdf4j.parsers.iterator.impl.RepositoryResultRDFIt;
import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.iterator.RDFIt;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryResult;

import javax.annotation.Nonnull;
import java.util.Collections;

public class RepositoryResultItParser extends AbstractRDF4JItParser {
    public RepositoryResultItParser(@Nonnull IterationElement itElement) {
        super(Collections.singleton(RepositoryResult.class), itElement);
    }

    @Override protected @Nonnull RDFIt<Statement> doParse(@Nonnull Object source) {
        RepositoryResult<?> result = (RepositoryResult<?>) source;
        return new RepositoryResultRDFIt(iterationElement(), source, null, result);
    }
}
