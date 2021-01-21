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

package com.github.lapesd.rdfit.components.normalizers.impl;

import com.github.lapesd.rdfit.components.annotations.Accepts;
import com.github.lapesd.rdfit.components.normalizers.BaseSourceNormalizer;
import com.github.lapesd.rdfit.source.RDFFile;
import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.source.RDFInputStreamSupplier;
import com.github.lapesd.rdfit.source.syntax.RDFLangs;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import com.github.lapesd.rdfit.util.URLCache;
import com.github.lapesd.rdfit.util.Utils;
import com.github.lapesd.rdfit.util.impl.RDFBlob;
import com.github.lapesd.rdfit.util.impl.WeighedURLCache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import static java.util.Arrays.asList;

/**
 * Converts {@link URL} instances into {@link RDFInputStream} and caches fetched URLs
 */
@Accepts(URL.class)
public class URLNormalizer extends BaseSourceNormalizer {
    public static final @Nonnull Set<RDFLang> FALLBACK_LANGS
            = Collections.unmodifiableSet(new HashSet<>(asList(
                    RDFLangs.TRIG, RDFLangs.TTL, RDFLangs.NQ, RDFLangs.NT,
                    RDFLangs.JSONLD, RDFLangs.RDFXML)));
    public static final @Nonnull List<RDFLang> DEFAULT_ORDER = asList(
            RDFLangs.HDT,
            RDFLangs.TRIG,
            RDFLangs.TTL,
            RDFLangs.JSONLD,
            RDFLangs.NQ,
            RDFLangs.NT,
            RDFLangs.THRIFT,
            RDFLangs.RDFXML,
            RDFLangs.TRIX,
            RDFLangs.OWL,
            RDFLangs.RDFJSON,
            RDFLangs.RDFA,
            RDFLangs.BRF
    );
    private @Nullable Set<RDFLang> acceptStringSource;
    private @Nullable String acceptString;
    private final @Nonnull URLCache cache;
    private boolean cacheFiles = false;

    public URLNormalizer() {
        this(WeighedURLCache.getDefault());
    }

    public URLNormalizer(@Nonnull URLCache cache) {
        this.cache = cache;
    }

    public void setCacheFiles(boolean cacheFiles) {
        this.cacheFiles = cacheFiles;
    }

    /**
     * Orders the {@link RDFLang} instances in the given set from most preferred to least preferred.
     *
     * @param supportedLangs list of supported languages
     * @return re-ordered list with all languages in supportedLangs
     */
    protected @Nonnull List<RDFLang> reorderAccept(@Nonnull Set<RDFLang> supportedLangs) {
        List<RDFLang> list = new ArrayList<>();
        for (RDFLang lang : DEFAULT_ORDER) {
            if (supportedLangs.contains(lang))
                list.add(lang);
        }
        assert list.containsAll(supportedLangs);
        return list;
    }

    /**
     * Generate an Accept string with q-values from 1.0 (implicit) to 0.2 in the given order.
     *
     * See <a href="https://tools.ietf.org/html/rfc2616#section-14.1">RFC 2616 sec. 14.1</a>
     * for details on Accept string syntax. The Accept string must end with the following
     * catch-all types at the lowest priority: "text/*;q=0.1, *\/*;q=0.1"
     *
     * @param langs list of accepted {@link RDFLang}s
     * @return An HTTP 1.1 accept string
     *
     *
     */
    protected @Nonnull String toAcceptString(@Nonnull List<RDFLang> langs) {
        StringBuilder b = new StringBuilder();
        double q = 1.0;
        for (int i = 0, size = Math.min(9, langs.size()); i < size; i++) {
            RDFLang lang = langs.get(i);
            b.append(lang.getContentType());
            if (q < 1)
                b.append(String.format(";q=%.1f", q));
            q -= 0.1;
            b.append(", ");
        }
        b.append("text/*;q=0.1, */*;q=0.1");
        return b.toString();
    }

    private @Nonnull String getAcceptString() {
        Set<RDFLang> currentSource = registry == null ? FALLBACK_LANGS
                                   : registry.getParserRegistry().getSupportedLangs();
        if (acceptStringSource == currentSource && acceptString != null)
            return acceptString;
        acceptStringSource = currentSource;
        return acceptString = toAcceptString(reorderAccept(currentSource));
    }

    @Override public @Nonnull Object normalize(@Nonnull Object source) {
        if (!(source instanceof URL))
            return source;
        URL url = (URL) source;
        if (!cacheFiles && url.getProtocol().equals("file")) {
            File file = new File(url.getFile());
            if (file.exists())
                return new RDFFile(file);
            else
                return new RDFInputStreamSupplier((Callable<InputStream>) url::openStream);
        }
        Supplier<RDFInputStream> supplier = cache.get(url);
        if (supplier != null)
            return supplier.get();
        return new RDFInputStreamSupplier((Callable<InputStream>) () -> {
            URLConnection c = url.openConnection();
            c.setRequestProperty("Accept", getAcceptString());
            try (InputStream in = c.getInputStream()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[128];
                for (int n = in.read(buf); n >= 0; n = in.read(buf))
                    out.write(buf, 0, n);
                byte[] data = out.toByteArray();
                cache.put(url, new RDFBlob(data, null, Utils.toASCIIString(url)));
                return new ByteArrayInputStream(data);
            }
        }, null, Utils.toASCIIString(url));
    }
}
