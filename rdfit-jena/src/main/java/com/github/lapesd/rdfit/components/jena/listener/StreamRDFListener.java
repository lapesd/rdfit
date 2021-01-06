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

package com.github.lapesd.rdfit.components.jena.listener;

import com.github.lapesd.rdfit.listener.RDFListenerBase;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;

import javax.annotation.Nonnull;

public class StreamRDFListener extends RDFListenerBase<Triple, Quad> {
    private final @Nonnull StreamRDF streamRDF;

    public StreamRDFListener(@Nonnull StreamRDF streamRDF) {
        super(Triple.class, Quad.class);
        this.streamRDF = streamRDF;
    }

    @Override public void triple(@Nonnull Triple triple) {
        streamRDF.triple(triple);
    }

    @Override public void quad(@Nonnull Quad quad) {
        streamRDF.quad(quad);
    }

    @Override public void finish(@Nonnull Object source) {
        streamRDF.finish();
    }

    @Override public void start(@Nonnull Object source) {
        streamRDF.start();
    }
}
