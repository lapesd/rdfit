package com.github.lapesd.rdfit.components.parsers;

import javax.annotation.Nonnull;

public class DefaultParserRegistryTest extends ParserRegistryTestBase {
    @Override protected @Nonnull ParserRegistry createRegistry() {
        return new DefaultParserRegistry();
    }
}