package com.github.lapesd.rdfit.components.parsers;

import com.github.lapesd.rdfit.components.ItParser;
import com.github.lapesd.rdfit.iterator.IterationElement;

import javax.annotation.Nonnull;
import java.util.Collection;

public abstract class BaseItParser extends BaseParser implements ItParser {
    protected final @Nonnull Class<?> valueClass;
    protected final @Nonnull IterationElement iterationElement;

    public BaseItParser(@Nonnull Collection<Class<?>> acceptedClasses, @Nonnull Class<?> valueClass,
                        @Nonnull IterationElement iterationElement) {
        super(acceptedClasses);
        this.valueClass = valueClass;
        this.iterationElement = iterationElement;
    }

    @Override public @Nonnull Class<?> valueClass() {
        return valueClass;
    }

    @Override public @Nonnull IterationElement iterationElement() {
        return iterationElement;
    }
}
