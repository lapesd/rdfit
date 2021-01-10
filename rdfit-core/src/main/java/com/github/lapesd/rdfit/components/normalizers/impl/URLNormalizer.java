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
import com.github.lapesd.rdfit.source.RDFInputStreamSupplier;
import com.github.lapesd.rdfit.source.syntax.RDFLangs;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.Callable;

import static java.util.Arrays.asList;

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
        return new RDFInputStreamSupplier((Callable<InputStream>) () -> {
            URLConnection c = ((URL) source).openConnection();
            c.setRequestProperty("Accept", getAcceptString());
            return c.getInputStream();
        });
    }
}
