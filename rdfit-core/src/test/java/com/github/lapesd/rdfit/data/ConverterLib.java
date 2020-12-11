package com.github.lapesd.rdfit.data;

import com.github.lapesd.rdfit.components.Converter;
import com.github.lapesd.rdfit.components.annotations.Accepts;
import com.github.lapesd.rdfit.components.annotations.Outputs;
import com.github.lapesd.rdfit.components.converters.BaseConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class ConverterLib {

    //   +------------------+
    //   v                  v
    //  TM1 <---> TM2 <--> TM3
    //   ^         ^
    //   |         |
    //  QM1 <---> QM2 ----> QM3
    //
    //


    public static final @Nonnull List<Converter> TRIPLE_CONVERTERS =
            asList(new TripleMock1Converter(), new TripleMock2Converter(),
                   new TripleMock3Converter(), new TripleMock3TripleMock1Converter(),
                   new TripleMock3TripleMock2Converter());
    public static final @Nonnull List<Converter> TRIPLE_DOWNGRADERS =
            asList(new TripleMock1Downgrader(), new TripleMock2Downgrader());
    public static final @Nonnull List<Converter> QUAD_CONVERTERS =
            asList(new QuadMock1Converter(), new QuadMock2Converter(),
                   new QuadMock2QuadMock3Converter());
    public static final @Nonnull List<Converter> ALL_CONVERTERS;

    static {
        ArrayList<Converter> list = new ArrayList<>();
        list.addAll(TRIPLE_CONVERTERS);
        list.addAll(TRIPLE_DOWNGRADERS);
        list.addAll(QUAD_CONVERTERS);
        ALL_CONVERTERS = Collections.unmodifiableList(list);
    }

    protected static abstract class BaseMockConverter extends BaseConverter {
        public BaseMockConverter(@Nonnull Collection<Class<?>> acceptedClasses,
                                 @Nonnull Class<?> outputClass) {
            super(acceptedClasses, outputClass);
        }

        public BaseMockConverter() {
        }

        @Override public @Nonnull String toString() {
            return getClass().getSimpleName();
        }
    }

    @Accepts(TripleMock2.class) @Outputs(TripleMock1.class)
    public static class TripleMock1Converter extends BaseMockConverter {
        @Override public @Nullable Object convert(@Nullable Object input) {
            if (input == null) return null;
            TripleMock2 t2 = (TripleMock2) input;
            return new TripleMock1(t2.getSubject(), t2.getPredicate(), t2.getObject());
        }
    }

    @Accepts(TripleMock1.class) @Outputs(TripleMock2.class)
    public static class TripleMock2Converter extends BaseMockConverter {
        @Override public @Nullable Object convert(@Nullable Object input) {
            if (input == null) return null;
            TripleMock1 t1 = (TripleMock1) input;
            return new TripleMock2(t1.getSubject(), t1.getPredicate(), t1.getObject());
        }
    }

    @Accepts({TripleMock1.class, TripleMock2.class}) @Outputs(TripleMock3.class)
    public static class TripleMock3Converter extends BaseMockConverter {
        @Override public @Nullable Object convert(@Nullable Object input) {
            if (input == null) return null;
            if (input instanceof TripleMock1) {
                TripleMock1 t = (TripleMock1) input;
                return new TripleMock3(t.getSubject(), t.getPredicate(), t.getObject());
            } else if (input instanceof TripleMock2) {
                TripleMock2 t = (TripleMock2) input;
                return new TripleMock3(t.getSubject(), t.getPredicate(), t.getObject());
            }
            return null;
        }
    }

    public static class TripleMock3TripleMock1Converter extends BaseMockConverter {
        public TripleMock3TripleMock1Converter() {
            super(singletonList(TripleMock3.class), TripleMock1.class);
        }

        @Override public @Nullable Object convert(@Nullable Object input) {
            if (input == null) return null;
            TripleMock3 t = (TripleMock3) input;
            return new TripleMock1(t.getSubject(), t.getPredicate(), t.getObject());
        }
    }

    public static class TripleMock3TripleMock2Converter extends BaseMockConverter {
        public TripleMock3TripleMock2Converter() {
            super(singletonList(TripleMock3.class), TripleMock2.class);
        }

        @Override public @Nullable Object convert(@Nullable Object input) {
            if (input == null) return null;
            TripleMock3 t = (TripleMock3) input;
            return new TripleMock2(t.getSubject(), t.getPredicate(), t.getObject());
        }
    }

    public static class TripleMock1Downgrader extends BaseMockConverter {
        public TripleMock1Downgrader() {
            super(singletonList(QuadMock1.class), TripleMock1.class);
        }

        @Override public @Nullable  Object convert(@Nullable Object input) {
            if (input == null) return null;
            TripleMock triple = SplitMockQuad.split(input).getTriple();
            assert  outputClass().isInstance(triple);
            return triple;
        }
    }

    public static class TripleMock2Downgrader extends BaseMockConverter {
        public TripleMock2Downgrader() {
            super(singletonList(QuadMock2.class), TripleMock2.class);
        }

        @Override public @Nullable Object convert(@Nullable Object input) {
            if (input == null) return null;
            TripleMock triple = SplitMockQuad.split(input).getTriple();
            assert outputClass().isInstance(triple);
            return triple;
        }
    }

    public static class QuadMock1Converter extends BaseMockConverter {
        public QuadMock1Converter() {
            super(singletonList(QuadMock2.class), QuadMock1.class);
        }

        @Override public @Nullable Object convert(@Nullable Object input) {
            if (input == null) return null;
            QuadMock2 q = (QuadMock2) input;
            TripleMock1 triple = new TripleMock1(q.getSubject(), q.getPredicate(), q.getObject());
            return new QuadMock1(q.getGraph(), triple);
        }
    }

    public static class QuadMock2Converter extends BaseMockConverter {
        public QuadMock2Converter() {
            super(singletonList(QuadMock1.class), QuadMock2.class);
        }

        @Override public @Nullable Object convert(@Nullable Object input) {
            if (input == null) return null;
            QuadMock1 q = (QuadMock1) input;
            TripleMock1 t = q.getTriple();
            return new QuadMock2(q.getGraph(), t.getSubject(), t.getPredicate(), t.getObject());
        }
    }

    /**
     * This converter purpose is to lead toa  dead-end in the conversion graph (above)
     */
    @Accepts(QuadMock2.class) @Outputs(QuadMock3.class)
    public static class QuadMock2QuadMock3Converter extends BaseMockConverter {
        @Override public @Nullable Object convert(@Nullable Object input) {
            if (input == null) return null;
            QuadMock2 q = (QuadMock2) input;
            TripleMock3 t = new TripleMock3(q.getSubject(), q.getPredicate(), q.getObject());
            return new QuadMock3(q.getGraph(), t);
        }
    }
}
