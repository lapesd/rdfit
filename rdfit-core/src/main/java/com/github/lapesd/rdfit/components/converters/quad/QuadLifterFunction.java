package com.github.lapesd.rdfit.components.converters.quad;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class QuadLifterFunction extends QuadLifterBase {
    protected final @Nonnull Function<Object, Object> function;

    public <T> QuadLifterFunction(@Nonnull Class<T>  tripleType,
                                  @Nonnull Function<? super T, ?> function) {
        super(tripleType);
        //noinspection unchecked
        this.function = (Function<Object, Object>) function;
    }

    @Override public @Nonnull Object lift(@Nonnull Object triple) {
        return function.apply(triple);
    }
}
