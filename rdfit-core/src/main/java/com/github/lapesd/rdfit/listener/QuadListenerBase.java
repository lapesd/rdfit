package com.github.lapesd.rdfit.listener;

import com.github.lapesd.rdfit.util.Utils;

import javax.annotation.Nonnull;

import static com.github.lapesd.rdfit.util.Utils.compactClass;

public abstract class QuadListenerBase<Q> extends RDFListenerBase<Void, Q> {
    public QuadListenerBase(@Nonnull Class<Q> quadType) {
        super(null, quadType);
    }

    @Override public void triple(@Nonnull Void triple) {
        throw new UnsupportedOperationException("This listener ("+this+") has tripleType()==null");
    }

    @Override public void quad(@Nonnull String graph, @Nonnull Void triple) {
        throw new UnsupportedOperationException("This listener ("+this+") has tripleType()==null. " +
                                                "quad(Object) should've been called instead");
    }

    @Override public @Nonnull String toString() {
        return String.format("%s<Q=%s>", Utils.toString(this), compactClass(quadType()));
    }
}
