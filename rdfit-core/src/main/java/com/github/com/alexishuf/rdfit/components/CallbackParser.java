package com.github.com.alexishuf.rdfit.components;

import com.github.com.alexishuf.rdfit.callback.RDFCallback;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A parser that will call one of the methods in the callback for every triple/quad parsed.
 *
 */
public interface CallbackParser extends Parser {
    /**
     * Class of triples delivered to the callbacks
     */
    @Nonnull Class<?> tripleType();

    /**
     * Class of quads delivered to the callbacks, or null if this parser does not produce quads
     */
    @Nullable Class<?> quadType();

    /**
     * Parse the given source and call the appropriate method in callback for every triple/quad.
     *
     * @param source the source to parse
     * @param callback the callback to deliver quads or triples
     */
    void parse(@Nonnull Object source, RDFCallback callback);
}
