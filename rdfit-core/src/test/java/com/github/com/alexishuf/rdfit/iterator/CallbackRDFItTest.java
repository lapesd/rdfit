package com.github.com.alexishuf.rdfit.iterator;

import javax.annotation.Nonnull;
import java.util.List;

import static com.github.com.alexishuf.rdfit.data.MockHelpers.*;

public class CallbackRDFItTest extends RDFItTestBase {
    private static final @Nonnull Object TEST_SOURCE = new Object() {
        @Override public String toString() {
            return "CallbackRDFItTest.TEST_SOURCE";
        }
    };

    @Override protected @Nonnull <T> RDFIt<T>
    createIt(@Nonnull Class<T> valueClass, @Nonnull List<?> data) {
        Class<?> quadType = isAnyMock(valueClass) || isQuadType(valueClass) ? valueClass : null;
        Class<?> tripleType = tripleTypeFor(valueClass);
        CallbackRDFIt<T> it;
        it = new CallbackRDFIt<>(TEST_SOURCE, tripleType, quadType, triple2quad(quadType));
        feedMocksToCallback(it.getCallback(), TEST_SOURCE, data).finish();
        return it;
    }
}