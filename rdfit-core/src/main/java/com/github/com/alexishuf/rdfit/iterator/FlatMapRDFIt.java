package com.github.com.alexishuf.rdfit.iterator;

import com.github.com.alexishuf.rdfit.errors.RDFItException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.function.Function;

public class FlatMapRDFIt<T> extends EagerRDFIt<T> {
    private final @Nonnull Iterator<?> inputIt;
    private final @Nonnull Function<Object, RDFIt<T>> function;
    private @Nullable RDFIt<T> currentIt = null;

    public FlatMapRDFIt(@Nonnull Class<? extends T> valueClass, @Nonnull Iterator<?> inputIt,
                        @Nonnull Function<?, RDFIt<T>> function) {
        super(valueClass);
        this.inputIt = inputIt;
        //noinspection unchecked
        this.function = (Function<Object, RDFIt<T>>) function;
    }

    @Override protected @Nullable T advance() {
        while ((currentIt == null || !currentIt.hasNext()) && inputIt.hasNext()) {
            Object input = inputIt.next();
            try {
                if (currentIt != null)
                    currentIt.close();
                currentIt = function.apply(input);
            } catch (RDFItException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new RDFItException(input, "Unexpected "+e.getClass().getSimpleName(), e);
            }
        }
        return currentIt != null && currentIt.hasNext() ? currentIt.next() : null;
    }

    @Override public void close() {
        if (currentIt != null)
            currentIt.close();
    }
}
