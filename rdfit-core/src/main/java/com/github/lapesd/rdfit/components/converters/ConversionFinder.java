package com.github.lapesd.rdfit.components.converters;

import com.github.lapesd.rdfit.errors.ConversionException;

import javax.annotation.Nonnull;

public interface ConversionFinder {
    /**
     * The {@link ConversionPath} which will be used in the next {@link #convert(Object)} call.
     */
    @Nonnull ConversionPath getConversionPath();

    /**
     * Convert the given object using the current path.
     *
     * @param input the object to convert from
     * @return the result of conversion
     * @throws ConversionException if the current path could not handle the conversion.
     */
    @Nonnull Object convert(@Nonnull Object input) throws ConversionException;

    /**
     * Advance to the next conversion path.
     *
     * @return true if a new path could be found, false otherwise. Once this returns false,
     *         it will never return true again.
     */
    boolean hasNext();
}
