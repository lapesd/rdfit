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

package com.github.lapesd.rdfit.source;

import com.github.lapesd.rdfit.source.syntax.RDFLangs;
import com.github.lapesd.rdfit.util.Utils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.*;

public class RDFResourceTest {
    private byte[] SKOS, RDF;
    private final String DIR = "com/github/lapesd/rdfit/";

    private byte[] readResource(@Nonnull String filename) throws IOException {
        String path = DIR + filename;
        InputStream is = ClassLoader.getSystemResourceAsStream(path);
        assertNotNull(is);
        return Utils.toBytes(is);
    }

    @BeforeClass
    public void beforeClass() throws Exception {
        SKOS = readResource("skos.rdf");
        RDF = readResource("rdf.ttl");
    }

    @Test
    public void testCreateRelative() throws Exception {
        RDFResource r = new RDFResource(RDFResource.class, "../skos.rdf");
        assertFalse(r.hasBaseIRI());
        assertEquals(r.getBaseIRI(), "file:"+DIR+"skos.rdf");
        assertNull(r.getLang());
        assertEquals(r.getOrDetectLang(), RDFLangs.RDFXML);
        assertEquals(Utils.toBytes(r.getInputStream()), SKOS);
    }

    @Test
    public void testCreateFullPath() throws Exception {
        RDFResource r = new RDFResource(DIR + "rdf.ttl");
        assertFalse(r.hasBaseIRI());
        assertEquals(r.getBaseIRI(), "file:"+DIR+"rdf.ttl");
        assertNull(r.getLang());
        assertEquals(Utils.toBytes(r.getInputStream()), RDF);
    }

    @Test
    public void testCreateFullPathWithIRI() throws Exception {
        String base = org.apache.jena.vocabulary.RDF.getURI().replaceAll("#$", "");
        RDFResource r = new RDFResource(DIR + "rdf.ttl", null, base);
        assertTrue(r.hasBaseIRI());
        assertEquals(r.getBaseIRI(), base);
        assertNull(r.getLang());
        assertEquals(Utils.toBytes(r.getInputStream()), RDF);
    }

    @Test
    public void testCreateWithLang() throws Exception {
        String base = org.apache.jena.vocabulary.RDF.getURI().replaceAll("#$", "");
        RDFResource r = new RDFResource(DIR + "rdf.ttl", RDFLangs.TTL, base);
        assertTrue(r.hasBaseIRI());
        assertEquals(r.getBaseIRI(), base);
        assertEquals(r.getLang(), RDFLangs.TTL);
        assertEquals(Utils.toBytes(r.getInputStream()), RDF);
    }

}