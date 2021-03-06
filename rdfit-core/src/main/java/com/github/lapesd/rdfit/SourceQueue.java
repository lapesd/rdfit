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

package com.github.lapesd.rdfit;

import com.github.lapesd.rdfit.components.SourceNormalizer;

import javax.annotation.Nonnull;

/**
 * A Queue of RDF sources for later processing by a {@link RDFItFactory} implementation.
 */
public interface SourceQueue extends AutoCloseable {
    enum When {
        /**
         * Schedule the load ASAP
         */
        Soon,
        /**
         * Schedule the load as late as possible. If two requests occur with this value,
         * the later request should be processed last.
         */
        Later
    }

    /**
     * Schedule a source to be eventually loaded.
     *
     * Implementations are not required to perform source deduplication.
     *
     * @param when when to schedule the load: ASAP or later.
     * @param source the source to be loaded
     */
    void add(@Nonnull When when, @Nonnull Object source);

    /**
     * Equivalent to {@link #add(When, Object)}, but keeps the original order of sources intact
     *
     * @param when whether to add sources to the beginning (Soon) or to the end (Later)
     * @param sources Iterable of sources to add
     */
    void addAll(@Nonnull When when, @Nonnull Iterable<?> sources);

    /**
     * Get the number of pending sources to be loaded in this queue.
     *
     * The number of actual sources loaded may vary as some scheduled sources may end up being
     * dismembered into numerous sources by {@link SourceNormalizer}s or may trigger calls
     * to {@link #add(When, Object)} during their parsing.
     *
     * @return number of pending sources
     */
    int length();

    /**
     * Indicates whether {@link #close()} has been called.
     *
      * @return true if {@link #close()} has been previously called, false otherwise.
     */
    boolean isClosed();

    /**
     * Mark the queue as closed, blocking further calls to {@link #add(When, Object)}.
     */
    @Override void close();
}
