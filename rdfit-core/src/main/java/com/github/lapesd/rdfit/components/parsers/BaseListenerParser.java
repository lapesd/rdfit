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

package com.github.lapesd.rdfit.components.parsers;

import com.github.lapesd.rdfit.components.ListenerParser;
import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.listener.RDFListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public abstract class BaseListenerParser extends BaseParser implements ListenerParser {
    protected final @Nullable Class<?> tripleClass;
    protected final @Nullable Class<?> quadClass;

    public BaseListenerParser(@Nonnull Collection<Class<?>> acceptedClasses,
                              @Nullable Class<?> tripleClass) {
        this(acceptedClasses, tripleClass, null);
    }

    public BaseListenerParser(@Nonnull Collection<Class<?>> acceptedClasses,
                              @Nullable Class<?> tripleClass, @Nullable Class<?> quadClass) {
        super(acceptedClasses);
        this.tripleClass = tripleClass;
        this.quadClass = quadClass;
        if (tripleClass == null && quadClass == null)
            throw new IllegalArgumentException("Both tripleClass and quadClass are null");
    }

    protected @Nonnull ListenerFeeder createListenerFeeder(@Nonnull RDFListener<?,?> listener,
                                                           @Nonnull Object source) {
        ConversionManager mgr = parserRegistry == null ? DefaultConversionManager.get()
                                                       : parserRegistry.getConversionManager();
        return new ListenerFeeder(listener, mgr).setSource(source);
    }

    @Override public @Nullable Class<?> tripleType() {
        return tripleClass;
    }

    @Override public @Nullable Class<?> quadType() {
        return quadClass;
    }
}
