package com.github.lapesd.rdfit.components.converters.quad;

import javax.annotation.Nonnull;

/**
 * Converts a triple instance of {@link #tripleType()} to a quad instance.
 */
public interface QuadLifter {
    @Nonnull Class<?> tripleType();
    @Nonnull Object lift(@Nonnull Object triple);
}
