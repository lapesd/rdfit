package com.github.com.alexishuf.rdfit.iterator;

import com.github.com.alexishuf.rdfit.util.Utils;

import javax.annotation.Nonnull;

public abstract class BaseRDFIt<T> implements RDFIt<T> {
    protected final @Nonnull Class<? extends T> valueClass;
    protected boolean closed = false;

    public BaseRDFIt(@Nonnull Class<? extends T> valueClass) {
        this.valueClass = valueClass;
    }

    @Override public @Nonnull Class<? extends T> valueClass() {
        return valueClass;
    }

    @Override public String toString() {
        return Utils.genericToString(this, valueClass());
    }

    @Override public void close() {
        closed = true;
    }
}
