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

package com.github.lapesd.rdfit.components.compress.normalizers.impl;

import com.github.lapesd.rdfit.source.RDFInputStream;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ArchiveEntrySourceIteratorTest {


    @DataProvider public static Object[][] testData() {
        return Stream.of(
                asList("../test_1.9r.zip", asList("", "file_a\n", "file_b\n", "file_c\n")),
                asList("../test_1.tar.xz", asList("", "file_a\n", "file_b\n", "file_c\n")),
                asList("../test_1.tar.gz", asList("", "file_a\n", "file_b\n", "file_c\n")),
                asList("../test_empty.tar.gz", Collections.emptyList()),
                asList("../test_empty.zip", Collections.emptyList())
                // cannot stream 7z, must open file
//                asList("test_1.7z", asList("file_a\n", "file_b\n", "file_c\n"))
        ).map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "testData")
    public void test(@Nonnull String resourcePath,
                     @Nonnull Collection<String> expectedContents) throws Exception {
        InputStream is = getClass().getResourceAsStream(resourcePath);
        assertNotNull(is);
        is = new BufferedInputStream(is);
        Object source = new Object();

        if (resourcePath.matches("^.*\\.tar\\.\\w+$")) {
            is = new CompressorStreamFactory().createCompressorInputStream(is);
            is = new BufferedInputStream(is);
        }
        ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(is);
        ArchiveEntrySourceIterator it = new ArchiveEntrySourceIterator(source, ais);

        List<String> actual = new ArrayList<>();
        while (it.hasNext()) {
            try (RDFInputStream ris = (RDFInputStream) it.next()) {
                byte[] arr = new byte[8192];
                arr = Arrays.copyOf(arr, IOUtils.readFully(ris.getInputStream(), arr));
                actual.add(new String(arr, StandardCharsets.UTF_8));
            }
        }
        assertEquals(new HashSet<>(actual), new HashSet<>(expectedContents));
        assertEquals(actual.size(), expectedContents.size());
    }

}