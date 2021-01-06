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

package com.github.lapesd.rdfit.components.converters.impl;

import com.github.lapesd.rdfit.components.converters.ConversionFinder;
import com.github.lapesd.rdfit.components.converters.ConversionPath;

import javax.annotation.Nonnull;

public class TrivialConversionFinder implements ConversionFinder {
    boolean hasNext = true;

    @Override public @Nonnull ConversionPath getConversionPath() {
        return ConversionPath.EMPTY;
    }

    @Override public @Nonnull Object convert(@Nonnull Object input) {
        assert hasNext; // convert() should not be called after a false hasNext().
        hasNext = false;
        return input;
    }

    @Override public boolean hasNext() {
        return hasNext;
    }
}
