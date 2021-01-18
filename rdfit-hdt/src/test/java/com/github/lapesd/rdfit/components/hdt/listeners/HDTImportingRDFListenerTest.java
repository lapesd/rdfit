package com.github.lapesd.rdfit.components.hdt.listeners;

import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.RIt;
import com.github.lapesd.rdfit.listener.TripleListenerBase;
import com.github.lapesd.rdfit.util.Utils;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.triples.TripleString;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class HDTImportingRDFListenerTest {
    private static final String imports = "http://www.w3.org/2002/07/owl#imports";
    private static final String EX = "http://example.org/";
    private static final String S1 = EX+"S1";
    private static final String S2 = EX+"S2";
    private static final String P2 = EX+"P2";
    private static final String O2 = EX+"O2";

    @Test
    public void test() throws Exception {
        File hdtFile = Files.createTempFile("rdfit", ".hdt").toFile();
        hdtFile.deleteOnExit();
        String hdtURI = Utils.toASCIIString(hdtFile.toURI());
        HDT mainHDT = HDTManager.generateHDT(
                singletonList(new TripleString(S1, imports, hdtURI)).iterator(),
                "file:///nowhere.hdt", new HDTSpecification(), (l, m) -> { });
        HDT hdt = HDTManager.generateHDT(singletonList(new TripleString(S2, P2, O2)).iterator(),
                hdtURI, new HDTSpecification(), (l, m) -> {});
        hdt.saveToHDT(hdtFile.getAbsolutePath(), (l, m) -> {});

        try {
            RDFItFactory factory = RIt.createFactory();
            List<List<String>> actual = new ArrayList<>();
            factory.parse(new HDTImportingRDFListener<>(
                    new TripleListenerBase<TripleString>(TripleString.class) {
                @Override public void triple(@Nonnull TripleString t) {
                    actual.add(asList(t.getSubject().toString(), t.getPredicate().toString(),
                                      t.getObject().toString()));
                }
            }), mainHDT);
            assertEquals(actual, asList(
                    asList(S1, imports, hdtURI),
                    asList(S2, P2, O2)
            ));
        } finally {
            assertTrue(hdtFile.delete());
        }
    }

}