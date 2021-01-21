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

import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.components.converters.quad.QuadLifter;
import com.github.lapesd.rdfit.components.converters.quad.QuadSplitter;
import com.github.lapesd.rdfit.components.converters.quad.SplitQuad;
import com.github.lapesd.rdfit.components.converters.util.ConversionCache;
import com.github.lapesd.rdfit.errors.InconvertibleException;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.listener.RDFListener;
import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

import static com.github.lapesd.rdfit.components.converters.util.ConversionPathSingletonCache.createCache;

/**
 * Helper class to deliver triples/quads to an {@link RDFListener} instance.
 */
public class ListenerFeeder implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(ListenerFeeder.class);
    private final @Nonnull RDFListener<Object, Object> target;
    private final @Nonnull ConversionCache downgrader;
    private final @Nonnull ConversionCache quadConverter;
    private final @Nonnull ConversionCache upgrader;
    private final @Nonnull ConversionCache tripleConverter;
    private final @Nonnull ConversionCache lifterTripleConverter;
    private final @Nullable QuadSplitter<Object> quadSplitter;
    private final @Nullable QuadLifter quadLifter;
    private @Nullable Object source;
    private boolean notifySource = false;

    public ListenerFeeder(@Nonnull RDFListener<?,?> target) {
        this(target, null);
    }

    public ListenerFeeder(@Nonnull RDFListener<?,?> target, @Nullable ConversionManager conMgr) {
        this(target, conMgr, null, null);
    }

    @SuppressWarnings("unchecked")
    public ListenerFeeder(@Nonnull RDFListener<?,?> target, @Nullable ConversionManager conMgr,
                          @Nullable QuadSplitter<?> qSplitter,
                          @Nullable QuadLifter qLifter) {
        this.target           = (RDFListener<Object, Object>) target;
        tripleConverter       = createCache(conMgr, target.tripleType());
        lifterTripleConverter = createCache(conMgr, qLifter == null ? null : qLifter.tripleType());
        downgrader            = createCache(conMgr, target.tripleType());
        quadConverter         = createCache(conMgr, target.quadType()  );
        upgrader              = createCache(conMgr, target.quadType()  );
        this.quadSplitter     = (QuadSplitter<Object>) qSplitter;
        this.quadLifter       = qLifter;
    }

    public @Nonnull ListenerFeeder setSource(@Nullable Object source, boolean notifySource) {
        if (this.notifySource && this.source != null)
            target.finish(this.source);
        this.notifySource = notifySource;
        this.source = source;
        if (this.source != null) {
            target.start(this.source);
            if (source instanceof RDFInputStream) {
                target.baseIRI(((RDFInputStream) source).getBaseIRI());
            } else if (source instanceof URL || source instanceof URI) {
                target.baseIRI(source.toString().replaceFirst("^file:/", "file://"));
            } else if (source instanceof File || source instanceof Path) {
                File file = source instanceof File ? (File)source : ((Path)source).toFile();
                String string = file.toURI().toASCIIString();
                target.baseIRI(string.replaceFirst("^file:/", "file://"));
            }
        }
        return this;
    }
    public @Nonnull ListenerFeeder setSource(@Nullable Object source) {
        return setSource(source, true);
    }
    public @Nonnull ListenerFeeder setSourceSilently(@Nullable Object source) {
        return setSource(source, false);
    }

    /**
     * Feed a quad and return true iff parsing of the current source should continue
     * @param quad quad to be fed
     * @return true if parsing should continue, false if a {@link InconvertibleException}
     *         happened and the target returned false on
     *         {@link RDFListener#notifyInconvertibleQuad(InconvertibleException)}.
     * @throws InterruptParsingException may be thrown by
     *         {@link RDFListener#notifyInconvertibleQuad(InconvertibleException)} in response to
     *         an {@link InconvertibleException}
     */
    public boolean feedQuad(@Nonnull Object quad) throws InterruptParsingException {
        try {
            innerFeedQuad(quad);
            return true;
        } catch (InconvertibleException e) {
            return target.notifyInconvertibleQuad(e);
        }
    }
    protected void innerFeedQuad(@Nonnull Object quad) {
        if (source == null)
            throw new IllegalStateException("No source set on "+this);
        Class<?> qt = target.quadType(), tt = target.tripleType();
        if (qt != null) {
            if (qt.isInstance(quad)) {
                target.quad(quad);
                return;
            } else {
                Object converted = quadConverter.convert(source, quad);
                if (qt.isInstance(converted)) {
                    target.quad(converted);
                    return;
                }
            }
        }
        if (tt != null) {
            // couldn't deliver as quad, try to split
            if (quadSplitter != null) {
                SplitQuad split = quadSplitter.apply(quad);
                Object triple = split.getTriple();
                if (!tt.isInstance(triple))
                    triple = tripleConverter.convert(source, triple); // throws on failure
                if (tt.isInstance(triple)) {
                    target.quad(split.getGraph(), triple);
                    return;
                }
            }

            // try downgrading to triple (discarding graph)
            Object triple = downgrader.convert(source, quad);
            if (!tt.isInstance(triple)) // check for no-op converter
                throw new InconvertibleException(source, triple, tt);
            target.triple(triple);
            return;
        }
        logger.error("Callback {} declares null quadType() and null tripleType(). " +
                     "Will force-feed quad {} from source {}", target, quad, source);
        target.quad(quad);
    }

    /**
     * Feed a triple and return true iff parsing of the current source should continue.
     *
     * @param triple triple to be fed
     * @return true if parsing should continue, false if a {@link InconvertibleException}
     *         happened and the target returned false on
     *         {@link RDFListener#notifyInconvertibleQuad(InconvertibleException)}.
     * @throws InterruptParsingException may be thrown by
     *         {@link RDFListener#notifyInconvertibleQuad(InconvertibleException)} in response to
     *         an {@link InconvertibleException}
     */
    public boolean feedTriple(@Nonnull Object triple) {
        try {
            innerFeedTriple(triple);
            return true;
        } catch (InconvertibleException e) {
            return target.notifyInconvertibleTriple(e);
        }
    }
    protected void innerFeedTriple(@Nonnull Object triple) {
        if (source == null)
            throw new IllegalStateException("No source set on "+this);
        Class<?> tt = target.tripleType(), qt = target.quadType();
        if (tt == null) {
            if (qt == null) {
                logger.error("Callback {} declares null quadType() and null tripleType(). " +
                             "Will force-feed triple {} from source {}", target, triple, source);
                target.triple(triple);
            } else {
                Object quad = null;
                if (quadLifter != null) {
                    Object lifterIn = lifterTripleConverter.convert(source, triple);
                    if (quadLifter.tripleType().isInstance(lifterIn))
                        quad = quadLifter.lift(triple);
                }
                if (quad == null) // lifter did not lift, try using Converters
                    quad = upgrader.convert(source, triple);
                if (!qt.isInstance(quad)) // ran out of alternatives
                    throw new InconvertibleException(source, triple, qt);
                target.quad(quad);
            }
        } else {
            if (!tt.isInstance(triple)) {
                triple = tripleConverter.convert(source, triple);
                if (!tt.isInstance(triple))
                    throw new InconvertibleException(source, triple, tt);
            }
            target.triple(triple);
        }
    }

    /**
     * Deliver a object that could be either a triple or a quad.
     *
     * This method employs several rules to solve the ambiguity, and in case of
     * {@link InconvertibleException} exceptions will try different conversion strategies
     * ({@link QuadLifter} or {@link QuadSplitter} as well as assuming the object was a triple
     * instead of a quad or a quad instead of a triple. Only if all attempts fail, an
     * {@link InconvertibleException} will be delivered to the target callback (according to
     * the first guess for whether obj was a quad or a triple).
     *
     * @param obj an object that could be a triple or a quad.
     * @return whether parsing of the current source should continue. finish(source) will
     *         not be called by this method, it will be called by {@link #close()} (unless
     *         {@link #setSourceSilently(Object)} was called instead of {@link #setSource(Object)}).
     * @throws InterruptParsingException if target's
     *         {@link RDFListener#notifyInconvertibleTriple(InconvertibleException)} or
     *         {@link RDFListener#notifyInconvertibleQuad(InconvertibleException)} throw it
     *         instead of returning true/false.
     * @throws RDFItException if thrown by the target (this should not occur)
     */
    public boolean feed(@Nonnull Object obj) {
        Class<?> tt = target.tripleType(), qt = target.quadType();
        boolean tripleFirst, tryBoth = (tt == null) == (qt == null);

        if (tt == null && qt == null) {
            logger.error("Callback {} declares neither tripleType() not quadType(). " +
                         "Will force-feed ambiguous object {} from source {} as a triple.",
                         target, obj, source);
            tripleFirst = true;
        } else if (tt != null && qt == null) {
            tripleFirst = true; // target only accepts triples
        } else if (tt == null) {
            tripleFirst = false; //target only accepts quads
        } else if (tt.equals(qt)) {
            logger.warn("Callback {} declares tripleType()==quadType()=={}. Will attempt " +
                        "to deliver ambiguous triple/quad object {} as a quad.", target, tt, obj);
            tripleFirst = false;
        } else if (qt.isInstance(obj) && (tt.isAssignableFrom(qt) || !tt.isInstance(obj))) {
            tripleFirst = false; // obj matches only qt or qt is more specific
        } else if (tt.isInstance(obj) && (qt.isAssignableFrom(tt) || !qt.isInstance(obj))) {
            tripleFirst = true;  // obj matches ony tt or tt is more specific
        } else {
            tripleFirst = qt.isAssignableFrom(tt); // start as a triple iff tt is more specific
        }

        try {
            try {
                if (tripleFirst) innerFeedTriple(obj);
                else             innerFeedQuad(obj);
            } catch (InconvertibleException e) {
                if (!tryBoth)
                    throw e;
                if (tripleFirst) innerFeedQuad(obj);
                else             innerFeedTriple(obj);
            }
        } catch (InconvertibleException e) {
            if (tripleFirst) return target.notifyInconvertibleTriple(e);
            else             return target.notifyInconvertibleQuad(e);
        } //InconvertibleException, RDFItException and anything else
        return true; // delivered without issue
    }

    @Override public @Nonnull String toString() {
        return String.format("%s{target=%s,notifySource=%b,source=%s}",
                             Utils.toString(this), target, notifySource, source);
    }

    @Override public void close() {
        setSource(null);
    }
}
