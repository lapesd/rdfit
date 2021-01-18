package com.github.lapesd.rdfit.components.hdt.iterator;

import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.components.converters.util.ConversionCache;
import com.github.lapesd.rdfit.components.converters.util.ConversionPathSingletonCache;
import com.github.lapesd.rdfit.iterator.BaseImportingRDFIt;
import com.github.lapesd.rdfit.iterator.RDFIt;
import org.rdfhdt.hdt.triples.TripleString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Pattern;

public class HDTImportingRDFIt<T> extends BaseImportingRDFIt<T> {
    private static final Pattern URL_RX = Pattern.compile("^[^:/]+://");
    private static final String imports = "http://www.w3.org/2002/07/owl#imports";

    private final ConversionCache conversion;

    public HDTImportingRDFIt(@Nonnull RDFIt<T> delegate) {
        super(delegate);
        DefaultConversionManager mgr = DefaultConversionManager.get();
        conversion = ConversionPathSingletonCache.createCache(mgr, TripleString.class);
    }

    public static @Nullable String getImportIRI(@Nonnull TripleString triple) {
        if (triple.getPredicate().toString().equals(imports)) {
            String object = triple.getObject().toString();
            if (URL_RX.matcher(object).find())
                return object;
        }
        return null;
    }

    @Override protected @Nullable String getImportIRI(@Nonnull Object tripleOrQuad) {
        TripleString ts = (TripleString) conversion.convert(getSource(), tripleOrQuad);
        return getImportIRI(ts);
    }
}
