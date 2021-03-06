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

package com.github.lapesd.rdfit.components.parsers;


import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.components.ItParser;
import com.github.lapesd.rdfit.components.ListenerParser;
import com.github.lapesd.rdfit.components.parsers.impl.iterator.ArrayItParser;
import com.github.lapesd.rdfit.components.parsers.impl.iterator.IterableItParser;
import com.github.lapesd.rdfit.components.parsers.impl.iterator.RDFItItParser;
import com.github.lapesd.rdfit.components.parsers.impl.listener.IterableListenerParser;
import com.github.lapesd.rdfit.components.parsers.impl.listener.QuadArrayListenerParser;
import com.github.lapesd.rdfit.components.parsers.impl.listener.RDFItListenerParser;
import com.github.lapesd.rdfit.components.parsers.impl.listener.TripleArrayListenerParser;
import com.github.lapesd.rdfit.impl.DefaultRDFItFactory;
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
            TripleArrayListenerParser.class,
            QuadArrayListenerParser.class));

    /**
     * Register parsers for Iterables, Streams and Iterables of the given triple class on
     * the {@link DefaultRDFItFactory}
     *
     * @param memberClass The {@link ItParser#valueClass()} or {@link ListenerParser#tripleType()}
     *                    of the {@link ItParser}s and {@link ListenerParser}s implementations to
     *                    be registered
     */
    public static void registerWithTripleClass(@Nonnull Class<?> memberClass) {
        registerWithTripleClass(DefaultRDFItFactory.get(), memberClass);
    }
    /**
     * Register parsers for Iterables, Streams and Iterables of the given quad class on
     * the {@link DefaultRDFItFactory}
     *
     * @param memberClass The {@link ItParser#valueClass()} or {@link ListenerParser#quadType()}
     *                    of the {@link ItParser}s and {@link ListenerParser}s implementations
     *                    to be registered.
     */
    public static void registerWithQuadClass(@Nonnull Class<?> memberClass) {
        registerWithQuadClass(DefaultRDFItFactory.get(), memberClass);
    }

    /**
     * Remove parsers handling the given triple or quad class registered by
     * this helper on the {@link DefaultRDFItFactory}
     *
     * @param memberClass {@link ItParser#valueClass()} of the {@link ItParser}s to be unregistered
     */
    public static void unregister(@Nonnull Class<?> memberClass) {
        unregister(DefaultRDFItFactory.get(), memberClass);
    }

    /**
     * Unregister from {@link DefaultRDFItFactory} any Listener parser that handles both the
     * given triple class and quad class.
     *
     * @param tripleClass class of triples handled by the {@link ListenerParser}
     * @param quadCLass   class of quads handled by the {@link ListenerParser}
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
        registry.register(new RDFItItParser(memberClass, IterationElement.TRIPLE));
        registry.register(new RDFItListenerParser(memberClass, memberClass));
        //ListenerParsers other than for RDFIt are not registered here, as it is more efficient
        //for downstream modules to register a single *ListenerParse handling both triples and quads
        registry.register(new IterableItParser(memberClass, IterationElement.TRIPLE));
        registry.register(new ArrayItParser(memberClass, IterationElement.TRIPLE));
    }

    public static void registerWithQuadClass(@Nonnull ParserRegistry registry,
                                             @Nonnull Class<?> memberClass) {
        registry.register(new RDFItItParser(memberClass, IterationElement.QUAD));
        registry.register(new RDFItListenerParser(memberClass, memberClass));
        registry.register(new IterableItParser(memberClass, IterationElement.QUAD));
        registry.register(new ArrayItParser(memberClass, IterationElement.QUAD));
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
