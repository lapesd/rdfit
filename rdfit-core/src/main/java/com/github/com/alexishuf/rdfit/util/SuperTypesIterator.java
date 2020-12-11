package com.github.com.alexishuf.rdfit.util;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator over all superclasses (depth-first) and over all interfaces (breadth-first)
 * of a given class
 *
 * The iterator can be reset (avoiding allocations).
 */
public class SuperTypesIterator implements Iterator<Class<?>> {
    private final @Nonnull ArrayDeque<Class<?>> queue = new ArrayDeque<>();

    public SuperTypesIterator() { }

    public SuperTypesIterator(@Nonnull Class<?> aClass) {
        reset(aClass);
    }

    public @Nonnull SuperTypesIterator reset(@Nonnull Class<?> leafClass) {
        queue.clear();
        for (Class<?> cls = leafClass; !cls.equals(Object.class); cls = cls.getSuperclass())
            queue.add(cls);
        return this;
    }

    @Override public boolean hasNext() {
        return !queue.isEmpty();
    }

    @Override public Class<?> next() {
        if (!hasNext()) throw new NoSuchElementException();
        Class<?> next = queue.remove();
        Collections.addAll(queue, next.getInterfaces());
        return next;
    }
}
