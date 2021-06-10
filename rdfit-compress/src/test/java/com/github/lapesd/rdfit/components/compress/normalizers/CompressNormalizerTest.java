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

package com.github.lapesd.rdfit.components.compress.normalizers;

import com.github.lapesd.rdfit.RIt;
import com.github.lapesd.rdfit.source.RDFFile;
import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.source.RDFResource;
import com.github.lapesd.rdfit.source.SourcesIterator;
import com.github.lapesd.rdfit.source.impl.SingletonSourcesIterator;
import com.github.lapesd.rdfit.util.Utils;
import org.apache.commons.compress.utils.IOUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;

import static com.github.lapesd.rdfit.source.fixer.TurtleFamilyFixerDecorator.TURTLE_FIXER;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.testng.Assert.*;

public class CompressNormalizerTest {
    private final @Nonnull List<File> tempFiles = new ArrayList<>();

    @AfterClass
    public void afterClass() {
        for (File f : tempFiles)
            assertTrue(!f.exists() || f.delete());
        tempFiles.clear();
    }

    private @Nonnull File extract(@Nonnull InputStream is) throws IOException {
        File f = Files.createTempFile("rdfit", "").toFile();
        f.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(f)) {
            IOUtils.copy(is, out);
        }
        return f;
    }

    @DataProvider public static Object[][] testData() {
        List<String> contents = asList("", "file_a\n", "file_b\n", "file_c\n");
        List<String> decoratedContents = asList(
                "<http://example.org/A%201> <http://example.org/p> \"lang{}\"@fr-1234.",
                "@prefix ex: <http://example.org>.\n[] ex:p <http://example.org/A%20%7B1%7D>, \"lang\"@pt-BR."
        );
        return Stream.of(
                asList("test_1.tar.gz", contents),
                asList("test_1.tar.xz", contents),
                asList("test_1.tar.zst", contents),
                asList("test_1.9r.zip", contents),
                asList("test_1.7z", contents),
                asList("test_empty.tar.gz", Collections.emptyList()),
                asList("test_empty.zip", Collections.emptyList()),
                asList("no_files.7z", Collections.emptyList()),
                asList("single_a.bz2", Collections.singletonList("single_a\n")),
                asList("single_b.xz", Collections.singletonList("single_b\n")),
                asList("test_decorator.tar.gz",  decoratedContents),
                asList("test_decorator.tar.zst", decoratedContents),
                asList("test_decorator.zip", decoratedContents),
                asList("test_decorator.7z",  decoratedContents),
                asList("test_decorator_file.nt.zst", decoratedContents.subList(0, 1)),
                asList("test_decorator_file.nt.gz",  decoratedContents.subList(0, 1))
        ).map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "testData")
    public void testRDFInputStream(@Nonnull String resourcePath,
                                   @Nonnull List<String> expectedContents) throws IOException {
        InputStream is = getClass().getResourceAsStream(resourcePath);
        assertNotNull(is);
        RDFInputStream ris = RDFInputStream.builder(is).decorator(TURTLE_FIXER).build();
        doTestRDFInputStream(expectedContents, ris);
    }

    @Test(dataProvider = "testData")
    public void testRDFFile(@Nonnull String resourcePath,
                                   @Nonnull List<String> expectedContents) throws IOException {
        InputStream is = getClass().getResourceAsStream(resourcePath);
        assertNotNull(is);
        RDFFile file = RDFFile.builder(extract(is)).deleteOnClose().decorator(TURTLE_FIXER).build();
        doTestRDFInputStream(expectedContents, file);
    }

    @DataProvider public @Nonnull Object[][] tolerantData() {
        return Stream.of(
                asList("lmdb-subset.bad-space.zip", "lmdb-subset.nt"),
                asList("lmdb-subset.bad-space.nt.zst", "lmdb-subset.nt"),
                asList("lmdb-subset.bad-space.tar.gz", "lmdb-subset.nt")
        ).map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "tolerantData")
    public void testTolerant(@Nonnull String badFile,
                             @Nonnull String fixedFile) throws IOException {
        ByteArrayOutputStream ac = new ByteArrayOutputStream(), ex = new ByteArrayOutputStream();
        try (InputStream in = Utils.openResource(getClass(), fixedFile)) {
            IOUtils.copy(in, ex);
        }
        try (RDFResource rdfRes = new RDFResource(getClass(), badFile)) {
            Object tolerant = RIt.tolerant(rdfRes);
            if (tolerant instanceof RDFInputStream) {
                try (InputStream in = ((RDFInputStream) tolerant).getInputStream()) {
                    IOUtils.copy(in, ac);
                }
            } else {
                SourcesIterator it = (SourcesIterator) tolerant;
                assertTrue(it.hasNext());
                try (InputStream in = ((RDFInputStream) it.next()).getInputStream()) {
                    IOUtils.copy(in, ac);
                }
                assertFalse(it.hasNext());
            }
        }

        assertEquals(new String(ac.toByteArray(), UTF_8), new String(ex.toByteArray(), UTF_8));
        assertEquals(ac.toByteArray(), ex.toByteArray());
    }

    private void doTestRDFInputStream(@Nonnull List<String> expectedContents, RDFInputStream ris) throws IOException {
        CompressNormalizer normalizer = new CompressNormalizer();
        Object source = ris;
        while (true) {
            Object normalized = normalizer.normalize(source);
            if (normalized == source)
                break;
            source = normalized;
        }
        if (!(source instanceof SourcesIterator))
            source = new SingletonSourcesIterator(source);
        SourcesIterator it = (SourcesIterator) source;
        assertNotNull(it);
        List<String> actual = new ArrayList<>();
        while (it.hasNext()) {
            try (RDFInputStream member = (RDFInputStream) it.next()) {
                byte[] buf = new byte[8192];
                buf = Arrays.copyOf(buf, IOUtils.readFully(member.getInputStream(), buf));
                actual.add(new String(buf, UTF_8));
            }
        }
        assertEquals(new HashSet<>(actual), new HashSet<>(expectedContents));
        assertEquals(actual.size(), expectedContents.size());
    }
}