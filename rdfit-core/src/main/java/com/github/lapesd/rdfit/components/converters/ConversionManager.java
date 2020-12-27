package com.github.lapesd.rdfit.components.converters;

import com.github.lapesd.rdfit.components.Converter;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public interface ConversionManager {
    /**
     * Register a converter for later usage by {@link #findPath(Object, Class)}.
     */
    void register(@Nonnull Converter converter);

    /**
     * Removes a specific previously registered instance. Instances are compared
     * with {@link Object#equals(Object)}.
     */
    void unregister(@Nonnull Converter instance);

    /**
     * Remove all {@link #register(Converter)}ed instances that match the predicate.
     */
    void unregisterIf(@Nonnull Predicate<? super Converter> predicate);

    default void unregisterAll(@Nonnull Class<? super Converter> aClass) {
        unregisterIf(r -> aClass.isAssignableFrom(r.getClass()));
    }

    /**
     * Get a {@link ConversionFinder} object to explore conversion paths from input into
     * an instance of the desired class.
     *
     * @param input the input object. If null will return a finder with no paths
     * @param desired the desired class.
     * @return the conversion finder object
     */
    @Nonnull ConversionFinder findPath(@Nonnull Object input, @Nonnull Class<?> desired);
}
