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

package com.github.lapesd.rdfit.components.hdt.listeners;

import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.listener.TripleListenerBase;
import com.github.lapesd.rdfit.util.NoSource;
import com.github.lapesd.rdfit.util.Utils;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.rdf.TripleWriter;
import org.rdfhdt.hdt.triples.TripleString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.OutputStream;

public class HDTFeeder extends TripleListenerBase<TripleString> {
    private static final Logger logger = LoggerFactory.getLogger(HDTFeeder.class);
    private static final HDTSpecification SPEC = new HDTSpecification();
    private final TripleWriter writer;
    protected final @Nonnull OutputStream outputStream;

    public HDTFeeder(@Nonnull OutputStream out,
                     @Nonnull String baseIRI) throws IOException {
        super(TripleString.class);
        this.writer = HDTManager.getHDTWriter(out, baseIRI, SPEC);
        this.outputStream = out;
    }

    @Override public void triple(@Nonnull TripleString triple) {
        try {
            writer.addTriple(triple);
        } catch (IOException e) {
            throw new RDFItException(source, this+" failed to write triple "+triple, e);
        }
    }

    @Override public void finish() {
        super.finish();
        try {
            writer.close();
        } catch (Exception e) {
            RDFItException ex = new RDFItException(NoSource.INSTANCE,
                                                   "Failed to close HDT TripleWriter", e);
            if (!notifySourceError(ex)) {
                logger.warn("Ignoring order to throw InterruptParsingException " +
                            "from {}.notifySourceError(), since we are already at finish()", this);
            }
        }
    }

    @Override public @Nonnull String toString() {
        return Utils.toString(this);
    }
}
