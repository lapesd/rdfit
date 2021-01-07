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
import com.github.lapesd.rdfit.data.*;
import com.github.lapesd.rdfit.iterator.EmptyRDFIt;
import com.github.lapesd.rdfit.iterator.Ex;
import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.iterator.RDFIt;
import com.github.lapesd.rdfit.listener.RDFListener;
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

import static com.github.lapesd.rdfit.iterator.IterationElement.QUAD;
import static com.github.lapesd.rdfit.iterator.IterationElement.TRIPLE;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.testng.Assert.*;

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
        ItParser itParser = registry.getItParser(source, null, null);
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

        ListenerParser cbParser = registry.getListenerParser(source, null, null);
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

    private static class DummyListenerParser extends BaseListenerParser {
        public DummyListenerParser(@Nonnull Class<?> accept, @Nullable Class<?> tripleClass,
                                   @Nullable Class<?> quadClass) {
            super(Collections.singleton(accept), tripleClass, quadClass);
        }

        @Override
        public void parse(@Nonnull Object source, @Nonnull RDFListener<?, ?> listener) {
        }
    }

    private static class DummyItParser extends BaseItParser {
        public DummyItParser(@Nonnull Class<?> accepted, @Nonnull Class<?> valueClass,
                             @Nonnull IterationElement iterationElement) {
            super(Collections.singleton(accepted), valueClass, iterationElement);
        }

        @Override public @Nonnull <T> RDFIt<T> parse(@Nonnull Object source) {
            //noinspection unchecked
            return new EmptyRDFIt<>((Class<T>) valueClass(), iterationElement(), source);
        }

        @Override public @Nonnull String toString() {
            return String.format("DummyItParser(%s, %s, %s)", acceptedClasses().iterator().next(),
                                                              valueClass(), iterationElement());
        }
    }

    @Test
    public void testLIFO() {
        ParserRegistry reg = createRegistry();
        DummyListenerParser p1 = new DummyListenerParser(String.class, TripleMock1.class, null);
        DummyListenerParser p2 = new DummyListenerParser(String.class, TripleMock1.class, null);
        DummyItParser pt1 = new DummyItParser(String.class, TripleMock1.class, TRIPLE);
        DummyItParser pt2 = new DummyItParser(String.class, TripleMock1.class, TRIPLE);
        DummyItParser pq1 = new DummyItParser(String.class, TripleMock1.class, QUAD);
        DummyItParser pq2 = new DummyItParser(String.class, TripleMock1.class, QUAD);
        reg.register(p1);
        reg.register(p2);
        reg.register(pt1);
        reg.register(pt2);
        reg.register(pq1);
        reg.register(pq2);
        assertSame(reg.getListenerParser("asd", null, null), p2);
        assertSame(reg.getItParser("asd", TRIPLE, null), pt2);
        assertSame(reg.getItParser("asd", QUAD, null), pq2);
    }

    @Test
    public void testLIFOSuperClass() {
        ParserRegistry reg = createRegistry();
        DummyListenerParser p1 = new DummyListenerParser(String.class, TripleMock1.class, null);
        DummyListenerParser p2 = new DummyListenerParser(String.class, TripleMock1.class, null);
        DummyListenerParser p3 = new DummyListenerParser(CharSequence.class, TripleMock1.class, null);
        DummyItParser pt1 = new DummyItParser(String.class, TripleMock1.class, TRIPLE);
        DummyItParser pt2 = new DummyItParser(String.class, TripleMock1.class, TRIPLE);
        DummyItParser pt3 = new DummyItParser(CharSequence.class, TripleMock1.class, TRIPLE);
        DummyItParser pq1 = new DummyItParser(String.class, TripleMock1.class, QUAD);
        DummyItParser pq2 = new DummyItParser(String.class, TripleMock1.class, QUAD);
        DummyItParser pq3 = new DummyItParser(CharSequence.class, TripleMock1.class, QUAD);
        reg.register(p1);
        reg.register(p2);
        reg.register(p3);
        reg.register(pt1);
        reg.register(pt2);
        reg.register(pt3);
        reg.register(pq1);
        reg.register(pq2);
        reg.register(pq3);
        assertSame(reg.getListenerParser("asd", null, null), p2);
        assertSame(reg.getItParser("asd", TRIPLE, null), pt2);
        assertSame(reg.getItParser("asd", QUAD, null), pq2);
    }

    @Test
    public void testPreferNoConversionTripleListenerParser() {
        ParserRegistry reg = createRegistry();
        DummyListenerParser p1 = new DummyListenerParser(String.class, TripleMock1.class, null);
        DummyListenerParser p2 = new DummyListenerParser(String.class, TripleMock2.class, null);
        reg.register(p1);
        reg.register(p2);
        assertSame(reg.getListenerParser("asd", TripleMock1.class, null), p1);
        assertSame(reg.getListenerParser("asd", TripleMock2.class, null), p2);
    }

    @Test
    public void testPreferNoConversionTripleAndQuadListenerParser() {
        ParserRegistry reg = createRegistry();
        DummyListenerParser p1 = new DummyListenerParser(String.class, TripleMock1.class, QuadMock1.class);
        DummyListenerParser p2 = new DummyListenerParser(String.class, TripleMock2.class, QuadMock2.class);
        reg.register(p1);
        reg.register(p2);
        assertSame(reg.getListenerParser("asd", TripleMock1.class, QuadMock1.class), p1);
        assertSame(reg.getListenerParser("asd", TripleMock1.class, QuadMock2.class), p1);
        assertSame(reg.getListenerParser("asd", TripleMock1.class, null), p1);
        assertSame(reg.getListenerParser("asd", null, QuadMock1.class), p1);

        assertSame(reg.getListenerParser("asd", TripleMock2.class, QuadMock2.class), p2);
        assertSame(reg.getListenerParser("asd", TripleMock2.class, QuadMock1.class), p2);
        assertSame(reg.getListenerParser("asd", TripleMock2.class, null), p2);
        assertSame(reg.getListenerParser("asd", null, QuadMock2.class), p2);
        assertSame(reg.getListenerParser("asd", null, null), p2);
    }

    @Test
    public void testPreferNoConversionItParser() {
        ParserRegistry reg = createRegistry();
        DummyItParser pt1 = new DummyItParser(String.class, TripleMock1.class, TRIPLE);
        DummyItParser pt2 = new DummyItParser(String.class, TripleMock2.class, TRIPLE);
        DummyItParser pq1t = new DummyItParser(String.class, TripleMock1.class, QUAD);
        DummyItParser pq2t = new DummyItParser(String.class, TripleMock2.class, QUAD);
        DummyItParser pq1 = new DummyItParser(String.class, QuadMock1.class, QUAD);
        DummyItParser pq2 = new DummyItParser(String.class, QuadMock2.class, QUAD);
        DummyItParser pq3 = new DummyItParser(String.class, QuadMock3.class, QUAD);

        reg.register(pt1);
        reg.register(pt2);
        reg.register(pq1t);
        reg.register(pq2t);
        reg.register(pq1);
        reg.register(pq2);
        reg.register(pq3);

        assertSame(reg.getItParser("asd", TRIPLE, TripleMock2.class), pt2);
        assertSame(reg.getItParser("asd", TRIPLE, TripleMock1.class), pt1);
        assertSame(reg.getItParser("asd", TRIPLE, null), pt2);
        assertSame(reg.getItParser("asd", QUAD, QuadMock1.class), pq1);
        assertSame(reg.getItParser("asd", QUAD, QuadMock2.class), pq2);
        assertSame(reg.getItParser("asd", QUAD, QuadMock3.class), pq3);
        assertSame(reg.getItParser("asd", QUAD, null), pq3);
    }
}