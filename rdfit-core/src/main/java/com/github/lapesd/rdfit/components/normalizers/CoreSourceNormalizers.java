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

package com.github.lapesd.rdfit.components.normalizers;

import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.components.SourceNormalizer;
import com.github.lapesd.rdfit.components.normalizers.impl.*;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;

public class CoreSourceNormalizers {
    private static final @Nonnull Set<Class<? extends SourceNormalizer>> CLASSES
            = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                    FileNormalizer.class, InputStreamNormalizer.class, ReaderNormalizer.class,
                    StringNormalizer.class, URINormalizer.class, URLNormalizer.class,
                    SupplierNormalizer.class, CallableNormalizer.class, ByteArrayNormalizer.class
    )));
    public static final @Nonnull List<Supplier<SourceNormalizer>> SUPPLIERS = Arrays.asList(
            FileNormalizer::new, InputStreamNormalizer::new, ReaderNormalizer::new,
            StringNormalizer::new, URINormalizer::new, URLNormalizer::new,
            SupplierNormalizer::new, CallableNormalizer::new, ByteArrayNormalizer::new
    );

    public static void registerAll(@Nonnull SourceNormalizerRegistry registry) {
        for (Supplier<SourceNormalizer> supplier : SUPPLIERS) registry.register(supplier.get());
    }
    public static void registerAll(@Nonnull RDFItFactory factory) {
        registerAll(factory.getNormalizerRegistry());
    }

    public static void unregisterAll(@Nonnull SourceNormalizerRegistry registry) {
        registry.unregisterIf(n -> CLASSES.contains(n.getClass()));
    }
    public static void unregisterAll(@Nonnull RDFItFactory factory) {
        unregisterAll(factory.getNormalizerRegistry());
    }

}
