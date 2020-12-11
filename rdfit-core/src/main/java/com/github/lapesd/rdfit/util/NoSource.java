package com.github.lapesd.rdfit.util;

import javax.annotation.Nonnull;

public class NoSource {
    public static final @Nonnull NoSource INSTANCE = new NoSource();

    @Override public String toString() {
        return "NO_SOURCE";
    }
}
