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
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import com.github.lapesd.rdfit.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import static java.lang.String.format;

@SuppressWarnings("UnusedReturnValue")
public class RDFInputStreamSupplier extends RDFInputStream {
    private static final Logger logger = LoggerFactory.getLogger(RDFInputStreamSupplier.class);

    private final @Nonnull Callable<InputStream> supplier;
    private @Nullable Callable<?> closeCallable;
    private @Nullable Runnable closeRunnable;
    private @Nullable String name;

    public static class Builder {
        private final @Nonnull Callable<InputStream> supplier;
        private @Nullable RDFLang lang;
        private @Nullable String baseIRI;
        private @Nullable Callable<?> closeCallable;
        private @Nullable Runnable closeRunnable;
        private @Nullable String name;
        private @Nullable RDFInputStreamDecorator decorator;

        public Builder(@Nonnull Callable<InputStream> supplier) {
            this.supplier = supplier;
        }

        public @Nonnull Builder lang(@Nullable RDFLang lang) {
            this.lang = lang;
            return this;
        }

        public @Nonnull Builder baseIRI(@Nullable String baseIRI) {
            this.baseIRI = baseIRI;
            return this;
        }

        public @Nonnull Builder onClose(@Nullable Callable<?> closeCallable) {
            this.closeRunnable = null;
            this.closeCallable = closeCallable;
            return this;
        }

        public @Nonnull Builder onClose(@Nullable Runnable closeRunnable) {
            this.closeCallable = null;
            this.closeRunnable = closeRunnable;
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

        public @Nonnull RDFInputStreamSupplier build() {
            RDFInputStreamSupplier ris = new RDFInputStreamSupplier(supplier, lang, baseIRI, name, decorator);
            if (closeCallable != null)
                ris.onClose(closeCallable);
            if (closeRunnable != null)
                ris.onClose(closeRunnable);
            return ris;
        }
    }

    public static @Nonnull Builder builder(@Nonnull Callable<InputStream> supplier) {
        return new Builder(supplier);
    }

    public RDFInputStreamSupplier(@Nonnull Supplier<InputStream> supplier) {
        this(supplier, null);
    }

    public RDFInputStreamSupplier(@Nonnull Supplier<InputStream> supplier,
                                  @Nullable RDFLang lang) {
        this(supplier, lang, null);
    }

    public RDFInputStreamSupplier(@Nonnull Supplier<InputStream> supplier,
                                  @Nullable RDFLang lang, @Nullable String baseIRI) {
        this((Callable<InputStream>) supplier::get, lang, baseIRI);
    }
    public RDFInputStreamSupplier(@Nonnull Supplier<InputStream> supplier,
                                  @Nullable RDFLang lang, @Nullable String baseIRI,
                                  @Nullable String name,
                                  @Nullable RDFInputStreamDecorator decorator) {
        this((Callable<InputStream>) supplier::get, lang, baseIRI, name, decorator);
    }

    public RDFInputStreamSupplier(@Nonnull Callable<InputStream> supplier) {
        this(supplier, null, null, null, null);
    }

    public RDFInputStreamSupplier(@Nonnull Callable<InputStream> supplier,
                                  @Nullable RDFLang lang) {
        this(supplier, lang, null, null, null);
    }

    public RDFInputStreamSupplier(@Nonnull Callable<InputStream> supplier,
                                  @Nullable RDFLang lang, @Nullable String baseIRI) {
        this(supplier, lang, baseIRI, null, null);
    }

    public RDFInputStreamSupplier(@Nonnull Callable<InputStream> supplier,
                              @Nullable RDFLang lang, @Nullable String baseIRI,
                              @Nullable String name) {
        this(supplier, lang, baseIRI, name, null);
    }

    public RDFInputStreamSupplier(@Nonnull Callable<InputStream> supplier,
                                  @Nullable RDFLang lang, @Nullable String baseIRI,
                                  @Nullable String name,
                                  @Nullable RDFInputStreamDecorator decorator) {
        super(null, lang, baseIRI, name, decorator, true);
        this.supplier = supplier;
    }

    public @Nonnull RDFInputStreamSupplier onClose(@Nonnull Callable<?> callable) {
        this.closeCallable = callable;
        this.closeRunnable = null;
        return this;
    }

    public @Nonnull RDFInputStreamSupplier onClose(@Nonnull Runnable runnable) {
        this.closeRunnable = runnable;
        this.closeCallable = null;
        return this;
    }

    @Override public @Nonnull InputStream getRawInputStream() {
        if (inputStream == null) {
            try {
                inputStream = supplier.call();
            } catch (Exception e) {
                throw new RDFItException(this, this+" InputStream supplier failed", e);
            }
            if (inputStream == null)
                throw new RDFItException(this, this+" InputStream supplier returned null");
        }
        return inputStream;
    }

    @Override public @Nonnull String toString() {
        if (name != null)
            return format("%s{syntax=%s,name=%s}", Utils.toString(this), lang, name);
        return format("%s{syntax=%s,supplier=%s}", Utils.toString(this), lang, supplier);
    }

    @Override public void close() {
        try {
            super.close();
        } finally {
            if (closeCallable != null) {
                try {
                    closeCallable.call();
                } catch (Exception e) {
                    logger.error("{}.close(): closeCallable {} failed", this, closeCallable, e);
                }
            } else if (closeRunnable != null) {
                try {
                    closeRunnable.run();
                } catch (RuntimeException e) {
                    logger.error("{}.close(): closeRunnable {} failed", this, closeRunnable, e);
                }
            }

        }
    }
}
