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

import javax.annotation.Nonnull;
import java.util.Iterator;

/**
 * A closeable iterator over triple-representing objects
 * @param <T> Value type of the iterator
 */
public interface RDFIt<T> extends Iterator<T>, AutoCloseable {
    /**
     * Type of the triples (or quads) returned by {@link #next()}
     *
     * @return A {@link Class} object for which results of {@link #next()}
     *         satisfy {@link Class#isInstance(Object)}
     */
    @Nonnull Class<? extends T> valueClass();

    /**
     * Whether this iterator is iterating triples or quads
     *
     * @return non-null {@link IterationElement}.
     */
    @Nonnull IterationElement itElement();


    /**
     * Get the source {@link Object} of the last value returned by {@link #next()}.
     *
     * @return current source object for the data in this iterator, or {@link NoSource#INSTANCE}
     *         if {@link #next()} has not yet been called or {@link #hasNext()} never
     *         returned true.
     */
    @Nonnull Object getSource();

    /**
     * Close the iterator, releasing any resources held by the instance.
     *
     * Calling {@link #hasNext()} or {@link #next()} is an invalid operation. Implementations,
     * however, are not required to throw {@link IllegalStateException} on such calls (the
     * implementations in the rdfit-* modules do).
     *
     * Generally, implementations should avoid throwing {@link RuntimeException} from this method.
     * Parse errors should be thrown as {@link RDFItException} from {@link #hasNext()}
     * or {@link #next()}.
     */
    @Override void close();
}
