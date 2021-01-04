package com.github.lapesd.rdfit.components.jena.listener;

import com.github.lapesd.rdfit.components.jena.converters.JenaConverters.Triple2Statement;
import com.github.lapesd.rdfit.errors.InconvertibleException;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.listener.RDFListener;
import com.github.lapesd.rdfit.util.Utils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import static java.lang.String.format;

public class ListenerStreamRDF implements StreamRDF {
    private static final Logger logger = LoggerFactory.getLogger(ListenerStreamRDF.class);

    private final @Nonnull RDFListener<?, ?> target;
    private final @Nonnull Object source;

    public ListenerStreamRDF(@Nonnull RDFListener<?, ?> target,
                             @Nonnull Object source) {
        Class<?> tt = target.tripleType(), qt = target.quadType();
        if (tt == null && qt == null)
            throw new IllegalArgumentException("target has no tripleType nor quadType");
        if (tt != null && !tt.isAssignableFrom(Triple.class) && !tt.isAssignableFrom(Statement.class))
            throw new IllegalArgumentException("target expects "+tt+" triples");
        if (qt != null && !qt.isAssignableFrom(Quad.class))
            throw new IllegalArgumentException("target expects "+qt+" quads");
        this.target = target;
        this.source = source;
    }


    @Override public void start() {
        target.start(source);
    }

    @SuppressWarnings("unchecked") @Override public void triple(Triple triple) {
        assert triple != null;
        Class<?> tt = target.tripleType(), qt = target.quadType();
        try {
            if (tt == null) {
                assert qt != null && qt.isAssignableFrom(Quad.class);
                ((RDFListener<?, Quad>) target).quad(new Quad(Quad.defaultGraphIRI, triple));
            } else if (tt.isAssignableFrom(Triple.class)) {
                ((RDFListener<Triple, ?>) target).triple(triple);
            } else if (tt.isAssignableFrom(Statement.class)) {
                Statement stmt = (Statement) Triple2Statement.INSTANCE.convert(triple);
                ((RDFListener<Statement, ?>) target).triple(stmt);
            } else {
                throw new IllegalStateException("target requires " + tt + " for triples");
            }
        } catch (InconvertibleException e) {
            if (!target.notifyInconvertibleTriple(e))
                throw new InterruptJenaParsingException();
        } catch (InterruptParsingException e) {
            throw e;
        } catch (Throwable t) {
            if (target.notifySourceError(RDFItException.wrap(source, t)))
                throw new InterruptJenaParsingException();
            else
                throw new InterruptParsingException();
        }
    }

    @SuppressWarnings("unchecked") @Override public void quad(Quad quad) {
        try {
            Class<?> tt = target.tripleType();
            if (target.quadType() == null) {
                assert tt != null;
                if (tt.equals(Triple.class)) {
                    ((RDFListener<Triple, ?>) target).triple(quad.asTriple());
                } else if (tt.equals(Statement.class)) {
                    Statement stmt = (Statement) Triple2Statement.INSTANCE.convert(quad.asTriple());
                    ((RDFListener<Statement, ?>) target).triple(stmt);
                } else {
                    throw new IllegalStateException("target requires " + tt + " for triples");
                }
            } else {
                Node graph = quad.getGraph();
                boolean defGraph = graph == null || graph.equals(Quad.defaultGraphNodeGenerated);
                if (defGraph && (Triple.class.equals(tt) || Statement.class.equals(tt))) {
                    Triple triple = quad.asTriple();
                    if (tt.equals(Triple.class)) {
                        ((RDFListener<Triple, ?>)target).triple(triple);
                    } else {
                        Statement stmt = (Statement) Triple2Statement.INSTANCE.convert(triple);
                        ((RDFListener<Statement, ?>)target).triple(stmt);
                    }
                } else {
                    if (defGraph)
                        quad = new Quad(Quad.defaultGraphIRI, quad.asTriple());
                    ((RDFListener<?, Quad>) target).quad(quad);
                }
            }
        } catch (InconvertibleException e) {
            if (!target.notifyInconvertibleQuad(e))
                throw new InterruptJenaParsingException();
        } catch (InterruptParsingException e) {
            throw e;
        } catch (Throwable t) {
            if (target.notifySourceError(RDFItException.wrap(source, t)))
                throw new InterruptJenaParsingException();
            else
                throw new InterruptParsingException();
        }
    }

    @Override public void base(String base) {
        logger.trace("{}.base({})", this, base);
    }

    @Override public void prefix(String prefix, String iri) {
        logger.trace("{}.prefix({}, {})", this, prefix, iri);
    }

    @Override public void finish() {
        target.finish(source);
    }

    @Override public @Nonnull String toString() {
        return format("%s{source=%s,target=%s}", Utils.toString(this), source, target);
    }
}
