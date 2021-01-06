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
import java.util.List;

public class MultiLangDetector implements LangDetector {
    private final @Nonnull List<LangDetector> detectors = new ArrayList<>();

    public void addOverride(@Nonnull LangDetector detector) {
        detectors.add(0, detector);
    }

    public void addFallback(@Nonnull LangDetector detector) {
        detectors.add(detector);
    }

    public boolean remove(@Nonnull LangDetector detector) {
        return detectors.remove(detector);
    }

    protected static class State implements LangDetector.State {
        private final @Nonnull List<LangDetector.State> states;

        public State(@Nonnull List<LangDetector.State> states) {
            this.states = states;
        }

        @Override public @Nullable RDFLang feedByte(byte value) {
            RDFLang last = null;
            boolean ambiguous = false;
            for (LangDetector.State state : states) {
                RDFLang lang = state.feedByte(value);
                ambiguous |= lang == null || last != null;
                if (!ambiguous && !RDFLangs.UNKNOWN.equals(lang))
                    last = lang;
            }
            return ambiguous ? null : (last == null ? RDFLangs.UNKNOWN : last);
        }

        @Override public @Nullable RDFLang end() {
            RDFLang last = null;
            for (LangDetector.State state : states) {
                RDFLang lang = state.end();
                if      (RDFLangs.isKnown(lang)) return lang;
                else if (lang != null)           last = lang;
            }
            return last;
        }
    }

    @Override public @Nonnull State createState() {
        ArrayList<LangDetector.State> list = new ArrayList<>();
        for (LangDetector detector : detectors) list.add(detector.createState());
        return new State(list);
    }
}
