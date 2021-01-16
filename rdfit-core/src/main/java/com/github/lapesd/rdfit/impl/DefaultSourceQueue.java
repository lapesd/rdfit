package com.github.lapesd.rdfit.impl;

import com.github.lapesd.rdfit.source.SourcesIterator;
import com.github.lapesd.rdfit.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.NoSuchElementException;

import static java.lang.String.format;

public class DefaultSourceQueue implements ConsumableSourceQueue {
    private static final Logger logger = LoggerFactory.getLogger(DefaultSourceQueue.class);
    private boolean closed = false;
    private final @Nonnull ArrayDeque<Object> deque = new ArrayDeque<>();
    private @Nullable SourcesIterator sourceIt;
    private @Nullable Object next;

    public DefaultSourceQueue(@Nonnull Object... sources) {
        for (Object source : sources) {
            if (source != null)
                deque.add(source);
        }
    }

    @Override public synchronized void add(@Nonnull When when, @Nonnull Object source) {
        logger.debug("{}.add({}, {})", this, when, source);
        if      (when == When.Soon ) deque.addFirst(source);
        else if (when == When.Later) deque.addLast(source);
        else                         throw new IllegalArgumentException("Unexpected when="+when);
    }

    @Override public synchronized int length() {
        return deque.size();
    }

    protected @Nullable Object advance() {
        while (true) {
            if (sourceIt != null) {
                if (sourceIt.hasNext())
                    return sourceIt.next();
                sourceIt = null;
            }
            if (deque.isEmpty())
                return null;
            Object next = deque.remove();
            if (next instanceof SourcesIterator)
                sourceIt = (SourcesIterator) next;
            else
                return next;
        }
    }

    @Override public synchronized boolean hasNext() {
        if (next == null)
            next = advance();
        return next != null;
    }

    @Override public synchronized @Nonnull Object next() {
        if (!hasNext())
            throw new NoSuchElementException();
        Object next = this.next;
        this.next = null;
        assert next != null;
        return next;
    }

    @Override public boolean isClosed() {
        return closed;
    }

    @Override public void close() {
        if (!closed)
            logger.debug("{}.close()", this);
        closed = true;
    }

    @Override public @Nonnull String toString() {
        return format("%s{length=%d, deque=%s}", Utils.toString(this), length(), deque);
    }
}
