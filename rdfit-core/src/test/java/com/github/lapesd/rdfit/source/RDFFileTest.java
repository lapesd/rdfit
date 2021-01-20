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
import com.github.lapesd.rdfit.util.impl.EternalCache;
import com.github.lapesd.rdfit.util.impl.RDFBlob;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.function.Supplier;

import static org.testng.Assert.*;

public class RDFFileTest {
    private static final String SKOS_NS = "http://www.w3.org/2004/02/skos/core#";
    private byte[] SKOS_DATA;

    @BeforeClass
    public void beforeClass() throws Exception {
        Supplier<RDFInputStream> supplier = EternalCache.getDefault().get(new URL(SKOS_NS));
        assertNotNull(supplier);
        SKOS_DATA = Utils.toBytes(supplier.get().getInputStream());
    }

    @Test
    public void testExtractRDFBlob() throws IOException {
        RDFBlob blob = new RDFBlob(EternalCache.class, "../../skos.rdf",
                                   RDFLangs.RDFXML, SKOS_NS);
        RDFFile file = RDFFile.createTemp(blob);
        assertTrue(file.getDeleteOnClose());
        assertTrue(file.hasBaseIRI());
        assertEquals(file.getBaseIRI(), SKOS_NS);
        byte[] actual = Utils.toBytes(file.getInputStream());
        assertEquals(actual, SKOS_DATA);
    }

    @Test
    public void testExtractInputStream() throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(SKOS_DATA);
        RDFFile file = RDFFile.createTemp(is);
        assertTrue(file.getDeleteOnClose());
        assertEquals(file.getBaseIRI(), Utils.toASCIIString(file.getFile().toURI()));
        assertNull(file.getLang());
    }

}