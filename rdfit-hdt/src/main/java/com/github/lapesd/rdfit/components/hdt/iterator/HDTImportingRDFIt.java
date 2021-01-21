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

package com.github.lapesd.rdfit.components.hdt.iterator;

import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.components.converters.util.ConversionCache;
import com.github.lapesd.rdfit.components.converters.util.ConversionPathSingletonCache;
import com.github.lapesd.rdfit.iterator.BaseImportingRDFIt;
import com.github.lapesd.rdfit.iterator.RDFIt;
import org.rdfhdt.hdt.triples.TripleString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Pattern;

/**
 * An {@link RDFIt} that processes owl:imports in {@link TripleString}s
 * @param <T> the triple type
 */
public class HDTImportingRDFIt<T> extends BaseImportingRDFIt<T> {
    private static final Pattern URL_RX = Pattern.compile("^([^:/]+://|file:)");
    private static final String imports = "http://www.w3.org/2002/07/owl#imports";

    private final ConversionCache conversion;

    public HDTImportingRDFIt(@Nonnull RDFIt<T> delegate) {
        super(delegate);
        DefaultConversionManager mgr = DefaultConversionManager.get();
        conversion = ConversionPathSingletonCache.createCache(mgr, TripleString.class);
    }

    /**
     * Return the IRI of an imported ontology or null the the triple is not an owl:imports triple.
     *
     * @param triple the input triple to evaluate
     * @return an IRI or null
     */
    public static @Nullable String getImportIRI(@Nonnull TripleString triple) {
        if (triple.getPredicate().toString().equals(imports)) {
            String object = triple.getObject().toString();
            if (URL_RX.matcher(object).find())
                return object;
        }
        return null;
    }

    /**
     * Calls {@link #getImportIRI(TripleString)}, converting tripleOrQuad if necessary.
     *
     * @param tripleOrQuad a triple or quad to evaluate
     * @return an ontology IRI or null
     */
    @Override protected @Nullable String getImportIRI(@Nonnull Object tripleOrQuad) {
        TripleString ts = (TripleString) conversion.convert(getSource(), tripleOrQuad);
        return getImportIRI(ts);
    }
}
