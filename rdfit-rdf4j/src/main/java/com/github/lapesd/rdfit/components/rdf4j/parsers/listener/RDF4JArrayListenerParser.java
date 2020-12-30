package com.github.lapesd.rdfit.components.rdf4j.parsers.listener;

import com.github.lapesd.rdfit.components.parsers.ListenerFeeder;
import com.github.lapesd.rdfit.components.parsers.impl.listener.BaseJavaListenerParser;
import org.eclipse.rdf4j.model.Statement;

import javax.annotation.Nonnull;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;

public class RDF4JArrayListenerParser extends BaseJavaListenerParser {

    public RDF4JArrayListenerParser() {
        super(Array.newInstance(Statement.class, 0).getClass(),
              Statement.class, Statement.class);
    }

    @Override protected @Nonnull Iterator<?> createIterator(@Nonnull Object source) {
        return Arrays.asList((Object[]) source).iterator();
    }

    @Override protected boolean feed(@Nonnull ListenerFeeder feeder, @Nonnull Object element) {
        Statement stmt = (Statement) element;
        if (stmt.getContext() == null)
            return feeder.feedTriple(stmt);
        else
            return feeder.feedQuad(stmt);
    }
}
