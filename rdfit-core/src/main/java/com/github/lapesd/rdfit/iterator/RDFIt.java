package com.github.lapesd.rdfit.iterator;

import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.util.NoSource;

import javax.annotation.Nonnull;
import java.util.Iterator;

/**
 * A closeable iterator over triple-representing objects
 * @param <T>
 */
public interface RDFIt<T> extends Iterator<T>, AutoCloseable {
    /**
     * Type of the triples (or quads) returned by {@link #next()}
     */
    @Nonnull Class<? extends T> valueClass();

    /**
     * Whether this iterator is iterating triples or quads
     */
    @Nonnull IterationElement itElement();


    /**
     * Get the source {@link Object} of the last value returned by {@link #next()}.
     * If {@link #next()}  has not yet been called, return {@link NoSource#INSTANCE}
     */
    @Nonnull Object getSource();

    /**
     * Close the iterator, releasing any resources held by the instance.
     *
     * Calling {@link #hasNext()} or {@link #next()} is an invalid operation. Implementations,
     * however, are not required to throw {@link IllegalStateException} on such calls (the
     * implementations in the rdfit-* modules do).
     *
     * Generally, implementations should avoid throwing {@link RuntimeException} from this method.
     * Parse errors should be thrown as {@link RDFItException} from {@link #hasNext()}
     * or {@link #next()}.
     */
    @Override void close();
}
