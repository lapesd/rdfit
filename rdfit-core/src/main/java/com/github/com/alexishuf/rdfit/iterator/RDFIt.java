package com.github.com.alexishuf.rdfit.iterator;

import com.github.com.alexishuf.rdfit.DefaultRDFItFactory;
import com.github.com.alexishuf.rdfit.RDFItFactory;
import com.github.com.alexishuf.rdfit.errors.RDFItException;

import javax.annotation.Nonnull;
import java.util.Iterator;

/**
 * A closeable iterator over triple-representing objects
 * @param <T>
 */
public interface RDFIt<T> extends Iterator<T>, AutoCloseable {
    /**
     * Shortcut for getting the default {@link RDFItFactory} instance
     */
    static @Nonnull RDFItFactory fac() {
        return DefaultRDFItFactory.get();
    }

    /**
     * Type of the triples (or quads) returned by {@link #next()}
     */
    @Nonnull Class<? extends T> valueClass();

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
