package com.github.lapesd.rdfit.components.parsers.impl.listener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.stream.Stream;

public class StreamListenerParser extends BaseJavaListenerParser {
    public StreamListenerParser(@Nullable Class<?> tripleClass, @Nullable Class<?> quadClass) {
        super(Stream.class, tripleClass, quadClass);
    }

    @Override protected @Nonnull Iterator<?> createIterator(@Nonnull Object source) {
        return ((Stream<?>)source).iterator();
    }
}
