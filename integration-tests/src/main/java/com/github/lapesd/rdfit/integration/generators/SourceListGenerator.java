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

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SourceListGenerator implements SourceGenerator {
    private final @Nonnull SourceGenerator child;

    public SourceListGenerator(@Nonnull SourceGenerator child) {
        this.child = child;
    }

    @Override
    public boolean isReusable() {
        return child.isReusable();
    }

    @Override
    public @Nonnull List<?> generate(@Nonnull TripleSet tripleSet, @Nonnull File tempDir) {
        List<List<Object>> lists = new ArrayList<>();
        for (Object source : child.generate(tripleSet, tempDir))
            lists.add(Collections.singletonList(source));
        return lists;
    }
}
