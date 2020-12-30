package com.github.lapesd.rdfit.errors;

import javax.annotation.Nonnull;

import static java.lang.String.format;

public class InconvertibleException extends RDFItException {
    private final @Nonnull Object input;
    private final @Nonnull Class<?> desired;

    public InconvertibleException(@Nonnull Object source, @Nonnull Object input, @Nonnull Class<?> desired) {
        this(source, input, desired, format("Could not find a conversion path from %s (%s) to %s",
                                            input, input.getClass(), desired));
    }
    public InconvertibleException(@Nonnull Object source, @Nonnull Object input,
                                  @Nonnull Class<?> desired, @Nonnull String reason) {
        super(source, reason);
        this.input = input;
        this.desired = desired;
    }

    public InconvertibleException(@Nonnull Object source, @Nonnull Object in,
                                  @Nonnull Class<?> desired,
                                  @Nonnull ConversionException e) {
        super(source, format("Could not find a working conversion path from %s (%s) to %s. " +
                             "First path error: %s", in, in.getClass(), desired, e.getMessage()),
              e);
        this.input = in;
        this.desired = desired;
    }

    public @Nonnull Object getInput() {
        return input;
    }

    public @Nonnull Class<?> getDesired() {
        return desired;
    }
}
