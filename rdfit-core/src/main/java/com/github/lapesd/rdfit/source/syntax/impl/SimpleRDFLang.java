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

package com.github.lapesd.rdfit.source.syntax.impl;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

public class SimpleRDFLang implements RDFLang {
    private final @Nonnull String name;
    private final @Nonnull String contentType;
    private final @Nonnull LinkedHashSet<String> extensions;
    private final boolean binary;

    public SimpleRDFLang(@Nonnull String name, @Nonnull List<String> extensions,
                         @Nonnull String contentType, boolean isBinary) {
        this.name = name;
        this.extensions = new LinkedHashSet<>(extensions);
        this.contentType = contentType;
        this.binary = isBinary;
    }

    @Override public @Nonnull String name() {
        return name;
    }

    @Override public @Nonnull String getMainExtension() {
        return extensions.iterator().next();
    }

    @Override public @Nonnull Collection<String> getExtensions() {
        return extensions;
    }

    @Override public @Nonnull String getContentType() {
        return contentType;
    }

    @Override public boolean isBinary() {
        return binary;
    }

    @Override public @Nonnull String toString() {
        return name();
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleRDFLang)) return false;
        SimpleRDFLang that = (SimpleRDFLang) o;
        return name.equals(that.name) && getExtensions().equals(that.getExtensions());
    }

    @Override public int hashCode() {
        return Objects.hash(name, getExtensions());
    }
}
