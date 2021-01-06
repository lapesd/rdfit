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

package com.github.lapesd.rdfit.components.jena.parsers.iterator;

import com.github.lapesd.rdfit.components.parsers.BaseItParser;
import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.iterator.PlainRDFIt;
import com.github.lapesd.rdfit.iterator.RDFIt;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import javax.annotation.Nonnull;
import java.util.Collections;

public class ModelItParser extends BaseItParser {
    public ModelItParser() {
        super(Collections.singleton(Model.class), Statement.class, IterationElement.TRIPLE);
    }

    @Override public @Nonnull <T> RDFIt<T> parse(@Nonnull Object source) {
        StmtIterator it = ((Model) source).listStatements();
        return new PlainRDFIt<>(Statement.class, IterationElement.TRIPLE, it, source);
    }
}
