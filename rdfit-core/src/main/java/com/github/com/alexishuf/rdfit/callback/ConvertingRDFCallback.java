package com.github.com.alexishuf.rdfit.callback;

import com.github.com.alexishuf.rdfit.components.CallbackParser;
import com.github.com.alexishuf.rdfit.conversion.ConversionManager;
import com.github.com.alexishuf.rdfit.conversion.ConversionPathSingletonCache;
import com.github.com.alexishuf.rdfit.errors.InconvertibleException;
import com.github.com.alexishuf.rdfit.util.NoSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class ConvertingRDFCallback extends DelegatingRDFCallback {
    protected @Nonnull Class<?> tripleType;
    protected @Nullable Class<?> quadType;
    protected @Nonnull ConversionPathSingletonCache tripleConversion;
    protected @Nullable ConversionPathSingletonCache quadConversion;
    protected @Nullable ConversionPathSingletonCache quadTripleConversion;
    private @Nonnull Object source = NoSource.INSTANCE;

    public ConvertingRDFCallback(@Nonnull RDFCallback callback,
                                 @Nonnull Class<?> rcvTripleType,
                                 @Nullable Class<?> rcvQuadType,
                                 @Nonnull ConversionManager convMgr) {
        super(callback);
        this.tripleType = rcvTripleType;
        this.quadType   = rcvQuadType  ;
        this.tripleConversion = new ConversionPathSingletonCache(convMgr, rcvTripleType);
        if (rcvQuadType != null) {
            this.quadConversion = new ConversionPathSingletonCache(convMgr, rcvQuadType);
            this.quadTripleConversion = new ConversionPathSingletonCache(convMgr, rcvTripleType);
        }
    }

    public static @Nonnull RDFCallback
    createIf(@Nonnull RDFCallback callback, @Nonnull CallbackParser parser,
             @Nonnull ConversionManager conversionManager) {
        return createIf(callback, parser.tripleType(), parser.quadType(), conversionManager);
    }
    public static @Nonnull RDFCallback createIf(@Nonnull RDFCallback cb,
                                                @Nullable Class<?> ot, @Nullable Class<?> oq,
                                                @Nonnull ConversionManager convMgr) {
        if (ot == null && oq == null)
            throw new IllegalArgumentException("Both ot and oq are null");
        Class<?> ct = cb.tripleType(), cq = cb.quadType();
        Class<?> rt = ot == null ? ct : ot;
        boolean needs = (ot == null && cq == null && !ct.isAssignableFrom(oq)) // oq -> ct
                     || (ot != null && !ct.isAssignableFrom(ot))                // ot -> ct
                     || (oq != null && cq != null && !cq.isAssignableFrom(oq)); //oq -> cq
        if (needs)
            return new ConvertingRDFCallback(cb, rt, oq, convMgr);
        return cb;
    }

    @Override public @Nonnull Class<?> tripleType() {
        return tripleType;
    }

    @Override public @Nullable Class<?> quadType() {
        return quadType;
    }

    @Override public <T> void triple(@Nonnull T triple) {
        Object converted = null;
        try {
            converted = tripleConversion.convert(source, triple);
        } catch (InconvertibleException e) {
            delegate.notifyInconvertibleTriple(e);
        }
        if (converted != null)
            delegate.triple(converted);
    }

    @Override public <T> void quad(@Nonnull Object graph, @Nonnull T triple) {
        Object converted = null;
        try {
            converted = tripleConversion.convert(source, triple);
        } catch (InconvertibleException e) {
            delegate.notifyInconvertibleTriple(e);
        }
        if (converted != null)
            delegate.quad(graph, converted);
    }

    @Override public <Q> void quad(@Nonnull Q quad) {
        assert quadConversion != null;
        assert quadTripleConversion != null;
        assert quadType != null;
        if (delegate.quadType() == null) {
            Object converted = null;
            try {
                converted = quadTripleConversion.convert(source, quad);
            } catch (InconvertibleException e) {
                delegate.notifyInconvertibleTriple(e);
            }
            if (converted != null)
                delegate.triple(converted);
        } else {
            Object converted = null;
            try {
                converted = quadConversion.convert(source, quad);
            } catch (InconvertibleException e) {
                delegate.notifyInconvertibleQuad(e);
            }
            if (converted != null)
                delegate.quad(converted);
        }
    }

    @Override public void start(@Nonnull Object source) {
        this.source = source;
        super.start(source);
    }

    @Override public void finish(@Nonnull Object source) {
        if (!Objects.equals(this.source, source))
            throw new IllegalStateException("finish("+source+"), expected finish("+this.source+")");
        this.source = NoSource.INSTANCE;
        super.finish(source);
    }
}
