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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

public class TransformingRDFIt<T> extends EagerRDFIt<T> {
    private final @Nonnull Logger logger = LoggerFactory.getLogger(TransformingRDFIt.class);
    private final @Nonnull RDFIt<?> in;
    private final @Nonnull Function<Object, ? extends T> function;

    public <X> TransformingRDFIt(@Nonnull Class<? extends T> valueClass,
                                 @Nonnull IterationElement itElement, @Nonnull RDFIt<?> in,
                                 @Nonnull Function<?, ? extends T> function) {
        super(valueClass, itElement, in.getSourceQueue());
        this.in = in;
        //noinspection unchecked
        this.function = (Function<Object, ? extends T>) function;
    }

    @Override public @Nonnull Object getSource() {
        return in.getSource();
    }

    @Override public @Nonnull SourceQueue getSourceQueue() {
        return in.getSourceQueue();
    }

    @Override protected @Nullable T advance() {
        while (in.hasNext()) {
            Object next = in.next();
            T result = function.apply(next);
            if (result == null) {
                logger.warn("{}.advance(): transformer function {} returned null for input {}. " +
                            "Ignoring element", this, function, next);
                assert false; // likely a bug
            } else {
                return result;
            }
        }
        return null;
    }
}
