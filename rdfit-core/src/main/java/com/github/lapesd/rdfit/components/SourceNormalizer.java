package com.github.lapesd.rdfit.components;

import com.github.lapesd.rdfit.components.normalizers.SourceNormalizerRegistry;
import com.github.lapesd.rdfit.source.RDFInputStream;

import javax.annotation.Nonnull;
import java.util.Collection;

public interface SourceNormalizer {
    void attachTo(@Nonnull SourceNormalizerRegistry registry);

    /**
     * A collection of classes that the {@link #normalize(Object)} method will act upon.
     */
    @Nonnull Collection<Class<?>> acceptedClasses();

    /**
     * Provide a normalized replacement for the source object.
     *
     * Typical use case is normalizing String, and other objects into
     * {@link RDFInputStream} instances.
     *
     * @param source the source to be nomralized
     * @return a replacement or the unmodified source itself.
     */
    @Nonnull Object normalize(@Nonnull Object source);
}
