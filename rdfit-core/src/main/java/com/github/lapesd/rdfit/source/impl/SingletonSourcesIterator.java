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

package com.github.lapesd.rdfit.source.impl;

import com.github.lapesd.rdfit.source.SourcesIterator;
import com.github.lapesd.rdfit.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.NoSuchElementException;

public class SingletonSourcesIterator implements SourcesIterator {
    private static final Logger logger = LoggerFactory.getLogger(SingletonSourcesIterator.class);
    private boolean has = true;
    private final @Nonnull Object source;

    public SingletonSourcesIterator(@Nonnull Object source) {
        this.source = source;
    }

    @Override public boolean hasNext() {
        return has;
    }

    @Override public Object next() {
        if (!hasNext()) throw new NoSuchElementException();
        has = false;
        return source;
    }

    @Override public @Nonnull String toString() {
        return Utils.toString(this)+"("+source+")";
    }

    @Override public void close() {
        if (has && source instanceof AutoCloseable) {
            try {
                ((AutoCloseable)source).close();
            } catch (Throwable t) {
                logger.error("Ignoring {} from {}.close() (called on close() of " +
                             "non-consumed SingletonSourcesIterator)", t.getClass(), source, t);
            }
        }
    }
}
