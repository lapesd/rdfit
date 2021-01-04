package com.github.lapesd.rdfit.components.normalizers;

import javax.annotation.Nonnull;

public class DefaultSourceNormalizerRegistryTest extends SourceNormalizerRegistryTestBase {
    @Override protected @Nonnull SourceNormalizerRegistry createRegistry() {
        return new DefaultSourceNormalizerRegistry();
    }
}