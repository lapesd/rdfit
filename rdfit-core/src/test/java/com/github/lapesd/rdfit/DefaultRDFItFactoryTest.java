package com.github.lapesd.rdfit;

import com.github.lapesd.rdfit.components.Parser;
import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.components.normalizers.CoreSourceNormalizers;
import com.github.lapesd.rdfit.components.normalizers.DefaultSourceNormalizerRegistry;
import com.github.lapesd.rdfit.components.parsers.DefaultParserRegistry;
import com.github.lapesd.rdfit.data.ConverterLib;
import com.github.lapesd.rdfit.data.ModelLib;

import javax.annotation.Nonnull;
import java.util.Collection;

public class DefaultRDFItFactoryTest extends RDFItFactoryTestBase {

    private @Nonnull RDFItFactory createFactory(@Nonnull Collection<? extends Parser> parsers) {
        DefaultParserRegistry parserRegistry = new DefaultParserRegistry();
        parsers.forEach(parserRegistry::register);
        DefaultConversionManager convMgr = new DefaultConversionManager();
        ConverterLib.ALL_CONVERTERS.forEach(convMgr::register);
        DefaultSourceNormalizerRegistry normalizerRegistry = new DefaultSourceNormalizerRegistry();
        CoreSourceNormalizers.registerAll(normalizerRegistry);
        return new DefaultRDFItFactory(parserRegistry, convMgr, normalizerRegistry);
    }

    @Override protected @Nonnull RDFItFactory createFactoryOnlyCbParsers() {
        return createFactory(ModelLib.ALL_CB_PARSERS);
    }

    @Override protected @Nonnull RDFItFactory createFactoryOnlyItParsers() {
        return createFactory(ModelLib.ALL_IT_PARSERS);
    }

    @Override protected @Nonnull RDFItFactory createFactoryAllParsers() {
        return createFactory(ModelLib.ALL_PARSERS);
    }
}