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
import java.util.Objects;
import java.util.function.Supplier;

import static java.lang.String.format;

public class RDFInputStream implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(RDFInputStream.class);

    protected @Nullable InputStream inputStream;
    protected @Nullable RDFLang lang;
    protected @Nullable String baseIRI;
    protected @Nullable String name;
    protected @Nullable RDFInputStreamDecorator decorator;
    protected @Nullable InputStream undecorated;

    public static class Builder {
        protected final @Nonnull InputStream inputStream;
        protected @Nullable RDFLang lang;
        protected @Nullable String baseIRI;
        protected @Nullable RDFInputStreamDecorator decorator;
        protected @Nullable String name;

        public Builder(@Nonnull InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public @Nonnull Builder lang(@Nullable RDFLang lang) {
            this.lang = lang;
            return this;
        }

        public @Nonnull Builder baseIRI(@Nullable String baseIRI) {
            this.baseIRI = baseIRI;
            return this;
        }

        public @Nonnull Builder name(@Nullable String name) {
            this.name = name;
            return this;
        }

        public @Nonnull Builder decorator(@Nullable RDFInputStreamDecorator decorator) {
            this.decorator = decorator;
            return this;
        }

        public @Nonnull RDFInputStream build() {
            return new RDFInputStream(inputStream, lang, baseIRI, name, decorator, true);
        }
    }
    public static @Nonnull Builder builder(@Nonnull InputStream inputStream) {
        return new Builder(inputStream);
    }
    public static @Nonnull Builder builder(@Nonnull RDFInputStream ris) {
        return new Builder(ris.getInputStream())
                .decorator(ris.getDecorator())
                .name(ris.getName())
                .lang(ris.getLang())
                .baseIRI(ris.hasBaseIRI() ? ris.getBaseIRI() : null);
    }

    public RDFInputStream(@Nonnull InputStream inputStream) {
        this(inputStream, null, null, null);
    }
    public RDFInputStream(@Nonnull InputStream inputStream,
                          @Nullable RDFInputStreamDecorator decorator) {
        this(inputStream, null, null, null, decorator);
    }

    public RDFInputStream(@Nonnull InputStream inputStream, @Nullable RDFLang lang) {
        this(inputStream, lang, null, null);
    }
    public RDFInputStream(@Nonnull InputStream inputStream, @Nullable RDFLang lang,
                          @Nullable RDFInputStreamDecorator decorator) {
        this(inputStream, lang, null, null, decorator);
    }
    public RDFInputStream(@Nonnull InputStream inputStream, @Nullable RDFLang lang,
                          @Nullable String baseIRI) {
        this(inputStream, lang, baseIRI, null);
    }
    public RDFInputStream(@Nonnull InputStream inputStream, @Nullable RDFLang lang,
                          @Nullable String baseIRI, @Nullable String name) {
        this(inputStream, lang, baseIRI, name, null, false);
    }
    public RDFInputStream(@Nonnull InputStream inputStream, @Nullable RDFLang lang,
                          @Nullable String baseIRI, @Nullable String name,
                          @Nullable RDFInputStreamDecorator decorator) {
        this(inputStream, lang, baseIRI, name, decorator, false);
    }
    protected RDFInputStream(@Nullable InputStream inputStream, @Nullable RDFLang lang,
                             @Nullable String baseIRI, @Nullable String name,
                             @Nullable RDFInputStreamDecorator decorator, boolean dummy) {
        this.inputStream = inputStream;
        this.lang = lang;
        this.baseIRI = baseIRI;
        this.name = name;
        this.decorator = decorator;
    }

    public @Nullable RDFInputStreamDecorator getDecorator() {
        return decorator;
    }

    protected @Nonnull InputStream getRawInputStream() {
        if (inputStream == null) throw new IllegalStateException();
        return inputStream;
    }

    /**
     * Get the {@link InputStream} containing RDF data. This method always returns the same instance
     *
     * If {@link RDFInputStream#getBufferedInputStream()} was previously called, this will return
     * the same instance as {@link RDFInputStream#getBufferedInputStream()}.
     *
     * @return the {@link InputStream}
     */
    public final @Nonnull InputStream getInputStream() {
        if (undecorated == null && decorator != null) {
            RDFLang lang;
            try {
                lang = getOrDetectLang(8192, this::getRawBufferedInputStream);
            } catch (IOException e) {
                logger.error("{} failed to detect language, getInputStream() " +
                             "will pass UNKNOWN to {}.applyIf()", this, decorator, e);
                lang = RDFLangs.UNKNOWN;
            }
            undecorated = inputStream;
            String ctxName = getName();
            if (ctxName == null) ctxName = toString();
            inputStream = decorator.applyIf(getRawInputStream(), lang,
                                            hasBaseIRI() ? getBaseIRI() : null, ctxName);
        } else if (inputStream == null) {
            inputStream = getRawInputStream();
        }
        return inputStream;
    }

    /**
     * Either a base IRI set upon instantiation or an auto-generated IRI.
     *
     * @return a non-null and non-empty base IRI to be used as fallback (if the input stream
     *         does not have the equivalent of a @base statement).
     */
    public @Nonnull String getBaseIRI() {
        if (baseIRI != null) {
            assert !baseIRI.isEmpty();
            return baseIRI;
        }
        return format("urn:inputstream:%x", System.identityHashCode(getInputStream()));
    }

    /**
     * Whether this object had an explicitly set base IRI set upon instantiation.
     *
     * @return true iff a base IRI was set upon instantiation
     */
    public boolean hasBaseIRI() {
        return baseIRI != null;
    }

    /**
     * A non-identifying string describing this {@link RDFInputStream} origin.
     *
     * @return Possibly null and possibly empty name.
     */
    public @Nullable String getName() {
        return name;
    }

    protected @Nonnull BufferedInputStream getRawBufferedInputStream() {
        InputStream is = getRawInputStream();
        if (!is.markSupported() || !(is instanceof BufferedInputStream))
            inputStream = is = new BufferedInputStream(is);
        return (BufferedInputStream)is;
    }

    /**
     * Get {@link RDFInputStream#getInputStream()} as a {@link BufferedInputStream}.
     *
     * This method always return the same instance and does not call neither
     * {@link BufferedInputStream#mark(int)} nor {@link BufferedInputStream#reset()}.
     *
     * @return a {@link BufferedInputStream}
     */
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
        return getOrDetectLang(8192, this::getBufferedInputStream);
    }

    public @Nonnull RDFLang getOrDetectLang(int maxBytes) throws IOException {
        return getOrDetectLang(maxBytes, this::getBufferedInputStream);
    }

    protected @Nonnull RDFLang
    getOrDetectLang(int maxBytes,
                    @Nonnull Supplier<BufferedInputStream> bufferedSupplier) throws IOException {
        if (lang == null) {
            BufferedInputStream is = bufferedSupplier.get();
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
        return format("%s{syntax=%s,name=%s,is=%s,decorator=%s}", Utils.toString(this), lang, name, inputStream, decorator);
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
