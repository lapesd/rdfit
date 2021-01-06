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

package com.github.lapesd.rdfit.components.normalizers;

import com.github.lapesd.rdfit.components.parsers.BaseListenerParser;
import com.github.lapesd.rdfit.components.parsers.DefaultParserRegistry;
import com.github.lapesd.rdfit.data.TripleMock1;
import com.github.lapesd.rdfit.listener.RDFListener;
import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.source.syntax.RDFLangs;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public abstract class SourceNormalizerRegistryTestBase {
    private static final String EX = "http://example.org/";
    private static final String NT = "<"+EX+"S> <"+EX+"P> <"+EX+"O>.\n";
    private static final String NQ = "<"+EX+"S> <"+EX+"P> <"+EX+"O>.\n" +
                                     "<"+EX+"S> <"+EX+"P> <"+EX+"O> <"+EX+"G>.\n";
    private final List<File> tempFiles = new ArrayList<>();

    private @Nonnull File createFile(@Nonnull String contents)  {
        File file;
        try {
            file = Files.createTempFile("rdfit", "").toFile();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        file.deleteOnExit();
        tempFiles.add(file);
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), UTF_8)) {
            w.write(contents);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return file;
    }

    protected abstract @Nonnull SourceNormalizerRegistry createRegistry();

    protected @Nonnull SourceNormalizerRegistry createFilledRegistry() {
        SourceNormalizerRegistry r = createRegistry();
        registerMockParsers(r);
        return r;
    }

    protected void registerMockParsers(@Nonnull SourceNormalizerRegistry registry) {
        DefaultParserRegistry parserRegistry = new DefaultParserRegistry();
        Set<Class<?>> classes = Collections.singleton(RDFInputStream.class);
        for (RDFLang lang : RDFLangs.getLangs()) {
            parserRegistry.register(new BaseListenerParser(classes, TripleMock1.class) {
                @Override
                public void parse(@Nonnull Object source, @Nonnull RDFListener<?, ?> listener)  {
                    throw new UnsupportedOperationException();
                }
            });
        }
        registry.setParserRegistry(parserRegistry);
    }

    @AfterClass
    public void afterClass() {
        for (File file : tempFiles) {
            if (file.exists() && !file.delete())
                fail("Could not delete "+file);
        }
    }

    private Predicate<Object> has(@Nonnull String content) {
        return o -> {
            if (!(o instanceof RDFInputStream)) return false;
            try {
                String s = IOUtils.toString(((RDFInputStream) o).getInputStream(), UTF_8);
                return s.equals(content);
            } catch (IOException e) {
                return false;
            }
        };
    }

    @DataProvider public Object[][] testData() throws Exception {
        List<List<Object>> rows = new ArrayList<>();
        for (String content : asList(NT, NQ)) {
            rows.add(asList(content, has(content)));
            rows.add(asList(createFile(content), has(content)));
            rows.add(asList(createFile(content).toPath(), has(content)));
            rows.add(asList(createFile(content).getAbsolutePath(), has(content)));
            rows.add(asList(createFile(content).toURI(), has(content)));
            rows.add(asList(createFile(content).toURI().toURL(), has(content)));
            rows.add(asList(createFile(content).toURI().toURL(), has(content)));
            rows.add(asList(new FileInputStream(createFile(content)), has(content)));
            rows.add(asList(new InputStreamReader(new FileInputStream(createFile(content)), UTF_8), has(content)));

            rows.add(asList((Callable<?>)() -> content, has(content)));
            rows.add(asList((Callable<?>)() -> createFile(content), has(content)));
            rows.add(asList((Callable<?>)() -> createFile(content).toPath(), has(content)));
            rows.add(asList((Callable<?>)() -> createFile(content).getAbsolutePath(), has(content)));
            rows.add(asList((Callable<?>)() -> createFile(content).toURI(), has(content)));
            rows.add(asList((Callable<?>)() -> createFile(content).toURI().toURL(), has(content)));
            rows.add(asList((Callable<?>)() -> createFile(content).toURI().toURL(), has(content)));
            rows.add(asList((Callable<?>)() -> new FileInputStream(createFile(content)), has(content)));
            rows.add(asList((Callable<?>)() -> new InputStreamReader(new FileInputStream(createFile(content)), UTF_8), has(content)));

            rows.add(asList((Supplier<?>)() -> content, has(content)));
            rows.add(asList((Supplier<?>)() -> createFile(content), has(content)));
            rows.add(asList((Supplier<?>)() -> createFile(content).toPath(), has(content)));
            rows.add(asList((Supplier<?>)() -> createFile(content).getAbsolutePath(), has(content)));
            rows.add(asList((Supplier<?>)() -> createFile(content).toURI(), has(content)));
        }
        return rows.stream().map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "testData")
    public void test(@Nonnull Object source, @Nonnull Predicate<Object> predicate) {
        SourceNormalizerRegistry registry = createRegistry();
        CoreSourceNormalizers.registerAll(registry);
        Object actual = registry.normalize(source);
        assertTrue(predicate.test(actual));
    }

}