package com.github.lapesd.rdfit.components.jena.parsers.iterator;

import com.github.lapesd.rdfit.components.parsers.BaseItParser;
import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.iterator.PlainRDFIt;
import com.github.lapesd.rdfit.iterator.RDFIt;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import javax.annotation.Nonnull;
import java.util.Collections;

public class ModelItParser extends BaseItParser {
    public ModelItParser() {
        super(Collections.singleton(Model.class), Statement.class, IterationElement.TRIPLE);
    }

    @Override public @Nonnull <T> RDFIt<T> parse(@Nonnull Object source) {
        StmtIterator it = ((Model) source).listStatements();
        return new PlainRDFIt<>(Statement.class, IterationElement.TRIPLE, it, source);
    }
}
