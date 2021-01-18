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

package com.github.lapesd.rdfit.integration.generators;

import com.github.lapesd.rdfit.integration.TripleSet;
import com.github.lapesd.rdfit.source.RDFFile;
import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.source.RDFInputStreamSupplier;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class RDFInputStreamGenerator implements SourceGenerator {

    @Override public boolean isReusable() {
        return false;
    }

    @Override public @Nonnull List<?> generate(@Nonnull TripleSet tripleSet,
                                               @Nonnull File tempDir) {
        List<RDFInputStream> list = new ArrayList<>();
        for (ImmutablePair<byte[], RDFLang> p : new ByteGenerator().generateWithLang(tripleSet)) {
            list.add(new RDFInputStream(new ByteArrayInputStream(p.left), p.right));
            Supplier<InputStream> supplier = () -> new ByteArrayInputStream(p.left);
            Callable<InputStream> callable = () -> new ByteArrayInputStream(p.left);
            list.add(new RDFInputStreamSupplier(supplier, p.right));
            list.add(new RDFInputStreamSupplier(callable, p.right));
            if (ByteGenerator.CAN_GUESS.contains(p.right)) {
                list.add(new RDFInputStream(new ByteArrayInputStream(p.left)));
                list.add(new RDFInputStreamSupplier(supplier));
                list.add(new RDFInputStreamSupplier(callable));
            }

            File f1 = FileGenerator.extractFile(tempDir, p.left);
            list.add(new RDFFile(f1, p.right, true));
            if (ByteGenerator.CAN_GUESS.contains(p.right)) {
                File f2 = FileGenerator.extractFile(tempDir, p.left);
                list.add(new RDFFile(f2, true));
            }
        }

        return list;
    }
}
