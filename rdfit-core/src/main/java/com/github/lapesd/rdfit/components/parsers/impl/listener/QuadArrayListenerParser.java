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
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;

/**
 * An {@link com.github.lapesd.rdfit.components.ListenerParser} over arrays that contains quads.
 */
public class QuadArrayListenerParser extends BaseJavaListenerParser {
    public QuadArrayListenerParser(@Nullable Class<?> memberClass,
                                   @Nonnull Class<?> tripleClass, @Nonnull Class<?> quadClass) {
        super(Array.newInstance(memberClass, 0).getClass(), tripleClass, quadClass);
    }

    public QuadArrayListenerParser(@Nullable Class<?> quadClass) {
        super(Array.newInstance(quadClass, 0).getClass(), null, quadClass);
    }

    @Override protected @Nonnull Iterator<?> createIterator(@Nonnull Object source) {
        return Arrays.asList((Object[])source).iterator();
    }
}
