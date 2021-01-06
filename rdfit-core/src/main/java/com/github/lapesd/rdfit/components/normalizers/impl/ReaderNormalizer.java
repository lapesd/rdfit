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
import com.github.lapesd.rdfit.source.RDFInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Converts {@link Reader} instances into {@link RDFInputStream} instances.
 *
 * The implementation relies on ReaderInputStream from commons-io. commons-io wil likely
 * be available either from jena, rdf4j or the client pom.xml. Since this is the only use of a
 * commons-io class in the rdfit-core module, using relection here is better than introducing yet
 * another source of dependency versioning problems.
 */
@Accepts(Reader.class)
public class ReaderNormalizer extends BaseSourceNormalizer {
    private static final Logger logger = LoggerFactory.getLogger(ReaderNormalizer.class);
    private static final String readerISQN = "org.apache.commons.io.input.ReaderInputStream";

    private Class<?> readerISCls;

    private @Nullable Class<?> getReaderISCls() {
        if (readerISCls != null)
            return readerISCls;
        try {
            readerISCls = Thread.currentThread().getContextClassLoader().loadClass(readerISQN);
        } catch (ClassNotFoundException e) {
            try {
                getClass().getClassLoader().loadClass(readerISQN);
            } catch (ClassNotFoundException ignored) { }
        }
        return readerISCls;
    }

    private @Nullable InputStream wrap(@Nonnull Reader reader) {
        Class<?> cls = getReaderISCls();
        if (cls == null) {
            logger.warn("No {} in the classpath, will not wrap Reader", readerISQN);
            return null;
        }
        Charset cs = StandardCharsets.UTF_8;
        try {
            Constructor<?> ct = cls.getConstructor(Reader.class, Charset.class);
            if (reader instanceof InputStreamReader)
                cs = Charset.forName(((InputStreamReader) reader).getEncoding());
            return (InputStream) ct.newInstance(reader, cs);
        } catch (InvocationTargetException e) {
            logger.error("ReaderInputStream({}, {}) failed", reader, cs, e);
        } catch (ReflectiveOperationException e) {
            logger.error("Unexpected exception when constructing a ReaderInputStream", e);
        } catch (RuntimeException e) {
            logger.error("Will not wrap Reader with ReaderInputStream due to exception", e);
        }
        return null;
    }

    @Override public @Nonnull Object normalize(@Nonnull Object source) {
        if (source instanceof Reader) {
            InputStream is = wrap((Reader) source);
            if (is != null)
                return new RDFInputStream(is);
        }
        return source;
    }
}
