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

package com.github.lapesd.rdfit.components;

import com.github.lapesd.rdfit.components.parsers.ParserRegistry;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;

public interface Parser extends Component {
    /**
     * Notify that the parser has been registered at the given registry.
     *
     * @param registry {@link ParserRegistry} to which this instance has been attached.
     */
    void attachTo(@Nonnull ParserRegistry registry);

    /**
     * Set of classes accepted by the parser. {@link #canParse(Object)} may reject specific
     * instances of these classes.
     *
     * @return non-empty set of object classes supported by this parser.
     */
    @Nonnull Collection<Class<?>> acceptedClasses();

    /**
     * Collection of RDF languages this parser actively provides support for.
     *
     * Model-style parsers (e.g. for query results or in-memory graphs) should return an
     * empty collection.
     *
     * @return Non-empty set of {@link RDFLang}s supported by this parser
     */
    @Nonnull Set<RDFLang> parsedLangs();

    /**
     * Indicates whether this {@link Parser} implementation can parse the given source.
     *
     * This method may perform I/O as part of such test. However, long running IO operations
     * and scanning whole files should be avoided.
     *
     * @param source the source to test
     * @return <code>true</code> if a subsequent call to a <code>parse</code> method will work
     *         (assuming the input is fully valid).
     */
    boolean canParse(@Nonnull Object source);
}
