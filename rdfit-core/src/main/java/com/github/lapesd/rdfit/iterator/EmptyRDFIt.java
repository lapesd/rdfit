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

import com.github.lapesd.rdfit.impl.ClosedSourceQueue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An {@link RDFIt} whose {@link #hasNext()} method returns false
 *
 * @param <T> the value class
 */
public class EmptyRDFIt<T> extends EagerRDFIt<T> {
    private final @Nonnull Object source;

    public EmptyRDFIt(@Nonnull Class<? extends T> valueClass, @Nonnull IterationElement itElement,
                      @Nonnull Object source) {
        super(valueClass, itElement, new ClosedSourceQueue());
        this.source = source;
    }

    @Override public @Nonnull Object getSource() {
        return source;
    }

    @Override protected @Nullable T advance() {
        return null;
    }
}
