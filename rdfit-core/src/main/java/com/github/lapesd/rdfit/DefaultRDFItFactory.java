package com.github.lapesd.rdfit;

import com.github.lapesd.rdfit.components.ItParser;
import com.github.lapesd.rdfit.components.ListenerParser;
import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.components.converters.quad.QuadLifter;
import com.github.lapesd.rdfit.components.parsers.DefaultParserRegistry;
import com.github.lapesd.rdfit.components.parsers.ParserRegistry;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.NoParserException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.iterator.*;
import com.github.lapesd.rdfit.listener.ConvertingRDFListener;
import com.github.lapesd.rdfit.listener.RDFListener;
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

import static com.github.lapesd.rdfit.iterator.IterationElement.QUAD;
import static com.github.lapesd.rdfit.iterator.IterationElement.TRIPLE;

public class DefaultRDFItFactory implements RDFItFactory {
    private static final Logger logger = LoggerFactory.getLogger(DefaultRDFItFactory.class);
    private static final @Nonnull DefaultRDFItFactory INSTANCE
            = new DefaultRDFItFactory(DefaultParserRegistry.get(), DefaultConversionManager.get());

    protected @Nonnull ParserRegistry parserRegistry;
    protected @Nonnull ConversionManager conversionMgr;
    private final @Nonnull ThreadPoolExecutor executor;

    public DefaultRDFItFactory(@Nonnull ParserRegistry parserRegistry,
                                  @Nonnull ConversionManager conversionManager) {
        this.parserRegistry = parserRegistry;
        this.conversionMgr = conversionManager;
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
        return INSTANCE;
    }

    @Override public @Nonnull ConversionManager getConverterManager() {
        return conversionMgr;
    }

    @Override public @Nonnull ParserRegistry getParserRegistry() {
        return parserRegistry;
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
        RDFIt<Object> it;
        ItParser itParser = parserRegistry.getItParser(source);
        if (itParser != null) {
            if (itElement.isQuad() && itParser.iterationElement().isTriple()) {
                it = itParser.parse(source);
                if (quadLifter == null) {
                    // no quad lifter, try using a converter
                    it = new ConvertingRDFIt<>(valueClass, itElement, it, conversionMgr);
                } else {
                    if (!tripleClass.isAssignableFrom(itParser.valueClass()))
                        it = new ConvertingRDFIt<>(tripleClass, TRIPLE, it, conversionMgr);
                    it = new TransformingRDFIt<>(valueClass, QUAD, it, quadLifter::lift);
                }
            } else {
                it = itParser.parse(source);
                if (!valueClass.isAssignableFrom(it.valueClass()))
                    it = new ConvertingRDFIt<>(valueClass, itElement, it, conversionMgr);
            }
        } else {
            ListenerParser cbParser = parserRegistry.getCallbackParser(source);
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
                } catch (RuntimeException e) {
                    cbIt.addException(new RDFItException(source, e));
                }
            });
            it = cbIt;
        }

        assert it.itElement() == itElement;
        assert it.valueClass().equals(valueClass);
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
                    //noinspection unchecked
                    parseSource((RDFListener<Object, Object>) listener, s);
                } catch (InterruptParsingException e) {
                    break;
                } catch (Throwable t) {
                    RDFItException e = t instanceof RDFItException ? (RDFItException)t
                                     : new RDFItException("Unexpected exception parsing "+s, t);
                    // triple and quad conversion exceptions are notified inside parseSource
                    if (!listener.notifySourceError(s, e))
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
        ListenerParser cbP = parserRegistry.getCallbackParser(source);
        if (cbP != null) {
            cb = ConvertingRDFListener.createIf(cb, cbP, conversionMgr);
            cbP.parse(source, cb);
        } else {
            ItParser itP = parserRegistry.getItParser(source);
            if (itP == null)
                throw new NoParserException(source);
            IterationElement itElement = itP.iterationElement();
            boolean isTriple = itElement.isTriple();
            Class<?> offTriple =           isTriple ? itP.valueClass() : null;
            Class<?> offQuad   = itElement.isQuad() ? itP.valueClass() : null;
            cb = ConvertingRDFListener.createIf(cb, offTriple, offQuad, conversionMgr);

            cb.start(source);
            try (RDFIt<Object> it = itP.parse(source)) {
                while (it.hasNext()) {
                    Object next = it.next();
                    if (next == null) {
                        logger.warn("{}.parseSource(): Ignoring null from {}.next() (source: {})",
                                    this, it, source);
                        assert false; // blow up in debug runs
                        continue;
                    }
                    if (isTriple) cb.triple(next);
                    else          cb.quad(next);
                }
            } catch (RDFItException e) {
                if (!cb.notifySourceError(source, e))
                    throw new InterruptParsingException();
            } finally {
                cb.finish(source);
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
