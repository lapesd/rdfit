package com.github.lapesd.rdfit.components.rdf4j.parsers.iterator;

import com.github.lapesd.rdfit.components.parsers.BaseItParser;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.iterator.ErrorRDFIt;
import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.iterator.RDFIt;
import org.eclipse.rdf4j.model.Statement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public abstract class AbstractRDF4JItParser extends BaseItParser {
    public AbstractRDF4JItParser(@Nonnull Collection<Class<?>> acceptedClasses,
                                 @Nonnull IterationElement itElement) {
        super(acceptedClasses, Statement.class, itElement);
    }

    protected @Nonnull <T> ErrorRDFIt<T>
    createError(@Nonnull Object source, @Nonnull String msg, @Nullable Throwable t) {
        RDFItException ex = t == null ? new RDFItException(source, msg)
                                      : new RDFItException(source, msg, t);
        //noinspection unchecked
        return new ErrorRDFIt<>((Class<T>) Statement.class, iterationElement(), source, ex);
    }

    protected @Nonnull <T> ErrorRDFIt<T> createError(@Nonnull Object source, @Nonnull String msg) {
        return createError(source, msg, null);
    }

    protected abstract @Nonnull RDFIt<Statement> doParse(@Nonnull Object source) throws Exception;

    @SuppressWarnings("unchecked")
    @Override public @Nonnull <T> RDFIt<T> parse(@Nonnull Object source) {
        boolean ok = false;
        for (Class<?> c : acceptedClasses()) {
            if ((ok = c.isAssignableFrom(source.getClass()))) break;
        }
        if (!ok)
            return createError(source, this+".parse() cannot handle "+source.getClass());
        try {
            return (RDFIt<T>) doParse(source);
        } catch (RDFItException e) {
            return new ErrorRDFIt<>((Class<T>)Statement.class, iterationElement(), source, e);
        } catch (Throwable t) {
            return createError(source, this+" failed to parse "+source, t);
        }
    }
}
