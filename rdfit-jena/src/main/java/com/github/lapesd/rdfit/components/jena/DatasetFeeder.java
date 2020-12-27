package com.github.lapesd.rdfit.components.jena;

import com.github.lapesd.rdfit.components.jena.converters.JenaConverters;
import com.github.lapesd.rdfit.errors.ConversionException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.listener.RDFListenerBase;
import com.github.lapesd.rdfit.util.Utils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.Quad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class DatasetFeeder extends RDFListenerBase<Statement, Quad> {
    private static final Logger logger = LoggerFactory.getLogger(DatasetFeeder.class);
    private final @Nonnull Dataset dataset;

    public DatasetFeeder(@Nonnull Dataset dataset) {
        super(Statement.class, Quad.class);
        this.dataset = dataset;
    }

    @Override public void triple(@Nonnull Statement triple) {
        dataset.getDefaultModel().add(triple);
    }

    @Override public void quad(@Nonnull Quad quad) {
        if (quad.isDefaultGraph()) {
            try {
                Statement stmt = (Statement) JenaConverters.Quad2Statement.INSTANCE.convert(quad);
                triple(stmt);
            } catch (ConversionException e) {
                throw new RDFItException("Unexpected ConversionException", e);
            }
        } else {
            Node graph = quad.getGraph();
            if (!graph.isURI())
                throw new RDFItException(source, "Graph "+graph+" in quad "+quad+"is not an IRI");
            String uri = graph.getURI();
            Model model = dataset.getNamedModel(uri);
            if (model == null) {
                logger.debug("Creating a new graph {} in dataset", uri);
                dataset.addNamedModel(uri, ModelFactory.createDefaultModel());
                model = dataset.getNamedModel(uri);
                assert model != null;
            }
            model.getGraph().add(quad.asTriple());
        }
    }

    public @Nonnull Dataset getDataset() {
        return dataset;
    }

    @Override public @Nonnull String toString() {
        return Utils.toString(this);
    }
}
