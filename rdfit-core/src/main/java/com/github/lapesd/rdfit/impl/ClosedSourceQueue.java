package com.github.lapesd.rdfit.impl;

import javax.annotation.Nonnull;
import java.util.NoSuchElementException;

public class ClosedSourceQueue implements ConsumableSourceQueue {
    @Override public void add(@Nonnull When when, @Nonnull Object source) {
        throw new IllegalStateException("SourceQueue is closed");
    }

    @Override public int length() {
        return 0;
    }

    @Override public boolean isClosed() {
        return true;
    }

    @Override public void close() {
    }

    @Override public boolean hasNext() {
        return false;
    }

    @Override public @Nonnull Object next() {
        throw new NoSuchElementException();
    }
}
