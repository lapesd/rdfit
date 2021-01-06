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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import static java.util.Arrays.asList;

public class MockFactory {
    public static final @Nonnull List<Class<?>> TRIPLE_CLASSES =
            asList(TripleMock1.class, TripleMock2.class, TripleMock3.class);
    public static final @Nonnull List<Class<?>> QUAD_CLASSES =
            asList(QuadMock1.class, QuadMock2.class, QuadMock3.class);

    public static @Nonnull <T> T
    createTriple(Class<T> cls, @Nonnull String s, @Nonnull String p, @Nonnull String o) {
        try {
            Constructor<?> c = cls.getConstructor(String.class, String.class, String.class);
            //noinspection unchecked
            return (T) c.newInstance(s, p, o);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static @Nonnull <T> T
    createQuad(Class<T> cls, @Nonnull String g, @Nonnull String s, @Nonnull String p,
               @Nonnull String o) {
        try {
            Constructor<?> c;
            c = cls.getConstructor(String.class, String.class, String.class, String.class);
            //noinspection unchecked
            return (T) c.newInstance(g, s, p, o);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static @Nonnull <T> T triple2quad(@Nonnull Class<T> quadClass, @Nonnull String graph,
                                             @Nonnull Object triple) {
        try {
            Method getSubject = triple.getClass().getMethod("getSubject");
            Method getPredicate = triple.getClass().getMethod("getPredicate");
            Method getObject = triple.getClass().getMethod("getObject");
            String s = getSubject.invoke(triple).toString();
            String p = getPredicate.invoke(triple).toString();
            String o = getObject.invoke(triple).toString();
            return createQuad(quadClass, graph, s, p, o);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
