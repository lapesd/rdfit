package com.github.lapesd.rdfit.iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

public class TransformingRDFIt<T> extends EagerRDFIt<T> {
    private final @Nonnull Logger logger = LoggerFactory.getLogger(TransformingRDFIt.class);
    private final @Nonnull RDFIt<?> in;
    private final @Nonnull Function<Object, ? extends T> function;

    public <X> TransformingRDFIt(@Nonnull Class<? extends T> valueClass,
                                 @Nonnull IterationElement itElement, @Nonnull RDFIt<?> in,
                                 @Nonnull Function<?, ? extends T> function) {
        super(valueClass, itElement);
        this.in = in;
        //noinspection unchecked
        this.function = (Function<Object, ? extends T>) function;
    }

    @Override public @Nonnull Object getSource() {
        return in.getSource();
    }

    @Override protected @Nullable T advance() {
        while (in.hasNext()) {
            Object next = in.next();
            T result = function.apply(next);
            if (result == null) {
                logger.warn("{}.advance(): transformer function {} returned null for input {}. " +
                            "Ignoring element", this, function, next);
                assert false; // likely a bug
            } else {
                return result;
            }
        }
        return null;
    }
}
