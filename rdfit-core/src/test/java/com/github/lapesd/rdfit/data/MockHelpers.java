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

import com.github.lapesd.rdfit.components.converters.quad.QuadLifter;
import com.github.lapesd.rdfit.components.converters.quad.QuadLifterFunction;
import com.github.lapesd.rdfit.listener.RDFListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class MockHelpers {
    public static @Nonnull RDFListener<?,?>
    feedMocksToCallback(RDFListener<?,?> cb, @Nonnull Object source, @Nonnull List<?> data) {
        @SuppressWarnings("unchecked")
        RDFListener<Object, Object> cb_ = (RDFListener<Object, Object>) cb;
        cb.start(source);
        for (Object e : data) {
            assertNotNull(e);
            if (isQuad(e)) {
                SplitMockQuad split = SplitMockQuad.split(e);
                if (cb.quadType() != null) {
                    assertTrue(requireNonNull(cb.quadType()).isInstance(e));
                    cb_.quad(e);
                } else {
                    cb_.quad(split.getGraph(), split.getTriple());
                }
            } else {
                assertNotNull(cb.tripleType());
                assertTrue(requireNonNull(cb.tripleType()).isInstance(e));
                cb_.triple(e);
            }
        }
        cb.finish(source);
        return cb;
    }

    public static boolean isQuad(@Nonnull Object object) {
        return object instanceof QuadMock;
    }
    public static boolean isAnyMock(@Nonnull Class<?> aClass) {
        return aClass.equals(Object.class) || aClass.equals(ElementMock.class);
    }
    public static boolean isQuadType(@Nonnull Class<?> aClass) {
        return QuadMock.class.isAssignableFrom(aClass);
    }
    public static boolean isTripleType(@Nonnull Class<?> aClass) {
        return TripleMock.class.isAssignableFrom(aClass);
    }
    public static Class<?> tripleTypeFor(@Nonnull Class<?> tripleOrQuadType) {
        if (isTripleType(tripleOrQuadType)) return tripleOrQuadType;
        if (tripleOrQuadType.equals(QuadMock1.class))
            return TripleMock1.class;
        else if (tripleOrQuadType.equals(QuadMock2.class))
            return TripleMock2.class;
        else if (ElementMock.class.isAssignableFrom(tripleOrQuadType))
            return TripleMock.class;
        else if (tripleOrQuadType.equals(Object.class))
            return TripleMock.class;
        throw new IllegalArgumentException("Cannot define a triple type for "+tripleOrQuadType);
    }

    public static @Nullable QuadLifter quadLifter(@Nullable Class<?> quadClass) {
        if (quadClass == null)
            return null;
        return quadLifter(tripleTypeFor(quadClass), quadClass, "");
    }
    public static @Nonnull QuadLifter quadLifter(@Nonnull Class<?> tripleType,
                                                 @Nonnull Class<?> quadClass,
                                                 @Nonnull String graph) {
        Function<Object, ?> f = t -> MockFactory.triple2quad(quadClass, graph, t);
        return new QuadLifterFunction(tripleType, f);

    }
}
