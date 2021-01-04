package com.github.lapesd.rdfit.components.normalizers;

import com.github.lapesd.rdfit.components.SourceNormalizer;
import com.github.lapesd.rdfit.components.annotations.Accepts;
import com.github.lapesd.rdfit.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

public abstract class BaseSourceNormalizer implements SourceNormalizer {
    private static final Logger logger = LoggerFactory.getLogger(BaseSourceNormalizer.class);
    private final @Nonnull Collection<Class<?>> acceptedClasses;
    protected @Nullable SourceNormalizerRegistry registry;

    public BaseSourceNormalizer(@Nonnull Collection<Class<?>> acceptedClasses) {
        this.acceptedClasses = acceptedClasses;
    }

    public BaseSourceNormalizer() {
        Accepts accepts = getClass().getAnnotation(Accepts.class);
        if (accepts == null)
            throw new UnsupportedOperationException("Default constructor requires @Accepts");
        //noinspection RedundantTypeArguments
        this.acceptedClasses = Arrays.<Class<?>>asList(accepts.value());
    }

    @Override public void attachTo(@Nonnull SourceNormalizerRegistry registry) {
        if (this.registry != null && this.registry != registry)
            logger.warn("Re-attaching {} from {} to {}", this, this.registry, registry);
        this.registry = registry;
    }

    @Override public @Nonnull Collection<Class<?>> acceptedClasses() {
        return acceptedClasses;
    }

    @Override public @Nonnull String toString() {
        return Utils.toString(this);
    }
}
