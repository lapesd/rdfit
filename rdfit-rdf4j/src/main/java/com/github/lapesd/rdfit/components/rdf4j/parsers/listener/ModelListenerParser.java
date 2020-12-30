package com.github.lapesd.rdfit.components.rdf4j.parsers.listener;

import com.github.lapesd.rdfit.components.rdf4j.listener.RDFListenerHandler;
import org.eclipse.rdf4j.model.Model;

import javax.annotation.Nonnull;
import java.util.Collections;

public class ModelListenerParser extends RDFHandlerParser {

    public ModelListenerParser() {
        super(Collections.singleton(Model.class));
    }

    @Override protected void parse(@Nonnull Object source, @Nonnull RDFListenerHandler handler) {
        handler.startRDF();
        ((Model)source).iterator().forEachRemaining(handler::handleStatement);
        handler.endRDF(); //on error, superclass will call endRDF() after notifying
    }
}
