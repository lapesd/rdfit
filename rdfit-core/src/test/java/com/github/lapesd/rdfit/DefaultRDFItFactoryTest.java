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

package com.github.lapesd.rdfit;

import com.github.lapesd.rdfit.components.Parser;
import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.components.normalizers.CoreSourceNormalizers;
import com.github.lapesd.rdfit.components.normalizers.DefaultSourceNormalizerRegistry;
import com.github.lapesd.rdfit.components.parsers.DefaultParserRegistry;
import com.github.lapesd.rdfit.data.ConverterLib;
import com.github.lapesd.rdfit.data.ModelLib;
import com.github.lapesd.rdfit.impl.DefaultRDFItFactory;

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