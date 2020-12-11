package com.github.lapesd.rdfit.listener;

import com.github.lapesd.rdfit.util.Utils;

import javax.annotation.Nonnull;

import static com.github.lapesd.rdfit.util.Utils.compactClass;

public abstract class TripleListenerBase<T> extends RDFListenerBase<T, Void> {
    public TripleListenerBase(@Nonnull Class<T> tripleType) {
        super(tripleType);
    }

    @Override public void quad(@Nonnull Void quad) {
        throw new UnsupportedOperationException("This listener ("+this+") has quadType()==null. " +
                                                "quad(String, Object) should've been called.");
    }

    @Override public @Nonnull String toString() {
        return String.format("%s<T=%s>", Utils.toString(this), compactClass(tripleType()));
    }
}
