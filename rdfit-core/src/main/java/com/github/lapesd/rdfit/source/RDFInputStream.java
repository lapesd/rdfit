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

package com.github.lapesd.rdfit.source;

import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.source.syntax.RDFLangs;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import com.github.lapesd.rdfit.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.String.format;

public class RDFInputStream implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(RDFInputStream.class);

    protected @Nullable InputStream inputStream;
    protected @Nullable RDFLang lang;
    protected @Nullable String baseIRI;

    public RDFInputStream(@Nonnull InputStream inputStream) {
        this(inputStream, null);
    }

    public RDFInputStream(@Nonnull InputStream inputStream, @Nullable RDFLang lang) {
        this(inputStream, lang, null);
    }
    public RDFInputStream(@Nonnull InputStream inputStream, @Nullable RDFLang lang,
                          @Nullable String baseIRI) {
        this(inputStream, lang, baseIRI, false);
    }
    protected RDFInputStream(@Nullable InputStream inputStream, @Nullable RDFLang lang,
                             @Nullable String baseIRI, boolean dummy) {
        this.inputStream = inputStream;
        this.lang = lang;
        this.baseIRI = baseIRI;
    }

    public @Nonnull InputStream getInputStream() {
        if (inputStream == null) throw new IllegalStateException();
        return inputStream;
    }

    public @Nonnull String getBaseIRI() {
        if (baseIRI != null)
            return baseIRI;
        return format("urn:inputstream:%x", System.identityHashCode(getInputStream()));
    }

    public boolean hasBaseIRI() {
        return baseIRI != null;
    }

    public @Nonnull BufferedInputStream getBufferedInputStream() {
        InputStream is = getInputStream();
        if (!is.markSupported() || !(is instanceof BufferedInputStream))
            inputStream = is = new BufferedInputStream(is);
        return (BufferedInputStream)is;
    }

    public @Nullable RDFLang getLang() {
        return lang;
    }

    public @Nonnull RDFLang getOrDetectLang() throws IOException {
        return getOrDetectLang(8192);
    }

    public @Nonnull RDFLang getOrDetectLang(int maxBytes) throws IOException {
        if (lang == null) {
            BufferedInputStream is = getBufferedInputStream();
            is.mark(maxBytes);
            lang = RDFLangs.guess(is, maxBytes);
            try {
                is.reset();
            } catch (IOException e) {
                throw new RDFItException("Unexpected BufferedInputStream.reset() " +
                                         "failure at " + this, e);
            }
        }
        assert lang != null;
        return lang;
    }

    @Override public @Nonnull String toString() {
        return format("%s{syntax=%s,is=%s}", Utils.toString(this), lang, inputStream);
    }

    @Override public void close() {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.error("{}.close(): failed to close inputStream", this, e);
            }
        }
    }
}
