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

package com.github.lapesd.rdfit.components;

import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.errors.ConversionException;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Converts object of one class into another
 */
public interface Converter extends Component {

    /**
     * Notify that this instance has been registered at the given {@link ConversionManager}.
     *
     * @param conversionManager the manager to which this component has been attached to
     */
    void attachTo(@Nonnull ConversionManager conversionManager);

    /**
     * Collection of classes accepted by this converter as input in {@link #convert(Object)}.
     *
     * @return non-null and non-empty collection of classes
     */
    @Nonnull Collection<Class<?>> acceptedClasses();

    /**
     * Class which is output by the {@link #convert(Object)} method
     *
     * @return non-null class of conversion results
     */
    @Nonnull Class<?> outputClass();

    /**
     * Convert the given input into a {@link #outputClass()} instance.
     *
     * @param input the input object
     * @return the converted instance
     * @throws ConversionException if input cannot be converted
     */
    @Nonnull Object convert(@Nonnull Object input) throws ConversionException;

    /**
     * Returns whether the converter will likely be able to handle this instance
     * (i.e., {@link #convert(Object)} will return non-null).
     *
     * @param input the instance to check
     * @return false if {@link #convert(Object)} would throw, true if it may return non-null
     */
    boolean canConvert(@Nonnull Object input);
}
