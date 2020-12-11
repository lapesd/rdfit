package com.github.com.alexishuf.rdfit.iterator;

public enum IterationElement {
    TRIPLE,
    QUAD;

    public boolean isTriple() { return this == TRIPLE; }
    public boolean   isQuad() { return this == QUAD  ; }
}
