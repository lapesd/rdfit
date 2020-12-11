package com.github.lapesd.rdfit.components.converters.util;

import com.github.lapesd.rdfit.errors.InconvertibleException;

import javax.annotation.Nonnull;

public interface ConversionCache {
    @Nonnull Object convert(@Nonnull Object source,
                            @Nonnull Object in) throws InconvertibleException;
}
