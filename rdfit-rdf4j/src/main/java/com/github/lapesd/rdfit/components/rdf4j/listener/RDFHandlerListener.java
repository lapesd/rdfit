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
import com.github.lapesd.rdfit.listener.RDFListenerBase;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFHandler;

import javax.annotation.Nonnull;

/**
 * A {@link RDFListener} that forwards triples and statements to an {@link RDFHandler}.
 */
public class RDFHandlerListener extends RDFListenerBase<Statement, Statement> {
    private boolean started = false;
    private final @Nonnull RDFHandler handler;

    public RDFHandlerListener(@Nonnull RDFHandler handler) {
        super(Statement.class, Statement.class);
        this.handler = handler;
    }

    @Override public void triple(@Nonnull Statement triple) {
        handler.handleStatement(triple);
    }

    @Override public void quad(@Nonnull Statement quad) {
        handler.handleStatement(quad);
    }

    @Override public void start(@Nonnull Object source) {
        super.start(source);
        if (!started) {
            started = true;
            handler.startRDF();
        }
    }

    @Override public void finish(@Nonnull Object source) {
        super.finish(source);
    }

    @Override public void finish() {
        handler.endRDF();
        super.finish();
    }

    @Override public @Nonnull String toString() {
        return "RDFListener2RDFHandler";
    }
}
