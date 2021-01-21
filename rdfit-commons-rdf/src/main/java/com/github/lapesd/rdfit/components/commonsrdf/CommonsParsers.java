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

package com.github.lapesd.rdfit.components.commonsrdf;

import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.components.Parser;
import com.github.lapesd.rdfit.components.commonsrdf.parsers.iterator.DatasetItParser;
import com.github.lapesd.rdfit.components.commonsrdf.parsers.iterator.GraphItParser;
import com.github.lapesd.rdfit.components.commonsrdf.parsers.listeners.DatasetListenerParser;
import com.github.lapesd.rdfit.components.commonsrdf.parsers.listeners.GraphListenerParser;
import com.github.lapesd.rdfit.components.parsers.JavaParsers;
import com.github.lapesd.rdfit.components.parsers.ParserRegistry;
import com.github.lapesd.rdfit.components.parsers.impl.listener.IterableListenerParser;
import com.github.lapesd.rdfit.components.parsers.impl.listener.TripleArrayListenerParser;
import com.github.lapesd.rdfit.iterator.IterationElement;
import org.apache.commons.rdf.api.Quad;
import org.apache.commons.rdf.api.Triple;
import org.apache.commons.rdf.api.TripleLike;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

/**
 * Helper class for registering parsers for {@link org.apache.commons.rdf.api.Graph},
 * {@link org.apache.commons.rdf.api.Dataset} and Iterable/arrays of commons-rdf triples/quads.
 */
public class CommonsParsers {
    private static final Set<Class<?>> CLASSES = unmodifiableSet(new HashSet<>(asList(
            GraphItParser.class, DatasetItParser.class,
            GraphListenerParser.class, DatasetListenerParser.class
    )));
    private static final List<Supplier<? extends Parser>> SUPPLIERS = asList(
            GraphItParser::new,
            () -> new DatasetItParser(IterationElement.TRIPLE),
            () -> new DatasetItParser(IterationElement.QUAD),
            GraphListenerParser::new,
            DatasetListenerParser::new
    );

    /**
     * Calls {@link #registerAll(ParserRegistry)} with {@link RDFItFactory#getParserRegistry()}.
     * @param factory the {@link RDFItFactory}
     */
    public static void registerAll(@Nonnull RDFItFactory factory) {
        registerAll(factory.getParserRegistry());
    }

    /**
     * Add parsers to the given {@link ParserRegistry}.
     * @param registry the {@link ParserRegistry}
     */
    public static void registerAll(@Nonnull ParserRegistry registry) {
        JavaParsers.registerWithQuadClass(Quad.class);
        JavaParsers.registerWithTripleClass(Triple.class);
        registry.register(new IterableListenerParser(Triple.class, Quad.class));
        registry.register(new TripleArrayListenerParser(TripleLike.class, Triple.class, Quad.class));
        for (Supplier<? extends Parser> supplier : SUPPLIERS)
            registry.register(supplier.get());
    }

    /**
     * Calls {@link #unregisterAll(ParserRegistry)} on {@link RDFItFactory#getParserRegistry()}
     * @param factory the {@link RDFItFactory}
     */
    public static void unregisterAll(@Nonnull RDFItFactory factory) {
        unregisterAll(factory.getParserRegistry());
    }

    /**
     * Unregister any parser that may have been added  with {@link #registerAll(ParserRegistry)}
     * @param registry the {@link ParserRegistry}
     */
    public static void unregisterAll(@Nonnull ParserRegistry registry) {
        JavaParsers.unregister(registry, Quad.class);
        JavaParsers.unregister(registry, Triple.class);
        JavaParsers.unregister(registry, Triple.class, Quad.class);
        registry.unregisterIf(p -> CLASSES.contains(p.getClass()));
    }
}
