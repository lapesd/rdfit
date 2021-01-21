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

package com.github.lapesd.rdfit.components.converters.quad;

import com.github.lapesd.rdfit.util.Utils;

import javax.annotation.Nonnull;

/**
 * Default implementation for some methods in {@link QuadLifter}
 */
public abstract class QuadLifterBase implements QuadLifter {
    protected final @Nonnull Class<?> tripleType;

    public QuadLifterBase(@Nonnull Class<?> tripleType) {
        this.tripleType = tripleType;
    }

    @Override public @Nonnull Class<?> tripleType() {
        return tripleType;
    }

    @Override public @Nonnull String toString() {
        return Utils.genericToString(this, tripleType());
    }
}
