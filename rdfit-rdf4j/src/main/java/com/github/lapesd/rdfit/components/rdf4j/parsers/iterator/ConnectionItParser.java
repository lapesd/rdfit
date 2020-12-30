package com.github.lapesd.rdfit.components.rdf4j.parsers.iterator;

import com.github.lapesd.rdfit.components.rdf4j.parsers.iterator.impl.RepositoryResultRDFIt;
import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.iterator.RDFIt;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;

import javax.annotation.Nonnull;
import java.util.Collections;

public class ConnectionItParser extends AbstractRDF4JItParser {
    public ConnectionItParser(@Nonnull IterationElement iterationElement) {
        super(Collections.singleton(RepositoryConnection.class), iterationElement);
    }

    @Override protected @Nonnull  RDFIt<Statement> doParse(@Nonnull Object source) {
        RepositoryResult<Statement> results;
        results = ((RepositoryConnection) source).getStatements(null, null, null);
        return new RepositoryResultRDFIt(iterationElement(), source, null, results);
    }
}
