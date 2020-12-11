package com.github.lapesd.rdfit;

import com.github.lapesd.rdfit.components.converters.quad.QuadLifter;
import com.github.lapesd.rdfit.iterator.RDFIt;
import com.github.lapesd.rdfit.listener.RDFListener;
import com.github.lapesd.rdfit.listener.TripleListenerBase;
import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.source.RDFInputStreamSupplier;
import com.github.lapesd.rdfit.source.syntax.RDFLangs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RIt {
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
