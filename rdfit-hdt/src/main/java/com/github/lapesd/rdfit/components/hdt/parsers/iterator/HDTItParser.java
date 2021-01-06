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

package com.github.lapesd.rdfit.components.hdt.parsers.iterator;

import com.github.lapesd.rdfit.components.parsers.BaseItParser;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.iterator.PlainRDFIt;
import com.github.lapesd.rdfit.iterator.RDFIt;
import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.source.syntax.RDFLangs;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TripleString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

public class HDTItParser extends BaseItParser {
    private static final Logger logger = LoggerFactory.getLogger(HDTItParser.class);
    private static final @Nonnull Set<Class<?>> CLASSES = unmodifiableSet(new HashSet<>(asList(
            RDFInputStream.class, HDT.class
    )));

    public HDTItParser() {
        super(CLASSES, TripleString.class, IterationElement.TRIPLE);
    }

    @Override public boolean canParse(@Nonnull Object source) {
        if (!super.canParse(source)) return false;

        if (source instanceof RDFInputStream) {
            try {
                return RDFLangs.HDT.equals(((RDFInputStream)source).getOrDetectLang());
            } catch (IOException e) {
                logger.info("IOException guessing lang of {}. Accepting as a source", source);
                return true;
            }
        }
        return source instanceof HDT;
    }

    @Override public @Nonnull <T> RDFIt<T> parse(@Nonnull Object source) {
        HDT hdt;
        if (source instanceof RDFInputStream) {
            try (RDFInputStream ris = (RDFInputStream) source) {
                hdt = HDTManager.loadHDT(ris.getBufferedInputStream());
            } catch (Throwable t) {
                throw new RDFItException(source, "Problem reading input " + source, t);
            }
        } else if (source instanceof HDT) {
            hdt = (HDT) source;
        } else {
            throw new IllegalArgumentException("parse() called with unsupported source type");
        }

        IteratorTripleString it;
        try {
            it = hdt.search(null, null, null);
        } catch (NotFoundException e) {
            throw new RDFItException(source, "Unexpected exception  search()ing "+source, e);
        }
        return new PlainRDFIt<>(TripleString.class, IterationElement.TRIPLE, it, source);
    }
}
