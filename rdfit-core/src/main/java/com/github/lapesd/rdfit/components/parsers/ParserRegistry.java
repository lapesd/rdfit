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

/**
 * Manages and lookups {@link Parser} instances
 */
public interface ParserRegistry {
    /**
     * The {@link ConversionManager} that {@link Parser}s shoudl use, if necessary.
     * @return a {@link ConversionManager}
     */
    @Nonnull ConversionManager getConversionManager();

    /**
     * Replaces the default {@link #getConversionManager()}
     * @param mgr the new {@link ConversionManager}
     */
    void setConversionManager(@Nonnull ConversionManager mgr);

    /**
     * all {@link RDFLang} instances supported by at least one {@link Parser}
     * @return the {@link Set} of {@link RDFLang}s
     */
    @Nonnull Set<RDFLang> getSupportedLangs();

    /**
     * Register a parser.
     *
     * @param parser {@link Parser} instance to register
     */
    void register(@Nonnull Parser parser);

    /**
     * Removes a specific {@link Parser} instance previously registered.
     *
     * @param parser Parser instance to be removed (comparison by {@link Object#equals(Object)}.
     */
    void unregister(@Nonnull Parser parser);

    /**
     * Remove all {@link Parser} instances that satisfy the given predicate
     *
     * @param predicate A {@link Predicate} to be satisfied by removed parsers
     */
    void unregisterIf(@Nonnull Predicate<? super Parser> predicate);

    /**
     * Removes all {@link Parser}s of the given class
     * @param aClass teh class to remove instances of
     */
    default void unregisterAll(@Nonnull Class<? extends Parser> aClass) {
        unregisterIf(aClass::isInstance);
    }

    /**
     * Get an {@link ItParser} that accepts source and whose {@link ItParser#itElement()}
     * matches the one given.
     *
     * If valueClass is not null, the most recently added {@link ItParser} that satisfies the
     * above conditions and also has an #{@link ItParser#valueClass()} assignable to valueClass
     * is returned. If the valueClass restriction is not satisfied by any registered
     * {@link ItParser} with given iterationElement, then the valueClass parameter is ignored.
     *
     * @param source source object that must be accepted by {@link Parser#canParse(Object)}
     * @param itElem whether the {@link ItParser} should iterate triples or quads
     * @param valueClass ideal {@link ItParser#valueClass()}
     * @return a {@link ItParser} or null if no registered {@link ItParser} accepts the source.
     */
    @Nullable ItParser getItParser(@Nonnull Object source,  @Nullable IterationElement itElem,
                                   @Nullable Class<?> valueClass);

    /**
     * Get a {@link ListenerParser} instance whose {@link Parser#canParse(Object)} accepts source.
     *
     * IF there are multiple registered {@link ListenerParser}s that accept source, the following
     * criteria define which is returned:
     * <ol>
     *     <li> The first parser whose both offered triple class and quad class are assignable to those requested
     *         <ol>
     *             <li>If none satisfied the previous, the parser that provides the desired triple</li>
     *             <li>If none satisfied the previous, the parser that provides the desired quad</li>
     *         </ol>
     *     </li>
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
