package com.github.lapesd.rdfit.listener;

import com.github.lapesd.rdfit.SourceQueue;
import com.github.lapesd.rdfit.errors.InconvertibleException;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.util.NoSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

@SuppressWarnings("unchecked")
public class DelegatingRDFListener<T, Q> implements RDFListener<T, Q> {
    protected final @Nonnull RDFListener<?, ?> target;
    protected @Nonnull Object source = NoSource.INSTANCE;

    public DelegatingRDFListener(@Nonnull RDFListener<?, ?> target) {
        this.target = target;
    }

    @Override public @Nullable Class<T> tripleType() {
        return (Class<T>) target.tripleType();
    }

    @Override public @Nullable Class<Q> quadType() {
        return (Class<Q>) target.quadType();
    }

    @Override public void attachSourceQueue(@Nonnull SourceQueue queue) {
        target.attachSourceQueue(queue);
    }

    @Override
    public boolean notifyInconvertibleTriple(@Nonnull InconvertibleException e)
            throws InterruptParsingException {
        return target.notifyInconvertibleTriple(e);
    }

    @Override
    public boolean notifyInconvertibleQuad(@Nonnull InconvertibleException e)
            throws InterruptParsingException {
        return target.notifyInconvertibleQuad(e);
    }

    @Override public boolean notifyParseWarning(@Nonnull String message) {
        return target.notifyParseWarning(message);
    }

    @Override public boolean notifyParseError(@Nonnull String message) {
        return target.notifyParseError(message);
    }

    @Override public boolean notifySourceError(@Nonnull RDFItException exception) {
        return target.notifySourceError(exception);
    }

    @Override public void triple(@Nonnull T triple) {
        ((RDFListener<T, Q>)target).triple(triple);
    }

    @Override public void quad(@Nonnull Q quad) {
        ((RDFListener<T, Q>)target).quad(quad);
    }

    @Override public void quad(@Nonnull String graph, @Nonnull T triple) {
        ((RDFListener<T, Q>)target).quad(graph, triple);
    }

    @Override public void prefix(@Nonnull String prefixLabel, @Nonnull String iriPrefix) {
        target.prefix(prefixLabel, iriPrefix);
    }

    @Override public void start(@Nonnull Object source) {
        target.start(source);
        this.source = source;
    }

    @Override public void finish(@Nonnull Object source) {
        if (!Objects.equals(this.source, source) && this.source != NoSource.INSTANCE)
            throw new IllegalStateException("finish("+source+"), expected finish("+this.source+")");
        this.source = NoSource.INSTANCE;
        target.finish(source);
    }

    @Override public void finish() {
        target.finish();
    }
}
