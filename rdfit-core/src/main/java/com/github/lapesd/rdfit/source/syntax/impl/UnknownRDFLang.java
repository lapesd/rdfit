package com.github.lapesd.rdfit.source.syntax.impl;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class UnknownRDFLang implements RDFLang {
    private static final List<String> EXTENSIONS = Collections.singletonList("");

    @Override public @Nonnull String name() {
        return "UNKNOWN";
    }
    @Override public @Nonnull String getMainExtension() {
        return "";
    }

    @Override public @Nonnull String getMainSuffix() {
        return "";
    }

    @Override public @Nonnull Collection<String> getExtensions() {
        return EXTENSIONS;
    }

    @Override public String toString() {
        return name();
    }

    @Override public boolean equals(Object obj) {
        return obj instanceof UnknownRDFLang;
    }

    @Override public int hashCode() {
        return 1;
    }
}
