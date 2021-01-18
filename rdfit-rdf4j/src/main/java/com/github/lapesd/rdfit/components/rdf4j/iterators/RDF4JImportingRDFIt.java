package com.github.lapesd.rdfit.components.rdf4j.iterators;

import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.components.converters.util.ConversionCache;
import com.github.lapesd.rdfit.components.converters.util.ConversionPathSingletonCache;
import com.github.lapesd.rdfit.iterator.BaseImportingRDFIt;
import com.github.lapesd.rdfit.iterator.RDFIt;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.OWL;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RDF4JImportingRDFIt<T> extends BaseImportingRDFIt<T> {
    private static final IRI imports = OWL.IMPORTS;
    private final @Nonnull ConversionCache cache;

    public RDF4JImportingRDFIt(@Nonnull RDFIt<T> delegate) {
        super(delegate);
        DefaultConversionManager mgr = DefaultConversionManager.get();
        cache = ConversionPathSingletonCache.createCache(mgr, Statement.class);
    }

    public static @Nullable String getImportIRI(@Nonnull Statement stmt) {
        if (stmt.getPredicate().equals(imports)) {
            Value object = stmt.getObject();
            if (object.isIRI())
                return object.toString();
        }
        return null;
    }

    @Override protected @Nullable String getImportIRI(@Nonnull Object tripleOrQuad) {
        return getImportIRI((Statement)cache.convert(getSource(), tripleOrQuad));
    }
}
