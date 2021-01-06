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

package com.github.lapesd.rdfit.components.rdf4j.parsers.listener;

import com.github.lapesd.rdfit.components.parsers.BaseListenerParser;
import com.github.lapesd.rdfit.components.rdf4j.listener.RDFListenerHandler;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.listener.RDFListener;
import org.eclipse.rdf4j.model.Statement;

import javax.annotation.Nonnull;
import java.util.Collection;

public abstract class RDFHandlerParser extends BaseListenerParser {
    public RDFHandlerParser(@Nonnull Collection<Class<?>> acceptedClasses) {
        super(acceptedClasses, Statement.class, Statement.class);
    }

    protected abstract void parse(@Nonnull Object source, @Nonnull RDFListenerHandler handler);

    @Override
    public void parse(@Nonnull Object source,
                      @Nonnull RDFListener<?, ?> listener) throws InterruptParsingException {
        boolean ok = false;
        for (Class<?> cls : acceptedClasses()) {
            if ((ok = cls.isAssignableFrom(source.getClass()))) break;
        }
        if (!ok) {
            String msg = String.format("%s.parse() received a %s, expected one of %s",
                                       this, source.getClass(), acceptedClasses());
            listener.notifySourceError(new RDFItException(source, msg));
        } else {
            try (RDFListenerHandler handler = new RDFListenerHandler(listener, source)) {
                parse(source, handler);
            } catch (InterruptParsingException e) {
                throw e;
            } catch (Throwable t) {
                if (!listener.notifySourceError(RDFItException.wrap(source, t)))
                    throw new InterruptParsingException();
            }
        }
    }
}
