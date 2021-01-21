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
import com.github.lapesd.rdfit.util.NoSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * An {@link RDFListener} that delegates all methods to another.
 * @param <T> the triple representation class
 * @param <Q> the quad representation class
 */
@SuppressWarnings("unchecked")
public class DelegatingRDFListener<T, Q> implements RDFListener<T, Q> {
    protected final @Nonnull RDFListener<?, ?> target;
    protected @Nonnull Object source = NoSource.INSTANCE;
    protected @Nullable String baseIRI = null;
    protected @Nullable SourceQueue sourceQueue = null;

    public DelegatingRDFListener(@Nonnull RDFListener<?, ?> target) {
        this.target = target;
    }

    @Override public @Nullable Class<T> tripleType() {
        return (Class<T>) target.tripleType();
    }

    @Override public @Nullable Class<Q> quadType() {
        return (Class<Q>) target.quadType();
    }

    @Override public void attachSourceQueue(@Nonnull SourceQueue queue) {
        this.sourceQueue = queue;
        target.attachSourceQueue(queue);
    }

    @Override
    public boolean notifyInconvertibleTriple(@Nonnull InconvertibleException e)
            throws InterruptParsingException {
        return target.notifyInconvertibleTriple(e);
    }

    @Override
    public boolean notifyInconvertibleQuad(@Nonnull InconvertibleException e)
            throws InterruptParsingException {
        return target.notifyInconvertibleQuad(e);
    }

    @Override public boolean notifyParseWarning(@Nonnull String message) {
        return target.notifyParseWarning(message);
    }

    @Override public boolean notifyParseError(@Nonnull String message) {
        return target.notifyParseError(message);
    }

    @Override public boolean notifySourceError(@Nonnull RDFItException exception) {
        return target.notifySourceError(exception);
    }

    @Override public void triple(@Nonnull T triple) {
        ((RDFListener<T, Q>)target).triple(triple);
    }

    @Override public void quad(@Nonnull Q quad) {
        ((RDFListener<T, Q>)target).quad(quad);
    }

    @Override public void quad(@Nonnull String graph, @Nonnull T triple) {
        ((RDFListener<T, Q>)target).quad(graph, triple);
    }

    @Override public void prefix(@Nonnull String prefixLabel, @Nonnull String iriPrefix) {
        target.prefix(prefixLabel, iriPrefix);
    }

    @Override public void start(@Nonnull Object source) {
        this.source = source;
        this.baseIRI = null;
        target.start(source);
    }

    @Override public void baseIRI(@Nonnull String baseIRI) {
        this.baseIRI = baseIRI;
        target.baseIRI(baseIRI);
    }

    @Override public void finish(@Nonnull Object source) {
        if (!Objects.equals(this.source, source) && this.source != NoSource.INSTANCE)
            throw new IllegalStateException("finish("+source+"), expected finish("+this.source+")");
        this.source = NoSource.INSTANCE;
        baseIRI = null;
        target.finish(source);
    }

    @Override public void finish() {
        target.finish();
        sourceQueue = null;
    }
}
