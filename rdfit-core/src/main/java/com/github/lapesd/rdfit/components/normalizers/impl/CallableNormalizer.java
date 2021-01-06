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

package com.github.lapesd.rdfit.components.normalizers.impl;

import com.github.lapesd.rdfit.components.annotations.Accepts;
import com.github.lapesd.rdfit.components.normalizers.BaseSourceNormalizer;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

@Accepts(Callable.class)
public class CallableNormalizer extends BaseSourceNormalizer {
    @Override public @Nonnull Object normalize(@Nonnull Object source) {
        if (!(source instanceof Callable))
            return source;
        try {
            return ((Callable<?>) source).call();
        } catch (InterruptParsingException | RDFItException e) {
            throw e;
        } catch (Throwable e) {
            throw new RDFItException(source, "Callable.call() failed:", e);
        }
    }
}
