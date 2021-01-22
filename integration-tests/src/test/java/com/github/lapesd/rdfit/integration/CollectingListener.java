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

package com.github.lapesd.rdfit.integration;

import com.github.lapesd.rdfit.errors.InconvertibleException;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.listener.RDFListenerBase;
import com.github.lapesd.rdfit.util.NoSource;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CollectingListener extends RDFListenerBase<Object, Object> {
    public final List<Exception> exceptions = new ArrayList<>();
    public final List<String> messages = new ArrayList<>();
    public final List<String> badCalls = new ArrayList<>();
    public final List<Object> acTriples = new ArrayList<>();
    public final List<Object> acQuads = new ArrayList<>();

    public CollectingListener() {
        this(Object.class, Object.class);
    }
    public CollectingListener(Class<Object> tripleType, Class<Object> quadType) {
        super(tripleType, quadType);
    }

    @Override public void triple(@Nonnull Object triple) {
        if (tripleType() == null)
            badCalls.add("triple() called with null tripleClass");
        acTriples.add(triple);
    }

    @Override public void quad(@Nonnull Object quad) {
        if (quadType() == null)
            badCalls.add("quad() called with null quadClass");
        acQuads.add(quad);
    }

    @Override
    public boolean notifyInconvertibleTriple(@Nonnull InconvertibleException e) throws InterruptParsingException {
        exceptions.add(e);
        return super.notifyInconvertibleTriple(e);
    }

    @Override
    public boolean notifyInconvertibleQuad(@Nonnull InconvertibleException e) throws InterruptParsingException {
        exceptions.add(e);
        return super.notifyInconvertibleQuad(e);
    }

    @Override public boolean notifySourceError(@Nonnull RDFItException e) {
        exceptions.add(e);
        return super.notifySourceError(e);
    }

    @Override public boolean notifyParseWarning(@Nonnull String message) {
        messages.add(message);
        return super.notifyParseWarning(message);
    }

    @Override public boolean notifyParseError(@Nonnull String message) {
        messages.add(message);
        return super.notifyParseError(message);
    }

    @Override public void finish(@Nonnull Object source) {
        if (!Objects.equals(this.source, source))
            badCalls.add("Mismatched finish(" + source + "): expected " + this.source);
        super.finish(source);
    }

    @Override public void start(@Nonnull Object source) {
        if (!this.source.equals(NoSource.INSTANCE))
            badCalls.add("start(" + source + "): did not receive finish(" + this.source + ")");
        super.start(source);
    }
}
