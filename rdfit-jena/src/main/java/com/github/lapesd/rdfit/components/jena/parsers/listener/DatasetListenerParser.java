package com.github.lapesd.rdfit.components.jena.parsers.listener;

import com.github.lapesd.rdfit.components.parsers.BaseListenerParser;
import com.github.lapesd.rdfit.components.parsers.ListenerFeeder;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.listener.RDFListener;
import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

public class DatasetListenerParser extends BaseListenerParser {
    private static final @Nonnull Set<Class<?>> CLASSES = unmodifiableSet(new HashSet<>(asList(
            Dataset.class, DatasetGraph.class
    )));

    public DatasetListenerParser() {
        super(CLASSES, null, Quad.class);
    }

    public void parse(@Nonnull Object source, @Nonnull DatasetGraph dsg,
                      @Nonnull RDFListener<?, ?> listener) throws InterruptParsingException {
        try (ListenerFeeder feeder = createListenerFeeder(listener, source)) {
            try {
                Iterator<Quad> it = dsg.find();
                while (it.hasNext()) {
                    if (!feeder.feedQuad(it.next()))
                        break;
                }
            } catch (InterruptParsingException e) {
                throw e;
            } catch (Throwable t) {
                if (!listener.notifySourceError(RDFItException.wrap(source, t)))
                    throw new InterruptParsingException();
            }
        }
    }

    @Override
    public void parse(@Nonnull Object source,
                      @Nonnull RDFListener<?, ?> listener) throws InterruptParsingException {
        DatasetGraph dsg = source instanceof DatasetGraph ? (DatasetGraph) source
                                                          : ((Dataset)source).asDatasetGraph();
        parse(source, dsg, listener);
    }
}
