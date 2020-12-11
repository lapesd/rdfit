package com.github.com.alexishuf.rdfit.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public abstract class TypeDispatcher<T> {
    private final @Nonnull Map<Class<?>, ArrayDeque<T>> map = new HashMap<>();
    private final @Nonnull SuperTypesIterator it = new SuperTypesIterator();

    public synchronized void add(@Nonnull Class<?> leafClass, @Nonnull T value) {
        it.reset(leafClass);
        while (it.hasNext()) {
            ArrayDeque<T> deque = map.computeIfAbsent(it.next(), k -> new ArrayDeque<>());
            if (!deque.contains(value))
                deque.addFirst(value);
        }
    }

    public synchronized void remove(@Nonnull T handler) {
        for (Collection<T> collection : map.values())
            collection.remove(handler);
    }

    public synchronized void removeIf(@Nonnull Predicate<? super T> predicate) {
        for (Collection<T> collection : map.values())
            collection.removeIf(predicate);
    }

    protected abstract boolean accepts(@Nonnull T handler, @Nonnull Object instance);

    public @Nonnull Iterator<T> get(@Nonnull Class<?> type) {
        return map.get(type).iterator();
    }

    public @Nonnull Iterator<T> get(@Nonnull Object instance) {
        return new Iterator<T>() {
            private final @Nonnull Iterator<T> it = map.get(instance.getClass()).iterator();
            private @Nullable T next;

            @Override public boolean hasNext() {
                while (it.hasNext()) {
                    T next = it.next();
                    if (accepts(next, instance))
                        this.next = next;
                }
                return this.next != null;
            }

            @Override public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                T next = this.next;
                this.next = null;
                return next;
            }
        };
    }
}
