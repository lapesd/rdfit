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
import com.github.lapesd.rdfit.errors.ConversionException;
import com.github.lapesd.rdfit.errors.ConversionPathException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ConversionPath {
    private static final Logger logger = LoggerFactory.getLogger(ConversionPath.class);

    private final @Nonnull List<Converter> path;
    public static final @Nonnull ConversionPath EMPTY
            = new ConversionPath(Collections.emptyList());

    public ConversionPath(@Nonnull List<Converter> path) {
        this.path = path;
    }

    /**
     * Apply the {@link Converter} in this path serially to input.
     *
     * @param input object to be converted
     * @return Converted object
     * @throws ConversionException if thrown by a Converter
     */
    public @Nonnull Object convert(@Nonnull Object input)  throws ConversionException {
        Object object = input;
        try {
            for (Converter converter : path) {
                try {
                    object = converter.convert(object);
                } catch (ClassCastException e) {
                    throw new ConversionException(e, object, converter, converter.outputClass(),
                                                  e.getMessage());
                }
            }
        } catch (ConversionException e) {
            throw new ConversionPathException(this, e);
        }

        return object;
    }

    public boolean canConvert(@Nullable Object object) {
        return object == null || path.isEmpty() || path.get(0).canConvert(object);
    }

    @Override public String toString() {
        StringBuilder builder = new StringBuilder("ConversionPath{");
        for (Converter converter : path)
            builder.append(converter).append(", ");
        assert !path.isEmpty();
        builder.setLength(builder.length()-2);
        return builder.append('}').toString();
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConversionPath)) return false;
        ConversionPath that = (ConversionPath) o;
        return path.equals(that.path);
    }

    @Override public int hashCode() {
        return Objects.hash(path);
    }
}
