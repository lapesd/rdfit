package com.github.lapesd.rdfit.generators;

import com.github.lapesd.rdfit.TripleSet;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class SupplierGenerator implements SourceGenerator {
    private final @Nonnull SourceGenerator child;

    public SupplierGenerator(@Nonnull SourceGenerator child) {
        this.child = child;
    }

    @Override public boolean isReusable() {
        return true;
    }

    @Override public @Nonnull List<?> generate(@Nonnull TripleSet tripleSet,
                                               @Nonnull File tempDir) {
        if (!child.isReusable())
            return Collections.emptyList();
        List<Supplier<?>> list = new ArrayList<>();
        for (Object o : child.generate(tripleSet, tempDir)) list.add(() -> o);
        return list;
    }
}
