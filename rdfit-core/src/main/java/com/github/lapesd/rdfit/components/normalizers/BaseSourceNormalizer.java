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

import com.github.lapesd.rdfit.components.SourceNormalizer;
import com.github.lapesd.rdfit.components.annotations.Accepts;
import com.github.lapesd.rdfit.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

/**
 * Shared implementation for {@link SourceNormalizer}
 */
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
