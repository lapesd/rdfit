package com.github.com.alexishuf.rdfit.conversion.impl;

import com.github.com.alexishuf.rdfit.conversion.ConversionFinder;
import com.github.com.alexishuf.rdfit.conversion.ConversionPath;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TrivialConversionFinder implements ConversionFinder {
    public static final @Nonnull ConversionFinder FAIL = new TrivialConversionFinder(true);
    public static final @Nonnull ConversionFinder NO_OP = new TrivialConversionFinder(false);

    private final boolean fail;

    public TrivialConversionFinder(boolean fail) {
        this.fail = fail;
    }


    @Override public @Nonnull ConversionPath getConversionPath() {
        return ConversionPath.EMPTY;
    }

    @Override public @Nullable Object convert(@Nullable Object input) {
        return fail ? null : input;
    }

    @Override public boolean hasNext() {
        return false;
    }
}
