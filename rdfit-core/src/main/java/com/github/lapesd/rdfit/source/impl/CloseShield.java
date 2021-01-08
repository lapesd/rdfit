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

package com.github.lapesd.rdfit.source.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

public class CloseShield extends InputStream {
    private static final Logger logger = LoggerFactory.getLogger(CloseShield.class);
    private final @Nonnull InputStream delegate;

    public CloseShield(@Nonnull InputStream delegate) {
        this.delegate = delegate;
    }

    public @Nonnull InputStream getDelegate() {
        return delegate;
    }

    @Override public int read() throws IOException {
        return delegate.read();
    }

    @Override public int read(@Nonnull byte[] b) throws IOException {
        return delegate.read(b);
    }

    @Override public int read(@Nonnull byte[] b, int off, int len) throws IOException {
        return delegate.read(b, off, len);
    }

    @Override public long skip(long n) throws IOException {
        return delegate.skip(n);
    }

    @Override public int available() throws IOException {
        return delegate.available();
    }

    @Override public void close() throws IOException {
        logger.debug("Ignoring close(), will not delegate to {}", delegate);
    }

    @Override public void mark(int readlimit) {
        delegate.mark(readlimit);
    }

    @Override public void reset() throws IOException {
        delegate.reset();
    }

    @Override public boolean markSupported() {
        return delegate.markSupported();
    }

    @Override public @Nonnull String toString() {
        return "CloseShield{"+delegate+"}";
    }
}
