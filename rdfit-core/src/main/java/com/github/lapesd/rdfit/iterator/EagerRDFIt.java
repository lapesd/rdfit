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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.NoSuchElementException;

/**
 * An {@link RDFIt} base class that requires implementing a single method
 *
 * @param <T> the value class
 */
public abstract class EagerRDFIt<T> extends BaseRDFIt<T> {
    protected T value;

    public EagerRDFIt(@Nonnull Class<?> valueClass, @Nonnull IterationElement itElement,
                      @Nonnull SourceQueue sourceQueue) {
        super(valueClass, itElement, sourceQueue);
    }

    protected abstract @Nullable T advance();

    @Override public boolean hasNext() {
        if (value == null)
            value = advance();
        return value != null;
    }

    @Override public T next() {
        if (!hasNext()) throw new NoSuchElementException("!"+this+".hasNext()");
        T next = this.value;
        assert next != null;
        this.value = null;
        return next;
    }
}
