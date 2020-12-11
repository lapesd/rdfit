package com.github.com.alexishuf.rdfit.conversion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ConversionFinder {
    /**
     * The {@link ConversionPath} which will be used in the next {@link #convert(Object)} call.
     */
    @Nonnull ConversionPath getConversionPath();

    /**
     * Convert the given object using the current path.
     *
     * @param input the object to convert from
     * @return the result of conversion, which can be null if input is null or a converter in the
     *         {@link #getConversionPath()} refused to convert its input.
     */
    @Nullable Object convert(@Nullable Object input);

    /**
     * Advance to the next conversion path.
     *
     * @return true if a new path could be found, false otherwise. Once this returns false,
     *         it will never return true again.
     */
    boolean hasNext();
}
