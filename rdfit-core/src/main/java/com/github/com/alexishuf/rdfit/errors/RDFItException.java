package com.github.com.alexishuf.rdfit.errors;

import javax.annotation.Nonnull;

public class RDFItException extends RuntimeException {
    protected @Nonnull Object source;

    public RDFItException(@Nonnull Object source, @Nonnull String message) {
        super(message);
        this.source = source;
    }

    public RDFItException(@Nonnull Object source, @Nonnull String message,
                          @Nonnull Throwable cause) {
        super(message, cause);
        this.source = source;
    }

    public RDFItException(@Nonnull Object source, @Nonnull Throwable cause) {
        this(source, cause.getMessage(), cause);
    }

    public @Nonnull Object getSource() {
        return source;
    }
}
