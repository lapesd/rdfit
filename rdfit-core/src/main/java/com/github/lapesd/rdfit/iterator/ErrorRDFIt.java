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
import com.github.lapesd.rdfit.impl.ClosedSourceQueue;

import javax.annotation.Nonnull;
import java.util.NoSuchElementException;

public class ErrorRDFIt<T> extends BaseRDFIt<T> {
    private final @Nonnull Object source;
    private final @Nonnull RDFItException exception;

    public ErrorRDFIt(@Nonnull Class<?> valueClass, @Nonnull IterationElement itElement,
                      @Nonnull Object source, @Nonnull RDFItException exception) {
        super(valueClass, itElement, new ClosedSourceQueue());
        this.source = source;
        this.exception = exception;
    }

    public @Nonnull RDFItException getException() {
        return exception;
    }

    @Override public @Nonnull Object getSource() {
        return source;
    }

    @Override public boolean hasNext() {
        throw exception;
    }

    @Override public T next() {
        throw new NoSuchElementException();
    }

    @Override public @Nonnull String toString() {
        return "ErrorRDFIt("+exception.getClass()+", "+exception.getMessage()+")";
    }
}
