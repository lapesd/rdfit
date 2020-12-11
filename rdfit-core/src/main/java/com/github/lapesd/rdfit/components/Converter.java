package com.github.lapesd.rdfit.components;

import com.github.lapesd.rdfit.components.converters.ConversionManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public interface Converter extends Component {

    /**
     * Notify that this instance has been registered at the given {@link ConversionManager}.
     */
    void attachTo(@Nonnull ConversionManager conversionManager);

    /**
     * Collection of classes accepted by this converter as input in {@link #convert(Object)}.
     */
    @Nonnull Collection<Class<?>> acceptedClasses();

    /**
     * Class which is output by the {@link #convert(Object)} method
     */
    @Nonnull Class<?> outputClass();

    /**
     * Convert the given input into a {@link #outputClass()} instance.
     *
     * @param input the input object
     * @return the converted instance, or null if input is null or the particular instance
     *         could not be handled.
     */
    @Nullable Object convert(@Nullable Object input);

    /**
     * Returns whether the converter will likely be able to handle this instance
     * (i.e., {@link #convert(Object)} will return non-null).
     *
     * @param input the instance to check
     * @return false if {@link #convert(Object)} would return null, true if it may return non-null
     */
    boolean canConvert(@Nullable Object input);
}
