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
import com.github.lapesd.rdfit.components.ItParser;
import com.github.lapesd.rdfit.components.ListenerParser;
import com.github.lapesd.rdfit.components.parsers.ParserRegistry;
import com.github.lapesd.rdfit.components.parsers.impl.iterator.ArrayItParser;
import com.github.lapesd.rdfit.components.parsers.impl.iterator.IterableItParser;
import com.github.lapesd.rdfit.components.rdf4j.parsers.iterator.*;
import com.github.lapesd.rdfit.components.rdf4j.parsers.listener.*;
import com.github.lapesd.rdfit.iterator.IterationElement;
import org.eclipse.rdf4j.model.Statement;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

public class RDF4JModelParsers {
    private static final List<Supplier<ItParser>> IT_SUPPLIERS = asList(
            () -> new IterableItParser(Statement.class, IterationElement.TRIPLE),
            () -> new IterableItParser(Statement.class, IterationElement.QUAD),
            () -> new ArrayItParser(Statement.class, IterationElement.TRIPLE),
            () -> new ArrayItParser(Statement.class, IterationElement.QUAD),
            () -> new ConnectionItParser(IterationElement.TRIPLE),
            () -> new ConnectionItParser(IterationElement.QUAD),
            () -> new RepositoryItParser(IterationElement.TRIPLE),
            () -> new RepositoryItParser(IterationElement.QUAD),
            () -> new RepositoryResultItParser(IterationElement.TRIPLE),
            () -> new RepositoryResultItParser(IterationElement.QUAD),
            () -> new TupleQueryResultItParser(IterationElement.TRIPLE),
            () -> new TupleQueryResultItParser(IterationElement.QUAD),
            () -> new TupleQueryItParser(IterationElement.TRIPLE),
            () -> new TupleQueryItParser(IterationElement.QUAD),
            () -> new GraphQueryItParser(IterationElement.TRIPLE),
            () -> new GraphQueryItParser(IterationElement.QUAD),
            () -> new ModelItParser(IterationElement.TRIPLE),
            () -> new ModelItParser(IterationElement.QUAD)
    );
    private static final Set<Class<?>> IT_CLASSES = unmodifiableSet(new HashSet<>(asList(
            IterableItParser.class,
            ArrayItParser.class,
            ConnectionItParser.class,
            RepositoryItParser.class,
            RepositoryResultItParser.class,
            TupleQueryResultItParser.class,
            TupleQueryItParser.class,
            GraphQueryItParser.class,
            ModelItParser.class
    )));
    private static final List<Supplier<ListenerParser>> LISTENER_SUPPLIERS = asList(
            RDF4JIterableListenerParser::new,
            RDF4JArrayListenerParser::new,
            RepositoryListenerParser::new,
            RepositoryConnectionListenerParser::new,
            RepositoryResultListenerParser::new,
            TupleQueryResultListenerParser::new,
            TupleQueryListenerParser::new,
            GraphQueryResultListenerParser::new,
            GraphQueryListenerParser::new,
            ModelListenerParser::new
    );
    private static final Set<Class<?>> LISTENER_CLASSES = unmodifiableSet(new HashSet<>(asList(
            RDF4JIterableListenerParser.class,
            RDF4JArrayListenerParser.class,
            RepositoryListenerParser.class,
            RepositoryConnectionListenerParser.class,
            RepositoryResultListenerParser.class,
            TupleQueryResultListenerParser.class,
            TupleQueryListenerParser.class,
            GraphQueryResultListenerParser.class,
            GraphQueryListenerParser.class,
            ModelListenerParser.class
    )));


    public static void registerItParsers(@Nonnull ParserRegistry registry) {
        for (Supplier<ItParser> supplier : IT_SUPPLIERS) registry.register(supplier.get());
    }
    public static void registerItParsers(@Nonnull RDFItFactory factory) {
        registerItParsers(factory.getParserRegistry());
    }

    public static void registerListenerParsers(@Nonnull ParserRegistry registry) {
        for (Supplier<ListenerParser> supplier : LISTENER_SUPPLIERS)
            registry.register(supplier.get());
    }
    public static void registerListenerParsers(@Nonnull RDFItFactory factory) {
        registerListenerParsers(factory.getParserRegistry());
    }

    public static void registerAll(@Nonnull ParserRegistry registry) {
        registerItParsers(registry);
        registerListenerParsers(registry);
    }
    public static void registerAll(@Nonnull RDFItFactory factory) {
        registerAll(factory.getParserRegistry());
    }


    public static void unregisterItParsers(@Nonnull ParserRegistry registry) {
        registry.unregisterIf(p -> IT_CLASSES.contains(p.getClass()));
    }
    public static void unregisterItParsers(@Nonnull RDFItFactory factory) {
        unregisterItParsers(factory.getParserRegistry());
    }

    public static void unregisterListenerParsers(@Nonnull ParserRegistry registry) {
        registry.unregisterIf(p -> LISTENER_CLASSES.contains(p.getClass()));
    }
    public static void unregisterListenerParsers(@Nonnull RDFItFactory factory) {
        unregisterListenerParsers(factory.getParserRegistry());
    }

    public static void unregisterAll(@Nonnull ParserRegistry registry) {
        unregisterItParsers(registry);
        unregisterListenerParsers(registry);
    }
    public static void unregisterAll(@Nonnull RDFItFactory factory) {
        unregisterAll(factory.getParserRegistry());
    }
}
