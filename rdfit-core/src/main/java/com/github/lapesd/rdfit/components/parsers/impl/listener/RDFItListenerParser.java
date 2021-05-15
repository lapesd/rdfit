/*
 * Copyright 2021 Alexis Armin Huf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.lapesd.rdfit.components.parsers.impl.listener;

import com.github.lapesd.rdfit.components.parsers.BaseListenerParser;
import com.github.lapesd.rdfit.components.parsers.ListenerFeeder;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.iterator.RDFIt;
import com.github.lapesd.rdfit.listener.RDFListener;
import com.github.lapesd.rdfit.util.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;

import static com.github.lapesd.rdfit.util.Utils.compactClass;

public class RDFItListenerParser extends BaseListenerParser {
    public RDFItListenerParser(@Nullable Class<?> tripleClass, @Nullable Class<?> quadClass) {
        super(Collections.singleton(RDFIt.class), tripleClass, quadClass);
    }

    @Override
    public void parse(@Nonnull Object sourceObj,
                      @Nonnull RDFListener<?, ?> listener) throws InterruptParsingException {
        RDFIt<?> sourceIt = (RDFIt<?>) sourceObj;
        boolean isTriple = sourceIt.itElement().isTriple();
        try (ListenerFeeder feeder = createListenerFeeder(listener, sourceIt)) {
            if (isTriple) sourceIt.forEachRemaining(feeder::feedTriple);
            else          sourceIt.forEachRemaining(feeder::feedQuad  );
        } catch (InterruptParsingException e) {
            throw e;
        } catch (Throwable t) {
            if (!listener.notifySourceError(RDFItException.wrap(sourceIt, t)))
                throw new InterruptParsingException();
        }
    }

    @Override public boolean canParse(@Nonnull Object source) {
        if (!super.canParse(source))
            return false;
        RDFIt<?> it = (RDFIt<?>) source;
        Class<?> tt = tripleType(), qt = quadType();
        // one instance of RdfItListenerParser will be registered for each triple or quad type
        // thus only that instance will match the corresponding RDFIt.
        if (it.itElement().isTriple() && tt != null) {
            return tt.isAssignableFrom(it.valueClass());
        } else if (it.itElement().isQuad() && qt != null) {
            return qt.isAssignableFrom(it.valueClass());
        }
        // if control reached this, then a conversion will be necessary, just do it.
        return true;
    }

    @Override public @Nonnull String toString() {
        return String.format("%s{triple=%s, quad=%s}", Utils.toString(this),
                              compactClass(tripleType()), compactClass(quadType()));
    }
}
