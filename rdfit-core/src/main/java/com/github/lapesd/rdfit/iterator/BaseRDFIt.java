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
import com.github.lapesd.rdfit.util.Utils;

import javax.annotation.Nonnull;

public abstract class BaseRDFIt<T> implements RDFIt<T> {
    protected final @Nonnull Class<?> valueClass;
    protected final @Nonnull IterationElement itElement;
    protected @Nonnull SourceQueue sourceQueue;
    protected boolean closed = false;

    protected BaseRDFIt(@Nonnull Class<?> valueClass, @Nonnull IterationElement itElement,
                        @Nonnull SourceQueue sourceQueue) {
        this.valueClass = valueClass;
        this.itElement = itElement;
        this.sourceQueue = sourceQueue;
    }

    @Override public @Nonnull Class<? extends T> valueClass() {
        //noinspection unchecked
        return (Class<? extends T>) valueClass;
    }

    @Override public @Nonnull IterationElement itElement() {
        return itElement;
    }

    @Override public @Nonnull SourceQueue getSourceQueue() {
        return sourceQueue;
    }

    @Override public String toString() {
        return Utils.genericToString(this, valueClass());
    }

    @Override public void close() {
        closed = true;
    }
}
