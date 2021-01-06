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
import com.github.lapesd.rdfit.components.Parser;
import com.github.lapesd.rdfit.components.jena.parsers.iterator.DatasetItParser;
import com.github.lapesd.rdfit.components.jena.parsers.iterator.GraphItParser;
import com.github.lapesd.rdfit.components.jena.parsers.iterator.ModelItParser;
import com.github.lapesd.rdfit.components.jena.parsers.listener.DatasetListenerParser;
import com.github.lapesd.rdfit.components.jena.parsers.listener.GraphListenerParser;
import com.github.lapesd.rdfit.components.jena.parsers.listener.ModelListenerParser;
import com.github.lapesd.rdfit.components.jena.parsers.listener.QueryExecutionListenerParser;
import com.github.lapesd.rdfit.components.parsers.JavaParsers;
import com.github.lapesd.rdfit.components.parsers.ParserRegistry;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.Quad;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public class JenaModelParsers {
    private static final @Nonnull List<ItParser> IT_PARSERS = asList(
            new DatasetItParser(), new GraphItParser(), new ModelItParser()) ;
    private static final @Nonnull List<ListenerParser> LISTENER_PARSERS = asList(
            new DatasetListenerParser(), new GraphListenerParser(), new ModelListenerParser(),
            new QueryExecutionListenerParser());
    private static final @Nonnull List<Parser> PARSERS;

    static {
        List<Parser> list = new ArrayList<>();
        list.addAll(IT_PARSERS);
        list.addAll(LISTENER_PARSERS);
        PARSERS = Collections.unmodifiableList(list);
    }

    public static void registerAll(@Nonnull ParserRegistry registry) {
        for (Parser p : PARSERS) registry.register(p);
    }
    public static void registerAll(@Nonnull RDFItFactory factory) {
        registerAll(factory.getParserRegistry());
    }
    public static void registerItParsers(@Nonnull ParserRegistry registry) {
        for (ItParser p : IT_PARSERS) registry.register(p);
        JavaParsers.registerWithTripleClass(registry, Statement.class);
        JavaParsers.registerWithTripleClass(registry, Triple.class);
        JavaParsers.registerWithQuadClass(registry, Quad.class);
    }
    public static void registerItParsers(@Nonnull RDFItFactory factory) {
        registerItParsers(factory.getParserRegistry());
    }
    public static void registerListenerParsers(@Nonnull ParserRegistry registry) {
        for (ListenerParser p : LISTENER_PARSERS) registry.register(p);
        JavaParsers.unregister(registry, Statement.class);
        JavaParsers.unregister(registry, Triple.class);
        JavaParsers.unregister(registry, Quad.class);
    }
    public static void registerListenerParsers(@Nonnull RDFItFactory factory) {
        registerListenerParsers(factory.getParserRegistry());
    }

    public static void unregisterAll(@Nonnull ParserRegistry registry) {
        for (Parser p : PARSERS) registry.unregister(p);
    }
    public static void unregisterAll(@Nonnull RDFItFactory factory) {
        unregisterAll(factory.getParserRegistry());
    }
    public static void unregisterAllItParsers(@Nonnull ParserRegistry registry) {
        for (ItParser p : IT_PARSERS) registry.unregister(p);
    }
    public static void unregisterAllItParsers(@Nonnull RDFItFactory factory) {
        unregisterAllItParsers(factory.getParserRegistry());
    }
    public static void unregisterAllListenerParsers(@Nonnull ParserRegistry registry) {
        for (ListenerParser p : LISTENER_PARSERS) registry.unregister(p);
    }
    public static void unregisterAllListenerParsers(@Nonnull RDFItFactory factory) {
        unregisterAllListenerParsers(factory.getParserRegistry());
    }
}
