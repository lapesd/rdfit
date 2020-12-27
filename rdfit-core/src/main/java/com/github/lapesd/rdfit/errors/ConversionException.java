package com.github.lapesd.rdfit.errors;

import com.github.lapesd.rdfit.components.Converter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.lang.String.format;

public class ConversionException extends Exception {
    private final @Nonnull Object input;
    private final @Nonnull Converter converter;
    private final @Nonnull Class<?> targetClass;
    private final @Nonnull String reason;

    private static @Nonnull String
    createMessage(@Nonnull Object input, @Nonnull Converter converter,
                  @Nonnull Class<?> targetClass, @Nonnull String reason) {
        return format("%s could not convert %s to %s: %s", converter, input, targetClass, reason);
    }

    public ConversionException(@Nonnull Object input, @Nonnull Converter converter,
                           @Nonnull String reason) {
        this(input, converter, converter.outputClass(), reason);
    }

    public ConversionException(@Nonnull Object input, @Nonnull Converter converter,
                               @Nonnull Class<?> targetClass, @Nonnull String reason) {
        this(input, converter, targetClass, reason, null);
    }

    public ConversionException(@Nonnull Throwable cause, @Nonnull Object input,
                               @Nonnull Converter converter, @Nonnull Class<?> targetClass,
                               @Nonnull String reason) {
        this(input, converter, targetClass, reason, cause);
    }

    protected ConversionException(@Nonnull Object input, @Nonnull Converter converter,
                                  @Nonnull Class<?> targetClass, @Nonnull String reason,
                                  @Nullable Throwable cause) {
        super(createMessage(input, converter, targetClass, reason), cause);
        this.input = input;
        this.converter = converter;
        this.targetClass = targetClass;
        this.reason = reason;
    }

    public @Nonnull Object getInput() {
        return input;
    }

    public @Nonnull Converter getConverter() {
        return converter;
    }

    public @Nonnull Class<?> getTargetClass() {
        return targetClass;
    }

    public @Nonnull String getReason() {
        return reason;
    }
}
