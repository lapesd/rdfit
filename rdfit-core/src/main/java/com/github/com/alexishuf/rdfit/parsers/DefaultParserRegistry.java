package com.github.com.alexishuf.rdfit.parsers;

import com.github.com.alexishuf.rdfit.components.CallbackParser;
import com.github.com.alexishuf.rdfit.components.ItParser;
import com.github.com.alexishuf.rdfit.components.Parser;
import com.github.com.alexishuf.rdfit.util.SuperTypesIterator;
import com.github.com.alexishuf.rdfit.util.TypeDispatcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class DefaultParserRegistry implements ParserRegistry {
    private static final DefaultParserRegistry INSTANCE = new DefaultParserRegistry();

    private final @Nonnull TypeDispatcher<ItParser> itParsers = new TypeDispatcher<ItParser>() {
        @Override protected boolean accepts(@Nonnull ItParser handler, @Nonnull Object instance) {
            return handler.canParse(instance);
        }
    };
    private final @Nonnull TypeDispatcher<CallbackParser> cbParsers
            = new TypeDispatcher<CallbackParser>() {
        @Override
        protected boolean accepts(@Nonnull CallbackParser handler, @Nonnull Object instance) {
            return handler.canParse(instance);
        }
    };

    public static @Nonnull DefaultParserRegistry get() {
        return INSTANCE;
    }

    protected <T extends Parser> void register(@Nonnull TypeDispatcher<T> dispatcher,
                                               @Nonnull T parser) {
        for (Class<?> leaf : parser.acceptedClasses())
            dispatcher.add(leaf, parser);
    }

    @Override public void register(@Nonnull Parser parser) {
        if (parser instanceof ItParser)
            register(itParsers, (ItParser) parser);
        if (parser instanceof CallbackParser)
            register(cbParsers, (CallbackParser) parser);
    }

    @Override public void unregister(@Nonnull Parser parser) {
        if (parser instanceof ItParser)
            itParsers.remove((ItParser) parser);
        if (parser instanceof CallbackParser)
            cbParsers.remove((CallbackParser) parser);
    }

    @Override public void unregisterIf(@Nonnull Predicate<? super Parser> predicate) {
        itParsers.removeIf(predicate);
        cbParsers.removeIf(predicate);
    }

    @Override public @Nullable ItParser getItParser(@Nonnull Object source) {
        Iterator<ItParser> it = itParsers.get(source);
        return it.hasNext() ? it.next() : null;
    }

    @Override public @Nullable CallbackParser getCallbackParser(@Nonnull Object source) {
        Iterator<CallbackParser> it = cbParsers.get(source);
        return it.hasNext() ? it.next() : null;
    }
}
