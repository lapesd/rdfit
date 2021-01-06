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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

public class PlainRDFIt<T> extends EagerRDFIt<T> {
    private static final @Nonnull Logger logger = LoggerFactory.getLogger(PlainRDFIt.class);
    private final @Nonnull Iterator<?> iterator;
    private final @Nonnull Object source;

    public PlainRDFIt(@Nonnull Class<?> valueClass, @Nonnull IterationElement itElement,
                      @Nonnull Iterator<?> iterator, @Nonnull Object source) {
        super(valueClass, itElement);
        this.iterator = iterator;
        this.source = source;
    }

    @Override public @Nonnull Object getSource() {
        return source;
    }

    @Override protected @Nullable T advance() {
        while (iterator.hasNext()) {
            @SuppressWarnings("unchecked") T next = (T)iterator.next();
            if (next == null) {
                logger.warn("Ignoring {} will ignore {}.next()=null", this, iterator);
                assert false;
            } else {
                return next;
            }
        }
        return null; //exhausted
    }

}
