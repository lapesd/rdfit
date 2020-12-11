package com.github.com.alexishuf.rdfit;

import com.github.com.alexishuf.rdfit.callback.ConvertingRDFCallback;
import com.github.com.alexishuf.rdfit.callback.RDFCallback;
import com.github.com.alexishuf.rdfit.components.CallbackParser;
import com.github.com.alexishuf.rdfit.components.ItParser;
import com.github.com.alexishuf.rdfit.conversion.ConversionManager;
import com.github.com.alexishuf.rdfit.conversion.impl.DefaultConversionManager;
import com.github.com.alexishuf.rdfit.errors.InterruptParsingException;
import com.github.com.alexishuf.rdfit.errors.NoParserException;
import com.github.com.alexishuf.rdfit.errors.RDFItException;
import com.github.com.alexishuf.rdfit.iterator.*;
import com.github.com.alexishuf.rdfit.parsers.DefaultParserRegistry;
import com.github.com.alexishuf.rdfit.parsers.ParserRegistry;
import com.github.com.alexishuf.rdfit.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class DefaultRDFItFactory implements RDFItFactory {
    private static final Logger logger = LoggerFactory.getLogger(DefaultRDFItFactory.class);
    private static final @Nonnull DefaultRDFItFactory INSTANCE
            = new DefaultRDFItFactory(DefaultParserRegistry.get(), DefaultConversionManager.get());

    protected @Nonnull ParserRegistry parserRegistry;
    protected @Nonnull ConversionManager conversionManager;
    private final @Nonnull ThreadPoolExecutor executor;

    protected DefaultRDFItFactory(@Nonnull ParserRegistry parserRegistry,
                               @Nonnull ConversionManager conversionManager) {
        this.parserRegistry = parserRegistry;
        this.conversionManager = conversionManager;
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

    private @Nonnull RDFIt<Object> iterateSources(@Nonnull Class<? extends Object> tripleClass,
                                             @Nullable Class<?> quadClass,
                                             @Nullable Function<?, ?> triple2quad,
                                             @Nonnull Object... sources) {
        if (quadClass != null && triple2quad == null)
            throw new NullPointerException("null triple2quad with non-null quadClass");
        if (sources.length == 0) {
            return new EmptyRDFIt<>(tripleClass);
        } else if (sources.length > 1) {
            Iterator<Object> sIt = Arrays.asList(sources).iterator();
            return new FlatMapRDFIt<>(tripleClass, sIt,
                    s -> iterateSources(tripleClass, quadClass, triple2quad, s));
        }
        Object source = sources[0];
        RDFIt<Object> it;
        ItParser itParser = parserRegistry.getItParser(source);
        if (itParser != null) {
            it = itParser.parse(source);
        } else {
            CallbackParser cbParser = parserRegistry.getCallbackParser(source);
            if (cbParser == null)
                throw new NoParserException(source);
            CallbackRDFIt<Object> cbIt;
            cbIt = new CallbackRDFIt<>(source, tripleClass, quadClass, triple2quad);
            executor.execute(() -> {
                try {
                    cbParser.parse(source, cbIt.getCallback());
                } catch (InterruptParsingException ignored) {
                } catch (RDFItException e) {
                    cbIt.addException(e);
                } catch (RuntimeException e) {
                    cbIt.addException(new RDFItException(source, e));
                }
            });
            it = cbIt;
        }

        Class<?> valueClass = quadClass == null ? tripleClass : quadClass;
        if (!valueClass.isAssignableFrom(it.valueClass()))
            it = new ConvertingRDFIt<>(tripleClass, it, conversionManager);
        return it;
    }

    @Override
    public @Nonnull <T> RDFIt<T> iterateTriples(@Nonnull Class<T> tripleClass,
                                                @Nonnull Object... sources) {
        //noinspection unchecked
        return (RDFIt<T>) iterateSources(tripleClass, null, null, sources);
    }

    @Nonnull @Override
    public <T> RDFIt<T> iterateQuads(@Nonnull Class<T> quadClass, @Nonnull Class<?> tripleClass,
                                     @Nonnull Function<?, ?> triple2quad,
                                     @Nonnull Object... sources) {
        //noinspection unchecked
        return (RDFIt<T>) iterateSources(tripleClass, quadClass, triple2quad, sources);
    }

    @Override
    public void parse(@Nonnull RDFCallback callback, @Nonnull Object... sources) {
        if (sources.length != 0) {
            for (Object s : sources) {
                try {
                    callback.start(s);
                } catch (InterruptParsingException e) {
                    logger.warn("Unexpected InterruptParsingException from {}.start({}). " +
                                "Will stop parsing nevertheless", callback, sources);
                    break;
                } catch (Throwable t) {
                    RDFItException wrap = new RDFItException(callback+".start("+s+") threw", t);
                    if (!callback.notifySourceError(s, wrap))
                        break;
                }
                try {
                    parseSource(callback, s);
                } catch (InterruptParsingException e) {
                    break;
                } catch (Throwable t) {
                    RDFItException e = t instanceof RDFItException ? (RDFItException)t
                                     : new RDFItException("Unexpected exception parsing "+s, t);
                    // triple and quad conversion exceptions are notified inside parseSource
                    if (!callback.notifySourceError(s, e))
                        break; // stop parsing
                    else continue;
                }
                try {
                    callback.finish(s);
                } catch (InterruptParsingException e) {
                    break;
                } catch (Throwable e) {
                    RDFItException wrap = new RDFItException(callback+".finish("+s+") threw", e);
                    if (!callback.notifySourceError(s, wrap))
                        break;
                }
            }
        }
        try {
            callback.finish();
        } catch (Throwable t) {
            throw new RDFItException(callback+".finish() threw "+t.getClass().getSimpleName(), t);
        }
    }

    protected void parseSource(@Nonnull RDFCallback cb,
                               @Nonnull Object source) throws RDFItException {
        CallbackParser cbP = parserRegistry.getCallbackParser(source);
        if (cbP != null) {
            cb = ConvertingRDFCallback.createIf(cb, cbP, conversionManager);
            cbP.parse(source, cb);
        } else {
            ItParser itP = parserRegistry.getItParser(source);
            if (itP == null)
                throw new NoParserException(source);
            IterationElement itElement = itP.iterationElement();
            boolean isTriple = itElement.isTriple();
            Class<?> offTriple =           isTriple ? itP.valueClass() : null;
            Class<?> offQuad   = itElement.isQuad() ? itP.valueClass() : null;
            cb = ConvertingRDFCallback.createIf(cb, offTriple, offQuad, conversionManager);

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
            }// throw up InterruptParsingException and RDFItException
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
