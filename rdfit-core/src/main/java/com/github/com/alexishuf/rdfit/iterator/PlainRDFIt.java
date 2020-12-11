package com.github.com.alexishuf.rdfit.iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

public class PlainRDFIt<T> extends EagerRDFIt<T> {
    private static final @Nonnull Logger logger = LoggerFactory.getLogger(PlainRDFIt.class);
    private final @Nonnull Iterator<? extends T> iterator;

    public PlainRDFIt(@Nonnull Class<? extends T> valueClass,
                      @Nonnull Iterator<? extends T> iterator) {
        super(valueClass);
        this.iterator = iterator;
    }

    @Override protected @Nullable T advance() {
        while (iterator.hasNext()) {
            T next = iterator.next();
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
