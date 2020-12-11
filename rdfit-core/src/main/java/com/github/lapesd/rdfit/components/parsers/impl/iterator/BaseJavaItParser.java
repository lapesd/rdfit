package com.github.lapesd.rdfit.components.parsers.impl.iterator;

import com.github.lapesd.rdfit.components.parsers.BaseItParser;
import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.iterator.PlainRDFIt;
import com.github.lapesd.rdfit.iterator.RDFIt;
import com.github.lapesd.rdfit.util.Utils;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Iterator;

import static com.github.lapesd.rdfit.util.Utils.compactClass;

public abstract class BaseJavaItParser extends BaseItParser {
    public static final int CAN_PARSE_MAX = 128;

    public BaseJavaItParser(@Nonnull Class<?> acceptedClass,
                            @Nonnull Class<?> valueClass, @Nonnull IterationElement iterationElement) {
        super(Collections.singleton(acceptedClass), valueClass, iterationElement);
    }

    protected abstract @Nonnull Iterator<?> createIterator(@Nonnull Object source);

    @Override public @Nonnull <T> RDFIt<T> parse(@Nonnull Object source) {
        return new PlainRDFIt<>(valueClass(), iterationElement, createIterator(source), source);
    }

    @Override public boolean canParse(@Nonnull Object source) {
        if (!super.canParse(source)) return false;
        Class<?> valueClass = valueClass();
        Iterator<?> it = createIterator(source);
        for (int i = 0; i < CAN_PARSE_MAX && it.hasNext(); ++i) {
            if (!valueClass.isInstance(it.next())) return false;
        }
        return true;
    }

    @Override public @Nonnull String toString() {
        return String.format("%s{%s<%s>}", Utils.toString(this),
                             compactClass(acceptedClasses().iterator().next()),
                             compactClass(valueClass()));
    }
}
