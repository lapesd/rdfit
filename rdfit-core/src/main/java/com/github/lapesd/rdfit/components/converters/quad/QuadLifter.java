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

import javax.annotation.Nonnull;

/**
 * Converts a triple instance of {@link #tripleType()} to a quad instance.
 */
public interface QuadLifter {
    /**
     * The required class for input triples
     * @return a Class instance.
     */
    @Nonnull Class<?> tripleType();

    /**
     * Converts a triple of the given {@link #tripleType()} into a quad.
     * @param triple the triple to lift
     * @return a quad containing the triple
     */
    @Nonnull Object lift(@Nonnull Object triple);
}
