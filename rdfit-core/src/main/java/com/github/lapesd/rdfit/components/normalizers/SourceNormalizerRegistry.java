package com.github.lapesd.rdfit.components.normalizers;

import com.github.lapesd.rdfit.components.SourceNormalizer;
import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.components.parsers.ParserRegistry;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public interface SourceNormalizerRegistry {
    @Nonnull ParserRegistry getParserRegistry();
    void setParserRegistry(@Nonnull ParserRegistry registry);
    @Nonnull ConversionManager getConversionManager();
    void setConversionManager(@Nonnull ConversionManager conversionManager);

    void register(@Nonnull SourceNormalizer normalizer);
    void unregister(@Nonnull SourceNormalizer instance);
    void unregisterIf(@Nonnull Predicate<? super SourceNormalizer> predicate);

    @Nonnull Object normalize(@Nonnull Object source);
}
