package com.github.lapesd.rdfit;

import com.github.lapesd.rdfit.components.Converter;
import com.github.lapesd.rdfit.components.Parser;
import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.components.converters.quad.QuadLifter;
import com.github.lapesd.rdfit.components.normalizers.SourceNormalizerRegistry;
import com.github.lapesd.rdfit.components.parsers.ParserRegistry;
import com.github.lapesd.rdfit.errors.InconvertibleException;
import com.github.lapesd.rdfit.iterator.RDFIt;
import com.github.lapesd.rdfit.listener.RDFListener;

import javax.annotation.Nonnull;
import java.util.Collection;

public interface RDFItFactory extends AutoCloseable {
    /**
     * Allows configuration of {@link Converter}s that will be transparently used during iteration.
     */
    @Nonnull ConversionManager getConversionManager();

    /**
     * Allows configuring the available parsers.
     */
    @Nonnull ParserRegistry getParserRegistry();

    /**
     * Allows configuration of how sources are processed before being mapped to a parser.
     */
    @Nonnull SourceNormalizerRegistry getNormalizerRegistry();

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
     * @param quadLifter A function that convert a triple (instance of tripleClass) into a
     *                    quad (instance of quadClass or convertible to it via {@link Converter}s)
     *                    May be null only if tripleClass is null
     * @param sources Array of sources to be parsed, in sequence. For {@link Collection} instances,
     *                the same rules in {@link #iterateTriples(Class, Object...)} apply except,
     *                a conversion path into quadClass will be preferred to a conversion path
     *                into a tripleClass, even if the former is longer.
     * @return A closeable (preferably lazy) iterator over quads
     */
    @Nonnull <Q> RDFIt<Q> iterateQuads(@Nonnull Class<Q> quadClass,
                                       @Nonnull QuadLifter quadLifter,
                                       @Nonnull Object... sources);

    /**
     * Same as {@link #iterateQuads(Class, QuadLifter, Object...)}, but will try to use a chain
     * of previously registered {@link Converter} instances to convert any triples output by
     * parsers into instances of quadClass
     */
    @Nonnull <T> RDFIt<T> iterateQuads(@Nonnull Class<T> quadClass, @Nonnull Object... sources);

    /**
     * Parse all given sources calling the listener for every triple/quad.
     *
     * @param listener The listener on to which triples and quads will be delivered. If the
     *                 listener has a declared quad type, any quads will be converted to that
     *                 quad class, else they will be converted to the desired triple class.
     *                 Triples will always be converted to the desired triple class.
     * @param sources Array of sources to be parsed. If a source is a {@link Collection} instance,
     *                each member will be initially considered a potential source. If no parser
     *                is found for a member, it will be considered a quad. If the listener does
     *                not define a quad class or if no conversion path to the desired quad class
     *                can be found, the member will be considered a triple. If no conversion path
     *                to the desired triple class can be found, a {@link InconvertibleException}
     *                will be thrown.
     */
    void parse(@Nonnull RDFListener<?,?> listener, @Nonnull Object... sources);

    @Override void close();
}
