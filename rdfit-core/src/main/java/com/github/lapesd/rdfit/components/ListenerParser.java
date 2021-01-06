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

import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.components.converters.quad.QuadLifter;
import com.github.lapesd.rdfit.components.converters.quad.QuadSplitter;
import com.github.lapesd.rdfit.components.parsers.ListenerFeeder;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.listener.RDFListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A parser that will call one of the methods in the callback for every triple/quad parsed.
 *
 */
public interface ListenerParser extends Parser {
    /**
     * Class of triples delivered to the callbacks
     */
    @Nullable Class<?> tripleType();

    /**
     * Class of quads delivered to the callbacks, or null if this parser does not produce quads
     */
    @Nullable Class<?> quadType();

    /**
     * Parse the given source and call the appropriate method in listener for every triple/quad.
     *
     * @param source the source to parse
     * @param listener the listener to deliver quads or triples.  See {@link ListenerFeeder}
     *                 for a reusable (and forgiving) implementation of the logic for delivering
     *                 triples/quads to a listener. Hint: parsers need not trigger type conversions
     *                 through {@link ConversionManager} or other methods, but should
     *                 provide {@link QuadLifter} and {@link QuadSplitter} implementations
     *                 to {@link ListenerFeeder} or implement that logic when the listener does
     *                 not expect quads or triples.
     * @throws InterruptParsingException if parsing of the sour
     */
    void parse(@Nonnull Object source,
               @Nonnull RDFListener<?,?> listener) throws InterruptParsingException;
}
