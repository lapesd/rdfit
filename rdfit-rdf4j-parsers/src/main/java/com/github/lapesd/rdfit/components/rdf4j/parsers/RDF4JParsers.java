package com.github.lapesd.rdfit.components.rdf4j.parsers;

import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.components.Parser;
import com.github.lapesd.rdfit.components.parsers.ParserRegistry;
import com.github.lapesd.rdfit.components.rdf4j.parsers.listener.RDF4JInputStreamParser;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.Collections.singletonList;

public class RDF4JParsers {
    private static final @Nonnull List<Parser> PARSERS
            = singletonList(new RDF4JInputStreamParser());

    public static void registerAll(@Nonnull ParserRegistry registry) {
        for (Parser p : PARSERS) registry.register(p);
    }
    public static void registerAll(@Nonnull RDFItFactory factory) {
        registerAll(factory.getParserRegistry());
    }

    public static void unregisterAll(@Nonnull ParserRegistry registry) {
        for (Parser p : PARSERS) registry.unregister(p);
    }
    public static void unregisterAll(@Nonnull RDFItFactory factory) {
        unregisterAll(factory.getParserRegistry());
    }
}
