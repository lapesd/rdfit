package com.github.com.alexishuf.rdfit.iterator;

import com.github.com.alexishuf.rdfit.callback.RDFCallback;
import com.github.com.alexishuf.rdfit.callback.RDFCallbackBase;
import com.github.com.alexishuf.rdfit.errors.InconvertibleException;
import com.github.com.alexishuf.rdfit.errors.InterruptParsingException;
import com.github.com.alexishuf.rdfit.errors.RDFItException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Function;

public class CallbackRDFIt<T> extends EagerRDFIt<T> {
    private final @Nonnull RDFCallback callback;
    private boolean abort = false, finished = false;
    private RDFItException exception = null;
    private static final Object END = new Object();
    private final @Nonnull BlockingQueue<Object> queue = new ArrayBlockingQueue<>(1024);

    public CallbackRDFIt(@Nonnull Object source, @Nonnull Class<?> tripleClass,
                         @Nullable Class<?> quadClass, @Nullable Function<?, ?> triple2quad) {
        //noinspection unchecked
        super((Class<? extends T>) (quadClass == null ? tripleClass : quadClass));
        if (quadClass != null && triple2quad == null)
            throw new NullPointerException("triple2quad == null in quad-producing CallbackRDFIt");
        callback = new RDFCallbackBase(tripleClass, quadClass) {
            private void deliver(@Nonnull Object value) {
                if (abort) {
                    abort = false;
                    finish();
                    throw new InterruptParsingException();
                }
                if (value != END && !valueClass.isInstance(value)) {
                    RuntimeException ex;
                    ex = new IllegalArgumentException(value+" is not a "+valueClass);
                    addException(new RDFItException(source, ex));
                    throw ex;
                }
                try {
                    queue.put(value);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            @Override public <X> void triple(@Nonnull X triple) {
                deliver(triple);
            }

            @Override public <Q> void quad(@Nonnull Q quad) {
                if (quadClass == null) {
                    RuntimeException ex = new UnsupportedOperationException("quadClass == null");
                    addException(new RDFItException(source, ex));
                    throw ex;
                }
                deliver(quad);
            }

            @Override public <X> void quad(@Nonnull Object graph, @Nonnull X triple) {
                if (quadClass != null) {
                    RuntimeException ex = new UnsupportedOperationException("quadClass != null");
                    addException(new RDFItException(source, ex));
                    throw ex;
                }
                deliver(triple);
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
            public boolean notifySourceError(@Nonnull Object source, @Nonnull RDFItException e) {
                addException(e);
                return false;
            }

            @Override public void finish() {
                synchronized (CallbackRDFIt.this) {
                    finished = true;
                    CallbackRDFIt.this.notifyAll();
                }
                try {
                    queue.put(END);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
    }

    public void addException(@Nonnull RDFItException e) {
        synchronized (this) {
            if (exception == null) {
                exception = e;
            } else {
                boolean novel = true;
                for (Throwable t : exception.getSuppressed()) {
                    if (!(novel = !e.equals(t))) break;
                }
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

    public @Nonnull RDFCallback getCallback() {
        return callback;
    }

    @Override protected @Nullable T advance() {
        try {
            Object object = queue.take();
            synchronized (this) {
                if (exception != null) throw exception;
            }
            if (object == END)
                return null; //exhausted
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
        }
        // parsing is stopped
    }
}
