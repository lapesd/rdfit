package com.github.lapesd.rdfit.source.syntax.impl;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

public class SimpleRDFLang implements RDFLang {
    private final @Nonnull String name;
    private final @Nonnull LinkedHashSet<String> extensions;
    private final boolean binary;

    public SimpleRDFLang(@Nonnull String name, @Nonnull List<String> extensions, boolean isBinary) {
        this.name = name;
        this.extensions = new LinkedHashSet<>(extensions);
        this.binary = isBinary;
    }

    @Override public @Nonnull String name() {
        return name;
    }

    @Override public @Nonnull String getMainExtension() {
        return extensions.iterator().next();
    }

    @Override public @Nonnull Collection<String> getExtensions() {
        return extensions;
    }

    @Override public boolean isBinary() {
        return binary;
    }

    @Override public @Nonnull String toString() {
        return name();
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleRDFLang)) return false;
        SimpleRDFLang that = (SimpleRDFLang) o;
        return name.equals(that.name) && getExtensions().equals(that.getExtensions());
    }

    @Override public int hashCode() {
        return Objects.hash(name, getExtensions());
    }
}
