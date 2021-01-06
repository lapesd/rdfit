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
