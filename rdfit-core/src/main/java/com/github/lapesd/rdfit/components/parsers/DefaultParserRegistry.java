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
import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import com.github.lapesd.rdfit.util.TypeDispatcher;

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
    private final @Nonnull TypeDispatcher<ListenerParser> cbParsers
            = new TypeDispatcher<ListenerParser>() {
        @Override
        protected boolean accepts(@Nonnull ListenerParser handler, @Nonnull Object instance) {
            return handler.canParse(instance);
        }
    };
    private @Nonnull ConversionManager conversionManager;
    private @Nullable Set<RDFLang> supportedLangs;

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

    @Override public @Nonnull Set<RDFLang> getSupportedLangs() {
        Set<RDFLang> set = this.supportedLangs;
        if (set == null) {
            set = new HashSet<>();
            for (ItParser       p : itParsers.getAll()) set.addAll(p.parsedLangs());
            for (ListenerParser p : cbParsers.getAll()) set.addAll(p.parsedLangs());
            this.supportedLangs = set;
        }
        return set;
    }

    protected <T extends Parser> void register(@Nonnull TypeDispatcher<T> dispatcher,
                                               @Nonnull T parser) {
        for (Class<?> leaf : parser.acceptedClasses())
            dispatcher.add(leaf, parser);
        parser.attachTo(this);
    }

    @Override public void register(@Nonnull Parser parser) {
        supportedLangs = null;
        if (parser instanceof ItParser)
            register(itParsers, (ItParser) parser);
        if (parser instanceof ListenerParser)
            register(cbParsers, (ListenerParser) parser);
    }

    @Override public void unregister(@Nonnull Parser parser) {
        supportedLangs = null;
        if (parser instanceof ItParser)
            itParsers.remove((ItParser) parser);
        if (parser instanceof ListenerParser)
            cbParsers.remove((ListenerParser) parser);
    }

    @Override public void unregisterIf(@Nonnull Predicate<? super Parser> predicate) {
        supportedLangs = null;
        itParsers.removeIf(predicate);
        cbParsers.removeIf(predicate);
    }

    @Override
    public @Nullable ItParser getItParser(@Nonnull Object source,
                                          @Nullable IterationElement itElem,
                                          @Nullable Class<?> valueClass) {
        Iterator<ItParser> it = itParsers.get(source);
        if (!it.hasNext()) return null;
        List<ItParser> list = new ArrayList<>();
        while (it.hasNext()) {
            ItParser p = it.next();
            list.add(p);
            if (itElem != null && itElem != p.itElement())
                continue;
            if (valueClass == null || valueClass.isAssignableFrom(p.valueClass()))
                return p;
        }
        if (valueClass != null) {
            for (ItParser p : list) {
                if (itElem == null || itElem.equals(p.itElement()))
                    return p;
            }
        }
        assert !list.isEmpty();
        return null;
    }

    @Override
    public @Nullable ListenerParser
    getListenerParser(@Nonnull Object source, @Nullable Class<?> tCls, @Nullable Class<?> qCls) {
        Iterator<ListenerParser> it = cbParsers.get(source);
        if (!it.hasNext())
            return null;
        List<ListenerParser> list = new ArrayList<>();
        while (it.hasNext()) {
            ListenerParser p = it.next();
            list.add(p);
            Class<?> pt = p.tripleType(), pq = p.quadType();
            boolean ok = (tCls == null || (pt != null && tCls.isAssignableFrom(pt))) &&
                         (qCls == null || (pq != null && qCls.isAssignableFrom(pq)));
            if (ok)
                return p;
        }
        if (tCls != null) {
            for (ListenerParser p : list) {
                Class<?> tt = p.tripleType();
                if (tt != null &&  tCls.isAssignableFrom(tt))
                    return p;
            }
        }
        if (qCls != null) {
            for (ListenerParser p : list) {
                Class<?> qt = p.quadType();
                if (qt != null && qCls.isAssignableFrom(qt))
                    return p;
            }
        }
        return list.get(0);
    }
}
