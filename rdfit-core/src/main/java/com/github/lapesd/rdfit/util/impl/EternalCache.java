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

package com.github.lapesd.rdfit.util.impl;

import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.util.URLCache;
import com.github.lapesd.rdfit.util.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class EternalCache implements URLCache {
    private static EternalCache INSTANCE = null;
    private final @Nonnull Map<String, Supplier<RDFInputStream>> map = new HashMap<>();

    public static @Nonnull EternalCache getDefault() {
        if (INSTANCE == null) {
            EternalCache cache = new EternalCache();
            cache.putResource("http://www.w3.org/1999/02/22-rdf-syntax-ns", "../../rdf.ttl");
            cache.putResource("http://www.w3.org/2000/01/rdf-schema", "../../rdf-schema.ttl");
            cache.putResource("http://www.w3.org/2002/07/owl", "../../owl.ttl");
            cache.putResource("http://xmlns.com/foaf/0.1/", "../../foaf.rdf");
            cache.putResource("http://www.w3.org/2006/time", "../../time.ttl");
            cache.putResource("http://www.w3.org/ns/prov", "../../prov-o.ttl");
            cache.putResource("http://www.w3.org/ns/prov-o", "../../prov-o.ttl");
            cache.putResource("http://www.w3.org/2004/02/skos/core", "../../skos.rdf");
            cache.putResource("http://purl.org/dc/elements/1.1/", "../../dcelements.ttl");
            cache.putResource("http://purl.org/dc/dcam/", "../../dcam.ttl");
            cache.putResource("http://purl.org/dc/dcmitype/", "../../dctype.ttl");
            cache.putResource("http://purl.org/dc/terms/", "../../dcterms.ttl");
            INSTANCE = cache;
        }
        return INSTANCE;
    }

    protected void putResource(@Nonnull String iri, @Nonnull String resourcePath) {
        try {
            put(new URL(iri), new RDFBlob(EternalCache.class, resourcePath, iri));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Bad IRI "+iri, e);
        }
    }

    @Override public synchronized
    @Nullable Supplier<RDFInputStream> put(@Nonnull URL url,
                                           @Nonnull Supplier<RDFInputStream> supplier) {
        return map.put(Utils.toCacheKey(url), supplier);
    }

    @Override public synchronized boolean putIfAbsent(@Nonnull URL url,
                                                      @Nonnull Supplier<RDFInputStream> supplier) {
        return map.putIfAbsent(Utils.toCacheKey(url), supplier) == null;
    }

    @Override public synchronized @Nullable Supplier<RDFInputStream> get(@Nonnull URL url) {
        return map.getOrDefault(Utils.toCacheKey(url), null);
    }
}
