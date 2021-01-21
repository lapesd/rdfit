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

package com.github.lapesd.rdfit.components.normalizers.impl;

import com.github.lapesd.rdfit.RIt;
import com.github.lapesd.rdfit.source.RDFFile;
import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.source.RDFInputStreamSupplier;
import com.github.lapesd.rdfit.source.RDFResource;
import com.github.lapesd.rdfit.util.Utils;
import com.google.common.base.Stopwatch;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

public class URLNormalizerTest {

    @Test
    public void testGetSKOS() throws Exception {
        URLNormalizer normalizer = new URLNormalizer();
        URL https = new URL("https://www.w3.org/2004/02/skos/core#");

        Stopwatch sw = Stopwatch.createStarted();
        Object normalized = normalizer.normalize(https);
        assertFalse(normalized instanceof RDFInputStreamSupplier);
        assertTrue(sw.elapsed(TimeUnit.MILLISECONDS) < 900);

        sw.reset().start();
        byte[] bytes = Utils.toBytes(((RDFInputStream) normalized).getInputStream());
        assertTrue(bytes.length > 15000);
        assertTrue(sw.elapsed(TimeUnit.MILLISECONDS) < 900);
    }

    @Test
    public void testCache() throws Exception {
        byte[] skos;
        try (RDFResource ris = new RDFResource(RIt.class, "skos.rdf")) {
            skos = Utils.toBytes(ris.getInputStream());
        }

        URLNormalizer normalizer = new URLNormalizer();
        normalizer.setCacheFiles(true);
        try (RDFFile temp = RDFFile.createTemp(new ByteArrayInputStream(skos))) {
            URL url = temp.getFile().toURI().toURL();
            try (RDFInputStream ris = (RDFInputStream) normalizer.normalize(url)) {
                assertEquals(Utils.toBytes(ris.getInputStream()), skos);
            }
            try (Writer w = new OutputStreamWriter(new FileOutputStream(temp.getFile()))) {
                w.write("CLEARED");
            }
            try (RDFInputStream ris = (RDFInputStream) normalizer.normalize(url)) {
                assertEquals(Utils.toBytes(ris.getInputStream()), skos);
            }
        }
    }

    @Test
    public void testDoNotCacheFiles() throws Exception {
        byte[] skosBytes, rdfBytes;
        try (RDFResource ris = new RDFResource(RIt.class, "skos.rdf")) {
            skosBytes = Utils.toBytes(ris.getInputStream());
        }
        try (RDFResource ris = new RDFResource(RIt.class, "rdf.ttl")) {
            rdfBytes = Utils.toBytes(ris.getInputStream());
        }

        URLNormalizer normalizer = new URLNormalizer();
        try (RDFFile temp = RDFFile.createTemp(new RDFResource(RIt.class, "skos.rdf"))) {
            URL url = temp.getFile().toURI().toURL();
            try (RDFInputStream ris = (RDFInputStream) normalizer.normalize(url)) {
                byte[] actual = Utils.toBytes(ris.getInputStream());
                assertEquals(actual, skosBytes);
            }

            try (FileOutputStream out = new FileOutputStream(temp.getFile());
                 RDFResource ris = new RDFResource(RIt.class, "rdf.ttl")) {
                IOUtils.copy(ris.getInputStream(), out);
            }
            try (RDFInputStream ris = (RDFInputStream) normalizer.normalize(url)) {
                byte[] actual = Utils.toBytes(ris.getInputStream());
                assertEquals(actual, rdfBytes);
            }
        }
    }

}