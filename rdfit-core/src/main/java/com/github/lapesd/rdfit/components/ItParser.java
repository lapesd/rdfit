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

package com.github.lapesd.rdfit.components;

import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.iterator.RDFIt;

import javax.annotation.Nonnull;

/**
 * A Parser that returns a {@link RDFIt} instance
 */
public interface ItParser extends Parser {
    /**
     * {@link RDFIt#valueClass()} of iterators returned by {@link #parse(Object)}.
     *
     * @return The {@link RDFIt#valueClass()} of iterators returned by {@link #parse(Object)}.
     */
    @Nonnull Class<?> valueClass();

    /**
     * Whether this parser will produce a {@link RDFIt} over triples or quads.
     *
     * @return The {@link RDFIt#itElement()} of {@link #parse(Object)}.
     */
    @Nonnull IterationElement itElement();

    /**
     * Create a {@link RDFIt} that iterates over parsed triples (or quads) from the given source.
     *
     * A RDFIt will iterate either only triples or only quads.
     *
     * @param <T> type of objects being iterated. This is compile-time only and may not
     *           match {@link RDFIt#valueClass()}
     * @param source source to iterate
     * @return An {@link RDFIt} over triples/quads (see {@link #itElement()}.
     */
    @Nonnull <T> RDFIt<T> parse(@Nonnull Object source);
}
