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

package com.github.lapesd.rdfit.listener;

import com.github.lapesd.rdfit.SourceQueue;
import com.github.lapesd.rdfit.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;

public abstract class BaseImportingRDFListener<T, Q> extends DelegatingRDFListener<T, Q> {
    private static final Logger logger = LoggerFactory.getLogger(BaseImportingRDFListener.class);
    private final @Nonnull HashSet<String> visited = new HashSet<>();

    public BaseImportingRDFListener(@Nonnull RDFListener<?, ?> target) {
        super(target);
    }

    protected abstract @Nullable String getTripleImportIRI(@Nonnull Object triple);
    protected abstract @Nullable String getQuadImportIRI(@Nonnull Object quad);

    protected void queueImport(@Nonnull String iri) {
        URL url = null;
        try {
            URI uri = Utils.createURIOrFix(iri);
            if (uri.isAbsolute()) {
                if (baseIRI != null) {
                    try {
                        URI baseURI = Utils.createURIOrFix(baseIRI);
                        uri = uri.resolve(baseURI);
                    } catch (URISyntaxException e) {
                        logger.error("baseIRI {} is not a valid URI {}. " +
                                     "Will not resolve relative import IRI {}", baseIRI, e, iri);
                    }
                }
            }
            if (uri.isAbsolute()) {
                url = uri.toURL();
                String key = Utils.toCacheKey(url);
                if (!visited.add(key))
                    return;
            } else {
                if (!visited.add(iri))
                    return;
            }
        } catch (URISyntaxException|MalformedURLException e) {
            String what = e instanceof URISyntaxException ? "URI" : "URL";
            logger.error("{} is not an valid {}. Will enqueue, but loading will fail", iri, what);
            if (!visited.add(iri))
                return;
        }
        if (sourceQueue == null)
            logger.error("Skipping import of {}: sourceQueue is null", iri);
        else if (sourceQueue.isClosed())
            logger.error("Skipping import of {}: sourceQueue {} is closed", iri, sourceQueue);
        else
            sourceQueue.add(SourceQueue.When.Soon, url == null ? iri : url);
    }

    @Override public void triple(@Nonnull T triple) {
        String iri = getTripleImportIRI(triple);
        if (iri != null)
            queueImport(iri);
        super.triple(triple);
    }

    @Override public void quad(@Nonnull Q quad) {
        String iri = getTripleImportIRI(quad);
        if (iri != null)
            queueImport(iri);
        super.quad(quad);
    }

    @Override public void quad(@Nonnull String graph, @Nonnull T triple) {
        String iri = getTripleImportIRI(triple);
        if (iri != null)
            queueImport(iri);
        super.quad(graph, triple);
    }
}
