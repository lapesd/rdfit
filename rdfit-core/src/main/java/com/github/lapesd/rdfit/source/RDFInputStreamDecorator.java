/*
 * Copyright 2021 Alexis Armin Huf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.lapesd.rdfit.source;

import com.github.lapesd.rdfit.source.syntax.RDFLangs;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;

public interface RDFInputStreamDecorator {
    /**
     * Return either inputStream or a decorated version of it.
     *
     * The caller releases ownership of the inputStream given as argument and holds
     * ownership of the returned InputStream (decorated or not). Decorated
     * {@link InputStream}s will call {@link InputStream#close()} on their delegates.
     *
     * @param inputStream the {@link InputStream} to decorate
     * @param lang the asserted or detected RDF syntax in the stream.
     *             May be {@link RDFLangs#UNKNOWN}
     * @param baseIRI the asserted base IRI fot the stream (not read from a @base statement
     *                within the stream). May be null
     * @param contextString a string to be used as context in log messages detailing actions
     *                      by the decorator (e.g., the file path that originated the inputStream).
     * @return Either inputStream, if this decorator does not apply to it or a new
     *         {@link InputStream} decorating inputStream.
     */
    @Nonnull InputStream applyIf(@Nonnull InputStream inputStream, @Nonnull RDFLang lang,
                                 @Nullable String baseIRI, @Nullable String contextString);
}
