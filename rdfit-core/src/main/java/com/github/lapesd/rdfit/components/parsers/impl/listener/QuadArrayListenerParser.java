package com.github.lapesd.rdfit.components.parsers.impl.listener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;

public class QuadArrayListenerParser extends BaseJavaListenerParser {
    public QuadArrayListenerParser(@Nullable Class<?> quadClass) {
        super(Array.newInstance(quadClass, 0).getClass(), null, quadClass);
    }

    @Override protected @Nonnull Iterator<?> createIterator(@Nonnull Object source) {
        return Arrays.asList((Object[])source).iterator();
    }
}
