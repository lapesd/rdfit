package com.github.lapesd.rdfit.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Utils {
    public static @Nonnull String compactClass(@Nullable Class<?> cls) {
        return cls == null ? "null" : cls.getName().replaceAll("(\\w)[^.]+\\.", "$1.");
    }

    public static @Nonnull String genericToString(@Nonnull Object instance,
                                           @Nonnull Class<?>... params) {
        StringBuilder builder = new StringBuilder(compactClass(instance.getClass()));
        builder.append('<');
        for (Class<?> param : params)
            builder.append(", ").append(compactClass(param));
        if (params.length > 0)
            builder.setLength(builder.length()-2);
        builder.append(String.format(">@%x", System.identityHashCode(instance)));
        return builder.append('>').toString();
    }

    public static @Nonnull String toString(@Nonnull Object instance) {
        return String.format("%s@%x", compactClass(instance.getClass()),
                                      System.identityHashCode(instance));
    }
}
