package com.github.com.alexishuf.rdfit.callback;

import com.github.com.alexishuf.rdfit.errors.InconvertibleException;
import com.github.com.alexishuf.rdfit.errors.InterruptParsingException;
import com.github.com.alexishuf.rdfit.errors.RDFItException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Consume triples (and quads) via a callback.
 *
 * The {@link #quad(Object)} and {@link #triple(Object)} methods are mutually exclusive: a quad instance will only be delivered to quad
 *
 * Use {@link RDFCallbackBase} for simplifying implementations.
 *
 */
public interface RDFCallback {
    /**
     * The type of Triple objects (T) supported by this callback, or null if triples
     * are not supported.
     */
    @Nonnull Class<?> tripleType();
    /**
     * The type of quad objects (Q) supported by this callback, or null if quads
     * are not supported.
     */
    @Nullable Class<?> quadType();

    /**
     * Notify that a triple from source could not be converted to {@link #tripleType()}.
     *
     * After this method, parsing can still continue, depending on the return value.
     *
     * @return if true, the parser will continue parsing the source, if false it will stop
     *         parsing that source, but other sources will still be processed.
     * @throws InterruptParsingException if the callback throws this, neither the source
     *                                   nor any subsequent sources will be parsed.
     */
    boolean notifyInconvertibleTriple(@Nonnull InconvertibleException e)
            throws InterruptParsingException;

    /**
     * Notify that a quad from source could not be converted to {@link #quadType()}
     * (if not null) or could not be converted to {@link #tripleType()}
     * (if {@link #quadType()} is null).
     *
     * After this method, parsing can still continue, depending on the return value.
     *
     * @return if true, the parser will continue parsing the source, if false it will stop
     *         parsing that source, but other sources will still be processed.
     * @throws InterruptParsingException if the callback throws this, neither the source
     *                                   nor any subsequent sources will be parsed.
     */
    boolean notifyInconvertibleQuad(@Nonnull InconvertibleException e)
            throws InterruptParsingException ;


    /**
     * Notify that an exception occurred when parsing the given source.
     *
     * Parsing for the offending source terminates, but further sources may still be parsed.
     *
     * @param source the offending source
     * @param exception the exception
     * @return if true, parsing of subsequent sources will continue normally, else no
     *         more sources will be parsed.
     */
    boolean notifySourceError(@Nonnull Object source, @Nonnull RDFItException exception);

    /**
     * This method is called for every triple. If {@link #quadType()} is null, a
     * {@link #quad(Object, Object)} implementation may delegate triples to this method.
     *
     * @param triple the triple instance
     */
    <X> void triple(@Nonnull X triple);

    /**
     * This method is called for each quad if {@link #quadType()} is not null.
     *
     * @param quad the quad object
     */
    <Q> void quad(@Nonnull Q quad);

    /**
     * This method is called to deliver a quad if {@link #quadType()} is null
     *
     * @param graph the graph IRI (or other identification object)
     * @param triple the triple, as would
     */
    <T> void quad(@Nonnull Object graph, @Nonnull T triple);

    /**
     * Called before a source starts being processed. All {@link #triple(Object)},
     * {@link #quad(Object)} and {@link #quad(Object, Object)} calls, until {@link #finish(Object)}
     * correspond to triples and quads in that source.
     */
    void start(@Nonnull Object source);

    /**
     * Notifies that a source finished parsing.
     *
     * This method is not called after a {@link #notifySourceError(Object, RDFItException)}
     * call for the same source.
     */
    void finish(@Nonnull Object source);

    /**
     * Notifies that parsing of all sources is complete. This method is called even
     * if {@link #notifySourceError(Object, RDFItException)} was called before, and even
     * if parsing was interrupted with {@link InterruptParsingException}.
     */
    void finish();
}
