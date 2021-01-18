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

package com.github.lapesd.rdfit.components.rdf4j.listener;

import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.components.converters.util.ConversionCache;
import com.github.lapesd.rdfit.components.converters.util.ConversionPathSingletonCache;
import com.github.lapesd.rdfit.components.rdf4j.iterators.RDF4JImportingRDFIt;
import com.github.lapesd.rdfit.listener.BaseImportingRDFListener;
import com.github.lapesd.rdfit.listener.RDFListener;
import org.eclipse.rdf4j.model.Statement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RDF4JImportingRDFListener<T, Q> extends BaseImportingRDFListener<T, Q> {
    private final @Nonnull ConversionCache conversion;

    public RDF4JImportingRDFListener(@Nonnull RDFListener<?, ?> target) {
        super(target);
        conversion = ConversionPathSingletonCache.createCache(DefaultConversionManager.get(),
                                                              Statement.class);
    }

    @Override protected @Nullable String getTripleImportIRI(@Nonnull Object triple) {
        Statement stmt = (Statement) conversion.convert(source, triple);
        return RDF4JImportingRDFIt.getImportIRI(stmt);
    }

    @Override protected @Nullable String getQuadImportIRI(@Nonnull Object quad) {
        return getTripleImportIRI(quad);
    }
}
