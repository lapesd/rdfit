package com.github.lapesd.rdfit.iterator;

import javax.annotation.Nonnull;
import java.util.List;

public class PlainRDFItTest extends RDFItTestBase {
    @Override
    protected @Nonnull <T> RDFIt<T> createIt(@Nonnull Class<T> valueClass,
                                             @Nonnull IterationElement itEl, @Nonnull List<?> data) {
        return new PlainRDFIt<>(valueClass, itEl, data.iterator(), data);
    }
}