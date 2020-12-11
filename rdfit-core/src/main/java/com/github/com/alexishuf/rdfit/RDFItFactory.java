package com.github.com.alexishuf.rdfit;

import com.github.com.alexishuf.rdfit.callback.RDFCallback;
import com.github.com.alexishuf.rdfit.components.Converter;
import com.github.com.alexishuf.rdfit.components.Parser;
import com.github.com.alexishuf.rdfit.errors.InconvertibleException;
import com.github.com.alexishuf.rdfit.errors.RDFItException;
import com.github.com.alexishuf.rdfit.iterator.RDFIt;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.Function;

public interface RDFItFactory extends AutoCloseable {

    /**
     * Sequentially iterate all triples in each of the sources. If any quad is met, the
     * graph information is discarded and the quad is delivered as a triple
     *
     * @param tripleClass the desired class for triple instances. Whathever triple or quad
     *                    a {@link Parser} produces, it will be converted into a instance
     *                    of the given tripleClass
     * @param sources an array of sources to be parsed, in sequence. If any of the sources is
     *                a Collection, each member will first be considered a source, and if
     *                no {@link Parser} is found for it, it will be considered a quad or a
     *                triple (whichever yields the shortest conversion path into the
     *                desired tripleClass)
     * @return A closeable (preferably lazy) iterator over triples.
     */
    @Nonnull <T> RDFIt<T> iterateTriples(@Nonnull Class<T> tripleClass, @Nonnull Object... sources);

    /**
     * same as {@link #iterateTriples(Class, Object...)}, but iterates over quads.
     *
     * @param quadClass The desired class of quad instances.
     * @param tripleClass The class of triples that triple2quad accepts
     * @param triple2quad A function that convert a triple (instance of tripleClass) into a
     *                    quad (instance of quadClass or convertible to it via {@link Converter}s)
     * @param sources Array of sources to be parsed, in sequence. For {@link Collection} instances,
     *                the same rules in {@link #iterateTriples(Class, Object...)} apply except,
     *                a conversion path into quadClass will be preferred to a conversion path
     *                into a tripleClass, even if the former is longer.
     * @return A closeable (preferably lazy) iterator over quads
     */
    @Nonnull <T> RDFIt<T> iterateQuads(@Nonnull Class<T> quadClass, @Nonnull Class<?> tripleClass,
                                       @Nonnull Function<?, ?> triple2quad,
                                       @Nonnull Object... sources);

    /**
     * Parse all given sources calling the callback for every triple/quad.
     *
     * @param callback The callback on to which triples and quads will be delivered. If the
     *                 callback has a declared quad type, any quads will be converted to that
     *                 quad class, else they will be converted to the desired triple class.
     *                 Triples will always be converted to the desired triple class.
     * @param sources Array of sources to be parsed. If a source is a {@link Collection} instance,
     *                each member will be initially considered a potential source. If no parser
     *                is found for a member, it will be considered a quad. If the callback does
     *                not define a quad class or if no conversion path to the desired quad class
     *                can be found, the member will be considered a triple. If no conversion path
     *                to the desired triple class can be found, a {@link InconvertibleException}
     *                will be thrown.
     */
    void parse(@Nonnull RDFCallback callback, @Nonnull Object... sources);

    @Override void close();
}
