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
 * An {@link RDFListener} for quads
 * @param <Q> the quad representation class
 */
public abstract class QuadListenerBase<Q> extends RDFListenerBase<Void, Q> {
    public QuadListenerBase(@Nonnull Class<Q> quadType) {
        super(null, quadType);
    }

    @Override public void triple(@Nonnull Void triple) {
        throw new UnsupportedOperationException("This listener ("+this+") has tripleType()==null");
    }

    @Override public void quad(@Nonnull String graph, @Nonnull Void triple) {
        throw new UnsupportedOperationException("This listener ("+this+") has tripleType()==null. " +
                                                "quad(Object) should've been called instead");
    }

    @Override public @Nonnull String toString() {
        return String.format("%s<Q=%s>", Utils.toString(this), compactClass(quadType()));
    }
}
