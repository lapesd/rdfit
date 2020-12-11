package com.github.lapesd.rdfit.components.converters.util;

import javax.annotation.Nonnull;

public class NoOpConversionCache implements ConversionCache{
    public static final @Nonnull NoOpConversionCache INSTANCE = new NoOpConversionCache();

    @Override
    public @Nonnull Object convert(@Nonnull Object source, @Nonnull Object in) {
        return in;
    }

    @Override public String toString() {
        return "NoOpConversionCache";
    }

    @Override public boolean equals(Object obj) {
        return obj instanceof NoOpConversionCache;
    }

    @Override public int hashCode() {
        return NoOpConversionCache.class.hashCode();
    }
}
