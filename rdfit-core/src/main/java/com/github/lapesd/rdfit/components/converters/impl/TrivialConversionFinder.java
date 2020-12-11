package com.github.lapesd.rdfit.components.converters.impl;

import com.github.lapesd.rdfit.components.converters.ConversionFinder;
import com.github.lapesd.rdfit.components.converters.ConversionPath;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TrivialConversionFinder implements ConversionFinder {
    boolean hasNext = true;

    @Override public @Nonnull ConversionPath getConversionPath() {
        return ConversionPath.EMPTY;
    }

    @Override public @Nullable Object convert(@Nullable Object input) {
        assert hasNext; // convert() should not be called after a false hasNext().
        hasNext = false;
        return input;
    }

    @Override public boolean hasNext() {
        return hasNext;
    }
}
