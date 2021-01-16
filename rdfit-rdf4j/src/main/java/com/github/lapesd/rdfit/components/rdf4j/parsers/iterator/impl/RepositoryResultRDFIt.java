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

package com.github.lapesd.rdfit.components.rdf4j.parsers.iterator.impl;

import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.impl.ClosedSourceQueue;
import com.github.lapesd.rdfit.iterator.BaseRDFIt;
import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.util.Utils;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RepositoryResultRDFIt extends BaseRDFIt<Statement> {
    private static final Logger logger = LoggerFactory.getLogger(RepositoryResultRDFIt.class);
    private final @Nonnull Object source;
    private final @Nullable RepositoryConnection connection;
    private final @Nonnull RepositoryResult<?> result;

    public RepositoryResultRDFIt(@Nonnull IterationElement itElement, @Nonnull Object source,
                                 @Nullable RepositoryConnection connection,
                                 @Nonnull RepositoryResult<?> result) {
        super(Statement.class, itElement, new ClosedSourceQueue());
        this.source = source;
        this.connection = connection;
        this.result = result;
    }

    @Override public @Nonnull Object getSource() {
        return source;
    }

    @Override public boolean hasNext() {
        try {
            return result.hasNext();
        } catch (Throwable t) {
            throw new RDFItException(this+".hasNext(): RepositoryResult failed", t);
        }
    }

    @Override public @Nonnull Statement next() {
        try {
            return (Statement) result.next();
        } catch (ClassCastException e) {
            String msg = this + ".next(): RepositoryResult over non-Statement";
            throw new RDFItException(getSource(), msg, e);
        } catch (Throwable t) {
            throw new RDFItException(getSource(), this+".next(): RepositoryResult failed", t);
        }
    }

    @Override public void close() {
        if (closed)
            return;
        try {
            result.close();
        } catch (Throwable t) {
            logger.error("{}.close() failed to close RepositoryResult {}: {}",
                         this, result, t.getMessage(), t);
        }
        try {
            if (connection != null)
                connection.close();
        } catch (Throwable t) {
            logger.error("{}.close() failed to close non-null connection {}: {}",
                         this, connection, t.getMessage(), t);
        }
        super.close();
    }

    @Override public @Nonnull String toString() {
        return String.format("%s{source=%s,conn=%s,result=%s}",
                             Utils.toString(this), source, connection, result);
    }
}
