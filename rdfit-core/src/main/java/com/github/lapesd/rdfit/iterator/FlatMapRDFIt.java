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

import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.util.NoSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.function.Function;

public class FlatMapRDFIt<T> extends EagerRDFIt<T> {
    private static final Logger logger = LoggerFactory.getLogger(FlatMapRDFIt.class);
    private final @Nonnull Iterator<?> inputIt;
    private final @Nonnull Function<Object, RDFIt<T>> function;
    private @Nullable RDFIt<T> currentIt = null;

    public FlatMapRDFIt(@Nonnull Class<? extends T> valueClass, @Nonnull IterationElement itElement,
                        @Nonnull Iterator<?> inputIt,
                        @Nonnull Function<?, RDFIt<T>> function) {
        super(valueClass, itElement);
        this.inputIt = inputIt;
        //noinspection unchecked
        this.function = (Function<Object, RDFIt<T>>) function;
    }

    @Override public @Nonnull Object getSource() {
        if (currentIt == null)
            return NoSource.INSTANCE;
        return currentIt.getSource();
    }

    @Override protected @Nullable T advance() {
        while ((currentIt == null || !currentIt.hasNext()) && inputIt.hasNext()) {
            Object input = inputIt.next();
            try {
                if (currentIt != null)
                    currentIt.close();
                currentIt = function.apply(input);
            } catch (RDFItException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new RDFItException(input, "Unexpected "+e.getClass().getSimpleName(), e);
            }
        }
        return currentIt != null && currentIt.hasNext() ? currentIt.next() : null;
    }

    @Override public void close() {
        if (currentIt != null)
            currentIt.close();
        if (inputIt instanceof AutoCloseable) {
            try {
                ((AutoCloseable) inputIt).close();
            } catch (Exception e) {
                logger.error("{}.close(): ignoring {}.close() exception", this, inputIt, e);
            }
        }
    }
}
