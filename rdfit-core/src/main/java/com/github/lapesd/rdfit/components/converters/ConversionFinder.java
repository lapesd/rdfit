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

import com.github.lapesd.rdfit.errors.ConversionException;

import javax.annotation.Nonnull;

/**
 * Explores the multiple possible {@link com.github.lapesd.rdfit.components.Converter} chains that
 * lead to a desired {@link Class}.
 */
public interface ConversionFinder {
    /**
     * The {@link ConversionPath} which will be used in the next {@link #convert(Object)} call.
     *
     * @return the {@link ConversionPath}
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
