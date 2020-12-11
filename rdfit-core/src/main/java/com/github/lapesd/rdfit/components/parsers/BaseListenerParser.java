package com.github.lapesd.rdfit.components.parsers;

import com.github.lapesd.rdfit.components.ListenerParser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public abstract class BaseListenerParser extends BaseParser implements ListenerParser {
    protected final @Nullable Class<?> tripleClass;
    protected final @Nullable Class<?> quadClass;

    public BaseListenerParser(@Nonnull Collection<Class<?>> acceptedClasses,
                          @Nullable Class<?> tripleClass) {
        this(acceptedClasses, tripleClass, null);
    }

    public BaseListenerParser(@Nonnull Collection<Class<?>> acceptedClasses,
                              @Nullable Class<?> tripleClass, @Nullable Class<?> quadClass) {
        super(acceptedClasses);
        this.tripleClass = tripleClass;
        this.quadClass = quadClass;
        if (tripleClass == null && quadClass == null)
            throw new IllegalArgumentException("Both tripleClass and quadClass are null");
    }

    @Override public @Nullable Class<?> tripleType() {
        return tripleClass;
    }

    @Override public @Nullable Class<?> quadType() {
        return quadClass;
    }
}
