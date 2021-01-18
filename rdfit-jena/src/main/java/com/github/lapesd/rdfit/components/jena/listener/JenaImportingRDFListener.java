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

package com.github.lapesd.rdfit.components.jena.listener;

import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.components.converters.util.ConversionCache;
import com.github.lapesd.rdfit.components.jena.iterators.JenaImportingRDFIt;
import com.github.lapesd.rdfit.listener.BaseImportingRDFListener;
import com.github.lapesd.rdfit.listener.RDFListener;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.Quad;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.github.lapesd.rdfit.components.converters.util.ConversionPathSingletonCache.createCache;

public class JenaImportingRDFListener<T, Q> extends BaseImportingRDFListener<T, Q> {
    private final @Nonnull ConversionCache tripleConversion, quad2tripleConversion;

    public JenaImportingRDFListener(@Nonnull RDFListener<T, Q> target) {
        super(target);
        tripleConversion = createCache(DefaultConversionManager.get(), Triple.class);
        quad2tripleConversion = createCache(DefaultConversionManager.get(), Triple.class);
    }

    @Override protected @Nullable String getTripleImportIRI(@Nonnull Object triple) {
        if (!(triple instanceof Statement || triple instanceof Quad))
            triple = tripleConversion.convert(source, triple);
        return JenaImportingRDFIt.getImportIRIJena(triple);
    }

    @Override protected @Nullable String getQuadImportIRI(@Nonnull Object quad) {
        return getTripleImportIRI(quad2tripleConversion.convert(source, quad));
    }
}
