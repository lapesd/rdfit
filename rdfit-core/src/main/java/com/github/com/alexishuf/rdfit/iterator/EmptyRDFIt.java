package com.github.com.alexishuf.rdfit.iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EmptyRDFIt<T> extends EagerRDFIt<T> {
    public EmptyRDFIt(@Nonnull Class<? extends T> valueClass) {
        super(valueClass);
    }

    @Override protected @Nullable T advance() {
        return null;
    }
}
