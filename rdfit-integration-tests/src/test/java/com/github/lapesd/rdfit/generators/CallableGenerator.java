package com.github.lapesd.rdfit.generators;

import com.github.lapesd.rdfit.TripleSet;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class CallableGenerator implements SourceGenerator{
    private final @Nonnull SourceGenerator child;

    public CallableGenerator(@Nonnull SourceGenerator child) {
        this.child = child;
    }

    @Override public boolean isReusable() {
        return true;
    }

    @Override public @Nonnull List<?> generate(@Nonnull TripleSet tripleSet,
                                               @Nonnull File tempDir) {
        List<Callable<?>> list = new ArrayList<>();
        if (child.isReusable()) {
            for (Object o : child.generate(tripleSet, tempDir)) list.add(() -> o);
        }
        return list;
    }
}
