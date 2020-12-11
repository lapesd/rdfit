package com.github.lapesd.rdfit.source.syntax.impl;

import com.github.lapesd.rdfit.source.syntax.LangDetector;
import com.github.lapesd.rdfit.source.syntax.RDFLangs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CookiesLangDetector implements LangDetector {
    private final @Nonnull LinkedHashMap<Cookie, RDFLang> cookie2syntax = new LinkedHashMap<>();

    @SuppressWarnings("UnusedReturnValue")
    public @Nonnull CookiesLangDetector addCookie(@Nonnull Cookie cookie, @Nonnull RDFLang syntax) {
        cookie2syntax.put(cookie, syntax);
        return this;
    }

    public class State implements LangDetector.State {
        private final @Nonnull List<Cookie.Matcher> matchers = new ArrayList<>();
        private final @Nonnull List<RDFLang> syntaxes = new ArrayList<>();

        public State() {
            for (Map.Entry<Cookie, RDFLang> e : cookie2syntax.entrySet()) {
                matchers.add(e.getKey().createMatcher());
                syntaxes.add(e.getValue());
            }
        }

        @Override public @Nullable RDFLang feedByte(byte value) {
            int firstMatch = -1;
            boolean ok = false;
            for (int i = 0, size = matchers.size(); i < size; i++) {
                Cookie.Matcher m = matchers.get(i);
                ok |= m.feed(value);
                if (m.isMatched() && m.isConclusive())
                    firstMatch = i;
            }
            if (!ok)
                return RDFLangs.UNKNOWN; //conclusive matches none
            return firstMatch >= 0 ? syntaxes.get(firstMatch) : null;
        }

        @Override public @Nullable RDFLang end() {
            for (int i = 0, size = matchers.size(); i < size; i++) {
                Cookie.Matcher m = matchers.get(i);
                if (m.isMatched() && m.isConclusive())
                    return syntaxes.get(i);
            }
            return RDFLangs.UNKNOWN;
        }
    }

    @Override public @Nonnull State createState() {
        return new State();
    }
}
