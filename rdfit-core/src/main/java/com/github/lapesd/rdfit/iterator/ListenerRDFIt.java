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

package com.github.lapesd.rdfit.iterator;

import com.github.lapesd.rdfit.SourceQueue;
import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.components.converters.quad.QuadLifter;
import com.github.lapesd.rdfit.components.converters.util.ConversionCache;
import com.github.lapesd.rdfit.errors.InconvertibleException;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.impl.ClosedSourceQueue;
import com.github.lapesd.rdfit.listener.RDFListener;
import com.github.lapesd.rdfit.listener.RDFListenerBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static com.github.lapesd.rdfit.components.converters.util.ConversionPathSingletonCache.createCache;
import static com.github.lapesd.rdfit.util.Utils.compactClass;

/**
 * An {@link RDFIt} backed by an {@link RDFListener} instance
 *
 * @param <T> the value type
 */
public class ListenerRDFIt<T> extends EagerRDFIt<T> {
    private static final Logger logger = LoggerFactory.getLogger(ListenerRDFIt.class);
    private final @Nonnull Listener listener;
    private boolean abort = false, finished = false;
    private final @Nonnull Object source;
    private RDFItException exception = null;
    private static final Object END = new Object();
    private final @Nonnull BlockingQueue<Object> queue = new ArrayBlockingQueue<>(1024);

    public ListenerRDFIt(@Nonnull Object source, @Nonnull Class<?> valueClass,
                     @Nonnull IterationElement itElement, @Nullable QuadLifter quadLifter,
                     @Nonnull ConversionManager convMgr) {
        this(source, valueClass, itElement, quadLifter, convMgr, new ClosedSourceQueue());
    }

    public ListenerRDFIt(@Nonnull Object source, @Nonnull Class<?> valueClass,
                         @Nonnull IterationElement itElement, @Nullable QuadLifter quadLifter,
                         @Nonnull ConversionManager convMgr,
                         @Nonnull SourceQueue sourceQueue) {
        super(valueClass, itElement, sourceQueue);
        this.source = source;
        listener = new Listener(quadLifter, convMgr);
    }

    @Override public @Nonnull Object getSource() {
        return source;
    }

    public void addException(@Nonnull RDFItException e) {
        synchronized (this) {
            if (exception == null) {
                exception = e;
            } else {
                boolean novel = !exception.equals(e);
                Throwable[] suppressed = exception.getSuppressed();
                for (int i = 0; novel && i < suppressed.length; i++)
                    novel = !e.equals(suppressed[i]);
                if (novel)
                    exception.addSuppressed(e);
            }
            finished = true;
            notifyAll();
        }
        try {
            queue.put(END);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }

    public @Nonnull RDFListener<?, ?> getListener() {
        return listener;
    }

    @Override protected @Nullable T advance() {
        try {
            Object object = queue.take();
            if (object == END) {
                queue.put(object); // stops a second advance() call from hanging
                synchronized (this) {
                    if (exception != null) throw exception;
                }
                return null; //exhausted
            }
            assert valueClass().isInstance(object);
            //noinspection unchecked
            return (T)object;
        } catch (InterruptedException e) {
            close();
            Thread.currentThread().interrupt();
            return null; //aborted
        }
    }

    @Override public void close() {
        synchronized (this) {
            abort = true;
            boolean interrupted = false;
            while (!finished) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            if (interrupted)
                Thread.currentThread().interrupt();
        }
        // parsing is stopped
    }

    /**
     * An {@link RDFListener} that feeds the {@link ListenerRDFIt}
     */
    protected class Listener extends RDFListenerBase<Object, Object> {
        private final @Nonnull ConversionCache lifterInputCache, triple2quadCache;
        private final @Nonnull ConversionCache quad2tripleCache, valueCache;
        private final @Nullable QuadLifter quadLifter;

        /**
         * Constructor
         *
         * @param quadLifter the lifter
         * @param conMgr {@link ConversionManager} to use for conversion
         */
        public Listener(@Nullable QuadLifter quadLifter, @Nonnull ConversionManager conMgr) {
            super(Object.class, Object.class);
            Class<?> lifterInput = quadLifter != null ? quadLifter.tripleType() : null;
            this.quadLifter = quadLifter;
            this.lifterInputCache = createCache(conMgr, lifterInput);
            this.quad2tripleCache = createCache(conMgr, valueClass);
            this.triple2quadCache = createCache(conMgr, valueClass);
            this.valueCache = createCache(conMgr, valueClass);
        }

        private void deliver(@Nonnull Object value) {
            if (abort) {
                abort = false;
                finish();
                throw new InterruptParsingException();
            }
            if (value != END && !valueClass.isInstance(value)) {
                String msg = value + " is not a " + compactClass(valueClass);
                addException(new RDFItException(source, msg));
                return;
            }
            try {
                queue.put(value);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override public void attachSourceQueue(@Nonnull SourceQueue queue) {
            ListenerRDFIt.this.sourceQueue = queue;
        }

        @Override public void triple(@Nonnull Object triple) {
            if (itElement.isQuad()) {
                if (quadLifter == null) {
                    deliver(triple2quadCache.convert(source, triple));
                } else {
                    Object converted = lifterInputCache.convert(source, triple);
                    Object quad = quadLifter.lift(converted);
                    deliver(quad);
                }
            } else {
                deliver(valueCache.convert(source, triple));
            }
        }

        @Override public void quad(@Nonnull String graph, @Nonnull Object triple) {
            if (itElement.isQuad()) {
                String msg = "quad(graph, "+triple+") called instead of quad version";
                addException(new RDFItException(source, msg));
            } else {
                deliver(valueCache.convert(source, triple));
            }
        }

        @Override public void quad(@Nonnull Object quad) {
            if (itElement.isTriple()) {
                deliver(quad2tripleCache.convert(source, quad));
            } else {
                deliver(valueCache.convert(source, quad));
            }
        }

        @Override
        public boolean notifyInconvertibleTriple(@Nonnull InconvertibleException e)
                throws InterruptParsingException {
            addException(e);
            throw new InterruptParsingException();

        }

        @Override
        public boolean notifyInconvertibleQuad(@Nonnull InconvertibleException e)
                throws InterruptParsingException {
            addException(e);
            throw new InterruptParsingException();
        }

        @Override
        public boolean notifySourceError(@Nonnull RDFItException e) {
            addException(e);
            return false;
        }

        @Override public void finish() {
            super.finish();
            synchronized (ListenerRDFIt.this) {
                finished = true;
                ListenerRDFIt.this.notifyAll();
            }
            try {
                queue.put(END);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
