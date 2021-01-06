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

package com.github.lapesd.rdfit.components.converters.util;

import com.github.lapesd.rdfit.errors.InconvertibleException;
import com.github.lapesd.rdfit.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

public class FunctionConversionCache implements ConversionCache {
    private static final Logger logger = LoggerFactory.getLogger(FunctionConversionCache.class);
    private final @Nonnull Function<Object, Object> function;
    private final @Nonnull Class<?> outType;

    public FunctionConversionCache(@Nonnull Function<?, ?> function) {
        this(function, Object.class);
    }

    public <R> FunctionConversionCache(@Nonnull Function<?, ? extends R> function,
                                       @Nonnull Class<R> outType) {
        //noinspection unchecked
        this.function = (Function<Object, Object>) function;
        this.outType = outType;
    }

    @Override
    public @Nonnull Object convert(@Nonnull Object source, @Nullable Object in)
            throws InconvertibleException {
        if (in == null) throw new NullPointerException("in cannot be null");
        Object out = function.apply(in);
        if (out == null) {
            logger.warn("{}.apply({}) == null, throwing InconvertibleException", function, in);
            throw new InconvertibleException(source, in, outType);
        }
        return out;
    }

    @Override public String toString() {
        return String.format("%s{outType=%s, function=%s}", Utils.toString(this),
                             Utils.compactClass(outType), function);
    }
}
