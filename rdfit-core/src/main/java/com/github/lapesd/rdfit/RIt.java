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

import com.github.lapesd.rdfit.components.converters.quad.QuadLifter;
import com.github.lapesd.rdfit.components.normalizers.CoreSourceNormalizers;
import com.github.lapesd.rdfit.iterator.RDFIt;
import com.github.lapesd.rdfit.listener.RDFListener;
import com.github.lapesd.rdfit.listener.TripleListenerBase;
import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.source.RDFInputStreamSupplier;
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
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RIt {
    private static final Logger logger = LoggerFactory.getLogger(RIt.class);
    private static boolean initialized = false;

    public static void init() {
        if (initialized)
            return;
        initialized = true;
        DefaultRDFItFactory factory = DefaultRDFItFactory.get();
        CoreSourceNormalizers.registerAll(factory);
        String root = "com.github.lapesd.rdfit.components";
        callRegisterAll(factory, root + ".jena.converters.JenaConverters");
        callRegisterAll(factory, root + ".jena.JenaModelParsers");
        callRegisterAll(factory, root + ".jena.JenaParsers");
        callRegisterAll(factory, root + ".rdf4j.RDF4JModelParsers");
        callRegisterAll(factory, root + ".rdf4j.RDF4JParsers");
        callRegisterAll(factory, root + ".hdt.HDTParsers");
        callRegisterAll(factory, root + ".hdt.converters.HDTConverters");
        callRegisterAll(factory, root + ".converters.JenaRDF4JConverters");
    }

    static {
        init();
    }

    private static void callRegisterAll(@Nonnull DefaultRDFItFactory factory,
                                        @Nonnull String className) {
        if (callRegisterAll(Thread.currentThread().getContextClassLoader(), factory, className))
            return;
        if (callRegisterAll(RIt.class.getClassLoader(), factory, className))
            logger.info("Registered {}", className);
        else
            logger.info("Did not registered {}", className);
    }
    private static boolean callRegisterAll(@Nonnull ClassLoader cl, @Nonnull DefaultRDFItFactory f,
                                           @Nonnull String className) {
        try {
            Class<?> cls = cl.loadClass(className);
            Method m = cls.getMethod("registerAll", RDFItFactory.class);
            int mods = m.getModifiers();
            if (!Modifier.isStatic(mods) && !Modifier.isPublic(mods))
                throw new RuntimeException(className+"registerAll: not public static");
            m.invoke(null, f);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Missing registerAll method in "+className, e);
        } catch (IllegalAccessException e) {
            logger.warn("IllegalAccessException invoking {}.registerAll", className, e);
            return false;
        } catch (InvocationTargetException e) {
            throw (RuntimeException)e.getCause();
        }
    }

    /**
     * Shortcut for {@link DefaultRDFItFactory#iterateTriples(Class, Object...)}.
     */
    public static @Nonnull <T> RDFIt<T> iterateTriples(@Nonnull Class<T> tripleClass,
                                                       @Nonnull Object... sources) {
        return DefaultRDFItFactory.get().iterateTriples(tripleClass, sources);
    }

    /**
     * Shortcut for {@link #iterateTriples(Class, Object...)}<code>.forEachRemaining(consumer)</code>
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
     */
    public static @Nonnull <Q> RDFIt<Q> iterateQuads(@Nonnull Class<Q> quadClass,
                                                     @Nonnull QuadLifter quadLifter,
                                                     @Nonnull Object... sources) {
        return DefaultRDFItFactory.get().iterateQuads(quadClass, quadLifter, sources);
    }

    /**
     * Shortcut for {@link #iterateQuads(Class, QuadLifter, Object...)}<code>.forEachRemaining(consumer)</code>.
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
     */
    public static @Nonnull <Q> RDFIt<Q> iterateQuads(@Nonnull Class<Q> quadClass,
                                                     @Nonnull Object... sources) {
        return DefaultRDFItFactory.get().iterateQuads(quadClass, sources);
    }

    /**
     * Shortcut for {@link #iterateQuads(Class, Object...)}<code>.forEachRemaining(consumer)</code>.
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
     */
    public static void parse(@Nonnull RDFListener<?,?> listener, @Nonnull Object... sources) {
        DefaultRDFItFactory.get().parse(listener, sources);
    }

    /**
     * Shortcut for {@link #parse(RDFListener, Object...)} with a {@link TripleListenerBase}
     * subclass that calls consumer
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

}
