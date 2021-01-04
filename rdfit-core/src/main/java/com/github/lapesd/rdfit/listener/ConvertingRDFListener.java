package com.github.lapesd.rdfit.listener;

import com.github.lapesd.rdfit.components.ListenerParser;
import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.components.converters.util.ConversionCache;
import com.github.lapesd.rdfit.components.converters.util.ConversionPathSingletonCache;
import com.github.lapesd.rdfit.errors.InconvertibleException;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.util.NoSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class ConvertingRDFListener<T, Q> implements RDFListener<T, Q> {
    protected @Nullable Class<T> tripleType;
    protected @Nullable Class<Q> quadType;
    protected @Nonnull RDFListener<Object, Object> target;
    protected @Nonnull ConversionCache tripleConversion;
    protected @Nonnull ConversionCache quadConversion;
    protected @Nonnull ConversionCache quadTripleConversion;
    private @Nonnull Object source = NoSource.INSTANCE;

    public ConvertingRDFListener(@Nonnull RDFListener<?, ?> target,
                                 @Nullable Class<T> rcvTripleType,
                                 @Nullable Class<Q> rcvQuadType,
                                 @Nonnull ConversionManager convMgr) {
        if (rcvTripleType == null && rcvQuadType != null)
            throw new IllegalArgumentException("Both rcvTripleType and rcvQuadType are null");
        //noinspection unchecked
        this.target = (RDFListener<Object, Object>) target;
        this.tripleType = rcvTripleType;
        this.quadType   = rcvQuadType  ;
        Class<?> outTripleType = target.tripleType();
        Class<?> outQuadType = target.quadType();
        this.tripleConversion = ConversionPathSingletonCache.createCache(convMgr, outTripleType);
        this.quadTripleConversion = ConversionPathSingletonCache.createCache(convMgr, outTripleType);
        this.quadConversion = ConversionPathSingletonCache.createCache(convMgr, outQuadType);
    }

    public static @Nonnull <T, Q> RDFListener<T, Q>
    createIf(@Nonnull RDFListener<T, Q> listener, @Nonnull ListenerParser parser,
             @Nonnull ConversionManager conversionManager) {
        return createIf(listener, parser.tripleType(), parser.quadType(), conversionManager);
    }
    public static @Nonnull <T, Q> RDFListener<T, Q>
    createIf(@Nonnull RDFListener<T, Q> cb, @Nullable Class<?> ot, @Nullable Class<?> oq,
             @Nonnull ConversionManager convMgr) {
        if (ot == null && oq == null)
            throw new IllegalArgumentException("Both ot and oq are null");
        Class<?> ct = cb.tripleType(), cq = cb.quadType();
        if (ct == null && cq == null)
            throw new IllegalArgumentException("Callback has both tripleType and quadType null");
        Class<?> rt = ot == null ? ct : ot;
        boolean needs = (oq != null && cq == null && !ct.isAssignableFrom(oq))  // oq -> ct
                     || (ot != null && ct != null && !ct.isAssignableFrom(ot))  // ot -> ct
                     || (oq != null && cq != null && !cq.isAssignableFrom(oq)); //oq -> cq
        if (needs) {
            //noinspection unchecked
            return  (RDFListener<T, Q>)new ConvertingRDFListener<>(cb, rt, oq, convMgr);
        }
        return cb;
    }

    @Override public @Nullable Class<T> tripleType() {
        return tripleType;
    }

    @Override public @Nullable Class<Q> quadType() {
        return quadType;
    }

    @Override public void triple(@Nonnull T triple) {
        Object converted = null;
        try {
            converted = tripleConversion.convert(source, triple);
        } catch (InconvertibleException e) {
            target.notifyInconvertibleTriple(e);
        }
        if (converted != null)
            target.triple(converted);
    }

    @Override public void quad(@Nonnull String graph, @Nonnull T triple) {
        Object converted = null;
        try {
            converted = tripleConversion.convert(source, triple);
        } catch (InconvertibleException e) {
            target.notifyInconvertibleTriple(e);
        }
        if (converted != null)
            target.quad(graph, converted);
    }

    @Override public void quad(@Nonnull Q quad) {
        assert quadType != null;
        if (target.quadType() == null) {
            Object converted = null;
            try {
                converted = quadTripleConversion.convert(source, quad);
            } catch (InconvertibleException e) {
                target.notifyInconvertibleTriple(e);
            }
            if (converted != null)
                target.triple(converted);
        } else {
            Object converted = null;
            try {
                converted = quadConversion.convert(source, quad);
            } catch (InconvertibleException e) {
                target.notifyInconvertibleQuad(e);
            }
            if (converted != null)
                target.quad(converted);
        }
    }

    @Override public void start(@Nonnull Object source) {
        this.source = source;
        target.start(source);
    }

    @Override public void finish(@Nonnull Object source) {
        if (!Objects.equals(this.source, source) && this.source != NoSource.INSTANCE)
            throw new IllegalStateException("finish("+source+"), expected finish("+this.source+")");
        this.source = NoSource.INSTANCE;
        target.finish(source);
    }

    @Override public boolean notifyInconvertibleTriple(@Nonnull InconvertibleException e)
            throws InterruptParsingException {
        return target.notifyInconvertibleTriple(e);
    }

    @Override public boolean notifyInconvertibleQuad(@Nonnull InconvertibleException e)
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

    @Override public void finish() {
        target.finish();
    }
}
