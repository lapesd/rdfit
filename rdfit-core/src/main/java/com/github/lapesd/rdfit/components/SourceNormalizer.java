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

package com.github.lapesd.rdfit.components;

import com.github.lapesd.rdfit.components.normalizers.SourceNormalizerRegistry;
import com.github.lapesd.rdfit.source.RDFInputStream;

import javax.annotation.Nonnull;
import java.util.Collection;

public interface SourceNormalizer {
    void attachTo(@Nonnull SourceNormalizerRegistry registry);

    /**
     * A collection of classes that the {@link #normalize(Object)} method will act upon.
     *
     * @return collection of classes {@link #normalize(Object)} may have an effect on.
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
