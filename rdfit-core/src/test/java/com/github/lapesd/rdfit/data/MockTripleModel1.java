package com.github.lapesd.rdfit.data;

import javax.annotation.Nonnull;
import java.util.Collection;

public class MockTripleModel1 {
    private final @Nonnull Collection<TripleMock1> triples;

    public MockTripleModel1(@Nonnull Collection<TripleMock1> triples) {
        this.triples = triples;
    }

    @Nonnull public Collection<TripleMock1> getTriples() {
        return triples;
    }
}
