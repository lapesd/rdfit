/*
 *    Copyright 2021 Alexis Armin Huf
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
