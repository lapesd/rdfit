package com.github.com.alexishuf.rdfit.callback;

import com.github.com.alexishuf.rdfit.errors.InconvertibleException;
import com.github.com.alexishuf.rdfit.errors.InterruptParsingException;
import com.github.com.alexishuf.rdfit.errors.RDFItException;
import com.github.com.alexishuf.rdfit.util.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DelegatingRDFCallback implements RDFCallback {
    protected final @Nonnull RDFCallback delegate;

    public DelegatingRDFCallback(@Nonnull RDFCallback delegate) {
        this.delegate = delegate;
    }

    @Override @Nonnull public Class<?> tripleType() {
        return delegate.tripleType();
    }

    @Override @Nullable public Class<?> quadType() {
        return delegate.quadType();
    }

    @Override
    public boolean notifyInconvertibleTriple(@Nonnull InconvertibleException e)
            throws InterruptParsingException {
        return delegate.notifyInconvertibleTriple(e);
    }

    @Override
    public boolean notifyInconvertibleQuad(@Nonnull InconvertibleException e)
            throws InterruptParsingException {
        return delegate.notifyInconvertibleQuad(e);
    }

    @Override
    public boolean notifySourceError(@Nonnull Object source, @Nonnull RDFItException exception) {
        return delegate.notifySourceError(source, exception);
    }

    @Override public <X> void triple(@Nonnull X triple) {
        delegate.triple(triple);
    }

    @Override public <Q> void quad(@Nonnull Q quad) {
        delegate.quad(quad);
    }

    @Override public <T> void quad(@Nonnull Object graph, @Nonnull T triple) {
        delegate.quad(graph, triple);
    }

    @Override public void start(@Nonnull Object source) {
        delegate.start(source);
    }

    @Override public void finish(@Nonnull Object source) {
        delegate.finish(source);
    }

    @Override public void finish() {
        delegate.finish();
    }

    @Override public String toString() {
        return String.format("%s{%s}", Utils.toString(this), delegate);
    }
}
