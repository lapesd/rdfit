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

package com.github.lapesd.rdfit.components.parsers;

import com.github.lapesd.rdfit.components.ItParser;
import com.github.lapesd.rdfit.components.ListenerParser;
import com.github.lapesd.rdfit.components.Parser;
import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Predicate;

public interface ParserRegistry {
    @Nonnull ConversionManager getConversionManager();

    void setConversionManager(@Nonnull ConversionManager mgr);

    @Nonnull Set<RDFLang> getSupportedLangs();

    /**
     * Register a parser.
     */
    void register(@Nonnull Parser parser);

    /**
     * Removes a specific {@link Parser} instance previously registered.
     */
    void unregister(@Nonnull Parser parser);

    /**
     * Remove all {@link Parser} instances that satisfy the given predicate
     */
    void unregisterIf(@Nonnull Predicate<? super Parser> predicate);

    default void unregisterAll(@Nonnull Class<? extends Parser> aClass) {
        unregisterIf(aClass::isInstance);
    }

    /**
     * Get an {@link ItParser} that accepts source and whose {@link ItParser#iterationElement()}
     * matches the one given.
     *
     * If tripleClass is not null, the most recently added {@link ItParser} that satisfies the
     * above conditions and also has an #{@link ItParser#valueClass()} assignable to tripleClass
     * is returned. If the tripleClass retriction is not satisfied by any registered
     * {@link ItParser} with given iterationElement, then the tripleClass parameter is ignored.
     *
     * @return a {@link ItParser} or null if no registered {@link ItParser} accepts the source.
     */
    @Nullable ItParser getItParser(@Nonnull Object source,  @Nullable IterationElement itElem,
                                   @Nullable Class<?> tripleClass);

    /**
     * Get a {@link ListenerParser} instance whose {@link Parser#canParse(Object)} accepts source.
     *
     * IF there are multiple registered {@link ListenerParser}s that accept source, the following
     * criteria define which is returned:
     * <ol>
     *     <li>The first parser whose both offered triple class and quad class are assignable to those requested</li>
     *         <ol>
     *             <li>If none satisfied the previous, the parser that provides the desired triple</li>
     *             <li>If none satisfied the previous, the parser that provides the desired quad</li>
     *         </ol>
     *     <li>In case of ties, the last registered parser wins</li>
     * </ol>
     *
     * @param source the source to be parsed
     * @param desiredTripleClass desired triple class or null if any triple class is accepted
     * @param desiredQuadClass desired quad class or null if any quad class is accepted
     * @return a {@link ListenerParser} or null if no registered {@link ListenerParser}
     *         accepts the source.
     */
    @Nullable ListenerParser getListenerParser(@Nonnull Object source,
                                               @Nullable Class<?> desiredTripleClass,
                                               @Nullable Class<?> desiredQuadClass);


}
