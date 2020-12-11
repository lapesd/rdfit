package com.github.lapesd.rdfit.components.jena.parsers.iterator;

import com.github.lapesd.rdfit.components.parsers.BaseItParser;
import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.iterator.PlainRDFIt;
import com.github.lapesd.rdfit.iterator.RDFIt;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;

import javax.annotation.Nonnull;
import java.util.Collections;

public class GraphItParser extends BaseItParser {
    public GraphItParser() {
        super(Collections.singleton(Graph.class), Triple.class, IterationElement.TRIPLE);
    }

    @Override public @Nonnull <T> RDFIt<T> parse(@Nonnull Object source) {
        ExtendedIterator<Triple> it = ((Graph) source).find();
        return new PlainRDFIt<>(Triple.class, IterationElement.TRIPLE, it, source);
    }
}
