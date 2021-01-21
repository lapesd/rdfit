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

package com.github.lapesd.rdfit.components.rdf4j;

import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.components.Parser;
import com.github.lapesd.rdfit.components.parsers.ParserRegistry;
import com.github.lapesd.rdfit.components.rdf4j.parsers.listener.RDF4JInputStreamParser;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Register parsers for RDF serializations backed by RDF4J
 */
public class RDF4JParsers {
    private static final @Nonnull List<Parser> PARSERS
            = singletonList(new RDF4JInputStreamParser());

    /**
     * Add all RDF4J parsers to the given registry
     * @param registry the {@link ParserRegistry}
     */
    public static void registerAll(@Nonnull ParserRegistry registry) {
        for (Parser p : PARSERS) registry.register(p);
    }

    /**
     * Calls {@link #registerAll(ParserRegistry)} with {@link RDFItFactory#getParserRegistry()}
     * @param factory the {@link RDFItFactory}
     */
    public static void registerAll(@Nonnull RDFItFactory factory) {
        registerAll(factory.getParserRegistry());
    }

    /**
     * Remove any parser added by {@link #registerAll(ParserRegistry)}
     * @param registry a {@link ParserRegistry}
     */
    public static void unregisterAll(@Nonnull ParserRegistry registry) {
        for (Parser p : PARSERS) registry.unregister(p);
    }

    /**
     * Call {@link #unregisterAll(ParserRegistry)} with {@link RDFItFactory#getParserRegistry()}
     * @param factory the {@link RDFItFactory}
     */
    public static void unregisterAll(@Nonnull RDFItFactory factory) {
        unregisterAll(factory.getParserRegistry());
    }
}
