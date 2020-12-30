package com.github.lapesd.rdfit.components.rdf4j.parsers.iterator;

import com.github.lapesd.rdfit.components.rdf4j.parsers.iterator.impl.RepositoryResultRDFIt;
import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.iterator.RDFIt;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;

import javax.annotation.Nonnull;
import java.util.Collections;

public class RepositoryItParser extends AbstractRDF4JItParser {
    public RepositoryItParser(@Nonnull IterationElement itElement) {
        super(Collections.singleton(Repository.class), itElement);
    }

    @Override protected @Nonnull RDFIt<Statement> doParse(@Nonnull Object source) {
        RepositoryConnection conn = ((Repository) source).getConnection();
        RepositoryResult<Statement> results = conn.getStatements(null, null, null);
        return new RepositoryResultRDFIt(iterationElement(), source, conn, results);
    }
}
