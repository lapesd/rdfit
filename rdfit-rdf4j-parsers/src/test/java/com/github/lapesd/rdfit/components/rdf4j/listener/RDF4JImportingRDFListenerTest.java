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

package com.github.lapesd.rdfit.components.rdf4j.listener;

import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.RIt;
import com.github.lapesd.rdfit.listener.TripleListenerBase;
import com.github.lapesd.rdfit.util.Utils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

public class RDF4JImportingRDFListenerTest {
    private static final String EX = "http://example.org/";
    private static final SimpleValueFactory SVF = SimpleValueFactory.getInstance();
    private static final IRI S1 = SVF.createIRI(EX+"S1");
    private static final IRI S2 = SVF.createIRI(EX+"S2");
    private static final IRI P2 = SVF.createIRI(EX+"P2");
    private static final IRI O2 = SVF.createIRI(EX+"O2");
    private static final IRI S3 = SVF.createIRI(EX+"S3");
    private static final IRI P3 = SVF.createIRI(EX+"P3");
    private static final IRI O3 = SVF.createIRI(EX+"O3");

    @Test
    public void test() throws Exception {
        String prefixes = "@prefix ex: <"+EX+">.\n@prefix owl: <"+ OWL.NAMESPACE +">.\n";
        File file = Files.createTempFile("rdfit", ".ttl").toFile();
        String fileURI = Utils.toASCIIString(file.toURI());
        String mainTTL = prefixes+"ex:S1 owl:imports <"+fileURI+">.\n" +
                "ex:S2 ex:P2 ex:O2.\n";
        String secondTTL = prefixes+"ex:S3 ex:P3 ex:O3.\n";
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), UTF_8)) {
            w.write(secondTTL);
        }

        RDFItFactory factory = RIt.createFactory();
        List<Statement> actual = new ArrayList<>();
        factory.parse(new RDF4JImportingRDFListener<>(new TripleListenerBase<Statement>(Statement.class) {
            @Override public void triple(@Nonnull Statement triple) {
                actual.add(triple);
            }
        }), mainTTL);
        assertEquals(actual, asList(
                SVF.createStatement(S1, OWL.IMPORTS, SVF.createIRI(fileURI)),
                SVF.createStatement(S2, P2, O2),
                SVF.createStatement(S3, P3, O3)
        ));
    }

}