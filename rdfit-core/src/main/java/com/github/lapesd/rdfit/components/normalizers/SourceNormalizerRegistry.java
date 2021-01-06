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
