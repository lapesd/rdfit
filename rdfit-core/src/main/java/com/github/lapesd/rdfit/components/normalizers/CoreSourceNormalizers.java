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
