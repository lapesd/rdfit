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
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.utils.IOUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SevenZSourceIteratorTest {

    @Test
    public void test() throws IOException {

        File file = Files.createTempFile("rdfit", "").toFile();
        try {
            file.deleteOnExit();
            try (InputStream in = getClass().getResourceAsStream("../test_1.7z");
                 FileOutputStream out = new FileOutputStream(file)) {
                IOUtils.copy(in, out);
            }

            SevenZFile szFile = new SevenZFile(file);
            SevenZSourceIterator it = new SevenZSourceIterator(file, szFile);
            List<String> contents = new ArrayList<>();
            while (it.hasNext()) {
                Object source = it.next();
                try (RDFInputStream ris = (RDFInputStream) source) {
                    byte[] buf = new byte[8192];
                    buf = Arrays.copyOf(buf, IOUtils.readFully(ris.getInputStream(), buf));
                    contents.add(new String(buf, StandardCharsets.UTF_8));
                }
            }
            HashSet<String> expected = new HashSet<>(asList("", "file_a\n", "file_b\n", "file_c\n"));
            assertEquals(new HashSet<>(contents), expected);
        } finally {
            assertTrue(!file.exists() || file.delete());
        }
    }
}