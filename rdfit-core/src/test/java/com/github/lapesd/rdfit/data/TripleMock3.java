package com.github.lapesd.rdfit.data;

import javax.annotation.Nonnull;
import java.util.Objects;

public class TripleMock3 implements TripleMock {
    private final @Nonnull String subject, predicate, object;

    public TripleMock3(@Nonnull String subject, @Nonnull String predicate, @Nonnull String object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    public @Nonnull String getSubject() {
        return subject;
    }

    public @Nonnull String getPredicate() {
        return predicate;
    }

    public @Nonnull String getObject() {
        return object;
    }

    @Override public String toString() {
        return String.format("[%s %s %s]", getSubject(), getPredicate(), getObject());
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TripleMock3)) return false;
        TripleMock3 triple = (TripleMock3) o;
        return getSubject().equals(triple.getSubject()) && getPredicate().equals(triple.getPredicate()) && getObject().equals(triple.getObject());
    }

    @Override public int hashCode() {
        return Objects.hash(getSubject(), getPredicate(), getObject());
    }
}
