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

public class SplitMockQuad {
    private final @Nonnull String graph;
    private final @Nonnull TripleMock triple;

    public SplitMockQuad(@Nonnull String graph, @Nonnull TripleMock triple) {
        this.graph = graph;
        this.triple = triple;
    }

    public static @Nonnull SplitMockQuad split(@Nonnull Object o) {
        if (o instanceof QuadMock1) {
            return new SplitMockQuad(((QuadMock1) o).getGraph(), ((QuadMock1) o).getTriple());
        } else if (o instanceof QuadMock2) {
            QuadMock2 m = (QuadMock2) o;
            TripleMock2 triple = new TripleMock2(m.getSubject(), m.getPredicate(), m.getObject());
            return new SplitMockQuad(m.getGraph(), triple);
        } else if (o instanceof QuadMock3) {
            return new SplitMockQuad(((QuadMock3)o).getGraph(), ((QuadMock3)o).getTriple());
        }
        throw new IllegalArgumentException("Cannot split "+o.getClass()+": "+o);
    }

    public @Nonnull String getGraph() {
        return graph;
    }

    public @Nonnull TripleMock getTriple() {
        return triple;
    }

    @Override public String toString() {
        return String.format("SplitMockQuad{%s, %s}", getGraph(), getTriple());
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SplitMockQuad)) return false;
        SplitMockQuad that = (SplitMockQuad) o;
        return getGraph().equals(that.getGraph()) && getTriple().equals(that.getTriple());
    }

    @Override public int hashCode() {
        return Objects.hash(getGraph(), getTriple());
    }
}
