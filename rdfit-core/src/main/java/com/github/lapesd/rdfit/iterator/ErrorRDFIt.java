package com.github.lapesd.rdfit.iterator;

import com.github.lapesd.rdfit.errors.RDFItException;

import javax.annotation.Nonnull;
import java.util.NoSuchElementException;

public class ErrorRDFIt<T> extends BaseRDFIt<T> {
    private final @Nonnull Object source;
    private final @Nonnull RDFItException exception;

    public ErrorRDFIt(@Nonnull Class<T> valueClass, @Nonnull IterationElement itElement,
                      @Nonnull Object source, @Nonnull RDFItException exception) {
        super(valueClass, itElement);
        this.source = source;
        this.exception = exception;
    }

    public @Nonnull RDFItException getException() {
        return exception;
    }

    @Override public @Nonnull Object getSource() {
        return source;
    }

    @Override public boolean hasNext() {
        throw exception;
    }

    @Override public T next() {
        throw new NoSuchElementException();
    }

    @Override public @Nonnull String toString() {
        return "ErrorRDFIt("+exception.getClass()+", "+exception.getMessage()+")";
    }
}
