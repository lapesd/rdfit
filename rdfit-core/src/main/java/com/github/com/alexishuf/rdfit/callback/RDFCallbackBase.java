package com.github.com.alexishuf.rdfit.callback;

import com.github.com.alexishuf.rdfit.errors.InconvertibleException;
import com.github.com.alexishuf.rdfit.errors.InterruptParsingException;
import com.github.com.alexishuf.rdfit.errors.RDFItException;
import com.github.com.alexishuf.rdfit.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class RDFCallbackBase implements RDFCallback {
    private static final Logger logger = LoggerFactory.getLogger(RDFCallbackBase.class);
    protected @Nonnull Class<?> tripleType;
    protected @Nullable Class<?> quadType;

    public RDFCallbackBase(@Nonnull Class<?> tripleType) {
        this(tripleType, null);
    }

    public RDFCallbackBase(@Nonnull Class<?> tripleType, @Nullable Class<?> quadType) {
        this.tripleType = tripleType;
        this.quadType = quadType;
    }

    @Override
    public boolean notifyInconvertibleTriple(@Nonnull InconvertibleException e)
            throws InterruptParsingException {
        logger.warn("Ignoring inconvertible triple {} of type {} from source {} (desired: {}).",
                    e.getInput(), e.getDesired(), e.getSource(), tripleType());
        throw new InterruptParsingException();
    }

    @Override
    public boolean notifyInconvertibleQuad(@Nonnull InconvertibleException e)
            throws InterruptParsingException {
        logger.warn("Ignoring inconvertible quad {} of type {} from source {} (desired: {}).",
                    e.getInput(), e.getDesired(), e.getSource(), tripleType());
        throw new InterruptParsingException();
    }

    @Override
    public boolean notifySourceError(@Nonnull Object source, @Nonnull RDFItException e) {
        logger.error("{}.notifySourceError(): Failed to parse {}. Stopping", this, source, e);
        return false; //do not parse next sources
    }

    @Override public @Nonnull Class<?> tripleType() {
        return tripleType;
    }

    @Override public @Nullable Class<?> quadType() {
        return quadType;
    }

    @Override public <Q> void quad(@Nonnull Q quad) {
        if (quadType() != null)
            throw new UnsupportedOperationException("quad(quad) not implemented");
        else
            throw new UnsupportedOperationException("should have called quad(graph, triple)");
    }

    @Override public <T> void quad(@Nonnull Object graph, @Nonnull T triple) {
        if (quadType() == null)
            triple(triple);
        else
            throw new UnsupportedOperationException("should have called quad(quad)");
    }

    @Override public void finish(@Nonnull Object source) {
        logger.debug("{}.finish({})", this, source);
    }

    @Override public void start(@Nonnull Object source) {
        logger.debug("{}.start({})", this, source);
    }

    @Override public void finish() {
        logger.debug("{}.finish()", this);
    }

    @Override public String toString() {
        return String.format("%s[tripleType=%s,quadType=%s]", Utils.toString(this),
                              tripleType(), quadType());
    }
}
