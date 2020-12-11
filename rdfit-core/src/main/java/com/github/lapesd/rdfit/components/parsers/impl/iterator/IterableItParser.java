package com.github.lapesd.rdfit.components.parsers.impl.iterator;

import com.github.lapesd.rdfit.iterator.IterationElement;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Iterator;

public class IterableItParser extends BaseJavaItParser {
    public IterableItParser(@Nonnull Class<?> valueCls, @Nonnull IterationElement itElement) {
        super(Iterable.class, valueCls, itElement);
    }

    @Override protected @Nonnull Iterator<?> createIterator(@Nonnull Object source) {
        return ((Collection<?>) source).iterator();
    }
}
