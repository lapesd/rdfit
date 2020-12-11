package com.github.lapesd.rdfit.components.converters;

import com.github.lapesd.rdfit.components.Converter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ConversionPath implements Function<Object, Object> {
    private final @Nonnull List<Converter> path;
    public static final @Nonnull ConversionPath EMPTY
            = new ConversionPath(Collections.emptyList());

    public ConversionPath(@Nonnull List<Converter> path) {
        this.path = path;
    }

    @Override public @Nullable Object apply(@Nullable Object o) {
        return o == null ? null : convert(o);
    }

    public @Nullable Object convert(@Nonnull Object input) {
        Object object = input;
        for (Converter converter : path) {
            if ((object = converter.convert(object)) == null) break;
        }
        return object;
    }

    public boolean canConvert(@Nullable Object object) {
        return object == null || path.isEmpty() || path.get(0).canConvert(object);
    }

    @Override public String toString() {
        StringBuilder builder = new StringBuilder("ConversionPath{");
        for (Converter converter : path)
            builder.append(converter).append(", ");
        assert !path.isEmpty();
        builder.setLength(builder.length()-2);
        return builder.append('}').toString();
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConversionPath)) return false;
        ConversionPath that = (ConversionPath) o;
        return path.equals(that.path);
    }

    @Override public int hashCode() {
        return Objects.hash(path);
    }
}
