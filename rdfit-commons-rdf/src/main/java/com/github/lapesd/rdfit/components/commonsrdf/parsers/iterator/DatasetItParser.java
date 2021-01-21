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

package com.github.lapesd.rdfit.components.commonsrdf.parsers.iterator;

import com.github.lapesd.rdfit.components.parsers.BaseItParser;
import com.github.lapesd.rdfit.impl.ClosedSourceQueue;
import com.github.lapesd.rdfit.iterator.EagerRDFIt;
import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.iterator.PlainRDFIt;
import com.github.lapesd.rdfit.iterator.RDFIt;
import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.Quad;
import org.apache.commons.rdf.api.Triple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;

/**
 * Create an {@link RDFIt} over a commons-rdf {@link Dataset}
 */
public class DatasetItParser extends BaseItParser {
    /**
     * Create a parser delivering triples or quads, as given.
     * @param itElement the {@link RDFIt#itElement()} of produced {@link RDFIt} instances
     */
    public DatasetItParser(@Nonnull IterationElement itElement) {
        this(itElement.isTriple() ? Triple.class : Quad.class, itElement);
    }

    /**
     * Create a parser whose {@link #parse(Object)}  method yields {@link RDFIt} instances
     * with the given {@link RDFIt#valueClass()} and {@link RDFIt#itElement()}.
     *
     * @param valueClass the {@link RDFIt#valueClass()}. Must be {@link Triple}, {@link Quad}
     *                   or a superclass of those
     * @param itElement the value of {@link RDFIt#itElement()}
     */
    public DatasetItParser(@Nonnull Class<?> valueClass, @Nonnull IterationElement itElement) {
        super(Collections.singleton(Dataset.class), valueClass, itElement);
    }

    @Override public @Nonnull <T> RDFIt<T> parse(@Nonnull Object source) {
        Dataset ds = (Dataset) source;
        Iterator<Quad> it = ds.iterate().iterator();
        if (itElement().isTriple()) {
            return new EagerRDFIt<T>(valueClass(), itElement(), new ClosedSourceQueue()) {
                @Override protected @Nullable T advance() {
                    //noinspection unchecked
                    return it.hasNext() ? (T)it.next().asTriple() : null;
                }

                @Override public @Nonnull Object getSource() {
                    return source;
                }
            };
        } else {
            return new PlainRDFIt<>(valueClass(), itElement(), it, source);
        }
    }
}
