/*
 * Copyright 2021 Alexis Armin Huf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.lapesd.rdfit.source.fixer;

import javax.annotation.Nonnull;

public interface FixerParser {
    /**
     * Clears internal state so that subsequent calls to {@link FixerParser#feedByte(int)}
     * are not affected by previous calls.
     *
     * @return <code>this</code>
     */
    @Nonnull FixerParser reset();

    /**
     * Signals the end of input for the entity being parsed/recovered. This should cause
     * buffering implementations to flush all fixed data to the output buffer.
     */
    void flush();

    /**
     * Feed a byte value into the state and return the state that should handle subsequent input.
     *
     * @param byteValue a byte read from input
     * @return the {@link FixerParser} that should receive the next input byte.
     */
    @Nonnull FixerParser feedByte(int byteValue);
}
