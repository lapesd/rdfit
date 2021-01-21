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

/**
 * Manages and lookupts {@link SourceNormalizer} instances
 */
public interface SourceNormalizerRegistry {
    /**
     * Get the {@link ParserRegistry} instance attached to this registry
     * @return a {@link ParserRegistry}
     */
    @Nonnull ParserRegistry getParserRegistry();

    /**
     * Changes the {@link ParserRegistry} retruned by {@link #getParserRegistry()}
     * @param registry the new {@link ParserRegistry}
     */
    void setParserRegistry(@Nonnull ParserRegistry registry);

    /**
     * Get the {@link ConversionManager} that attached {@link SourceNormalizer} should use,
     * if necessary
     *
     * @return the {@link ConversionManager} instance
     */
    @Nonnull ConversionManager getConversionManager();

    /**
     * Change the {@link ConversionManager} returner by {@link #getConversionManager()}
     * @param conversionManager the new instance.
     */
    void setConversionManager(@Nonnull ConversionManager conversionManager);

    /**
     * Add a {@link SourceNormalizer} for new lookups.
     * @param normalizer the new normalizer
     */
    void register(@Nonnull SourceNormalizer normalizer);

    /**
     * Removes a specific {@link SourceNormalizer} instance,
     * if {@link #register(SourceNormalizer)}ed.
     *
     * @param instance the instance to remove
     */
    void unregister(@Nonnull SourceNormalizer instance);


    /**
     * Remove all {@link SourceNormalizer}s that match the given predicate.
     *
     * @param predicate {@link Predicate} to test if a instance should be removed
     */
    void unregisterIf(@Nonnull Predicate<? super SourceNormalizer> predicate);

    /**
     * Repeatedly applies the first applicable {@link SourceNormalizer} instance to the given
     * input (or objects resulting from normalization in previous iterations) until no
     * {@link SourceNormalizer} is applicable.
     *
     * @param source RDF source to normalize
     * @return normalized RDF source.
     */
    @Nonnull Object normalize(@Nonnull Object source);
}
