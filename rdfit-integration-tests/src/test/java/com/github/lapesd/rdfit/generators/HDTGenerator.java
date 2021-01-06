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
