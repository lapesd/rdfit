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

package com.github.lapesd.rdfit.components.parsers.impl.listener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

/**
 * A {@link com.github.lapesd.rdfit.components.ListenerParser} over {@link Iterable}s
 */
public class IterableListenerParser extends BaseJavaListenerParser {
    public IterableListenerParser(@Nullable Class<?> tripleClass, @Nullable Class<?> quadClass) {
        super(Iterable.class, tripleClass, quadClass);
    }

    @Override protected @Nonnull Iterator<?> createIterator(@Nonnull Object source) {
        return ((Iterable<?>)source).iterator();
    }
}
