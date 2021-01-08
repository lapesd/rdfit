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

import com.github.lapesd.rdfit.source.RDFFile;
import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.source.SourcesIterator;
import com.github.lapesd.rdfit.source.impl.SingletonSourcesIterator;
import org.apache.commons.compress.utils.IOUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;

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
        List<String> contents = asList("file_a\n", "file_b\n", "file_c\n");
        return Stream.of(
                asList("test_1.tar.gz", contents),
                asList("test_1.tar.xz", contents),
                asList("test_1.tar.zst", contents),
                asList("test_1.9r.zip", contents),
                asList("test_1.7z", contents),
                asList("test_empty.tar.gz", Collections.emptyList()),
                asList("test_empty.zip", Collections.emptyList()),
                asList("single_a.bz2", Collections.singletonList("single_a\n")),
                asList("single_b.xz", Collections.singletonList("single_b\n"))
        ).map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "testData")
    public void testRDFInputStream(@Nonnull String resourcePath,
                                   @Nonnull List<String> expectedContents) throws IOException {
        InputStream is = getClass().getResourceAsStream(resourcePath);
        assertNotNull(is);
        RDFInputStream ris = new RDFInputStream(is);
        doTestRDFInputStream(expectedContents, ris);
    }

    @Test(dataProvider = "testData")
    public void testRDFFile(@Nonnull String resourcePath,
                                   @Nonnull List<String> expectedContents) throws IOException {
        InputStream is = getClass().getResourceAsStream(resourcePath);
        assertNotNull(is);
        doTestRDFInputStream(expectedContents, new RDFFile(extract(is), true));
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
                actual.add(new String(buf, StandardCharsets.UTF_8));
            }
        }
        assertEquals(new HashSet<>(actual), new HashSet<>(expectedContents));
        assertEquals(actual.size(), expectedContents.size());
    }
}