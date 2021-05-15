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

package com.github.lapesd.rdfit.components.parsers.impl.iterator;

import com.github.lapesd.rdfit.components.parsers.BaseItParser;
import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.iterator.RDFIt;
import com.github.lapesd.rdfit.util.Utils;

import javax.annotation.Nonnull;
import java.util.Collections;

public class RDFItItParser extends BaseItParser {
    public RDFItItParser(@Nonnull Class<?> valueClass, @Nonnull IterationElement iterationElement) {
        super(Collections.singleton(RDFIt.class), valueClass, iterationElement);
    }

    @Override public @Nonnull <T> RDFIt<T> parse(@Nonnull Object source) {
        //noinspection unchecked
        RDFIt<T> it = (RDFIt<T>) source;
        assert valueClass().isAssignableFrom(it.valueClass());
        return it;
    }

    @Override public boolean canParse(@Nonnull Object source) {
        return super.canParse(source)
                && valueClass().isAssignableFrom(((RDFIt<?>)source).valueClass());
    }

    @Override public @Nonnull String toString() {
        return Utils.toString(this);
    }
}
