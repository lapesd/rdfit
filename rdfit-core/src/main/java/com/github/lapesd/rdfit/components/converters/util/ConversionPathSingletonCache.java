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

import com.github.lapesd.rdfit.components.converters.ConversionFinder;
import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.components.converters.ConversionPath;
import com.github.lapesd.rdfit.errors.ConversionException;
import com.github.lapesd.rdfit.errors.InconvertibleException;
import com.github.lapesd.rdfit.util.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.lang.String.format;

/**
 * A ConversionCache that keeps a single cached {@link ConversionPath}
 */
public class ConversionPathSingletonCache implements ConversionCache {
    private final @Nonnull ConversionManager conversionManager;
    private final @Nonnull Class<?> outputClass;
    private @Nullable ConversionPath conversionPath;

    public ConversionPathSingletonCache(@Nonnull ConversionManager conversionManager,
                                        @Nonnull Class<?> outputClass) {
        this.conversionManager = conversionManager;
        this.outputClass = outputClass;
    }

    public static @Nonnull ConversionCache createCache(@Nullable ConversionManager conMgr,
                                                       @Nullable Class<?> outputClass) {
        return conMgr == null || outputClass == null
                ? NoOpConversionCache.INSTANCE
                : new ConversionPathSingletonCache(conMgr, outputClass);
    }

    @Override public @Nonnull Object convert(@Nonnull Object source,
                                             @Nonnull Object in) throws InconvertibleException {
        if (outputClass.isInstance(in))
            return in; // no work

        ConversionException first = null;
        try {
            if (conversionPath != null && conversionPath.canConvert(in)) // try cached path
                return conversionPath.convert(in);
        } catch (ConversionException e) {
            first = e;
        }


        ConversionFinder finder = conversionManager.findPath(in, outputClass);
        while (finder.hasNext()) {
            try {
                Object out = finder.convert(in);
                conversionPath = finder.getConversionPath();
                return out;
            } catch (ConversionException e) {
                if (first == null) first = e;
            }
        }

        throw new InconvertibleException(source, in, outputClass);
    }

    @Override public @Nonnull String toString() {
        return format("%s{conversionManager=%s, outputClass=%s}", Utils.toString(this),
                      conversionManager, Utils.compactClass(outputClass));
    }
}
