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

package com.github.lapesd.rdfit.components.parsers.impl.iterator;

import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.util.Utils;

import javax.annotation.Nonnull;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;

import static com.github.lapesd.rdfit.util.Utils.compactClass;
import static java.lang.String.format;

public class ArrayItParser extends BaseJavaItParser {

    public ArrayItParser(@Nonnull Class<?> valueClass, @Nonnull IterationElement iterationElement) {
        super(Array.newInstance(valueClass, 0).getClass(), valueClass, iterationElement);
    }

    @Override protected @Nonnull Iterator<?> createIterator(@Nonnull Object source) {
        //noinspection ArraysAsListWithZeroOrOneArgument
        return Arrays.asList(source).iterator();
    }

    @Override public @Nonnull String toString() {
        return format("%s{%s}", Utils.toString(this),
                                compactClass(acceptedClasses().iterator().next()));
    }
}
