package com.github.lapesd.rdfit.generators;

import com.github.lapesd.rdfit.TripleSet;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;

public interface SourceGenerator {
    @Nonnull List<?> generate(@Nonnull TripleSet tripleSet, @Nonnull File tempDir);
    boolean isReusable();
}
