package com.github.lapesd.rdfit.components.jena;

import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.components.Parser;
import com.github.lapesd.rdfit.components.jena.converters.JenaConverters;
import com.github.lapesd.rdfit.components.jena.parsers.listener.JenaInputStreamParser;
import com.github.lapesd.rdfit.components.parsers.ParserRegistry;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class JenaParsers {
    private static final @Nonnull List<Parser> PARSERS
            = Collections.singletonList(new JenaInputStreamParser());

    public static void registerAll(@Nonnull ParserRegistry registry) {
        for (Parser p : PARSERS) registry.register(p);
    }
    public static void registerAll(@Nonnull RDFItFactory factory) {
        registerAll(factory.getParserRegistry());
        JenaConverters.registerAll(factory);
    }

    public static void unregisterAll(@Nonnull ParserRegistry registry) {
        for (Parser p : PARSERS) registry.unregister(p);
    }
    public static void unregisterAll(@Nonnull RDFItFactory factory) {
        unregisterAll(factory.getParserRegistry());
        JenaConverters.unregisterAll(factory);
    }
}
