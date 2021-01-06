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

package com.github.lapesd.rdfit.components.parsers;

import com.github.lapesd.rdfit.components.ItParser;
import com.github.lapesd.rdfit.components.ListenerParser;
import com.github.lapesd.rdfit.components.Parser;
import com.github.lapesd.rdfit.data.ModelLib;
import com.github.lapesd.rdfit.iterator.Ex;
import com.github.lapesd.rdfit.iterator.RDFIt;
import com.github.lapesd.rdfit.listener.RDFListenerBase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public abstract class ParserRegistryTestBase {
    @DataProvider public Object[][] testData() {
        return Stream.of(
                asList("No parsers", Collections.emptyList(),
                        new ModelLib.TripleModelMock1(asList(Ex.T1, Ex.T2)),
                        null, null,
                        null, null, null),
                asList("TRIPLE_1_IT_PARSERS, TMM2", ModelLib.TRIPLE_1_IT_PARSERS,
                       new ModelLib.TripleModelMock2(asList(Ex.U1, Ex.U2)),
                       null, null,
                       null, null, null),
                asList("TRIPLE_1_IT_PARSERS, TMM1", ModelLib.TRIPLE_1_IT_PARSERS,
                        new ModelLib.TripleModelMock1(asList(Ex.T1, Ex.T2)),
                        ModelLib.TripleModelMock1.IT_PARSER, asList(Ex.T1, Ex.T2),
                        null, null, null),
                asList("ALL_IT_PARSERS, TMM1", ModelLib.ALL_IT_PARSERS,
                        new ModelLib.TripleModelMock1(asList(Ex.T1, Ex.T2)),
                        ModelLib.TripleModelMock1.IT_PARSER, asList(Ex.T1, Ex.T2),
                        null, null, null),
                asList("ALL_PARSERS, TMM1", ModelLib.ALL_PARSERS,
                        new ModelLib.TripleModelMock1(asList(Ex.T1, Ex.T2)),
                        ModelLib.TripleModelMock1.IT_PARSER, asList(Ex.T1, Ex.T2),
                        ModelLib.TripleModelMock1.CB_PARSER, asList(Ex.T1, Ex.T2), null),

                asList("TRIPLE_3_ITERABLE_IT_PARSER, List<TM3>",
                        singleton(ModelLib.TRIPLE_3_ITERABLE_IT_PARSER),
                        asList(Ex.V1, Ex.V2),
                        ModelLib.TRIPLE_3_ITERABLE_IT_PARSER, asList(Ex.V1, Ex.V2),
                        null, null, null),
                asList("ALL_IT_PARSERS, List<TM3>", ModelLib.ALL_IT_PARSERS,
                        asList(Ex.V1, Ex.V2),
                        ModelLib.TRIPLE_3_ITERABLE_IT_PARSER, asList(Ex.V1, Ex.V2),
                        null, null, null),
                asList("ALL_PARSERS, List<TM3>", ModelLib.ALL_PARSERS,
                        asList(Ex.V1, Ex.V2),
                        ModelLib.TRIPLE_3_ITERABLE_IT_PARSER, asList(Ex.V1, Ex.V2),
                        ModelLib.TRIPLE_3_ITERABLE_CB_PARSER, asList(Ex.V1, Ex.V2), null),

                // mixed triple/quads
                asList("ALL_PARSERS, List<TM1, QM1>", ModelLib.ALL_PARSERS,
                       asList(Ex.T1, Ex.Q2),
                       null, null,
                       ModelLib.TRIPLEQUAD_1_ITERABLE_CB_PARSER,
                       singletonList(Ex.T1), singletonList(Ex.Q2))

        ).map(List::toArray).toArray(Object[][]::new);
    }

    protected abstract @Nonnull ParserRegistry createRegistry();

    @Test(dataProvider = "testData")
    public void test(@Nonnull String ignored, @Nonnull Collection<Parser> parsers,
                     @Nonnull Object source, @Nullable Parser expectedItParser,
                     @Nullable Collection<Object> expectedItValues,
                     @Nullable Parser expectedCbParser,
                     @Nullable Collection<Object> expectedTriples,
                     @Nullable Collection<Object> expectedQuads) {
        if (expectedItValues == null) expectedItValues = Collections.emptyList();
        if (expectedTriples  == null) expectedTriples  = Collections.emptyList();
        if (expectedQuads    == null) expectedQuads    = Collections.emptyList();

        ParserRegistry registry = createRegistry();
        for (Parser p : parsers)
            registry.register(p);
        ItParser itParser = registry.getItParser(source);
        assertEquals(itParser, expectedItParser);
        if (itParser != null) {
            assertNotNull(expectedItValues);
            List<Object> actualItValues = new ArrayList<>();
            try (RDFIt<Object> it = itParser.parse(source)) {
                while (it.hasNext())
                    actualItValues.add(it.next());
            }
            assertEquals(actualItValues, expectedItValues);
        }

        ListenerParser cbParser = registry.getCallbackParser(source);
        assertEquals(cbParser, expectedCbParser);
        if (cbParser != null) {
            ArrayList<Object> actualTriples = new ArrayList<>(), actualQuads = new ArrayList<>();
            //noinspection unchecked
            RDFListenerBase<Object, Object> cb = new RDFListenerBase<Object, Object>(
                    (Class<Object>) cbParser.tripleType(),
                    (Class<Object>) cbParser.quadType()) {
                @Override public void triple(@Nonnull Object triple) {
                    actualTriples.add(triple);
                }

                @Override public void quad(@Nonnull Object quad) {
                    actualQuads.add(quad);
                }
            };
            cbParser.parse(source, cb);
            assertEquals(actualTriples, expectedTriples);
            assertEquals(actualQuads, expectedQuads);
        }
    }
}