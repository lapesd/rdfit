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

package com.github.lapesd.rdfit.listener;

import com.github.lapesd.rdfit.util.Utils;

import javax.annotation.Nonnull;

import static com.github.lapesd.rdfit.util.Utils.compactClass;

/**
 * An {@link RDFListener} for triples
 * @param <T> the triple representation class
 */
public abstract class TripleListenerBase<T> extends RDFListenerBase<T, Void> {
    public TripleListenerBase(@Nonnull Class<T> tripleType) {
        super(tripleType);
    }

    @Override public void quad(@Nonnull Void quad) {
        throw new UnsupportedOperationException("This listener ("+this+") has quadType()==null. " +
                                                "quad(String, Object) should've been called.");
    }

    @Override public @Nonnull String toString() {
        return String.format("%s<T=%s>", Utils.toString(this), compactClass(tripleType()));
    }
}
