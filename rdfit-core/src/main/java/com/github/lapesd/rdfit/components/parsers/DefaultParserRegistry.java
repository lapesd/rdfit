package com.github.lapesd.rdfit.components.parsers;

import com.github.lapesd.rdfit.components.ItParser;
import com.github.lapesd.rdfit.components.ListenerParser;
import com.github.lapesd.rdfit.components.Parser;
import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.util.TypeDispatcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.function.Predicate;

public class DefaultParserRegistry implements ParserRegistry {
    private static final DefaultParserRegistry INSTANCE = new DefaultParserRegistry();
    private final @Nonnull TypeDispatcher<ItParser> itParsers = new TypeDispatcher<ItParser>() {
        @Override protected boolean accepts(@Nonnull ItParser handler, @Nonnull Object instance) {
            return handler.canParse(instance);
        }
    };
    private final @Nonnull TypeDispatcher<ListenerParser> cbParsers
            = new TypeDispatcher<ListenerParser>() {
        @Override
        protected boolean accepts(@Nonnull ListenerParser handler, @Nonnull Object instance) {
            return handler.canParse(instance);
        }
    };
    private @Nonnull ConversionManager conversionManager;

    public static @Nonnull DefaultParserRegistry get() {
        return INSTANCE;
    }

    public DefaultParserRegistry() {
        this(DefaultConversionManager.get());
    }

    public DefaultParserRegistry(@Nonnull ConversionManager conversionManager) {
        this.conversionManager = conversionManager;
    }

    @Override public @Nonnull ConversionManager getConversionManager() {
        return conversionManager;
    }

    public void setConversionManager(@Nonnull ConversionManager conversionManager) {
        this.conversionManager = conversionManager;
    }

    protected <T extends Parser> void register(@Nonnull TypeDispatcher<T> dispatcher,
                                               @Nonnull T parser) {
        for (Class<?> leaf : parser.acceptedClasses())
            dispatcher.add(leaf, parser);
        parser.attachTo(this);
    }

    @Override public void register(@Nonnull Parser parser) {
        if (parser instanceof ItParser)
            register(itParsers, (ItParser) parser);
        if (parser instanceof ListenerParser)
            register(cbParsers, (ListenerParser) parser);
    }

    @Override public void unregister(@Nonnull Parser parser) {
        if (parser instanceof ItParser)
            itParsers.remove((ItParser) parser);
        if (parser instanceof ListenerParser)
            cbParsers.remove((ListenerParser) parser);
    }

    @Override public void unregisterIf(@Nonnull Predicate<? super Parser> predicate) {
        itParsers.removeIf(predicate);
        cbParsers.removeIf(predicate);
    }

    @Override public @Nullable ItParser getItParser(@Nonnull Object source,
                                                    @Nullable IterationElement itElement) {
        Iterator<ItParser> it = itParsers.get(source);
        while (it.hasNext()) {
            ItParser parser = it.next();
            if (itElement == null || itElement.equals(parser.iterationElement()))
                return parser;
        }
        return null;
    }

    @Override public @Nullable ListenerParser getCallbackParser(@Nonnull Object source) {
        Iterator<ListenerParser> it = cbParsers.get(source);
        return it.hasNext() ? it.next() : null;
    }
}
