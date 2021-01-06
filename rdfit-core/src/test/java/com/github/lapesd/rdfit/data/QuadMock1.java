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

package com.github.lapesd.rdfit.data;

import javax.annotation.Nonnull;
import java.util.Objects;

import static java.lang.String.format;

public class QuadMock1 implements QuadMock {
    private final @Nonnull String graph;
    private final @Nonnull TripleMock1 triple;

    public QuadMock1(@Nonnull String graph, @Nonnull TripleMock1 triple) {
        this.graph = graph;
        this.triple = triple;
    }

    public QuadMock1(@Nonnull String graph, @Nonnull String subject,
                     @Nonnull String predicate, @Nonnull String object) {
        this.graph = graph;
        this.triple = new TripleMock1(subject, predicate, object);
    }

    public @Nonnull String getGraph() {
        return graph;
    }

    public @Nonnull TripleMock1 getTriple() {
        return triple;
    }

    @Override public @Nonnull String toString() {
        TripleMock1 t = getTriple();
        return format("(%s %s %s %s)", getGraph(), t.getSubject(), t.getPredicate(), t.getObject());
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuadMock1)) return false;
        QuadMock1 quadMock1 = (QuadMock1) o;
        return getGraph().equals(quadMock1.getGraph()) && getTriple().equals(quadMock1.getTriple());
    }

    @Override public int hashCode() {
        return Objects.hash(getGraph(), getTriple());
    }
}
