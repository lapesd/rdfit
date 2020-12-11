package com.github.lapesd.rdfit.components.parsers;


import com.github.lapesd.rdfit.DefaultRDFItFactory;
import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.components.ItParser;
import com.github.lapesd.rdfit.components.ListenerParser;
import com.github.lapesd.rdfit.components.parsers.impl.iterator.ArrayItParser;
import com.github.lapesd.rdfit.components.parsers.impl.iterator.IterableItParser;
import com.github.lapesd.rdfit.components.parsers.impl.listener.IterableListenerParser;
import com.github.lapesd.rdfit.components.parsers.impl.listener.QuadArrayListenerParser;
import com.github.lapesd.rdfit.components.parsers.impl.listener.StreamListenerParser;
import com.github.lapesd.rdfit.components.parsers.impl.listener.TripleArrayListenerParser;
import com.github.lapesd.rdfit.iterator.IterationElement;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Helper class for registering registering parsers of collections
 */
public class JavaParsers {
    private static final @Nonnull Set<Class<?>> CLASSES = new HashSet<>(asList(
            IterableItParser.class, ArrayItParser.class, IterableListenerParser.class,
            StreamListenerParser.class, TripleArrayListenerParser.class,
            QuadArrayListenerParser.class));

    /**
     * Register parsers for Iterables, Streams and Iterables of the given triple class on
     * the {@link DefaultRDFItFactory}
     */
    public static void registerWithTripleClass(@Nonnull Class<?> memberClass) {
        registerWithTripleClass(DefaultRDFItFactory.get(), memberClass);
    }
    /**
     * Register parsers for Iterables, Streams and Iterables of the given quad class on
     * the {@link DefaultRDFItFactory}
     */
    public static void registerWithQuadClass(@Nonnull Class<?> memberClass) {
        registerWithQuadClass(DefaultRDFItFactory.get(), memberClass);
    }

    /**
     * Remove parsers handling the given triple or quad class registered by
     * this helper on the {@link DefaultRDFItFactory}
     */
    public static void unregister(@Nonnull Class<?> memberClass) {
        unregister(DefaultRDFItFactory.get(), memberClass);
    }

    /**
     * Unregister from {@link DefaultRDFItFactory} any Listener parser that handles both the
     * given triple class and quad class.
     */
    public static void unregister(@Nonnull Class<?> tripleClass, @Nonnull Class<?> quadCLass) {
        unregister(DefaultRDFItFactory.get(), tripleClass, quadCLass);
    }


    public static void registerWithTripleClass(@Nonnull RDFItFactory factory,
                                               @Nonnull Class<?> memberClass) {
        registerWithTripleClass(factory.getParserRegistry(), memberClass);
    }
    public static void registerWithQuadClass(@Nonnull RDFItFactory factory,
                                             @Nonnull Class<?> memberClass) {
        registerWithQuadClass(factory.getParserRegistry(), memberClass);
    }
    public static void unregister(@Nonnull RDFItFactory factory,
                                  @Nonnull Class<?> memberClass) {
        unregister(factory.getParserRegistry(), memberClass);
    }
    public static void unregister(@Nonnull RDFItFactory factory, @Nonnull Class<?> tripleClass,
                                  @Nonnull Class<?> quadClass) {
        unregister(factory.getParserRegistry(), tripleClass, quadClass);
    }


    public static void registerWithTripleClass(@Nonnull ParserRegistry registry,
                                               @Nonnull Class<?> memberClass) {
        registry.register(new IterableItParser(memberClass, IterationElement.TRIPLE));
        registry.register(new ArrayItParser(memberClass, IterationElement.TRIPLE));
        registry.register(new IterableListenerParser(memberClass, null));
        registry.register(new StreamListenerParser(memberClass, null));
        registry.register(new TripleArrayListenerParser(memberClass));
    }

    public static void registerWithQuadClass(@Nonnull ParserRegistry registry,
                                             @Nonnull Class<?> memberClass) {
        registry.register(new IterableItParser(memberClass, IterationElement.QUAD));
        registry.register(new ArrayItParser(memberClass, IterationElement.QUAD));
        registry.register(new IterableListenerParser(null, memberClass));
        registry.register(new StreamListenerParser(null, memberClass));
        registry.register(new TripleArrayListenerParser(memberClass));
    }

    public static void unregister(@Nonnull ParserRegistry registry,
                                  @Nonnull Class<?> memberClass) {
        registry.unregisterIf(p -> {
            if (!CLASSES.contains(p.getClass()))
                return false;
            if (p instanceof ItParser)
                return ((ItParser)p).valueClass().equals(memberClass);
            else if (p instanceof ListenerParser) {
                ListenerParser lp = (ListenerParser) p;
                Class<?> tt = lp.tripleType(), qt = lp.quadType();
                assert tt != null || qt != null;
                return (tt == null && qt.equals(memberClass))
                    || (qt == null && tt.equals(memberClass));
            }
            return false;
        });
    }

    public static void unregister(@Nonnull ParserRegistry registry,
                                  @Nonnull Class<?> tripleClass, @Nonnull Class<?> quadClass) {
        registry.unregisterIf(p -> {
            if (!(p instanceof ListenerParser) || !CLASSES.contains(p.getClass()))
                return false;
            ListenerParser lp = (ListenerParser) p;
            Class<?> tt = lp.tripleType(), qt = lp.quadType();
            return tt != null && qt != null && tt.equals(tripleClass) && qt.equals(quadClass);
        });
    }
}
