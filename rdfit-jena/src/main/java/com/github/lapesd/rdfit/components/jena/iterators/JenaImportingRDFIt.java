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

package com.github.lapesd.rdfit.components.jena.iterators;

import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.components.converters.util.ConversionCache;
import com.github.lapesd.rdfit.iterator.BaseImportingRDFIt;
import com.github.lapesd.rdfit.iterator.RDFIt;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.OWL2;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.github.lapesd.rdfit.components.converters.util.ConversionPathSingletonCache.createCache;

public class JenaImportingRDFIt<T> extends BaseImportingRDFIt<T> {
    private static final @Nonnull Resource importsResource = OWL2.imports;
    private static final @Nonnull Node imports = OWL2.imports.asNode();

    private final @Nonnull ConversionCache cache;

    public JenaImportingRDFIt(@Nonnull RDFIt<T> delegate) {
        super(delegate);
        cache = createCache(DefaultConversionManager.get(), Triple.class);
    }

    public static @Nullable String getImportIRIJena(@Nullable Object jenaObject) {
        if (jenaObject == null)
            return null;
        else if (jenaObject instanceof Quad)
            jenaObject = ((Quad) jenaObject).asTriple();

        if (jenaObject instanceof Triple) {
            Triple triple = (Triple) jenaObject;
            if (triple.getPredicate().equals(imports)) {
                Node object = triple.getObject();
                if (object.isURI())
                    return object.getURI();
            }
        } else if (jenaObject instanceof Statement) {
            Statement stmt = (Statement) jenaObject;
            if (stmt.getPredicate().equals(importsResource)) {
                RDFNode object = stmt.getObject();
                if (object.isURIResource())
                    return object.asResource().getURI();
            }
        } else {
            throw new IllegalArgumentException("Expected a Quad, Triple or Statement, got a "+
                                               jenaObject.getClass()+" "+jenaObject);
        }
        return null;
    }

    @Override protected @Nullable String getImportIRI(@Nonnull Object tripleOrQuad) {
        if (!(tripleOrQuad instanceof Statement || tripleOrQuad instanceof Quad))
            tripleOrQuad = cache.convert(getSource(), tripleOrQuad);
        return getImportIRIJena(tripleOrQuad);
    }
}
