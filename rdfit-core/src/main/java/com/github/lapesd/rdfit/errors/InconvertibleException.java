package com.github.lapesd.rdfit.errors;

import javax.annotation.Nonnull;

public class InconvertibleException extends RDFItException {
    private final @Nonnull Object input;
    private final @Nonnull Class<?> desired;

    public InconvertibleException(@Nonnull Object source, @Nonnull Object input, @Nonnull Class<?> desired) {
        super(source, String.format("Could not find a conversion path from %s (class %s) to %s",
                                    input, input.getClass(), desired));
        this.input = input;
        this.desired = desired;
    }

    public @Nonnull Object getInput() {
        return input;
    }

    public @Nonnull Class<?> getDesired() {
        return desired;
    }
}
