package com.github.lapesd.rdfit.errors;

import javax.annotation.Nonnull;

public class RDFItException extends RuntimeException {
    protected @Nonnull Object source;

    public static @Nonnull RDFItException wrap(@Nonnull Object source, @Nonnull Throwable t) {
        return t instanceof RDFItException ? (RDFItException) t : new RDFItException(source, t);
    }

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
