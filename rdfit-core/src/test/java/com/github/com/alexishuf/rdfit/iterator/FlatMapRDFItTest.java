package com.github.com.alexishuf.rdfit.iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.*;

public class FlatMapRDFItTest extends RDFItTestBase {

    @Override
    protected @Nonnull <T> RDFIt<T> createIt(@Nonnull Class<T> valueClass, @Nonnull List<?> data) {
        int[] nCloses = {0};
        int[] nClosesExpected = {0};
        //noinspection unchecked
        return new FlatMapRDFIt<T>(valueClass, data.iterator(),
                v -> new PlainRDFIt<T>(valueClass, Collections.singleton((T)v).iterator()) {
                    @Override public void close() {
                        super.close();
                        ++nCloses[0];
                    }
                }) {

            @Override protected @Nullable T advance() {
                T obj = super.advance();
                if (obj != null)
                    ++nClosesExpected[0];
                return obj;
            }

            @Override public void close() {
                super.close();
                assertEquals(nCloses[0], nClosesExpected[0]);
            }
        };
    }
}