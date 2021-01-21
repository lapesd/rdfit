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

import javax.annotation.Nonnull;

/**
 * Whether triples or quads are being iterated
 */
public enum IterationElement {
    TRIPLE,
    QUAD;

    public boolean isTriple() { return this == TRIPLE; }
    public boolean   isQuad() { return this == QUAD  ; }

    public @Nonnull IterationElement toggle() {
        return this == TRIPLE ? QUAD : TRIPLE;
    }
}
