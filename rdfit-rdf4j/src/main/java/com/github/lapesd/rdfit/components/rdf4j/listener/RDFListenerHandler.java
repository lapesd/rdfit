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

package com.github.lapesd.rdfit.components.rdf4j.listener;

import com.github.lapesd.rdfit.listener.RDFListener;
import com.github.lapesd.rdfit.util.Utils;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import static java.lang.String.format;

/**
 * A {@link RDFHandler} implementation that delegates to a {@link RDFListener}
 */
public class RDFListenerHandler implements RDFHandler, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(RDFListenerHandler.class);

    private final @Nonnull RDFListener<?, ?> target;
    private final @Nonnull Object source;
    private final boolean deliverQuads, deliverTriples;
    private int started = 0, finished = 0;

    public RDFListenerHandler(@Nonnull RDFListener<?, ?> target, @Nonnull Object source) {
        Class<?> tt = target.tripleType(), qt = target.quadType();
        if (tt == null && qt == null)
            throw new IllegalArgumentException("No triple nor quad type in "+target);
        if (tt != null && !tt.isAssignableFrom(Statement.class))
            throw new IllegalArgumentException(getClass()+" cannot provide "+tt+" triples");
        if (qt != null && !qt.isAssignableFrom(Statement.class))
            throw new IllegalArgumentException(getClass()+" cannot provide "+qt+" quads");
        this.deliverTriples = tt != null;
        this.deliverQuads = qt != null;
        this.target = target;
        this.source = source;
    }

    public @Nonnull RDFListener<?, ?> getTarget() {
        return target;
    }

    @Override public void startRDF() throws RDFHandlerException {
        ++started;
        target.start(source);
    }

    @Override public void endRDF() throws RDFHandlerException {
        ++finished;
        if (finished <= started)
            target.finish(source);
    }

    @Override public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
        target.prefix(prefix, uri);
    }

    @Override public void handleComment(String comment) throws RDFHandlerException {
        logger.debug("{}: # {}", this, comment);
    }

    @SuppressWarnings("unchecked")
    @Override public void handleStatement(Statement stmt) throws RDFHandlerException {
        boolean isQuad = stmt.getContext() != null;
        if (!isQuad && deliverTriples)
            ((RDFListener<Statement, ?>)target).triple(stmt);
        else if (isQuad && !deliverQuads)
            ((RDFListener<Statement, ?>)target).quad(stmt.getContext().toString(), stmt);
        else
            ((RDFListener<?, Statement>)target).quad(stmt);
    }

    @Override public @Nonnull String toString() {
        return format("%s{source=%s, target=%s}", Utils.toString(this), source, target);
    }

    @Override public void close() {
        endRDF();
    }
}
