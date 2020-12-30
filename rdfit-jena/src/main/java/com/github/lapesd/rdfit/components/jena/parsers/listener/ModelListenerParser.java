package com.github.lapesd.rdfit.components.jena.parsers.listener;

import com.github.lapesd.rdfit.components.parsers.BaseListenerParser;
import com.github.lapesd.rdfit.components.parsers.ListenerFeeder;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.listener.RDFListener;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import javax.annotation.Nonnull;
import java.util.Collections;

public class ModelListenerParser extends BaseListenerParser {
    public ModelListenerParser() {
        super(Collections.singleton(Model.class), Statement.class);
    }

    @Override
    public void parse(@Nonnull Object source, @Nonnull RDFListener<?, ?> listener)
            throws InterruptParsingException {
        try (ListenerFeeder feeder = createListenerFeeder(listener, source)) {
            try {
                StmtIterator it = ((Model) source).listStatements();
                while (it.hasNext())
                    if (!feeder.feedTriple(it.next()))
                        break;
            } catch (InterruptParsingException e) {
                throw e;
            } catch (Throwable t) {
                if (!listener.notifySourceError(RDFItException.wrap(source, t)))
                    throw new InterruptParsingException();
            }
        }
    }
}
