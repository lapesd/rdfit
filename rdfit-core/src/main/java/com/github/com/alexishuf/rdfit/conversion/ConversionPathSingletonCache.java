package com.github.com.alexishuf.rdfit.conversion;

import com.github.com.alexishuf.rdfit.errors.InconvertibleException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConversionPathSingletonCache {
    private final @Nonnull ConversionManager conversionManager;
    private final @Nonnull Class<?> outputClass;
    private @Nullable ConversionPath conversionPath;

    public ConversionPathSingletonCache(@Nonnull ConversionManager conversionManager,
                                        @Nonnull Class<?> outputClass) {
        this.conversionManager = conversionManager;
        this.outputClass = outputClass;
    }

    public @Nonnull Object convert(@Nonnull Object source,
                                   @Nonnull Object in) throws InconvertibleException {
        Object out = null;
        if (conversionPath != null && conversionPath.canConvert(in))
            out = conversionPath.convert(in);
        if (out == null) {
            ConversionFinder finder = conversionManager.findPath(in, outputClass);
            while (finder.hasNext() && out == null) {
                if ((out = finder.convert(in)) != null)
                    conversionPath = finder.getConversionPath();
            }
        }
        if (out == null)
            throw new InconvertibleException(source, in, outputClass);
        return out;
    }

}
