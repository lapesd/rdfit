package com.github.lapesd.rdfit.iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

public class PlainRDFIt<T> extends EagerRDFIt<T> {
    private static final @Nonnull Logger logger = LoggerFactory.getLogger(PlainRDFIt.class);
    private final @Nonnull Iterator<?> iterator;
    private final @Nonnull Object source;

    public PlainRDFIt(@Nonnull Class<?> valueClass, @Nonnull IterationElement itElement,
                      @Nonnull Iterator<?> iterator, @Nonnull Object source) {
        super(valueClass, itElement);
        this.iterator = iterator;
        this.source = source;
    }

    @Override public @Nonnull Object getSource() {
        return source;
    }

    @Override protected @Nullable T advance() {
        while (iterator.hasNext()) {
            @SuppressWarnings("unchecked") T next = (T)iterator.next();
            if (next == null) {
                logger.warn("Ignoring {} will ignore {}.next()=null", this, iterator);
                assert false;
            } else {
                return next;
            }
        }
        return null; //exhausted
    }

}
