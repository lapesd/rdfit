/*
 * Copyright 2021 Alexis Armin Huf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.lapesd.rdfit.integration;

import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.RIt;
import com.github.lapesd.rdfit.components.jena.JenaParsers;
import com.github.lapesd.rdfit.components.rdf4j.RDF4JParsers;
import com.github.lapesd.rdfit.impl.DefaultRDFItFactory;
import com.github.lapesd.rdfit.iterator.RDFIt;
import com.github.lapesd.rdfit.listener.RDFListenerBase;
import com.github.lapesd.rdfit.source.RDFFile;
import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.source.RDFInputStreamSupplier;
import com.github.lapesd.rdfit.source.RDFResource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jena.atlas.io.NullOutputStream;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Quad;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.github.lapesd.rdfit.util.Utils.openResource;
import static java.util.Arrays.asList;
import static org.testng.Assert.*;

public class NoExceptionParseTest {
    private @Nullable File tempDir;
    private RDFItFactory rdf4j, jena;

    @BeforeClass
    public void beforeClass() {
        rdf4j = RIt.createFactory();
        jena = RIt.createFactory();
        JenaParsers.unregisterAll(rdf4j);
        RDF4JParsers.unregisterAll(jena);
    }

    @AfterClass
    public void afterClass() throws IOException {
        if (tempDir != null && tempDir.exists()) {
            FileUtils.deleteDirectory(tempDir);
            tempDir = null;
        }
    }

    @DataProvider public @Nonnull Object[][] testData() throws IOException {
        List<String> names = asList(
                "dbpedia-nyt_links.nt",
                "eswc-2006-complete.bad-host.rdf",
                "eswc-2006-complete.rdf",
                "eswc-2009-complete+comment.rdf",
                "eswc-2009-complete+DOCTYPE.rdf",
                "eswc-2009-complete+encoding.rdf",
                "eswc-2009-complete+everything.rdf",
                "eswc-2009-complete.rdf",
                "foaf.rdf",
                "geonames-1.n3",
                "geonames-2.n3",
                "iswc-2008-complete.bad-space.rdf",
                "iswc-2008-complete.rdf",
                "LargeRDFBench-tbox.hdt",
                "LDOW-2008-complete.rdf",
                "linkedtcga-a-expression_gene_Lookup.nt",
                "linkedtcga-a-expression_gene_Lookup.unquoted.nt",
                "lmdb-subset.bad-space.nt",
                "lmdb-subset.nt",
                "nationwidechildrens.org_biospecimen_tumor_sample_lgg.nt",
                "owled-2007-complete+everything.rdf",
                "owled-2007-complete.rdf",
                "skos_categories_en.uchar.nt",
                "TCGA-BF-A1PU-01A-11D-A18Z-02_BC0VYMACXX---TCGA-BF-A1PU-10A-01D-A18Z-02_BC0VYMACXX---Segment.tsv.n3",
                "time.rdf"
        );
        List<Object> inputList = new ArrayList<>();
        if (tempDir == null) {
            tempDir = Files.createTempDirectory("rdfit-integration").toFile();
            for (String name : names) {
                File file = new File(tempDir, name);
                try (InputStream in = openResource(getClass(), name);
                     FileOutputStream out = new FileOutputStream(file)) {
                    IOUtils.copy(in, out);
                }
                inputList.add(file);
                inputList.add(file.getAbsolutePath());
                inputList.add((Supplier<RDFInputStream>)()-> new RDFFile(file));
                inputList.add("file://"+file.getAbsolutePath().replace('\\', '/').replace(" ", "%20"));
                inputList.add(file.toPath());
                inputList.add(file.toPath().toUri());
            }
        }
        String packageDir = getClass().getName().replace('.', '/').replaceAll("/[^/]+$", "");
        for (String name : names) {
            inputList.add((Supplier<?>) ()-> new RDFResource(getClass(), name));
            inputList.add((Supplier<?>) ()-> new RDFResource(packageDir+'/'+name));
            inputList.add((Supplier<?>) ()-> new RDFResource('/'+packageDir+'/'+name));
            inputList.add((Supplier<?>) ()-> new RDFInputStream(openResource(getClass(), name)));
            inputList.add((Supplier<?>) ()-> openResource(getClass(), name));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (InputStream in = openResource(getClass(), name)) {
                IOUtils.copy(in, out);
            }
            inputList.add((Supplier<?>) () -> new RDFInputStream(new ByteArrayInputStream(out.toByteArray())));
            inputList.add((Supplier<?>) () -> new ByteArrayInputStream(out.toByteArray()));
            inputList.add(out.toByteArray());
        }
        return inputList.stream().map(i -> new Object[]{i}).toArray(Object[][]::new);
    }

    private @Nonnull Object unwrapSupplier(@Nonnull Object object) {
        return object instanceof Supplier ? ((Supplier<?>) object).get() : object;
    }

    private void doParseTest(@Nonnull RDFItFactory factory, @Nonnull Class<?> tripleClass,
                             @Nullable Class<?> quadClass, @Nonnull Object input) {
        input = unwrapSupplier(input);
        AtomicInteger triples = new AtomicInteger(), quads = new AtomicInteger();
        //noinspection unchecked
        factory.parse(new RDFListenerBase<Object, Object>((Class<Object>) tripleClass,
                (Class<Object>) quadClass) {
            @Override public void triple(@Nonnull Object triple) {
                triples.incrementAndGet();
            }
            @Override public void quad(@Nonnull Object quad) {
                quads.incrementAndGet();
            }
        }, RIt.tolerant(input));
        assertTrue(triples.get() > 0);
        assertEquals(quads.get(), 0);
    }

    private void doIterateTriplesTest(@Nonnull RDFItFactory factory, @Nonnull Class<?> tripleClass,
                                      @Nonnull Object input) {
        input = unwrapSupplier(input);
        try (RDFIt<?> it = factory.iterateTriples(tripleClass, RIt.tolerant(input))) {
            assertTrue(it.hasNext());
            while (it.hasNext())
                assertTrue(tripleClass.isInstance(it.next()));
        }
    }

    private void doIterateQuadsTest(@Nonnull RDFItFactory factory, @Nonnull Class<?> quadClass,
                                      @Nonnull Object input) {
        input = unwrapSupplier(input);
        try (RDFIt<?> it = factory.iterateQuads(quadClass, RIt.tolerant(input))) {
            assertTrue(it.hasNext());
            while (it.hasNext())
                assertTrue(quadClass.isInstance(it.next()));
        }
    }

    @Test(dataProvider = "testData")
    public void testParseWithRDF4J(@Nonnull Object input) {
        doParseTest(rdf4j, org.eclipse.rdf4j.model.Statement.class, null, input);
        doParseTest(rdf4j, org.eclipse.rdf4j.model.Statement.class,
                           org.eclipse.rdf4j.model.Statement.class, input);
    }

    @Test(dataProvider = "testData")
    public void testParseWithJena(@Nonnull Object input) {
        doParseTest(jena, Triple.class, null, input);
        doParseTest(jena, Triple.class, Quad.class, input);
        doParseTest(jena, Statement.class, null, input);
        doParseTest(jena, Triple.class, Quad.class, input);
    }

    @Test(dataProvider = "testData")
    public void testParseWithDefault(@Nonnull Object input) {
        DefaultRDFItFactory fac = DefaultRDFItFactory.get();
        doParseTest(fac, org.eclipse.rdf4j.model.Statement.class, null, input);
        doParseTest(fac, org.eclipse.rdf4j.model.Statement.class,
                org.eclipse.rdf4j.model.Statement.class, input);
        doParseTest(fac, Triple.class, null, input);
        doParseTest(fac, Triple.class, Quad.class, input);
        doParseTest(fac, Statement.class, null, input);
        doParseTest(fac, Triple.class, Quad.class, input);
    }

    @Test(dataProvider = "testData")
    public void testIterateTriplesWithRDF4J(@Nonnull Object input) {
        doIterateTriplesTest(rdf4j, org.eclipse.rdf4j.model.Statement.class, input);
    }
    @Test(dataProvider = "testData")
    public void testIterateQuadsWithRDF4J(@Nonnull Object input) {
        doIterateQuadsTest(rdf4j, org.eclipse.rdf4j.model.Statement.class, input);
    }

    @Test(dataProvider = "testData")
    public void testIterateTriplesWithJena(@Nonnull Object input) {
        doIterateTriplesTest(jena, Triple.class, input);
        doIterateTriplesTest(jena, Statement.class, input);
    }
    @Test(dataProvider = "testData")
    public void testIterateQuadsWithJena(@Nonnull Object input) {
        doIterateQuadsTest(jena, Quad.class, input);
    }

    @Test(dataProvider = "testData")
    public void testIterateTriplesWithDefault(@Nonnull Object input) {
        DefaultRDFItFactory fac = DefaultRDFItFactory.get();
        doIterateTriplesTest(fac, Triple.class, input);
        doIterateTriplesTest(fac, Statement.class, input);
        doIterateTriplesTest(fac, org.eclipse.rdf4j.model.Statement.class, input);
    }
    @Test(dataProvider = "testData")
    public void testIterateQuadsWithDefault(@Nonnull Object input) {
        DefaultRDFItFactory fac = DefaultRDFItFactory.get();
        doIterateQuadsTest(fac, Quad.class, input);
        doIterateQuadsTest(fac, org.eclipse.rdf4j.model.Statement.class, input);
    }
}
