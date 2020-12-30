package com.github.lapesd.rdfit.source.syntax.impl;

import com.github.lapesd.rdfit.source.syntax.RDFLangs;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Represents a particular RDF concrete syntax.
 *
 * Since this is used for parsing, a single instance should represent all possible serialization
 * options of a language (e.g., JSON-LD).
 *
 * @see RDFLangs for a enum-like container of predefined instances.
 */
public interface RDFLang {
    @Nonnull String name();
    @Nonnull String getMainExtension();
    default @Nonnull String getMainSuffix() {
        return "." + getMainExtension();
    }
    @Nonnull Collection<String> getExtensions();
    boolean isBinary();
}
