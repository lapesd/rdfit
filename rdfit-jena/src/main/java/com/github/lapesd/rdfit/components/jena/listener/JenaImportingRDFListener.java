package com.github.lapesd.rdfit.components.jena.listener;

import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.components.converters.util.ConversionCache;
import com.github.lapesd.rdfit.listener.BaseImportingRDFListener;
import com.github.lapesd.rdfit.listener.RDFListener;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.OWL2;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.github.lapesd.rdfit.components.converters.util.ConversionPathSingletonCache.createCache;

public class JenaImportingRDFListener<T, Q> extends BaseImportingRDFListener<T, Q> {
    private static final @Nonnull Resource importsResource = OWL2.imports;
    private static final @Nonnull Node imports = OWL2.imports.asNode();
    private final @Nonnull ConversionCache tripleConversion, quad2tripleConversion;

    public JenaImportingRDFListener(@Nonnull RDFListener<T, Q> target) {
        super(target);
        tripleConversion = createCache(DefaultConversionManager.get(), Triple.class);
        quad2tripleConversion = createCache(DefaultConversionManager.get(), Triple.class);
    }

    @Override protected @Nullable String getTripleImportIRI(@Nonnull Object triple) {
        if (triple instanceof Statement) {
            Statement stmt = (Statement) triple;
            if (stmt.getPredicate().equals(importsResource)) {
                RDFNode object = stmt.getObject();
                if (object.isURIResource())
                    return object.asResource().getURI();
            }
        } else {
            Triple jenaTriple = (Triple) tripleConversion.convert(source, triple);
            if (jenaTriple.getPredicate().equals(imports)) {
                Node object = jenaTriple.getObject();
                if (object.isURI())
                    return object.getURI();
            }
        }
        return null;
    }

    @Override protected @Nullable String getQuadImportIRI(@Nonnull Object quad) {
        return getTripleImportIRI(quad2tripleConversion.convert(source, quad));
    }
}
