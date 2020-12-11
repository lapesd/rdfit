package com.github.lapesd.rdfit.data;

import javax.annotation.Nonnull;
import java.util.Objects;

import static java.lang.String.format;

public class QuadMock1 implements QuadMock {
    private final @Nonnull String graph;
    private final @Nonnull TripleMock1 triple;

    public QuadMock1(@Nonnull String graph, @Nonnull TripleMock1 triple) {
        this.graph = graph;
        this.triple = triple;
    }

    public QuadMock1(@Nonnull String graph, @Nonnull String subject,
                     @Nonnull String predicate, @Nonnull String object) {
        this.graph = graph;
        this.triple = new TripleMock1(subject, predicate, object);
    }

    public @Nonnull String getGraph() {
        return graph;
    }

    public @Nonnull TripleMock1 getTriple() {
        return triple;
    }

    @Override public @Nonnull String toString() {
        TripleMock1 t = getTriple();
        return format("(%s %s %s %s)", getGraph(), t.getSubject(), t.getPredicate(), t.getObject());
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuadMock1)) return false;
        QuadMock1 quadMock1 = (QuadMock1) o;
        return getGraph().equals(quadMock1.getGraph()) && getTriple().equals(quadMock1.getTriple());
    }

    @Override public int hashCode() {
        return Objects.hash(getGraph(), getTriple());
    }
}
