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

package com.github.lapesd.rdfit.components.hdt.listeners;

import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.components.converters.util.ConversionCache;
import com.github.lapesd.rdfit.components.converters.util.ConversionPathSingletonCache;
import com.github.lapesd.rdfit.components.hdt.iterator.HDTImportingRDFIt;
import com.github.lapesd.rdfit.listener.BaseImportingRDFListener;
import com.github.lapesd.rdfit.listener.RDFListener;
import org.rdfhdt.hdt.triples.TripleString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HDTImportingRDFListener<T, Q> extends BaseImportingRDFListener<T, Q> {
    private final ConversionCache tripleConversion, quadConversion;

    public HDTImportingRDFListener(@Nonnull RDFListener<?, ?> target) {
        super(target);
        DefaultConversionManager mgr = DefaultConversionManager.get();
        tripleConversion = ConversionPathSingletonCache.createCache(mgr, TripleString.class);
        quadConversion = ConversionPathSingletonCache.createCache(mgr, TripleString.class);
    }

    @Override protected @Nullable String getTripleImportIRI(@Nonnull Object triple) {
        TripleString ts = (TripleString) tripleConversion.convert(source, triple);
        return HDTImportingRDFIt.getImportIRI(ts);
    }

    @Override protected @Nullable String getQuadImportIRI(@Nonnull Object quad) {
        return getTripleImportIRI(quadConversion.convert(source, quad));
    }
}
