package com.github.com.alexishuf.rdfit.data;

import javax.annotation.Nonnull;

import java.util.Objects;

import static java.lang.String.format;

public class QuadMock2 extends TripleMock2 implements QuadMock {
    private final @Nonnull String graph;

    public QuadMock2(@Nonnull String graph, @Nonnull String subject, @Nonnull String predicate,
                     @Nonnull String object) {
        super(subject, predicate, object);
        this.graph = graph;
    }

    public @Nonnull String getGraph() {
        return graph;
    }

    @Override public @Nonnull String toString() {
        return format("{%s %s %s %s}", getGraph(), getSubject(), getPredicate(), getObject());
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuadMock2)) return false;
        if (!super.equals(o)) return false;
        QuadMock2 quadMock2 = (QuadMock2) o;
        return getGraph().equals(quadMock2.getGraph());
    }

    @Override public int hashCode() {
        return Objects.hash(super.hashCode(), getGraph());
    }
}
