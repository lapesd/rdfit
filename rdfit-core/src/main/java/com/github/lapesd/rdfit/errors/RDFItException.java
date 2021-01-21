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

package com.github.lapesd.rdfit.errors;

import javax.annotation.Nonnull;

/**
 * An error processing RDF data
 */
public class RDFItException extends RuntimeException {
    protected @Nonnull Object source;

    /**
     * Wraps another exception as an {@link RDFItException} if not already one
     *
     * @param source the source where the error occurred
     * @param t the exception to wrap
     * @return an {@link RDFItException} instance
     */
    public static @Nonnull RDFItException wrap(@Nonnull Object source, @Nonnull Throwable t) {
        return t instanceof RDFItException ? (RDFItException) t : new RDFItException(source, t);
    }

    /**
     * Creates a new standalone {@link RDFItException}
     * @param source the source that caused the error
     * @param message a description
     */
    public RDFItException(@Nonnull Object source, @Nonnull String message) {
        super(message);
        this.source = source;
    }

    /**
     * Create a {@link RDFItException} that wraps another {@link Throwable}
     *
     * @param source the affected RDF source
     * @param message a description
     * @param cause a wrapped {@link Throwable}
     */
    public RDFItException(@Nonnull Object source, @Nonnull String message,
                          @Nonnull Throwable cause) {
        super(message, cause);
        this.source = source;
    }

    /**
     * Wraps another {@link Throwable} without adding a message
     *
     * @param source the affected source
     * @param cause the original {@link Throwable}
     */
    public RDFItException(@Nonnull Object source, @Nonnull Throwable cause) {
        this(source, cause.getMessage(), cause);
    }

    /**
     * The affected source
     * @return the affected RDF source instance
     */
    public @Nonnull Object getSource() {
        return source;
    }
}
