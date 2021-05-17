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

package com.github.lapesd.rdfit.source;

import com.github.lapesd.rdfit.source.fixer.TurtleFamilyFixerDecorator;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;

import static com.github.lapesd.rdfit.source.syntax.RDFLangs.*;
import static com.github.lapesd.rdfit.util.Utils.extractResource;
import static com.github.lapesd.rdfit.util.Utils.openResource;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.testng.Assert.*;

public class FixTurtleWithDecoratorTest {
    private @Nullable File tempDir;
    private static final @Nonnull List<String> NAMES = asList("bad.nt", "bad.ttl");

    @BeforeClass
    public void beforeClass() throws IOException {
        if (tempDir != null)
            return; // no work
        tempDir = Files.createTempDirectory("rdfit").toFile();
        for (String name : NAMES) {
            String fixed = name.replaceAll("bad", "fixed");
            extractResource(new File(tempDir, name), getClass(), name);
            extractResource(new File(tempDir, fixed), getClass(), fixed);
        }
    }

    @AfterClass
    public void afterClass() throws IOException {
        try {
            if (tempDir != null)
                FileUtils.deleteDirectory(tempDir);
        } finally {
            tempDir = null;
        }
    }

    private @Nonnull Stream<RDFInputStream>
    sourcesFor(@Nonnull String filename) {
        try {
            TurtleFamilyFixerDecorator decorator = TurtleFamilyFixerDecorator.TURTLE_FIXER;
            File file = new File(tempDir, filename);
            byte[] bytes = IOUtils.toString(new FileInputStream(file), UTF_8).getBytes(UTF_8);
            FileInputStream fileInputStream1 = new FileInputStream(file);
            FileInputStream fileInputStream2 = new FileInputStream(file);
            String extension = FilenameUtils.getExtension(filename).toLowerCase();
            RDFLang lang = extension.equals("nt") ? NT : (extension.equals("ttl") ? TTL : UNKNOWN);
            assertNotEquals(lang, UNKNOWN);
            return Stream.of(
                    RDFResource.builder(getClass(), filename).decorator(decorator).build(),
                    RDFResource.builder(getClass(), filename).decorator(decorator)
                            .lang(lang).build(),
                    RDFFile.builder(file).decorator(decorator).build(),
                    RDFFile.builder(file).decorator(decorator)
                           .lang(lang).build(),
                    RDFInputStream.builder(fileInputStream1).decorator(decorator).build(),
                    RDFInputStream.builder(fileInputStream2).decorator(decorator)
                                  .lang(lang).build(),
                    RDFInputStreamSupplier.builder(() -> new FileInputStream(file))
                                          .decorator(decorator).build(),
                    RDFInputStreamSupplier.builder(() -> new FileInputStream(file))
                            .decorator(decorator).lang(lang).build(),
                    RDFBytesInputStream.builder(bytes).decorator(decorator).build(),
                    RDFBytesInputStream.builder(bytes).decorator(decorator).lang(lang).build()
            );
        } catch (IOException e) {
            throw new AssertionError("Could not generate sources for "+filename, e);
        }
    }

    private @Nonnull String expectedContents(@Nonnull String filename) {
        String fixedFilename = filename.replaceAll("bad", "fixed");
        try {
            return IOUtils.toString(openResource(getClass(), fixedFilename), UTF_8);
        } catch (IOException e) {
            throw new AssertionError("Could not read resource"+fixedFilename, e);
        }
    }

    @DataProvider public @Nonnull Object[][] testData() throws IOException {
        beforeClass(); //ensure files are already extracted
        return NAMES.stream().flatMap(n -> sourcesFor(n).map(s -> asList(s, expectedContents(n))))
                .map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "testData")
    public void test(@Nonnull RDFInputStream in, @Nonnull String expected) throws IOException {
        assertEquals(IOUtils.toString(in.getInputStream(), UTF_8), expected);
    }
}
