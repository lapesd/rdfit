package com.github.lapesd.rdfit.components;

import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.iterator.RDFIt;

import javax.annotation.Nonnull;

/**
 * A Parser that returns a {@link RDFIt} instance
 */
public interface ItParser extends Parser {
    /**
     * {@link RDFIt#valueClass()} of iterators returned by {@link #parse(Object)}.
     */
    @Nonnull Class<?> valueClass();

    /**
     * Whether this parser will produce a {@link RDFIt} over triples or quads.
     */
    @Nonnull IterationElement iterationElement();

    /**
     * Create a {@link RDFIt} that iterates over parsed triples (or quads) from the given source.
     *
     * A RDFIt will iterate either only triples or only quads.
     */
    @Nonnull <T> RDFIt<T> parse(@Nonnull Object source);
}
