package com.github.com.alexishuf.rdfit.iterator;

import com.github.com.alexishuf.rdfit.conversion.ConversionManager;
import com.github.com.alexishuf.rdfit.conversion.ConversionPath;
import com.github.com.alexishuf.rdfit.conversion.ConversionPathSingletonCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConvertingRDFIt<T> extends EagerRDFIt<T> {
    private static final Logger logger = LoggerFactory.getLogger(ConvertingRDFIt.class);

    private final @Nonnull RDFIt<?> source;
    private final @Nonnull ConversionPathSingletonCache conversionCache;
    private @Nullable ConversionPath conversionPath;

    public ConvertingRDFIt(@Nonnull Class<? extends T> valueClass, @Nonnull RDFIt<?> source,
                           @Nonnull ConversionManager conversionManager) {
        super(valueClass);
        this.source = source;
        this.conversionCache = new ConversionPathSingletonCache(conversionManager, valueClass);
    }

    @Override protected @Nullable T advance() {
        while (source.hasNext()) {
            Object in = source.next();
            if (in == null) {
                logger.warn("{}.advance() ignoring null from {}.next()", this, source);
                assert false;
                continue;
            }
            //noinspection unchecked
            return (T)conversionCache.convert(source, in);
        }
        return null; //exhausted
    }

    @Override public void close() {
        source.close();
        super.close();
    }
}
