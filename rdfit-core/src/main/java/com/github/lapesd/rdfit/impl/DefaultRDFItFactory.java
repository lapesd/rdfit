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

package com.github.lapesd.rdfit.impl;

import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.RIt;
import com.github.lapesd.rdfit.components.ItParser;
import com.github.lapesd.rdfit.components.ListenerParser;
import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.components.converters.quad.QuadLifter;
import com.github.lapesd.rdfit.components.normalizers.DefaultSourceNormalizerRegistry;
import com.github.lapesd.rdfit.components.normalizers.SourceNormalizerRegistry;
import com.github.lapesd.rdfit.components.parsers.DefaultParserRegistry;
import com.github.lapesd.rdfit.components.parsers.ListenerFeeder;
import com.github.lapesd.rdfit.components.parsers.ParserRegistry;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.NoParserException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.iterator.*;
import com.github.lapesd.rdfit.listener.ConvertingRDFListener;
import com.github.lapesd.rdfit.listener.RDFListener;
import com.github.lapesd.rdfit.source.SourcesIterator;
import com.github.lapesd.rdfit.util.NoSource;
import com.github.lapesd.rdfit.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.github.lapesd.rdfit.iterator.IterationElement.QUAD;
import static com.github.lapesd.rdfit.iterator.IterationElement.TRIPLE;

public class DefaultRDFItFactory implements RDFItFactory {
    private static final Logger logger = LoggerFactory.getLogger(DefaultRDFItFactory.class);
    private static final @Nonnull DefaultRDFItFactory INSTANCE
            = new DefaultRDFItFactory(DefaultParserRegistry.get(), DefaultConversionManager.get(),
                                      DefaultSourceNormalizerRegistry.get());

    protected @Nonnull ParserRegistry parserRegistry;
    protected @Nonnull ConversionManager conversionMgr;
    protected @Nonnull SourceNormalizerRegistry normalizerRegistry;
    private final @Nonnull ThreadPoolExecutor executor;

    /**
     * Create a new {@link DefaultRDFItFactory}.
     *
     * {@link ParserRegistry#setConversionManager(ConversionManager)},
     * {@link SourceNormalizerRegistry#setParserRegistry(ParserRegistry)} and
     * {@link SourceNormalizerRegistry#setConversionManager(ConversionManager)} will NOT be
     * called to link the provided registries to one another.
     *
     * @param parserRegistry the parser registry
     * @param conversionManager the conversion manager
     * @param normalizerRegistry the normalizers registry
     */
    public DefaultRDFItFactory(@Nonnull ParserRegistry parserRegistry,
                               @Nonnull ConversionManager conversionManager,
                               @Nonnull SourceNormalizerRegistry normalizerRegistry) {
        this.parserRegistry = parserRegistry;
        this.conversionMgr = conversionManager;
        this.normalizerRegistry = normalizerRegistry;
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        SecurityManager secMgr = System.getSecurityManager();
        executor = new ThreadPoolExecutor(0, availableProcessors * 8,
                5, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(), new ThreadFactory() {
            private final AtomicInteger threads = new AtomicInteger(0);
            final ThreadGroup group = secMgr != null ? secMgr.getThreadGroup()
                                                     : Thread.currentThread().getThreadGroup();

            @Override public Thread newThread(@Nonnull Runnable r) {
                String name = "DefaultRDFItFactory" + threads.incrementAndGet();
                Thread thread = new Thread(group, r, name, 0);
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    public static @Nonnull DefaultRDFItFactory get() {
        RIt.init();
        return INSTANCE;
    }

    @Override public @Nonnull ConversionManager getConversionManager() {
        return conversionMgr;
    }

    @Override public @Nonnull ParserRegistry getParserRegistry() {
        return parserRegistry;
    }

    @Override public @Nonnull SourceNormalizerRegistry getNormalizerRegistry() {
        return normalizerRegistry;
    }

    private @Nonnull RDFIt<Object>
    iterateSources(@Nonnull IterationElement itElement, @Nullable Class<?> tripleClass,
                   @Nullable Class<?> quadClass, @Nullable QuadLifter quadLifter,
                   @Nonnull Object... sources) {
        if (tripleClass == null && quadClass == null)
            throw new NullPointerException("Both tripleClass and quadClass are null");
        assert quadLifter == null || tripleClass != null;
        Class<?> valueClass = itElement.isTriple() ? tripleClass : quadClass;
        if (valueClass == null)
            throw new IllegalArgumentException("null *Class parameter corresponding to itElement");

        if (sources.length == 0) {
            return new EmptyRDFIt<>(valueClass, itElement, NoSource.INSTANCE);
        } else if (sources.length > 1) {
            return new FlatMapRDFIt<>(valueClass,  itElement, Arrays.asList(sources).iterator(),
                    s -> iterateSources(itElement, tripleClass, quadClass, quadLifter, s));
        }
        Object source = sources[0];
        if (source == null)
            return new EmptyRDFIt<>(valueClass, itElement, NoSource.INSTANCE);
        Object normalized = normalizerRegistry.normalize(source);
        if (normalized instanceof SourcesIterator) {
            return new FlatMapRDFIt<>(valueClass, itElement, (SourcesIterator)normalized,
                    s -> iterateSources(itElement, tripleClass, quadClass, quadLifter, s));
        } else if (normalized instanceof RDFItException) {
            return new ErrorRDFIt<>(valueClass, itElement, source, (RDFItException) normalized);
        } else {
            return iterateSource(itElement, tripleClass, quadLifter, valueClass, normalized);
        }
    }

    private @Nonnull RDFIt<Object> iterateSource(@Nonnull IterationElement itElement,
                                                 @Nullable Class<?> tripleClass,
                                                 @Nullable QuadLifter quadLifter,
                                                 @Nonnull Class<?> valueClass, @Nonnull Object in) {
        RDFIt<Object> it;
        ItParser itParser = parserRegistry.getItParser(in, itElement, valueClass);
        if (itParser == null) {
            IterationElement other = itElement.toggle();
            Class<?> otherClass = itElement == QUAD ? tripleClass : valueClass;
            itParser = parserRegistry.getItParser(in, other, otherClass);
            if (itParser == null) {
                it = parse2It(itElement, quadLifter, valueClass, in);
            } else {
                it = itParser.parse(in);
                if (valueClass.isAssignableFrom(it.valueClass())) {
                    it = new TransformingRDFIt<>(valueClass, itElement, it, Function.identity());
                } else if (quadLifter == null) {
                    it = new ConvertingRDFIt<>(valueClass, itElement, it, conversionMgr);
                } else {
                    assert tripleClass != null;
                    if (!tripleClass.isAssignableFrom(itParser.valueClass()))
                        it = new ConvertingRDFIt<>(tripleClass, TRIPLE, it, conversionMgr);
                    it = new TransformingRDFIt<>(valueClass, QUAD, it, quadLifter::lift);
                }
            }
        } else {
            it = itParser.parse(in);
            if (!valueClass.isAssignableFrom(it.valueClass()))
                it = new ConvertingRDFIt<>(valueClass, itElement, it, conversionMgr);
        }

        assert it.itElement() == itElement;
        assert it.valueClass().equals(valueClass);
        return it;
    }

    private @Nonnull RDFIt<Object> parse2It(@Nonnull IterationElement itElement,
                                           @Nullable QuadLifter quadLifter,
                                           @Nonnull Class<?> valueClass, @Nonnull Object source) {
        RDFIt<Object> it;
        Class<?> tCls = quadLifter != null ? quadLifter.tripleType() : valueClass;
        ListenerParser cbParser = parserRegistry.getListenerParser(source, tCls, valueClass);
        if (cbParser == null)
            throw new NoParserException(source);

        ListenerRDFIt<Object> cbIt = new ListenerRDFIt<>(source, valueClass, itElement,
                quadLifter, conversionMgr);
        executor.execute(() -> {
            try {
                cbParser.parse(source, cbIt.getListener());
                cbIt.getListener().finish();
            } catch (InterruptParsingException ignored) {
            } catch (RDFItException e) {
                cbIt.addException(e);
            } catch (Throwable t) {
                cbIt.addException(new RDFItException(source, t));
            }
        });
        it = cbIt;
        return it;
    }

    @Override
    public @Nonnull <T> RDFIt<T> iterateTriples(@Nonnull Class<T> tripleClass,
                                                @Nonnull Object... sources) {
        //noinspection unchecked
        return (RDFIt<T>) iterateSources(TRIPLE, tripleClass, null,
                                         null, sources);
    }

    @Override
    public @Nonnull <T> RDFIt<T>
    iterateQuads(@Nonnull Class<T> quadClass, @Nonnull QuadLifter quadLifter, @Nonnull Object... sources) {
        //noinspection unchecked
        return (RDFIt<T>) iterateSources(QUAD, quadLifter.tripleType(), quadClass,
                                         quadLifter, sources);
    }

    @Override
    public @Nonnull <T> RDFIt<T> iterateQuads(@Nonnull Class<T> quadClass,
                                              @Nonnull Object... sources) {
        //noinspection unchecked
        return (RDFIt<T>) iterateSources(QUAD, null,
                                         quadClass, null, sources);
    }

    @Override
    public void parse(@Nonnull RDFListener<?,?> listener, @Nonnull Object... sources) {
        if (sources.length != 0) {
            for (Object s : sources) {
                if (s == null)
                    continue;
                try {
                    s = normalizerRegistry.normalize(s);
                    if (s instanceof SourcesIterator) {
                        for (SourcesIterator it = (SourcesIterator) s; it.hasNext(); )
                            parse(listener, it.next());
                        return;
                    } else if (s instanceof RDFItException) {
                        throw (RDFItException)s;
                    }
                    //noinspection unchecked
                    parseSource((RDFListener<Object, Object>) listener, s);
                } catch (InterruptParsingException e) {
                    break;
                } catch (Throwable t) {
                    RDFItException e = t instanceof RDFItException ? (RDFItException)t
                                     : new RDFItException("Unexpected exception parsing "+s, t);
                    // triple and quad conversion exceptions are notified inside parseSource
                    if (!listener.notifySourceError(e))
                        break; // stop parsing
                }
            }
        }
        try {
            listener.finish();
        } catch (Throwable t) {
            throw new RDFItException(listener+".finish() threw "+t.getClass().getSimpleName(), t);
        }
    }

    protected void parseSource(@Nonnull RDFListener<Object, Object> cb,
                               @Nonnull Object source) throws InterruptParsingException,
                                                              RDFItException {
        Class<?> cTT = cb.tripleType(), cQT = cb.quadType();
        ListenerParser cbP = parserRegistry.getListenerParser(source, cTT, cQT);
        if (cbP != null) {
            cb = ConvertingRDFListener.createIf(cb, cbP, conversionMgr);
            cbP.parse(source, cb);
        } else {
            ItParser itP = parserRegistry.getItParser(source, QUAD, cQT);
            if (itP == null) {
                itP = parserRegistry.getItParser(source, TRIPLE, cTT);
                if (itP == null)
                    throw new NoParserException(source);
            }
            IterationElement itElement = itP.itElement();
            boolean isTriple = itElement.isTriple();
            Class<?> offTriple =           isTriple ? itP.valueClass() : null;
            Class<?> offQuad   = itElement.isQuad() ? itP.valueClass() : null;
            cb = ConvertingRDFListener.createIf(cb, offTriple, offQuad, conversionMgr);

            try (RDFIt<Object> it = itP.parse(source);
                 ListenerFeeder feeder = new ListenerFeeder(cb, conversionMgr).setSource(source)) {
                while (it.hasNext()) {
                    Object next = it.next();
                    if (next == null) {
                        logger.warn("{}.parseSource(): Ignoring null from {}.next() (source: {})",
                                    this, it, source);
                        assert false; // blow up in debug runs
                        continue;
                    }
                    if (isTriple) feeder.feedTriple(next);
                    else          feeder.feedQuad(next);
                }
            } catch (Throwable t) {
                if (!cb.notifySourceError(RDFItException.wrap(source, t)))
                    throw new InterruptParsingException();
            } // InterruptParsingException is propagated
        }
    }

    @Override public void close() {
        if (this == INSTANCE) {
            logger.error("Calling close on shared singleton!");
            assert false; // blow up in development only
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                logger.warn("{}.close() will not wait non-terminating executor", this);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); //restore interrupt flag
        }
    }

    @Override public String toString() {
        if (this == INSTANCE)
            return "DefaultRDFItFactory.INSTANCE";
        return Utils.toString(this);
    }
}
