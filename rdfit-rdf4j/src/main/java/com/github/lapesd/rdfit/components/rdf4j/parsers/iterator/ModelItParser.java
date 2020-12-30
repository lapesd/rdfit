package com.github.lapesd.rdfit.components.rdf4j.parsers.iterator;

import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.iterator.PlainRDFIt;
import com.github.lapesd.rdfit.iterator.RDFIt;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;

import javax.annotation.Nonnull;
import java.util.Collections;

public class ModelItParser extends AbstractRDF4JItParser {
    public ModelItParser(@Nonnull IterationElement itElement) {
        super(Collections.singleton(Model.class), itElement);
    }

    @Override protected @Nonnull RDFIt<Statement> doParse(@Nonnull Object source) {
        return new PlainRDFIt<>(Statement.class, iterationElement(),
                                ((Model)source).iterator(), source);
    }
}
