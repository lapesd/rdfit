package com.github.lapesd.rdfit.components.converters.impl;

import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.components.converters.ConversionManagerTestBase;

import javax.annotation.Nonnull;

public class DefaultConversionManagerTest extends ConversionManagerTestBase {
    @Override protected @Nonnull ConversionManager createManager() {
        return new DefaultConversionManager();
    }
}