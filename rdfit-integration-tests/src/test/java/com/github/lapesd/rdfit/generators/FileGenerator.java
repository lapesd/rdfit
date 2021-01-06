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

package com.github.lapesd.rdfit.generators;

import com.github.lapesd.rdfit.TripleSet;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileGenerator implements SourceGenerator {
    @Override public boolean isReusable() {
        return true;
    }

    @Override public @Nonnull List<?> generate(@Nonnull TripleSet tripleSet,
                                               @Nonnull File tempDir) {
         List<Object> list = new ArrayList<>();
         for (ImmutablePair<byte[], RDFLang> p : new ByteGenerator().generateWithLang(tripleSet)) {
             if (!ByteGenerator.CAN_GUESS.contains(p.right))
                 continue;
             list.add(extractFile(tempDir, p.left));
             list.add(extractFile(tempDir, p.left).toPath());
             list.add(extractFile(tempDir, p.left).toURI());
             try {
                 list.add(extractFile(tempDir, p.left).toURI().toURL());
             } catch (MalformedURLException e) {
                throw new RuntimeException(e);
             }
             list.add(extractFile(tempDir, p.left).getAbsolutePath());
             list.add("file://"+extractFile(tempDir, p.left).getAbsolutePath().replace(" ", "%20"));
         }
         return list;
     }

    public static @Nonnull File extractFile(@Nonnull File dir, byte[] data) {
        try {
            File file = Files.createTempFile(dir.toPath(), "rdfit", "").toFile();
            file.deleteOnExit();
            try (FileOutputStream out = new FileOutputStream(file)) {
                IOUtils.copy(new ByteArrayInputStream(data), out);
            }
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
