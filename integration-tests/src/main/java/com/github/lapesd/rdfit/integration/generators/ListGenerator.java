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

import com.github.lapesd.rdfit.integration.TestDataGenerator;
import com.github.lapesd.rdfit.integration.TripleSet;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ListGenerator implements SourceGenerator {
    @Override public boolean isReusable() {
        return false;
    }

    @Override
    public @Nonnull List<List<?>> generate(@Nonnull TripleSet tripleSet, @Nonnull File tempDir) {
        List<List<?>> lists = new ArrayList<>();
        if (!tripleSet.hasQuads()) {
            for (Object object : TestDataGenerator.tripleClasses) {
                if (!(object instanceof Class<?>)) continue;
                lists.add(tripleSet.export((Class<?>) object));
            }
        }
        for (Object object : TestDataGenerator.quadClasses) {
            if (!(object instanceof Class<?>)) continue;
            lists.add(tripleSet.export((Class<?>)object));
        }
        return lists;
    }
}
