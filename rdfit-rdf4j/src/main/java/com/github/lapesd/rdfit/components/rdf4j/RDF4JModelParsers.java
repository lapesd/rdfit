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
import java.util.List;

import static java.util.Arrays.asList;

public class RDF4JModelParsers {
    private static final @Nonnull List<ItParser> IT_PARSERS = asList(
            new IterableItParser(Statement.class, IterationElement.TRIPLE),
            new IterableItParser(Statement.class, IterationElement.QUAD),
            new ArrayItParser(Statement.class, IterationElement.TRIPLE),
            new ArrayItParser(Statement.class, IterationElement.QUAD),
            new ConnectionItParser(IterationElement.TRIPLE),
            new ConnectionItParser(IterationElement.QUAD),
            new RepositoryItParser(IterationElement.TRIPLE),
            new RepositoryItParser(IterationElement.QUAD),
            new RepositoryResultItParser(IterationElement.TRIPLE),
            new RepositoryResultItParser(IterationElement.QUAD),
            new TupleQueryResultItParser(IterationElement.TRIPLE),
            new TupleQueryResultItParser(IterationElement.QUAD),
            new TupleQueryItParser(IterationElement.TRIPLE),
            new TupleQueryItParser(IterationElement.QUAD),
            new GraphQueryItParser(IterationElement.TRIPLE),
            new GraphQueryItParser(IterationElement.QUAD),
            new ModelItParser(IterationElement.TRIPLE),
            new ModelItParser(IterationElement.QUAD)
    );
    private static final @Nonnull List<ListenerParser> LISTENER_PARSERS = asList(
            new RDF4JIterableListenerParser(),
            new RDF4JArrayListenerParser(),
            new RepositoryListenerParser(),
            new RepositoryConnectionListenerParser(),
            new RepositoryResultListenerParser(),
            new TupleQueryResultListenerParser(),
            new TupleQueryListenerParser(),
            new GraphQueryResultListenerParser(),
            new GraphQueryListenerParser(),
            new ModelListenerParser()
    );

    public static void registerItParsers(@Nonnull ParserRegistry registry) {
        for (ItParser p : IT_PARSERS) registry.register(p);
    }
    public static void registerItParsers(@Nonnull RDFItFactory factory) {
        registerItParsers(factory.getParserRegistry());
    }

    public static void registerListenerParsers(@Nonnull ParserRegistry registry) {
        for (ListenerParser p : LISTENER_PARSERS) registry.register(p);
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
        for (ItParser p : IT_PARSERS) registry.unregister(p);
    }
    public static void unregisterItParsers(@Nonnull RDFItFactory factory) {
        unregisterItParsers(factory.getParserRegistry());
    }

    public static void unregisterListenerParsers(@Nonnull ParserRegistry registry) {
        for (ListenerParser p : LISTENER_PARSERS) registry.unregister(p);
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
