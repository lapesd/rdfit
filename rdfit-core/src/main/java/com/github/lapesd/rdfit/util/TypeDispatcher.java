package com.github.lapesd.rdfit.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;

public abstract class TypeDispatcher<T> {
    private final @Nonnull Map<Class<?>, Collection<T>> map = new HashMap<>();
    private final @Nonnull Set<T> set = new HashSet<>();

    public synchronized void add(@Nonnull Class<?> leafClass, @Nonnull T value) {
        ArrayDeque<T> q = (ArrayDeque<T>) map.computeIfAbsent(leafClass, k -> new ArrayDeque<>());
        if (!q.contains(value))
            q.addFirst(value);
        set.add(value);
    }

    public synchronized void remove(@Nonnull T handler) {
        for (Collection<T> collection : map.values())
            collection.remove(handler);
        set.remove(handler);
    }

    public synchronized void removeIf(@Nonnull Predicate<? super T> predicate) {
        for (Collection<T> collection : map.values()) {
            for (Iterator<T> it = collection.iterator(); it.hasNext(); ){
                T handler = it.next();
                if (predicate.test(handler)) {
                    it.remove();
                    set.remove(handler);
                }
            }
        }
    }

    public synchronized @Nonnull Set<T> getAll() {
        return new HashSet<>(set);
    }

    protected abstract boolean accepts(@Nonnull T handler, @Nonnull Object instance);

    public @Nonnull Iterator<T> get(@Nonnull Class<?> type) {
        return map.getOrDefault(type, emptyList()).iterator();
    }

    public @Nonnull Iterator<T> get(@Nonnull Object instance) {
        return new Iterator<T>() {
            private final @Nonnull SuperTypesIterator stIt
                    = new SuperTypesIterator(instance.getClass());
            private @Nonnull Iterator<T> it = Collections.emptyIterator();
            private @Nullable T next;

            @Override public boolean hasNext() {
                while (this.next == null && (it.hasNext() || stIt.hasNext()) ) {
                    while (!it.hasNext() && stIt.hasNext())
                        it = get(stIt.next());
                    if (it.hasNext()) {
                        T next = it.next();
                        if (accepts(next, instance))
                            this.next = next;
                    }
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
