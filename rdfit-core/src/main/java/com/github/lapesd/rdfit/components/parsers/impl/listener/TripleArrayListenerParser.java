package com.github.lapesd.rdfit.components.parsers.impl.listener;

import javax.annotation.Nonnull;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;

public class TripleArrayListenerParser extends BaseJavaListenerParser {
    public TripleArrayListenerParser(@Nonnull Class<?> tripleClass) {
        super(Array.newInstance(tripleClass, 0).getClass(), tripleClass, null);
    }

    @Override protected @Nonnull Iterator<?> createIterator(@Nonnull Object source) {
        return Arrays.asList((Object[]) source).iterator();
    }
}
