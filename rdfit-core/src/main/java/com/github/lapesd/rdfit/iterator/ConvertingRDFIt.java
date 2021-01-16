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

package com.github.lapesd.rdfit.iterator;

import com.github.lapesd.rdfit.SourceQueue;
import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.components.converters.util.ConversionCache;
import com.github.lapesd.rdfit.components.converters.util.ConversionPathSingletonCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConvertingRDFIt<T> extends EagerRDFIt<T> {
    private static final Logger logger = LoggerFactory.getLogger(ConvertingRDFIt.class);

    private final @Nonnull RDFIt<?> source;
    private final @Nonnull ConversionCache conversionCache;

    public ConvertingRDFIt(@Nonnull Class<? extends T> valueClass,
                           @Nonnull IterationElement itElement, @Nonnull RDFIt<?> source,
                           @Nonnull ConversionManager conversionManager) {
        super(valueClass, itElement, source.getSourceQueue());
        this.source = source;
        this.conversionCache = new ConversionPathSingletonCache(conversionManager, valueClass);
    }

    @Override public @Nonnull SourceQueue getSourceQueue() {
        return source.getSourceQueue();
    }

    @Override public @Nonnull Object getSource() {
        return source.getSource();
    }

    @Override protected @Nullable T advance() {
        while (source.hasNext()) {
            Object in = source.next();
            if (in == null) {
                logger.warn("{}.advance() ignoring null from {}.next()", this, source);
                assert false;
                continue;
            }
            //noinspection unchecked
            return (T)conversionCache.convert(source, in);
        }
        return null; //exhausted
    }

    @Override public void close() {
        source.close();
        super.close();
    }
}
