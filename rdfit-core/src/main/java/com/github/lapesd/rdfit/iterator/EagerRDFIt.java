package com.github.lapesd.rdfit.iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.NoSuchElementException;

public abstract class EagerRDFIt<T> extends BaseRDFIt<T> {
    protected T value;

    public EagerRDFIt(@Nonnull Class<?> valueClass, @Nonnull IterationElement itElement) {
        super(valueClass, itElement);
    }

    protected abstract @Nullable T advance();

    @Override public boolean hasNext() {
        if (value == null)
            value = advance();
        return value != null;
    }

    @Override public T next() {
        if (!hasNext()) throw new NoSuchElementException("!"+this+".hasNext()");
        T next = this.value;
        assert next != null;
        this.value = null;
        return next;
    }
}
