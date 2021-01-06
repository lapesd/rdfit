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

public class QuadMock2 implements QuadMock {
    private final @Nonnull String graph, subject, predicate, object;

    public QuadMock2(@Nonnull String graph, @Nonnull String subject, @Nonnull String predicate,
                     @Nonnull String object) {
        this.graph = graph;
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    public @Nonnull String getGraph() {
        return graph;
    }

    public @Nonnull String getSubject() {
        return subject;
    }

    public @Nonnull String getPredicate() {
        return predicate;
    }

    public @Nonnull String getObject() {
        return object;
    }

    @Override public @Nonnull String toString() {
        return format("{%s %s %s %s}", getGraph(), getSubject(), getPredicate(), getObject());
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuadMock2)) return false;
        QuadMock2 quadMock2 = (QuadMock2) o;
        return getGraph().equals(quadMock2.getGraph())
                && getSubject().equals(quadMock2.getSubject())
                && getPredicate().equals(quadMock2.getPredicate())
                && getObject().equals(quadMock2.getObject());
    }

    @Override public int hashCode() {
        return Objects.hash(getGraph(), getSubject(), getPredicate(), getObject());
    }
}
