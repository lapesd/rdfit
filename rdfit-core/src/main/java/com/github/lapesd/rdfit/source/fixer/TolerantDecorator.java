/*
 * Copyright 2021 Alexis Armin Huf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.lapesd.rdfit.source.fixer;

import com.github.lapesd.rdfit.source.RDFInputStreamDecorator;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;

public class TolerantDecorator implements RDFInputStreamDecorator {
    public static final @Nonnull TolerantDecorator TOLERANT = new TolerantDecorator();

    @Override
    public @Nonnull InputStream applyIf(@Nonnull InputStream is, @Nonnull RDFLang lang,
                                        @Nullable String baseIRI, @Nullable String ctx) {
        if (TurtleFamilyFixerDecorator.LANGS.contains(lang))
            return TurtleFamilyFixerDecorator.TURTLE_FIXER.applyIf(is, lang, baseIRI, ctx);
        if (XMLIRIFixerStreamDecorator.LANGS.contains(lang))
            return XMLIRIFixerStreamDecorator.XML_FIXER.applyIf(is, lang, baseIRI, ctx);
        return is;
    }
}
