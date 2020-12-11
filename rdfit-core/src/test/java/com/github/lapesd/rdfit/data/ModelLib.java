package com.github.lapesd.rdfit.data;

import com.github.lapesd.rdfit.components.ItParser;
import com.github.lapesd.rdfit.components.ListenerParser;
import com.github.lapesd.rdfit.components.Parser;
import com.github.lapesd.rdfit.components.parsers.impl.iterator.ArrayItParser;
import com.github.lapesd.rdfit.components.parsers.impl.iterator.BaseJavaItParser;
import com.github.lapesd.rdfit.components.parsers.impl.iterator.IterableItParser;
import com.github.lapesd.rdfit.components.parsers.impl.listener.BaseJavaListenerParser;
import com.github.lapesd.rdfit.components.parsers.impl.listener.IterableListenerParser;
import com.github.lapesd.rdfit.components.parsers.impl.listener.TripleArrayListenerParser;
import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.util.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static com.github.lapesd.rdfit.util.Utils.compactClass;
import static java.lang.String.format;
import static java.util.Arrays.asList;

public class ModelLib {
    public static final ItParser TRIPLE_1_ARRAY_IT_PARSER = new ArrayItParser(TripleMock1.class, IterationElement.TRIPLE);
    public static final ItParser TRIPLE_1_ITERABLE_IT_PARSER = new IterableItParser(TripleMock1.class, IterationElement.TRIPLE);
    public static List<ItParser> TRIPLE_1_IT_PARSERS = asList(
            TRIPLE_1_ARRAY_IT_PARSER,
            TRIPLE_1_ITERABLE_IT_PARSER,
            TripleModelMock1.IT_PARSER
    );

    public static final ItParser TRIPLE_2_ARRAY_IT_PARSER = new ArrayItParser(TripleMock2.class, IterationElement.TRIPLE);
    public static final ItParser TRIPLE_2_ITERABLE_IT_PARSER = new IterableItParser(TripleMock2.class, IterationElement.TRIPLE);
    public static List<ItParser> TRIPLE_2_IT_PARSERS = asList(
            TRIPLE_2_ARRAY_IT_PARSER,
            TRIPLE_2_ITERABLE_IT_PARSER,
            TripleModelMock2.IT_PARSER
    );

    public static final ItParser TRIPLE_3_ARRAY_IT_PARSER = new ArrayItParser(TripleMock3.class, IterationElement.TRIPLE);
    public static final ItParser TRIPLE_3_ITERABLE_IT_PARSER = new IterableItParser(TripleMock3.class, IterationElement.TRIPLE);
    public static List<ItParser> TRIPLE_3_IT_PARSERS = asList(
            TRIPLE_3_ARRAY_IT_PARSER,
            TRIPLE_3_ITERABLE_IT_PARSER,
            TripleModelMock3.IT_PARSER
    );

    public static final ItParser QUAD_1_ARRAY_IT_PARSER = new ArrayItParser(TripleMock1.class, IterationElement.QUAD);
    public static final ItParser QUAD_1_ITERABLE_IT_PARSER = new IterableItParser(TripleMock1.class, IterationElement.QUAD);
    public static List<ItParser> QUAD_1_IT_PARSERS = asList(
            QUAD_1_ARRAY_IT_PARSER,
            QUAD_1_ITERABLE_IT_PARSER,
            QuadModelMock1.IT_PARSER
    );

    public static final ItParser QUAD_2_ARRAY_IT_PARSER = new ArrayItParser(TripleMock2.class, IterationElement.QUAD);
    public static final ItParser QUAD_2_ITERABLE_IT_PARSER = new IterableItParser(TripleMock2.class, IterationElement.QUAD);
    public static List<ItParser> QUAD_2_IT_PARSERS = asList(
            QUAD_2_ARRAY_IT_PARSER,
            QUAD_2_ITERABLE_IT_PARSER,
            QuadModelMock2.IT_PARSER
    );

    public static final ItParser QUAD_3_ARRAY_IT_PARSER = new ArrayItParser(QuadMock3.class, IterationElement.QUAD);
    public static final ItParser QUAD_3_ITERABLE_IT_PARSER = new IterableItParser(QuadMock3.class, IterationElement.QUAD);
    public static List<ItParser> QUAD_3_IT_PARSERS = asList(
            QUAD_3_ARRAY_IT_PARSER,
            QUAD_3_ITERABLE_IT_PARSER,
            QuadModelMock3.IT_PARSER
    );

    public static final ListenerParser TRIPLE_1_ARRAY_CB_PARSER = new TripleArrayListenerParser(TripleMock1.class);
    public static final ListenerParser TRIPLE_1_ITERABLE_CB_PARSER = new IterableListenerParser(TripleMock1.class, null);
    public static List<ListenerParser> TRIPLE_1_CB_PARSERS = asList(
            TRIPLE_1_ARRAY_CB_PARSER,
            TRIPLE_1_ITERABLE_CB_PARSER,
            TripleModelMock1.CB_PARSER
    );

    public static final ListenerParser TRIPLE_2_ARRAY_CB_PARSER = new TripleArrayListenerParser(TripleMock2.class);
    public static final ListenerParser TRIPLE_2_ITERABLE_CB_PARSER = new IterableListenerParser(TripleMock2.class, null);
    public static List<ListenerParser> TRIPLE_2_CB_PARSERS = asList(
            TRIPLE_2_ARRAY_CB_PARSER,
            TRIPLE_2_ITERABLE_CB_PARSER,
            TripleModelMock2.CB_PARSER
    );


    public static final ListenerParser TRIPLE_3_ARRAY_CB_PARSER = new TripleArrayListenerParser(TripleMock3.class);
    public static final ListenerParser TRIPLE_3_ITERABLE_CB_PARSER = new IterableListenerParser(TripleMock3.class, null);
    public static List<ListenerParser> TRIPLE_3_CB_PARSERS = asList(
            TRIPLE_3_ARRAY_CB_PARSER,
            TRIPLE_3_ITERABLE_CB_PARSER,
            TripleModelMock3.CB_PARSER
    );

    public static final ListenerParser QUAD_1_ARRAY_CB_PARSER = new TripleArrayListenerParser(QuadMock1.class);
    public static final ListenerParser QUAD_1_ITERABLE_CB_PARSER = new IterableListenerParser(null, QuadMock1.class);
    public static final ListenerParser TRIPLEQUAD_1_ITERABLE_CB_PARSER = new IterableListenerParser(TripleMock1.class, QuadMock1.class);
    public static List<ListenerParser> QUAD_1_CB_PARSERS = asList(
            QUAD_1_ARRAY_CB_PARSER,
            QUAD_1_ITERABLE_CB_PARSER,
            TRIPLEQUAD_1_ITERABLE_CB_PARSER,
            QuadModelMock1.CB_PARSER
    );

    public static final ListenerParser QUAD_2_ARRAY_CB_PARSER = new TripleArrayListenerParser(QuadMock2.class);
    public static final ListenerParser QUAD_2_ITERABLE_CB_PARSER = new IterableListenerParser(QuadMock2.class, null);
    public static List<ListenerParser> QUAD_2_CB_PARSERS = asList(
            QUAD_2_ARRAY_CB_PARSER,
            QUAD_2_ITERABLE_CB_PARSER,
            QuadModelMock2.CB_PARSER
    );

    public static final ListenerParser QUAD_3_ARRAY_CB_PARSER = new TripleArrayListenerParser(QuadMock3.class);
    public static final ListenerParser QUAD_3_ITERABLE_CB_PARSER = new IterableListenerParser(QuadMock3.class, null);
    public static List<ListenerParser> QUAD_3_CB_PARSERS = asList(
            QUAD_3_ARRAY_CB_PARSER,
            QUAD_3_ITERABLE_CB_PARSER,
            QuadModelMock3.CB_PARSER
    );

    public static List<ItParser> TRIPLE_IT_PARSERS;
    public static List<ItParser> QUAD_IT_PARSERS;
    public static List<ItParser> ALL_IT_PARSERS;
    public static List<ListenerParser> TRIPLE_CB_PARSERS;
    public static List<ListenerParser> QUAD_CB_PARSERS;
    public static List<ListenerParser> ALL_CB_PARSERS;
    public static List<ListenerParser> ALL_HYBRID_CB_PARSERS;
    public static List<Parser> ALL_PARSERS;

    static {
        List<ItParser> itList = new ArrayList<>();
        itList.addAll(TRIPLE_1_IT_PARSERS);
        itList.addAll(TRIPLE_2_IT_PARSERS);
        itList.addAll(TRIPLE_3_IT_PARSERS);
        TRIPLE_IT_PARSERS = Collections.unmodifiableList(itList);

        itList = new ArrayList<>();
        itList.addAll(QUAD_1_IT_PARSERS);
        itList.addAll(QUAD_2_IT_PARSERS);
        itList.addAll(QUAD_3_IT_PARSERS);
        QUAD_IT_PARSERS = Collections.unmodifiableList(itList);

        itList = new ArrayList<>();
        itList.addAll(TRIPLE_IT_PARSERS);
        itList.addAll(QUAD_IT_PARSERS);
        ALL_IT_PARSERS = Collections.unmodifiableList(itList);

        List<ListenerParser> cbList = new ArrayList<>();
        cbList.addAll(TRIPLE_1_CB_PARSERS);
        cbList.addAll(TRIPLE_2_CB_PARSERS);
        cbList.addAll(TRIPLE_3_CB_PARSERS);
        TRIPLE_CB_PARSERS = Collections.unmodifiableList(cbList);

        cbList = new ArrayList<>();
        cbList.addAll(QUAD_1_CB_PARSERS);
        cbList.addAll(QUAD_2_CB_PARSERS);
        cbList.addAll(QUAD_3_CB_PARSERS);
        QUAD_CB_PARSERS = Collections.unmodifiableList(cbList);



        cbList = new ArrayList<>();
        for (Class<?> triple : MockFactory.TRIPLE_CLASSES) {
            for (Class<?> quad : MockFactory.QUAD_CLASSES) {
                cbList.add(new HybridModel.CbParser(triple, quad));
            }
        }
        ALL_HYBRID_CB_PARSERS = Collections.unmodifiableList(cbList);

        cbList = new ArrayList<>();
        cbList.addAll(TRIPLE_CB_PARSERS);
        cbList.addAll(QUAD_CB_PARSERS);
        cbList.addAll(ALL_HYBRID_CB_PARSERS);
        ALL_CB_PARSERS = Collections.unmodifiableList(cbList);

        List<Parser> list = new ArrayList<>();
        list.addAll(ALL_IT_PARSERS);
        list.addAll(ALL_CB_PARSERS);
        list.addAll(ALL_HYBRID_CB_PARSERS);

        ALL_PARSERS = Collections.unmodifiableList(list);
    }

    @SuppressWarnings("unchecked") public static @Nonnull Model getModel(@Nonnull Collection<?> collection) {
        if (collection.isEmpty())
            return new TripleModelMock1(new ArrayList<>());
        Class<?> tripleClass = null, quadClass = null;
        for (Object o : collection) {
            if (o instanceof TripleMock) {
                if (tripleClass == null) tripleClass = o.getClass();
                else if (!tripleClass.equals(o.getClass())) {
                    if (o.getClass().isAssignableFrom(tripleClass))
                        tripleClass = o.getClass();
                    else if (!tripleClass.isAssignableFrom(o.getClass()))
                        throw new AssertionError("Incompatible triple classes");
                }
            } else if (o instanceof QuadMock) {
                if (quadClass == null) {
                    quadClass = o.getClass();
                } else if (!quadClass.equals(o.getClass())) {
                    if (o.getClass().isAssignableFrom(quadClass))
                        quadClass = o.getClass();
                    else if (!quadClass.isAssignableFrom(o.getClass()))
                        throw new AssertionError("Incompatible quad classes");
                }
            }
        }
        ArrayList<?> list = new ArrayList<>(collection);
        if (tripleClass != null && quadClass != null) {
            return new HybridModel(tripleClass, quadClass, list);
        } else if (Objects.equals(tripleClass, TripleMock1.class)) {
            return new TripleModelMock1((List<TripleMock1>) list);
        } else if (Objects.equals(tripleClass, TripleMock2.class)) {
            return new TripleModelMock2((List<TripleMock2>) list);
        } else if (Objects.equals(tripleClass, TripleMock3.class)) {
            return new TripleModelMock3((List<TripleMock3>) list);
        } else if (Objects.equals(quadClass, QuadMock1.class)) {
            return new QuadModelMock1((List<QuadMock1>) list);
        } else if (Objects.equals(quadClass, QuadMock2.class)) {
            return new QuadModelMock2((List<QuadMock2>) list);
        } else if (Objects.equals(quadClass, QuadMock3.class)) {
            return new QuadModelMock3((List<QuadMock3>) list);
        }
        throw new RuntimeException("Cannot get Model for tripleClass="+tripleClass+
                                   " and quadClass="+quadClass);
    }

    public static abstract class Model {
        private final @Nonnull List<?> data;

        public Model(@Nonnull List<?> data) {
            this.data = data;
        }

        public @Nonnull List<Object> getData() {
            //noinspection unchecked
            return (List<Object>) data;
        }

        public @Nonnull List<Object> getTriples() {
            ArrayList<Object> list = new ArrayList<>();
            for (Object o : data) {
                if (o instanceof TripleMock)
                    list.add(o);
            }
            return list;
        }

        public @Nonnull List<Object> getQuads() {
            ArrayList<Object> list = new ArrayList<>();
            for (Object o : data) {
                if (o instanceof QuadMock)
                    list.add(o);
            }
            return list;
        }

        @Override public String toString() {
            return format("%s%s", Utils.toString(this), getData());
        }
    }

    public static class HybridModel extends Model {
        private final @Nonnull Class<?> tripleClass, quadClass;

        public HybridModel(@Nonnull Class<?> tripleClass, @Nonnull Class<?> quadClass,
                           @Nonnull List<?> data) {
            super(data);
            this.tripleClass = tripleClass;
            this.quadClass = quadClass;
        }
        public static class CbParser extends BaseJavaListenerParser {
            public CbParser(@Nullable Class<?> tripleClass, @Nullable Class<?> quadClass) {
                super(HybridModel.class, tripleClass, quadClass);
            }

            @Override public boolean canParse(@Nonnull Object source) {
                if (!super.canParse(source)) return false;
                HybridModel m = (HybridModel) source;
                if (tripleClass == null || !tripleClass.isAssignableFrom(m.tripleClass))
                    return false;
                else
                    return quadClass != null && quadClass.isAssignableFrom(m.quadClass);
            }

            @Override protected @Nonnull Iterator<?> createIterator(@Nonnull Object source) {
                return ((HybridModel)source).getData().iterator();
            }
        }


        public @Nonnull Class<?> getTripleClass() {
            return tripleClass;
        }
        public @Nonnull Class<?> getQuadClass() {
            return quadClass;
        }
        public @Nonnull CbParser createParser() {
            return new CbParser(tripleClass, quadClass);
        }

        @Override public String toString() {
            return format("%s{%s,%s}%s", Utils.toString(this), compactClass(tripleClass),
                          compactClass(quadClass), getData());
        }
    }

    public static class TripleModelMock1 extends Model {
        public static class ItParser extends BaseJavaItParser {
            public ItParser() {
                super(TripleModelMock1.class, TripleMock1.class, IterationElement.TRIPLE);
            }
            @Override protected @Nonnull Iterator<?> createIterator(@Nonnull Object source) {
                return ((TripleModelMock1)source).getData().iterator();
            }
        }
        public static final @Nonnull ItParser IT_PARSER = new ItParser();
        public static class CbParser extends BaseJavaListenerParser {
            public CbParser() {
                super(TripleModelMock1.class, TripleMock1.class, null);
            }
            @Override protected @Nonnull Iterator<?> createIterator(@Nonnull Object source) {
                return ((TripleModelMock1)source).getData().iterator();
            }
        }
        public static final @Nonnull CbParser CB_PARSER = new CbParser();
        public TripleModelMock1(@Nonnull List<TripleMock1> data) {
            super(data);
        }
    }
    public static class TripleModelMock2 extends Model {
        public static class ItParser extends BaseJavaItParser {
            public ItParser() {
                super(TripleModelMock2.class, TripleMock2.class, IterationElement.TRIPLE);
            }
            @Override protected @Nonnull Iterator<?> createIterator(@Nonnull Object source) {
                return ((TripleModelMock2)source).getData().iterator();
            }
        }
        public static class CbParser extends BaseJavaListenerParser {
            public CbParser() {
                super(TripleModelMock2.class, TripleMock2.class, null);
            }
            @Override protected @Nonnull Iterator<?> createIterator(@Nonnull Object source) {
                return ((TripleModelMock2)source).getData().iterator();
            }
        }
        public static final @Nonnull CbParser CB_PARSER = new CbParser();
        public static final @Nonnull ItParser IT_PARSER = new ItParser();
        public TripleModelMock2(@Nonnull List<TripleMock2> data) {
            super(data);
        }
    }
    public static class TripleModelMock3 extends Model {
        public static class ItParser extends BaseJavaItParser {
            public ItParser() {
                super(TripleModelMock3.class, TripleMock3.class, IterationElement.TRIPLE);
            }
            @Override protected @Nonnull Iterator<?> createIterator(@Nonnull Object source) {
                return ((TripleModelMock3)source).getData().iterator();
            }
        }
        public static final @Nonnull ItParser IT_PARSER = new ItParser();
        public static class CbParser extends BaseJavaListenerParser {
            public CbParser() {
                super(TripleModelMock3.class, TripleMock3.class, null);
            }
            @Override protected @Nonnull Iterator<?> createIterator(@Nonnull Object source) {
                return ((TripleModelMock3)source).getData().iterator();
            }
        }
        public static final @Nonnull CbParser CB_PARSER = new CbParser();
        public TripleModelMock3(@Nonnull List<TripleMock3> data) {
            super(data);
        }
    }

    public static class QuadModelMock1 extends Model {
        public static class ItParser extends BaseJavaItParser {
            public ItParser() {
                super(QuadModelMock1.class, QuadMock1.class, IterationElement.QUAD);
            }
            @Override protected @Nonnull Iterator<?> createIterator(@Nonnull Object source) {
                return ((QuadModelMock1)source).getData().iterator();
            }
        }
        public static final @Nonnull ItParser IT_PARSER = new ItParser();
        public static class CbParser extends BaseJavaListenerParser {
            public CbParser() {
                super(QuadModelMock1.class, null, QuadMock1.class);
            }
            @Override protected @Nonnull Iterator<?> createIterator(@Nonnull Object source) {
                return ((QuadModelMock1)source).getData().iterator();
            }
        }
        public static final @Nonnull CbParser CB_PARSER = new CbParser();
        public QuadModelMock1(@Nonnull List<QuadMock1> data) {
            super(data);
        }
    }

    public static class QuadModelMock2 extends Model {
        public static class ItParser extends BaseJavaItParser {
            public ItParser() {
                super(QuadModelMock2.class, QuadMock2.class, IterationElement.QUAD);
            }
            @Override protected @Nonnull Iterator<?> createIterator(@Nonnull Object source) {
                return ((QuadModelMock2)source).getData().iterator();
            }
        }
        public static final @Nonnull ItParser IT_PARSER = new ItParser();

        public static class CbParser extends BaseJavaListenerParser {
            public CbParser() {
                super(QuadModelMock2.class, null, QuadMock2.class);
            }
            @Override protected @Nonnull Iterator<?> createIterator(@Nonnull Object source) {
                return ((QuadModelMock2)source).getData().iterator();
            }
        }
        public static final @Nonnull CbParser CB_PARSER = new CbParser();
        public QuadModelMock2(@Nonnull List<QuadMock2> data) {
            super(data);
        }
    }

    public static class QuadModelMock3 extends Model {
        public static class ItParser extends BaseJavaItParser {
            public ItParser() {
                super(QuadModelMock3.class, QuadMock3.class, IterationElement.QUAD);
            }
            @Override protected @Nonnull Iterator<?> createIterator(@Nonnull Object source) {
                return ((QuadModelMock3)source).getData().iterator();
            }
        }
        public static final @Nonnull ItParser IT_PARSER = new ItParser();

        public static class CbParser extends BaseJavaListenerParser {
            public CbParser() {
                super(QuadModelMock3.class, null, QuadMock3.class);
            }
            @Override protected @Nonnull Iterator<?> createIterator(@Nonnull Object source) {
                return ((QuadModelMock3)source).getData().iterator();
            }
        }
        public static final @Nonnull CbParser CB_PARSER = new CbParser();
        public QuadModelMock3(@Nonnull List<QuadMock3> data) {
            super(data);
        }
    }

}
