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

package com.github.lapesd.rdfit;

import com.github.lapesd.rdfit.components.DefaultComponentsBundle;
import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.components.converters.quad.QuadLifter;
import com.github.lapesd.rdfit.components.normalizers.CoreSourceNormalizers;
import com.github.lapesd.rdfit.components.normalizers.DefaultSourceNormalizerRegistry;
import com.github.lapesd.rdfit.components.parsers.DefaultParserRegistry;
import com.github.lapesd.rdfit.impl.DefaultRDFItFactory;
import com.github.lapesd.rdfit.iterator.RDFIt;
import com.github.lapesd.rdfit.listener.RDFListener;
import com.github.lapesd.rdfit.listener.TripleListenerBase;
import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.source.RDFInputStreamSupplier;
import com.github.lapesd.rdfit.source.fixer.TurtleFamilyFixerDecorator;
import com.github.lapesd.rdfit.source.syntax.RDFLangs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.github.lapesd.rdfit.source.fixer.TolerantDecorator.TOLERANT;

/**
 * Helper class with shortcuts to the {@link DefaultRDFItFactory} and other general helpers
 */
public class RIt {
    private static final Logger logger = LoggerFactory.getLogger(RIt.class);
    private static boolean initialized = false;

    /**
     * Shortcut for {@link DefaultRDFItFactory#iterateTriples(Class, Object...)}.
     *
     * @param <T>  the desired triple type
     * @param tripleClass The {@link Class} object for T
     * @param sources the sources to iterate over
     * @return An {@link RDFIt} over triples from all sources (in the given sequence)
     */
    public static @Nonnull <T> RDFIt<T> iterateTriples(@Nonnull Class<T> tripleClass,
                                                       @Nonnull Object... sources) {
        return DefaultRDFItFactory.get().iterateTriples(tripleClass, sources);
    }

    /**
     * Shortcut for {@link #iterateTriples(Class, Object...)}<code>.forEachRemaining(consumer)</code>
     *
     * @param <T> type of triples
     * @param tripleClass Class object of T
     * @param consumer function consuming the triples
     * @param sources list
     */
    public static <T> void forEachTriple(@Nonnull Class<T> tripleClass,
                                         @Nonnull Consumer<? super T> consumer,
                                         @Nonnull Object... sources) {
        try (RDFIt<T> it = iterateTriples(tripleClass, sources)) {
            it.forEachRemaining(consumer);
        }
    }

    /**
     * Shortcut for {@link DefaultRDFItFactory#iterateQuads(Class, QuadLifter, Object...)}.
     *
     * @param <Q> quad type
     * @param quadClass Class object for Q
     * @param quadLifter Function that converts triples into quads
     * @param sources list of sources to iterate over
     * @return An iterator over quads parsed from sources
     */
    public static @Nonnull <Q> RDFIt<Q> iterateQuads(@Nonnull Class<Q> quadClass,
                                                     @Nonnull QuadLifter quadLifter,
                                                     @Nonnull Object... sources) {
        return DefaultRDFItFactory.get().iterateQuads(quadClass, quadLifter, sources);
    }

    /**
     * Shortcut for {@link #iterateQuads(Class, QuadLifter, Object...)}<code>.forEachRemaining(consumer)</code>.
     * @param <Q> Desired quad type
     * @param quadClass Class object for Q
     * @param quadLifter Function that transforms triples into quads
     * @param consumer handler for quads of type Q
     * @param sources set of sources to iterate
     */
    public static <Q> void forEachQuad(@Nonnull Class<Q> quadClass, @Nonnull QuadLifter quadLifter,
                                       @Nonnull Consumer<? super Q> consumer,
                                       @Nonnull Object... sources) {
        try (RDFIt<Q> it = iterateQuads(quadClass, quadLifter, sources)) {
            it.forEachRemaining(consumer);
        }
    }

    /**
     * Shortcut for {@link DefaultRDFItFactory#iterateQuads(Class, QuadLifter, Object...)}.
     *
     * @param <Q> the desired quad type
     * @param quadClass the {@link Class} object for Q
     * @param sources the sources to iterate over
     * @return An {@link RDFIt} over triples from all sources (in the given sequence)
     */
    public static @Nonnull <Q> RDFIt<Q> iterateQuads(@Nonnull Class<Q> quadClass,
                                                     @Nonnull Object... sources) {
        return DefaultRDFItFactory.get().iterateQuads(quadClass, sources);
    }

    /**
     * Shortcut for {@link #iterateQuads(Class, Object...)}<code>.forEachRemaining(consumer)</code>.
     * @param <Q> desired quad type
     * @param quadClass {@link Class} object for Q
     * @param consumer function that will consume each quad
     * @param sources sources to get quads from
     */
    public static <Q> void forEachQuad(@Nonnull Class<Q> quadClass,
                                       @Nonnull Consumer<? super Q> consumer,
                                       @Nonnull Object... sources) {
        try (RDFIt<Q> it = iterateQuads(quadClass, sources)) {
            it.forEachRemaining(consumer);
        }
    }

    /**
     * Shortcut for {@link DefaultRDFItFactory#parse(RDFListener, Object...)}
     *
     * @param listener {@link RDFListener} object that will consume triples, quads and events
     * @param sources sources to parse
     */
    public static void parse(@Nonnull RDFListener<?,?> listener, @Nonnull Object... sources) {
        DefaultRDFItFactory.get().parse(listener, sources);
    }

    /**
     * Shortcut for {@link #parse(RDFListener, Object...)} with a {@link TripleListenerBase}
     * subclass that calls consumer
     *
     * @param <T> desired triple class
     * @param tripleClass {@link Class} object for T
     * @param consumer function that will consume each iterated triple
     * @param sources sources to get triples from
     */
    public static <T> void parseTriples(@Nonnull Class<T> tripleClass,
                                        @Nonnull Consumer<? super T> consumer,
                                        @Nonnull Object... sources) {
        parse(new TripleListenerBase<T>(tripleClass) {
            @Override public void triple(@Nonnull T triple) {
                consumer.accept(triple);
            }
        }, sources);
    }

    public static @Nonnull RDFInputStream wrap(@Nullable InputStream inputStream) {
        if (inputStream == null)
            return new RDFInputStream(new ByteArrayInputStream(new byte[0]), RDFLangs.NT);
        return new RDFInputStream(inputStream);
    }

    public static @Nonnull RDFInputStreamSupplier wrap(@Nonnull Callable<InputStream> supplier) {
        return new RDFInputStreamSupplier(supplier);
    }

    public static @Nonnull RDFInputStreamSupplier wrap(@Nonnull Supplier<InputStream> supplier) {
        return new RDFInputStreamSupplier(supplier);
    }

    /**
     * Wrap the given source such that parsers tolerate bad triples in turtle-like languages.
     *
     * Examples of such bad triples are IRIs with reserved chars (i.e., the regex
     * <code>[#x00-#x20&gt;&lt;"}{|\^`]</code>) and lang tags with _'s (e.g., @pt_BR or @fr_2374). Such
     * triples tend to appear in older RDF dumps from older systems. One example are the
     * LargeRDFBench dumps.
     *
     * If the source Object normalizes to a {@link RDFInputStream} (this includes, File, Path,
     * String, InputStream, etc.) instance, it will be wrapped into another {@link RDFInputStream}
     * decorated with {@link TurtleFamilyFixerDecorator}. If source does not normalize to
     * a {@link RDFInputStream}, then the result of normalization will be returned, but it will
     * not have the "tolerance" functionality.
     *
     * @param source a source (anything is valid). In order to tolerate bad triples, it should
     *               normalize directly or indirectly to an {@link RDFInputStream}.
     * @return the result of normalizing source or an {@link RDFInputStream} that tolerates
     *         bad triples during parsing.
     */
    public static @Nonnull Object tolerant(@Nonnull Object source) {
        return DefaultRDFItFactory.get().getNormalizerRegistry().normalize(source, TOLERANT);
    }

    /**
     * Create a {@link RDFItFactory} with all components that are registered by default in
     * {@link DefaultRDFItFactory}.
     *
     * @return A new {@link RDFItFactory} independent from {@link DefaultRDFItFactory#get()}.
     */
    public static @Nonnull RDFItFactory createFactory() {
        DefaultRDFItFactory factory = new DefaultRDFItFactory(new DefaultParserRegistry(),
                new DefaultConversionManager(), new DefaultSourceNormalizerRegistry());
        init(factory);
        return factory;
    }

    /**
     * Initialize the rdfit library by registering components in the default {@link RDFItFactory}.
     *
     * This method will be called implicitly in most cases. client code should not need
     * to call it, except for debugging or non-conventional classpath shenanigans.
     */
    public static void init() {
        if (initialized)
            return;
        initialized = true;
        init(DefaultRDFItFactory.get());
    }

    /**
     * Register all rdfit components (in any of the modules that happen to be in the classpath)
     * with the given factory.
     *
     * @param factory factory to receive instances of the available components.
     */
    public static void init(@Nonnull RDFItFactory factory) {
        CoreSourceNormalizers.registerAll(factory);
        for (DefaultComponentsBundle bundle : ServiceLoader.load(DefaultComponentsBundle.class))
            bundle.registerAll(factory);
    }

    static {
        init();
    }
}
