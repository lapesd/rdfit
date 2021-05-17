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

package com.github.lapesd.rdfit.components.normalizers;

import com.github.lapesd.rdfit.components.SourceNormalizer;
import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.components.parsers.DefaultParserRegistry;
import com.github.lapesd.rdfit.components.parsers.ParserRegistry;
import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.source.RDFInputStreamDecorator;
import com.github.lapesd.rdfit.util.TypeDispatcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;

public class DefaultSourceNormalizerRegistry implements SourceNormalizerRegistry {
    private static final DefaultSourceNormalizerRegistry INSTANCE
            = new DefaultSourceNormalizerRegistry();
    static {
        INSTANCE.setParserRegistry(DefaultParserRegistry.get());
        INSTANCE.setConversionManager(DefaultConversionManager.get());
    }

    private final @Nonnull TypeDispatcher<SourceNormalizer> dispatcher
            = new TypeDispatcher<SourceNormalizer>() {
        @Override
        protected boolean accepts(@Nonnull SourceNormalizer handler, @Nonnull Object instance) {
            return true;
        }
    };
    private @Nullable ParserRegistry parserRegistry;
    private @Nullable ConversionManager conversionManager;

    public static @Nonnull DefaultSourceNormalizerRegistry get() {
        return INSTANCE;
    }

    @Override public @Nonnull ParserRegistry getParserRegistry() {
        return parserRegistry == null ? DefaultParserRegistry.get() : parserRegistry;
    }

    @Override public @Nonnull ConversionManager getConversionManager() {
        return conversionManager == null ? DefaultConversionManager.get() : conversionManager;
    }

    @Override public void setParserRegistry(@Nonnull ParserRegistry registry) {
        this.parserRegistry = registry;
    }

    @Override public void setConversionManager(@Nonnull ConversionManager conversionManager) {
        this.conversionManager = conversionManager;
    }

    private @Nonnull Object apply(@Nonnull Object source,
                                  @Nullable RDFInputStreamDecorator decorator) {
        if (source instanceof RDFInputStream) {
            RDFInputStream ris = (RDFInputStream) source;
            if (!Objects.equals(ris.getDecorator(), decorator))
                return RDFInputStream.builder(ris).decorator(decorator).build();
        }
        return source;
    }

    @Override public @Nonnull Object normalize(@Nonnull Object source,
                                               @Nullable RDFInputStreamDecorator decorator) {
        source = apply(source, decorator);
        for (boolean changed = true; changed; ) {
            changed = false;
            Iterator<SourceNormalizer> it = dispatcher.get(source);
            while (it.hasNext()) {
                Object old = source;
                source = it.next().normalize(source);
                if ((changed = source != old)) {
                    source = apply(source, decorator);
                    break;
                }
            }
        }
        return source;
    }

    @Override public void register(@Nonnull SourceNormalizer normalizer) {
        for (Class<?> cls : normalizer.acceptedClasses())
            dispatcher.add(cls, normalizer);
        normalizer.attachTo(this);
    }

    @Override public void unregister(@Nonnull SourceNormalizer instance) {
        dispatcher.remove(instance);
    }

    @Override public void unregisterIf(@Nonnull Predicate<? super SourceNormalizer> predicate) {
        dispatcher.removeIf(predicate);
    }
}
