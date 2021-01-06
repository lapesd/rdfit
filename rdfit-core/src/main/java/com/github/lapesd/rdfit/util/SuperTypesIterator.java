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

package com.github.lapesd.rdfit.util;

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

    public void reset(@Nonnull Class<?> leafClass) {
        queue.clear();
        for (Class<?> c = leafClass; c != null && !c.equals(Object.class); c = c.getSuperclass())
            queue.add(c);
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
