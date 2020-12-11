package com.github.com.alexishuf.rdfit.util;

import javax.annotation.Nonnull;

public class Utils {
    public static @Nonnull String compactClassName(@Nonnull Class<?> cls) {
        return cls.getName().replaceAll("(\\w)[^.]+\\.", "$1.");
    }

    public static @Nonnull String genericToString(@Nonnull Object instance,
                                           @Nonnull Class<?>... params) {
        StringBuilder builder = new StringBuilder(compactClassName(instance.getClass()));
        builder.append('<');
        for (Class<?> param : params)
            builder.append(", ").append(compactClassName(param));
        if (params.length > 0)
            builder.setLength(builder.length()-2);
        builder.append(String.format(">@%x", System.identityHashCode(instance)));
        return builder.append('>').toString();
    }

    public static @Nonnull String toString(@Nonnull Object instance) {
        return String.format("%s@%x", compactClassName(instance.getClass()),
                                      System.identityHashCode(instance));
    }
}
