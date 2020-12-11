package com.github.com.alexishuf.rdfit.data;

import com.github.com.alexishuf.rdfit.callback.RDFCallback;
import org.testng.Assert;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class MockHelpers {
    public static @Nonnull RDFCallback
    feedMocksToCallback(RDFCallback cb, @Nonnull Object source, @Nonnull List<?> data) {
        cb.start(source);
        for (Object e : data) {
            assertNotNull(e);
            if (isQuad(e)) {
                SplitMockQuad split = SplitMockQuad.split(e);
                if (cb.quadType() != null) {
                    assertTrue(requireNonNull(cb.quadType()).isInstance(e));
                    cb.quad(e);
                } else {
                    cb.quad(split.getGraph(), split.getTriple());
                }
            } else {
                assertTrue(cb.tripleType().isInstance(e));
                cb.triple(e);
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

    public static @Nullable <Q> Function<Object, Q> triple2quad(@Nullable Class<Q> quadClass) {
        return triple2quad(quadClass, "");
    }

    @SuppressWarnings("unchecked") 
    public static @Nullable <Q> Function<Object, Q> triple2quad(@Nullable Class<Q> quadClass,
                                                               @Nonnull String graph) {
        if (quadClass == null)
            return null;
        if (quadClass.equals(QuadMock1.class)) {
            return t -> (Q)new QuadMock1(graph, (TripleMock1) t);
        } else if (quadClass.equals(QuadMock2.class)) {
            return t -> {
                TripleMock2 t2 = (TripleMock2) t;
                return (Q) new QuadMock2(graph, t2.getSubject(), t2.getPredicate(), t2.getObject());
            };
        } else if (quadClass.equals(Object.class) || quadClass.equals(ElementMock.class)
                                                  || quadClass.equals(QuadMock.class)) {
            return t -> {
                if (t instanceof TripleMock1) {
                    return (Q) new QuadMock1(graph, (TripleMock1) t);
                } else if (t instanceof TripleMock2) {
                    TripleMock2 t2 = (TripleMock2) t;
                    return (Q) new QuadMock2(graph, t2.getSubject(), t2.getPredicate(),
                                                    t2.getObject());
                } else {
                    throw new IllegalArgumentException("Cannot convert "+t+" into q QuadMock");
                }
            };
        }
        throw new IllegalArgumentException("Don't know how to create a "+quadClass);
    }
}
