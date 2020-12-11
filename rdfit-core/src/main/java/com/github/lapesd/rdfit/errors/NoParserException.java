package com.github.lapesd.rdfit.errors;

import javax.annotation.Nonnull;

public class NoParserException extends RDFItException {
    public NoParserException(@Nonnull Object source) {
        super(source, "No parser accepts source "+source);
    }
}
