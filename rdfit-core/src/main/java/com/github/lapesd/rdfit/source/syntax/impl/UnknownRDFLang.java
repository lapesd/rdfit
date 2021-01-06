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
import java.util.Collections;
import java.util.List;

public class UnknownRDFLang implements RDFLang {
    private static final List<String> EXTENSIONS = Collections.singletonList("");

    @Override public @Nonnull String name() {
        return "UNKNOWN";
    }
    @Override public @Nonnull String getMainExtension() {
        return "";
    }

    @Override public @Nonnull String getMainSuffix() {
        return "";
    }

    @Override public @Nonnull Collection<String> getExtensions() {
        return EXTENSIONS;
    }

    @Override public boolean isBinary() {
        return false;
    }

    @Override public @Nonnull String getContentType() {
        return "*/*";
    }

    @Override public String toString() {
        return name();
    }

    @Override public boolean equals(Object obj) {
        return obj instanceof UnknownRDFLang;
    }

    @Override public int hashCode() {
        return 1;
    }
}
