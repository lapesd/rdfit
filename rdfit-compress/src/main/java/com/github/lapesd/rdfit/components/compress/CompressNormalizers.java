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

package com.github.lapesd.rdfit.components.compress;

import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.components.SourceNormalizer;
import com.github.lapesd.rdfit.components.compress.normalizers.CompressNormalizer;
import com.github.lapesd.rdfit.components.normalizers.SourceNormalizerRegistry;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;

public class CompressNormalizers {
    private static final Set<Class<?>> CLASSES = singleton(CompressNormalizer.class);
    private static final List<Supplier<SourceNormalizer>> SUPPLIERS
            = singletonList(CompressNormalizer::new);

    public static void registerAll(@Nonnull SourceNormalizerRegistry registry) {
        for (Supplier<SourceNormalizer> s : SUPPLIERS) registry.register(s.get());
    }
    public static void registerAll(@Nonnull RDFItFactory factory) {
        registerAll(factory.getNormalizerRegistry());
    }

    public static void unregisterAll(@Nonnull SourceNormalizerRegistry registry) {
        registry.unregisterIf(p -> CLASSES.contains(p.getClass()));
    }
    public static void unregisterAll(@Nonnull RDFItFactory factory) {
        unregisterAll(factory.getNormalizerRegistry());
    }
}
