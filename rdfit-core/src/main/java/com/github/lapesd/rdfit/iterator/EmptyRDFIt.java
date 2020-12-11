package com.github.lapesd.rdfit.iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EmptyRDFIt<T> extends EagerRDFIt<T> {
    private final @Nonnull Object source;

    public EmptyRDFIt(@Nonnull Class<? extends T> valueClass, @Nonnull IterationElement itElement, @Nonnull Object source) {
        super(valueClass, itElement);
        this.source = source;
    }

    @Override public @Nonnull Object getSource() {
        return source;
    }

    @Override protected @Nullable T advance() {
        return null;
    }
}
