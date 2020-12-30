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
