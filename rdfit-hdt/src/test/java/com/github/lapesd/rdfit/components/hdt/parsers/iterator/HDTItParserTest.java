package com.github.lapesd.rdfit.components.hdt.parsers.iterator;

import com.github.lapesd.rdfit.DefaultRDFItFactory;
import com.github.lapesd.rdfit.components.hdt.HDTHelpers;
import com.github.lapesd.rdfit.errors.InconvertibleException;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.iterator.RDFIt;
import com.github.lapesd.rdfit.listener.TripleListenerBase;
import com.github.lapesd.rdfit.source.RDFFile;
import com.github.lapesd.rdfit.source.syntax.RDFLangs;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.rdf.TripleWriter;
import org.rdfhdt.hdt.triples.TripleString;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class HDTItParserTest {
    private static final String EX = "http://example.org/";
    private static final String S1 = EX+"S1";
    private static final String P1 = EX+"P1";
    private static final String O1 = EX+"O1";
    private static final String S2 = EX+"S2";
    private static final String P2 = EX+"P2";
    private static final String O2 = EX+"O2";

    private static final List<File> tempFiles = new ArrayList<>();

    @AfterClass
    public void afterClass() {
        for (File file : tempFiles) {
            if (file.exists() && !file.delete())
                fail("Failed to delete file "+file);
        }
        tempFiles.clear();
    }

    private @Nonnull File createFile() throws IOException {
        File file = Files.createTempFile("rdfit-hdt", ".hdt").toFile();
        file.deleteOnExit();
        tempFiles.add(file);
        return file;
    }

    @DataProvider public Object[][] testData() throws Exception {
        File f = createFile();
        TripleWriter writer = HDTManager.getHDTWriter(f.getAbsolutePath(), f.toURI().toString(),
                                                      new HDTSpecification());
        writer.addTriple(new TripleString(S1, P1, O1));
        writer.addTriple(new TripleString(S2, P2, O2));
        writer.close();

        HDT hdt = HDTManager.generateHDT(asList(new TripleString(S1, P1, O1),
                                         new TripleString(S1, P2, O2)).iterator(),
                                         "file://nowhere.hdt", new HDTSpecification(),
                                         (level, message) -> {});
        List<TripleString> ex1 = asList(new TripleString(S1, P1, O1), new TripleString(S2, P2, O2));
        List<TripleString> ex2 = asList(new TripleString(S1, P1, O1), new TripleString(S1, P2, O2));

        return Stream.of(
                asList(f, ex1),
                asList(new RDFFile(f, RDFLangs.HDT), ex1),
                asList(new RDFFile(f), ex1),
                asList(f.toPath(), ex1),
                asList(f.toURI(), ex1),
                asList(f.toURI().toURL(), ex1),
                asList(f.toURI().toString(), ex1),
                asList(f.getAbsolutePath(), ex1),
                asList("file://"+f.getAbsolutePath().replaceAll(" ", "%20"), ex1),

                asList(hdt, ex2),

                asList(ex1, ex1),
                asList(new LinkedList<>(ex1), ex1),
                asList(ex1.toArray(new TripleString[0]), ex1)
        ).map(List::toArray).toArray(Object[][]::new);
    }

    private void assertTripleStringsEqual(@Nonnull List<TripleString> expected, List<TripleString> actual) {
        assertEquals(actual.size(), expected.size());
        for (int i = 0, size = actual.size(); i < size; i++) {
            TripleString a = actual.get(i), e = expected.get(i);
            assertEquals(a.getSubject().toString(), e.getSubject().toString());
            assertEquals(a.getPredicate().toString(), e.getPredicate().toString());
            assertEquals(a.getObject().toString(), e.getObject().toString());
        }
    }

    @Test(dataProvider = "testData")
    public void testIterate(Object source, @Nonnull List<TripleString> expected) {
        DefaultRDFItFactory factory = new DefaultRDFItFactory();
        HDTHelpers.registerAll(factory);
        List<TripleString> actual = new ArrayList<>();
        try (RDFIt<TripleString> it = factory.iterateTriples(TripleString.class, source)) {
            while (it.hasNext())
                actual.add(it.next());
        }
        assertTripleStringsEqual(expected, actual);
    }

    @Test(dataProvider = "testData")
    public void testParse(Object source, @Nonnull List<TripleString> expected) {
        DefaultRDFItFactory factory = new DefaultRDFItFactory();
        HDTHelpers.registerAll(factory);
        List<TripleString> actual = new ArrayList<>();
        List<Exception> exceptions = new ArrayList<>();
        factory.parse(new TripleListenerBase<TripleString>(TripleString.class) {
            @Override public void triple(@Nonnull TripleString triple) {
                actual.add(triple);
            }

            @Override
            public boolean notifyInconvertibleTriple(@Nonnull InconvertibleException e) throws InterruptParsingException {
                exceptions.add(e);
                return true;
            }

            @Override
            public boolean notifyInconvertibleQuad(@Nonnull InconvertibleException e) throws InterruptParsingException {
                exceptions.add(e);
                return true;
            }

            @Override
            public boolean notifySourceError(@Nonnull RDFItException e) {
                exceptions.add(e);
                return true;
            }
        }, source);

        assertTripleStringsEqual(actual, expected);
        assertEquals(exceptions, Collections.emptyList());
    }

}