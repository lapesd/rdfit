package com.github.lapesd.rdfit.components.converters.util;

import com.github.lapesd.rdfit.components.converters.ConversionFinder;
import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.components.converters.ConversionPath;
import com.github.lapesd.rdfit.errors.InconvertibleException;
import com.github.lapesd.rdfit.util.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.lang.String.format;

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

        Object out = null;
        if (conversionPath != null && conversionPath.canConvert(in)) // try cached path
            out = conversionPath.convert(in);

        if (out == null) { // explore all paths from the start
            ConversionFinder finder = conversionManager.findPath(in, outputClass);
            while (finder.hasNext() && out == null) {
                if ((out = finder.convert(in)) != null)
                    conversionPath = finder.getConversionPath();
            }
        }

        if (out == null) //exhausted
            throw new InconvertibleException(source, in, outputClass);
        return out;
    }

    @Override public @Nonnull String toString() {
        return format("%s{conversionManager=%s, outputClass=%s}", Utils.toString(this),
                      conversionManager, Utils.compactClass(outputClass));
    }
}
