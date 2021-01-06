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

package com.github.lapesd.rdfit.components.converters;

import com.github.lapesd.rdfit.components.Converter;
import com.github.lapesd.rdfit.components.annotations.Accepts;
import com.github.lapesd.rdfit.components.annotations.Outputs;
import com.github.lapesd.rdfit.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

public abstract class BaseConverter implements Converter {
    protected static final Logger logger = LoggerFactory.getLogger(BaseConverter.class);
    protected final @Nonnull Collection<Class<?>> acceptedClasses;
    protected final @Nonnull Class<?> outputClass;
    protected @Nullable ConversionManager conversionManager;

    public BaseConverter(@Nonnull Collection<Class<?>> acceptedClasses,
                         @Nonnull Class<?> outputClass) {
        this.acceptedClasses = acceptedClasses;
        this.outputClass = outputClass;
    }

    public BaseConverter() {
        Accepts accepts = getClass().getAnnotation(Accepts.class);
        if (accepts == null)
            throw new UnsupportedOperationException("Default constructor requires @Accepts");
        //noinspection RedundantTypeArguments
        this.acceptedClasses = Arrays.<Class<?>>asList(accepts.value());
        Outputs outputs = getClass().getAnnotation(Outputs.class);
        if (outputs == null)
            throw new UnsupportedOperationException("Default constructor requires @Outputs");
        this.outputClass = outputs.value();
    }

    @Override public void attachTo(@Nonnull ConversionManager conversionManager) {
        if (this.conversionManager != null && !this.conversionManager.equals(conversionManager)) {
            logger.info("Changing attached ConversionManager of {} from {} to {}",
                        this, this.conversionManager, conversionManager);
        }
        this.conversionManager = conversionManager;
    }

    @Override public @Nonnull Collection<Class<?>> acceptedClasses() {
        return acceptedClasses;
    }

    @Override public @Nonnull Class<?> outputClass() {
        return outputClass;
    }

    @Override public boolean canConvert(@Nonnull Object input) {
        for (Class<?> cls : acceptedClasses) {
            if (cls.isInstance(input)) return true;
        }
        return false;
    }

    @Override public @Nonnull String toString() {
        return Utils.toString(this);
    }
}
