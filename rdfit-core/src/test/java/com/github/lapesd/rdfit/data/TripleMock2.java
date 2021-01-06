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

public class TripleMock2 implements TripleMock {
    private final @Nonnull String subject, predicate, object;

    public TripleMock2(@Nonnull String subject, @Nonnull String predicate, @Nonnull String object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
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

    @Override public String toString() {
        return String.format("{%s %s %s}", getSubject(), getPredicate(), getObject());
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TripleMock2)) return false;
        TripleMock2 triple = (TripleMock2) o;
        return getSubject().equals(triple.getSubject()) && getPredicate().equals(triple.getPredicate()) && getObject().equals(triple.getObject());
    }

    @Override public int hashCode() {
        return Objects.hash(getSubject(), getPredicate(), getObject());
    }
}
