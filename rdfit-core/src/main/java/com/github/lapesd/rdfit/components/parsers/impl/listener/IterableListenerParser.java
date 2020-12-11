package com.github.lapesd.rdfit.components.parsers.impl.listener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

public class IterableListenerParser extends BaseJavaListenerParser {
    public IterableListenerParser(@Nullable Class<?> tripleClass, @Nullable Class<?> quadClass) {
        super(Iterable.class, tripleClass, quadClass);
    }

    @Override protected @Nonnull Iterator<?> createIterator(@Nonnull Object source) {
        return ((Iterable<?>)source).iterator();
    }
}
