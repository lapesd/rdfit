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

package com.github.lapesd.rdfit.components.jena;

import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.components.ItParser;
import com.github.lapesd.rdfit.components.ListenerParser;
import com.github.lapesd.rdfit.components.jena.parsers.iterator.DatasetItParser;
import com.github.lapesd.rdfit.components.jena.parsers.iterator.GraphItParser;
import com.github.lapesd.rdfit.components.jena.parsers.iterator.ModelItParser;
import com.github.lapesd.rdfit.components.jena.parsers.listener.*;
import com.github.lapesd.rdfit.components.parsers.JavaParsers;
import com.github.lapesd.rdfit.components.parsers.ParserRegistry;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.Quad;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

public class JenaModelParsers {
    private static final List<Supplier<ItParser>> IT_SUPPLIERS = asList(
            DatasetItParser::new, GraphItParser::new, ModelItParser::new
    );
    private static final Set<Class<?>> IT_CLASSES = unmodifiableSet(new HashSet<>(asList(
            DatasetItParser.class, GraphItParser.class, ModelItParser.class
    )));
    private static final List<Supplier<ListenerParser>> LISTENER_SUPPLIERS = asList(
            DatasetListenerParser::new, GraphListenerParser::new, ModelListenerParser::new,
            QueryExecutionListenerParser::new
    );
    private static final Set<Class<?>> LISTENER_CLASSES = unmodifiableSet(new HashSet<>(asList(
            DatasetListenerParser.class, GraphListenerParser.class, ModelListenerParser.class,
            QueryExecutionListenerParser.class
    )));

    public static void registerAll(@Nonnull ParserRegistry registry) {
        registerItParsers(registry);
        registerListenerParsers(registry);
    }
    public static void registerAll(@Nonnull RDFItFactory factory) {
        registerAll(factory.getParserRegistry());
    }
    public static void registerItParsers(@Nonnull ParserRegistry registry) {
        for (Supplier<ItParser> supplier : IT_SUPPLIERS)
            registry.register(supplier.get());
        JavaParsers.registerWithTripleClass(registry, Statement.class);
        JavaParsers.registerWithTripleClass(registry, Triple.class);
        JavaParsers.registerWithQuadClass(registry, Quad.class);
    }
    public static void registerItParsers(@Nonnull RDFItFactory factory) {
        registerItParsers(factory.getParserRegistry());
    }
    public static void registerListenerParsers(@Nonnull ParserRegistry registry) {
        for (Supplier<ListenerParser> supplier : LISTENER_SUPPLIERS)
            registry.register(supplier.get());
        registry.register(new JenaIterableListenerParser(Triple.class, Quad.class));
        registry.register(new JenaIterableListenerParser(Statement.class, Quad.class));
        registry.register(new JenaArrayListenerParser(Triple.class, Quad.class));
        registry.register(new JenaArrayListenerParser(Statement.class, Quad.class));
    }
    public static void registerListenerParsers(@Nonnull RDFItFactory factory) {
        registerListenerParsers(factory.getParserRegistry());
    }

    public static void unregisterAll(@Nonnull ParserRegistry registry) {
        unregisterAllItParsers(registry);
        unregisterAllListenerParsers(registry);
    }
    public static void unregisterAll(@Nonnull RDFItFactory factory) {
        unregisterAll(factory.getParserRegistry());
    }
    public static void unregisterAllItParsers(@Nonnull ParserRegistry registry) {
        registry.unregisterIf(p -> IT_CLASSES.contains(p.getClass()));
    }
    public static void unregisterAllItParsers(@Nonnull RDFItFactory factory) {
        unregisterAllItParsers(factory.getParserRegistry());
    }
    public static void unregisterAllListenerParsers(@Nonnull ParserRegistry registry) {
        registry.unregisterIf(p -> LISTENER_CLASSES.contains(p.getClass()));
    }
    public static void unregisterAllListenerParsers(@Nonnull RDFItFactory factory) {
        unregisterAllListenerParsers(factory.getParserRegistry());
    }
}
