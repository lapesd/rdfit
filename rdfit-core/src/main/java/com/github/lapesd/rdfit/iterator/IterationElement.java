package com.github.lapesd.rdfit.iterator;

import javax.annotation.Nonnull;

public enum IterationElement {
    TRIPLE,
    QUAD;

    public boolean isTriple() { return this == TRIPLE; }
    public boolean   isQuad() { return this == QUAD  ; }

    public @Nonnull IterationElement toggle() {
        return this == TRIPLE ? QUAD : TRIPLE;
    }
}
