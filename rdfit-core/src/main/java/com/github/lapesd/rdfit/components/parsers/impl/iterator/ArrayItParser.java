package com.github.lapesd.rdfit.components.parsers.impl.iterator;

import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.util.Utils;

import javax.annotation.Nonnull;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;

import static com.github.lapesd.rdfit.util.Utils.compactClass;
import static java.lang.String.format;

public class ArrayItParser extends BaseJavaItParser {

    public ArrayItParser(@Nonnull Class<?> valueClass, @Nonnull IterationElement iterationElement) {
        super(Array.newInstance(valueClass, 0).getClass(), valueClass, iterationElement);
    }

    @Override protected @Nonnull Iterator<?> createIterator(@Nonnull Object source) {
        //noinspection ArraysAsListWithZeroOrOneArgument
        return Arrays.asList(source).iterator();
    }

    @Override public @Nonnull String toString() {
        return format("%s{%s}", Utils.toString(this),
                                compactClass(acceptedClasses().iterator().next()));
    }
}
