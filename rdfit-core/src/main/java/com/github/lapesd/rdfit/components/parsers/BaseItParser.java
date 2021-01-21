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

package com.github.lapesd.rdfit.components.parsers;

import com.github.lapesd.rdfit.components.ItParser;
import com.github.lapesd.rdfit.iterator.IterationElement;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Default implementations for some methods in {@link BaseItParser}
 */
public abstract class BaseItParser extends BaseParser implements ItParser {
    protected final @Nonnull Class<?> valueClass;
    protected final @Nonnull IterationElement iterationElement;

    public BaseItParser(@Nonnull Collection<Class<?>> acceptedClasses, @Nonnull Class<?> valueClass,
                        @Nonnull IterationElement iterationElement) {
        super(acceptedClasses);
        this.valueClass = valueClass;
        this.iterationElement = iterationElement;
    }

    @Override public @Nonnull Class<?> valueClass() {
        return valueClass;
    }

    @Override public @Nonnull IterationElement itElement() {
        return iterationElement;
    }
}
