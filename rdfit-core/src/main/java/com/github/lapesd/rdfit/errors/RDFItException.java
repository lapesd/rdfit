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

public class RDFItException extends RuntimeException {
    protected @Nonnull Object source;

    public static @Nonnull RDFItException wrap(@Nonnull Object source, @Nonnull Throwable t) {
        return t instanceof RDFItException ? (RDFItException) t : new RDFItException(source, t);
    }

    public RDFItException(@Nonnull Object source, @Nonnull String message) {
        super(message);
        this.source = source;
    }

    public RDFItException(@Nonnull Object source, @Nonnull String message,
                          @Nonnull Throwable cause) {
        super(message, cause);
        this.source = source;
    }

    public RDFItException(@Nonnull Object source, @Nonnull Throwable cause) {
        this(source, cause.getMessage(), cause);
    }

    public @Nonnull Object getSource() {
        return source;
    }
}
