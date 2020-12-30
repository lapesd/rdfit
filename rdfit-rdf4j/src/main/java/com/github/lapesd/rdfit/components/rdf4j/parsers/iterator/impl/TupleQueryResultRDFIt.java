package com.github.lapesd.rdfit.components.rdf4j.parsers.iterator.impl;

import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.iterator.EagerRDFIt;
import com.github.lapesd.rdfit.iterator.IterationElement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class TupleQueryResultRDFIt extends EagerRDFIt<Statement> {
    private static final SimpleValueFactory FACTORY = SimpleValueFactory.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(TupleQueryResultRDFIt.class);
    private final @Nonnull Object source;
    private final @Nonnull TupleQueryResult result;
    private final @Nonnull List<String> vars;

    public TupleQueryResultRDFIt(@Nonnull IterationElement itElement, @Nonnull Object source,
                                 @Nonnull TupleQueryResult result) {
        super(Statement.class, itElement);
        this.source = source;
        this.result = result;
        this.vars = result.getBindingNames();
        int size = this.vars.size();
        if (size != 3 && size != 4) {
            throw new RDFItException(source, "Cannot view a TupleQueryResult as a set of " +
                    "Statements unless it has 3 or 4 variables (" + size + " given)");
        }
    }

    @Override public @Nonnull Object getSource() {
        return source;
    }

    @Override protected @Nullable Statement advance() {
        while (result.hasNext()) {
            BindingSet binding = result.next();
            Value sv = binding.getValue(vars.get(0));
            Value pv = binding.getValue(vars.get(1));
            Value ov = binding.getValue(vars.get(2));
            Value gv = vars.size() > 3 ? binding.getValue(vars.get(3)) : null;
            if (sv == null || pv == null || ov == null) {
                logger.warn("Skipping potential quad {} {} {} {}: null S/P/O", sv, pv, ov, gv);
            } else if (!sv.isResource()) {
                logger.warn("Skipping potential quad {} {} {} {}: subject is not a resource",
                        sv, pv, ov, gv);
            } else if (!pv.isIRI()) {
                logger.warn("Skipping potential quad {} {} {} {}: predicate is not an URI",
                        sv, pv, ov, gv);
            } else if (gv != null && !gv.isResource()) {
                logger.warn("Skipping potential quad {} {} {} {}: graph is not a Resource",
                        sv, pv, ov, gv);
            } else if (gv != null) {
                return FACTORY.createStatement((Resource) sv, (IRI) pv, ov, (Resource) gv);
            } else {
                return FACTORY.createStatement((Resource) sv, (IRI) pv, ov);
            }
        }
        return null;
    }

    @Override public void close() {
        try {
            result.close();
        } catch (Throwable t) {
            logger.error("Ignoring failure to close() {}.", result, t);
        }
        super.close();
    }
}
