package com.github.com.alexishuf.rdfit.conversion.impl;

import com.github.com.alexishuf.rdfit.components.Converter;
import com.github.com.alexishuf.rdfit.conversion.ConversionFinder;
import com.github.com.alexishuf.rdfit.conversion.ConversionManager;
import com.github.com.alexishuf.rdfit.conversion.ConversionPath;
import com.github.com.alexishuf.rdfit.util.TypeDispatcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class DefaultConversionManager implements ConversionManager {
    public static final DefaultConversionManager INSTANCE = new DefaultConversionManager();

    private final @Nonnull TypeDispatcher<Converter> dispatcher = new TypeDispatcher<Converter>() {
        @Override protected boolean accepts(@Nonnull Converter handler, @Nonnull Object instance) {
            return handler.canConvert(instance);
        }
    };

    public static @Nonnull DefaultConversionManager get() {
        return INSTANCE;
    }

    @Override public synchronized void register(@Nonnull Converter converter) {
        for (Class<?> cls : converter.acceptedClasses())
            dispatcher.add(cls, converter);
    }

    @Override public synchronized void unregister(@Nonnull Converter instance) {
        dispatcher.remove(instance);
    }

    @Override public synchronized void unregisterIf(@Nonnull Predicate<? super Converter> pred) {
        dispatcher.removeIf(pred);
    }

    protected static class Step {
        @Nullable Step predecessor;
        @Nonnull Converter converter;

        public Step(@Nonnull Converter converter, @Nullable Step predecessor) {
            this.converter = converter;
            this.predecessor = predecessor;
        }

        public @Nonnull ConversionPath toPath() {
            ArrayList<Converter> list = new ArrayList<>();
            for (Step s = this; s != null; s = s.predecessor)
                list.add(s.converter);
            Collections.reverse(list);
            return new ConversionPath(list);
        }
    }

    protected void addSteps(@Nonnull Collection<? super Step> dst,
                            @Nullable Step current, @Nullable Object object) {
        assert current != null || object != null;
        Iterator<Converter> it = object != null ? dispatcher.get(object)
                                                : dispatcher.get(current.converter.outputClass());
        while (it.hasNext())
            dst.add(new Step(it.next(), current));
    }

    @Override
    public @Nonnull ConversionFinder findPath(@Nullable Object input, @Nonnull Class<?> desired) {
        if (input == null || desired.isInstance(input))
            return TrivialConversionFinder.NO_OP;

        HashSet<Converter> visited = new HashSet<>();
        ArrayDeque<Step> queue = new ArrayDeque<>();
        addSteps(queue, null, input);

        return new ConversionFinder() {
            private ConversionPath conversionPath;

            @Override public @Nonnull ConversionPath getConversionPath() {
                if (conversionPath == null)
                    throw new IllegalStateException("hasNext() nto called or returned false");
                return conversionPath;
            }

            @Override public @Nullable Object convert(@Nullable Object input) {
                if (input == null) return null;
                return getConversionPath().convert(input);
            }

            @Override public boolean hasNext() {
                while (!queue.isEmpty()) {
                    Step step = queue.remove();
                    if (!visited.add(step.converter))
                        continue; // cycle
                    if (desired.isAssignableFrom(step.converter.outputClass())) {
                        conversionPath = step.toPath();
                        return true;
                    }
                    addSteps(queue, step, step.converter.outputClass());
                }
                return false;
            }
        };
    }
}
