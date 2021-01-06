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

import com.github.lapesd.rdfit.errors.InconvertibleException;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.util.NoSource;
import com.github.lapesd.rdfit.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.github.lapesd.rdfit.util.Utils.compactClass;

public abstract class RDFListenerBase<T, Q> implements RDFListener<T, Q> {
    private static final Logger logger = LoggerFactory.getLogger(RDFListenerBase.class);
    protected @Nullable Class<T> tripleType;
    protected @Nullable Class<Q> quadType;
    protected @Nonnull Object source = NoSource.INSTANCE;

    public RDFListenerBase(@Nullable Class<T> tripleType) {
        this(tripleType, null);
    }

    public RDFListenerBase(@Nullable Class<T> tripleType, @Nullable Class<Q> quadType) {
        assert tripleType != null || quadType != null;
        this.tripleType = tripleType;
        this.quadType = quadType;
    }

    @Override
    public boolean notifyInconvertibleTriple(@Nonnull InconvertibleException e)
            throws InterruptParsingException {
        logger.warn("Ignoring inconvertible triple {} of type {} from source {} (desired: {}).",
                    e.getInput(), e.getDesired(), e.getSource(), tripleType());
        throw new InterruptParsingException();
    }

    @Override
    public boolean notifyInconvertibleQuad(@Nonnull InconvertibleException e)
            throws InterruptParsingException {
        logger.warn("Ignoring inconvertible quad {} of type {} from source {} (desired: {}).",
                    e.getInput(), e.getDesired(), e.getSource(), tripleType());
        throw new InterruptParsingException();
    }

    @Override public boolean notifyParseWarning(@Nonnull String message) {
        logger.warn("Ignoring warning parsing source {}: {}", source, message);
        return true;
    }

    @Override public boolean notifyParseError(@Nonnull String message) {
        logger.warn("Ignoring recoverable error parsing source {}: {}", source, message);
        return true;
    }

    @Override
    public boolean notifySourceError(@Nonnull RDFItException e) {
        logger.error("{}.notifySourceError(): Failed to parse {}. Stopping", this, source, e);
        return false; //do not parse next sources
    }

    @Override public @Nullable Class<T> tripleType() {
        return tripleType;
    }

    @Override public @Nullable Class<Q> quadType() {
        return quadType;
    }

    @Override public void quad(@Nonnull Q quad) {
        if (quadType() == null)
            throw new UnsupportedOperationException("Should have called quad(graph, triple)");
        else
            throw new UnsupportedOperationException("Method quad(quad) not implemented.");
    }

    @Override public void quad(@Nonnull String graph, @Nonnull T triple) {
        if (quadType() == null)
            triple(triple);
        else
            throw new UnsupportedOperationException("Should have called quad(quad)");
    }

    @Override public void finish(@Nonnull Object source) {
        logger.debug("{}.finish({})", this, source);
        this.source = NoSource.INSTANCE;
    }

    @Override public void start(@Nonnull Object source) {
        this.source = source;
        logger.debug("{}.start({})", this, source);
    }

    @Override public void finish() {
        logger.debug("{}.finish()", this);
    }

    @Override public String toString() {
        return String.format("%s<T=%s, Q=%s>", Utils.toString(this),
                             compactClass(tripleType()), compactClass(quadType()));
    }
}
