package com.github.lapesd.rdfit.components.hdt;

import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.components.Parser;
import com.github.lapesd.rdfit.components.hdt.parsers.iterator.HDTItParser;
import com.github.lapesd.rdfit.components.parsers.JavaParsers;
import com.github.lapesd.rdfit.components.parsers.ParserRegistry;
import org.rdfhdt.hdt.triples.TripleString;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class HDTParsers {
    private static final List<Parser> PARSERS = Collections.singletonList(new HDTItParser());

    public static void registerAll(@Nonnull RDFItFactory factory) {
        registerAll(factory.getParserRegistry());
    }
    public static void registerAll(@Nonnull ParserRegistry registry) {
        for (Parser p : PARSERS) registry.register(p);
        JavaParsers.registerWithTripleClass(registry, TripleString.class);
    }

    public static void unregisterAll(@Nonnull RDFItFactory factory) {
        unregisterAll(factory.getParserRegistry());
    }
    public static void unregisterAll(@Nonnull ParserRegistry registry) {
        for (Parser p : PARSERS) registry.unregister(p);
        JavaParsers.unregister(registry, TripleString.class);
    }
}
