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

package com.github.lapesd.rdfit.generators;

import com.github.lapesd.rdfit.TripleSet;
import com.github.lapesd.rdfit.components.hdt.converters.HDTConverters;
import org.apache.jena.graph.Triple;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.listener.ProgressOut;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.triples.TripleString;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HDTGenerator implements SourceGenerator {
    @Override public boolean isReusable() {
        return true;
    }

    @Override public @Nonnull List<?> generate(@Nonnull TripleSet tripleSet,
                                               @Nonnull File tempDir) {
        if (tripleSet.hasQuads())
            return Collections.emptyList();
        try {
            List<TripleString> tripleStrings = new ArrayList<>();
            for (Triple triple : tripleSet.export(Triple.class))
                tripleStrings.add(HDTConverters.Triple2TripleString.INSTANCE.convert(triple));
            String baseURI = "http://example.org/baseHDTGenerator";
            HDT hdt = HDTManager.generateHDT(tripleStrings.iterator(), baseURI,
                                             new HDTSpecification(), new ProgressOut());
            return Collections.singletonList(hdt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
