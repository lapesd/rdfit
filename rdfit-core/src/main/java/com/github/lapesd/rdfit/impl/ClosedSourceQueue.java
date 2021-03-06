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

package com.github.lapesd.rdfit.impl;

import javax.annotation.Nonnull;
import java.util.NoSuchElementException;

public class ClosedSourceQueue implements ConsumableSourceQueue {
    @Override public void add(@Nonnull When when, @Nonnull Object source) {
        throw new IllegalStateException("SourceQueue is closed");
    }

    @Override public void addAll(@Nonnull When when, @Nonnull Iterable<?> sources) {
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
