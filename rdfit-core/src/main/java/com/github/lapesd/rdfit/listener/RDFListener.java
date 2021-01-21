/*
 *    Copyright 2021 Alexis Armin Huf
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.lapesd.rdfit.listener;

import com.github.lapesd.rdfit.SourceQueue;
import com.github.lapesd.rdfit.errors.InconvertibleException;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Consume triples (and quads) via a callback.
 *
 * Use {@link RDFListenerBase}, {@link TripleListenerBase} or {@link QuadListenerBase} for
 * simplifying implementations.
 */
public interface RDFListener<T, Q> {
    /**
     * The type of Triple objects (T) supported by this callback, or null if triples
     * are not supported.
     *
     * @return class of triples accepted by {@link #triple(Object)} and
     *         by {@link #quad(String, Object)}.
     */
    @Nullable Class<T> tripleType();
    /**
     * The type of quad objects (Q) supported by this callback, or null if quads
     * are not supported.
     *
     * @return class of quads accepted by {@link #quad(Object)} or null if such method
     *         should not be called.
     */
    @Nullable Class<Q> quadType();

    /**
     * Attach this {@link RDFListener} to a {@link SourceQueue}, allowing it to later
     * queue additional sources.
     *
     * The attachment is only valid until {@link #finish()} is called, i.e., until parsing of
     * the queue finishes. Trying to {@link SourceQueue#add(SourceQueue.When, Object)} after
     * the queue has been fully processed will cause an {@link IllegalStateException}.
     *
     * @param queue the {@link SourceQueue} to use when queueing additional sources.
     */
    void attachSourceQueue(@Nonnull SourceQueue queue);

    /**
     * Notify that a triple from source could not be converted to {@link #tripleType()}.
     *
     * After this method, parsing can still continue, depending on the return value.
     *
     * @param e exception describing the triple
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
     * @param e Exception describing the issue
     * @return if true, the parser will continue parsing the source, if false it will stop
     *         parsing that source, but other sources will still be processed.
     * @throws InterruptParsingException if the callback throws this, neither the source
     *                                   nor any subsequent sources will be parsed.
     */
    boolean notifyInconvertibleQuad(@Nonnull InconvertibleException e)
            throws InterruptParsingException ;


    /**
     * Notify a wanrning issued by the underlying parser while parsing the current source.
     *
     * @param message the warning message
     * @return true if parsing of this source should continue. If false, parsing of the source
     *         will terminate but {@link #notifySourceError(RDFItException)} will not be called.
     */
    boolean notifyParseWarning(@Nonnull String message);

    /**
     * Notify about a recoverable error when parsing the input at the source.
     *
     * @param message The error message
     * @return true if parsing of this source should continue. If false, parsing of the source
     *         will terminate but {@link #notifySourceError(RDFItException)} will not be called.
     */
    boolean notifyParseError(@Nonnull String message);


    /**
     * Notify that an exception occurred when parsing the given source.
     *
     * Parsing for the offending source terminates, but further sources may still be parsed.
     *
     * @param exception the exception
     * @return if true, parsing of subsequent sources will continue normally, else no
     *         more sources will be parsed.
     */
    boolean notifySourceError(@Nonnull RDFItException exception);

    /**
     * This method is called for every triple. If {@link #quadType()} is null, a
     * {@link #quad(String, Object)} implementation may delegate triples to this method.
     *
     * @param triple the triple instance
     */
    void triple(@Nonnull T triple);

    /**
     * This method is called for each quad if {@link #quadType()} is not null.
     *
     * @param quad the quad object
     */
    void quad(@Nonnull Q quad);

    /**
     * This method is called to deliver a quad if {@link #quadType()} is null and the parser
     * can break the quad into a graph and a triple instance.
     *
     * @param graph the graph IRI
     * @param triple the triple, as would
     */
    void quad(@Nonnull String graph, @Nonnull T triple);

    /**
     * Notify a prefix definition (e.g., <code>@prefix label: &lt;IRI&gt;</code>) from the input document.
     *
     * @param prefixLabel the prefix label
     * @param iriPrefix the IRI used as prefix
     */
    void prefix(@Nonnull String prefixLabel, @Nonnull String iriPrefix);

    /**
     * Called before a source starts being processed. All {@link #triple(Object)},
     * {@link #quad(Object)} and {@link #quad(String, Object)} calls, until {@link #finish(Object)}
     * correspond to triples and quads in that source.
     *
     * @param source the source being started
     */
    void start(@Nonnull Object source);

    /**
     * Called when the source defines a base URI (e.g., Turtle/TriG @base) or just after start()
     * if the particular source has an implicit value (e.g., the source originates from an URL,
     * which then becomes the base IRI).
     *
     * This method may be called twice for a given source if it was loaded from an IRI and its
     * contents also define an explicit @base. In such scenario, the base IRI defined within
     * the source will be delivered later.
     *
     * <strong>Important:</strong> triples and quads are always delivered with absolute IRIs unless
     * the particular parser absolutely cannot create an absolute IRI. {@link RDFListener}s can
     * safely ignore this method.
     *
     * @param baseIRI the baseIRI
     */
    void baseIRI(@Nonnull String baseIRI);

    /**
     * Notifies that a source finished parsing.
     *
     * This method is not called after a {@link #notifySourceError(RDFItException)}
     * call for the same source.
     *
     * @param source The source being finished
     */
    void finish(@Nonnull Object source);

    /**
     * Notifies that parsing of all sources is complete. This method is called even
     * if {@link #notifySourceError(RDFItException)} was called before, and even
     * if parsing was interrupted with {@link InterruptParsingException}.
     */
    void finish();
}
