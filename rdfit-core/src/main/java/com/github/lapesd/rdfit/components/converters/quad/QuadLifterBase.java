package com.github.lapesd.rdfit.components.converters.quad;

import com.github.lapesd.rdfit.util.Utils;

import javax.annotation.Nonnull;

public abstract class QuadLifterBase implements QuadLifter {
    protected final @Nonnull Class<?> tripleType;

    public QuadLifterBase(@Nonnull Class<?> tripleType) {
        this.tripleType = tripleType;
    }

    @Override public @Nonnull Class<?> tripleType() {
        return tripleType;
    }

    @Override public @Nonnull String toString() {
        return Utils.genericToString(this, tripleType());
    }
}
