package com.github.lapesd.rdfit.iterator;

import com.github.lapesd.rdfit.util.Utils;

import javax.annotation.Nonnull;

public abstract class BaseRDFIt<T> implements RDFIt<T> {
    protected final @Nonnull Class<?> valueClass;
    protected final @Nonnull IterationElement itElement;
    protected boolean closed = false;

    public BaseRDFIt(@Nonnull Class<?> valueClass, @Nonnull IterationElement itElement) {
        this.valueClass = valueClass;
        this.itElement = itElement;
    }

    @Override public @Nonnull Class<? extends T> valueClass() {
        //noinspection unchecked
        return (Class<? extends T>) valueClass;
    }

    @Override public @Nonnull IterationElement itElement() {
        return itElement;
    }

    @Override public String toString() {
        return Utils.genericToString(this, valueClass());
    }

    @Override public void close() {
        closed = true;
    }
}
