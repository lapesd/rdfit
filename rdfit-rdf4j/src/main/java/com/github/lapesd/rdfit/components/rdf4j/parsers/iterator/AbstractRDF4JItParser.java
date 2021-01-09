/*
 *    Copyright 2021 Alexis Armin Huf
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
        return new ErrorRDFIt<>(Statement.class, itElement(), source, ex);
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
            return new ErrorRDFIt<>(Statement.class, itElement(), source, e);
        } catch (Throwable t) {
            return createError(source, this+" failed to parse "+source, t);
        }
    }
}
