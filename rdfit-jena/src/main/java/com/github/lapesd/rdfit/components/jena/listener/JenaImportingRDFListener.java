package com.github.lapesd.rdfit.components.jena.listener;

import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.components.converters.util.ConversionCache;
import com.github.lapesd.rdfit.components.jena.iterators.JenaImportingRDFIt;
import com.github.lapesd.rdfit.listener.BaseImportingRDFListener;
import com.github.lapesd.rdfit.listener.RDFListener;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.Quad;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.github.lapesd.rdfit.components.converters.util.ConversionPathSingletonCache.createCache;

public class JenaImportingRDFListener<T, Q> extends BaseImportingRDFListener<T, Q> {
    private final @Nonnull ConversionCache tripleConversion, quad2tripleConversion;

    public JenaImportingRDFListener(@Nonnull RDFListener<T, Q> target) {
        super(target);
        tripleConversion = createCache(DefaultConversionManager.get(), Triple.class);
        quad2tripleConversion = createCache(DefaultConversionManager.get(), Triple.class);
    }

    @Override protected @Nullable String getTripleImportIRI(@Nonnull Object triple) {
        if (!(triple instanceof Statement || triple instanceof Quad))
            triple = tripleConversion.convert(source, triple);
        return JenaImportingRDFIt.getImportIRIJena(triple);
    }

    @Override protected @Nullable String getQuadImportIRI(@Nonnull Object quad) {
        return getTripleImportIRI(quad2tripleConversion.convert(source, quad));
    }
}
