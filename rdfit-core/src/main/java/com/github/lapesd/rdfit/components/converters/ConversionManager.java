/*
 *    Copyright 2021 Alexis Armin Huf
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.lapesd.rdfit.components.converters;

import com.github.lapesd.rdfit.components.Converter;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

/**
 * Registers and lookups {@link Converter} instances
 */
public interface ConversionManager {
    /**
     * Register a converter for later usage by {@link #findPath(Object, Class)}.
     *
     * @param converter The {@link Converter} instance to attach
     */
    void register(@Nonnull Converter converter);

    /**
     * Removes a specific previously registered instance. Instances are compared
     * with {@link Object#equals(Object)}.
     *
     * @param instance the {@link Converter} instance to remove (compared
     *                 by {@link Object#equals(Object)}).
     */
    void unregister(@Nonnull Converter instance);

    /**
     * Remove all {@link #register(Converter)}ed instances that match the predicate.
     *
     * @param predicate {@link Predicate} that, if true, causes a {@link Converter} instance to
     *                                   be removed
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
