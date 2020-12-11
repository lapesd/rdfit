package com.github.lapesd.rdfit.iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;

public class FlatMapRDFItTest extends RDFItTestBase {

    @Override
    protected @Nonnull <T> RDFIt<T> createIt(@Nonnull Class<T> valueClass,
                                             @Nonnull IterationElement itEl, @Nonnull List<?> data) {
        int[] nCloses = {0};
        int[] nClosesExpected = {0};
        return new FlatMapRDFIt<T>(valueClass, itEl, data.iterator(),
                v -> {
                    @SuppressWarnings("unchecked")
                    Set<T> singleton = Collections.singleton((T) v);
                    return new PlainRDFIt<T>(valueClass, itEl, singleton.iterator(), singleton) {
                        @Override public void close() {
                            super.close();
                            ++nCloses[0];
                        }
                    };
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