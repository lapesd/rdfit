package com.github.lapesd.rdfit.iterator;

import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.components.converters.ConversionPath;
import com.github.lapesd.rdfit.components.converters.util.ConversionCache;
import com.github.lapesd.rdfit.components.converters.util.ConversionPathSingletonCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConvertingRDFIt<T> extends EagerRDFIt<T> {
    private static final Logger logger = LoggerFactory.getLogger(ConvertingRDFIt.class);

    private final @Nonnull RDFIt<?> source;
    private final @Nonnull ConversionCache conversionCache;
    private @Nullable ConversionPath conversionPath;

    public ConvertingRDFIt(@Nonnull Class<? extends T> valueClass,
                           @Nonnull IterationElement itElement, @Nonnull RDFIt<?> source,
                           @Nonnull ConversionManager conversionManager) {
        super(valueClass, itElement);
        this.source = source;
        this.conversionCache = new ConversionPathSingletonCache(conversionManager, valueClass);
    }

    @Override public @Nonnull Object getSource() {
        return source.getSource();
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
