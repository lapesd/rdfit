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

package com.github.lapesd.rdfit.components.hdt;

import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.components.hdt.parsers.iterator.HDTItParser;
import com.github.lapesd.rdfit.components.parsers.JavaParsers;
import com.github.lapesd.rdfit.components.parsers.ParserRegistry;
import org.rdfhdt.hdt.triples.TripleString;

import javax.annotation.Nonnull;

public class HDTParsers {
    public static void registerAll(@Nonnull RDFItFactory factory) {
        registerAll(factory.getParserRegistry());
    }
    public static void registerAll(@Nonnull ParserRegistry registry) {
        registry.register(new HDTItParser());
        JavaParsers.registerWithTripleClass(registry, TripleString.class);
    }

    public static void unregisterAll(@Nonnull RDFItFactory factory) {
        unregisterAll(factory.getParserRegistry());
    }
    public static void unregisterAll(@Nonnull ParserRegistry registry) {
        registry.unregisterIf(HDTItParser.class::isInstance);
        JavaParsers.unregister(registry, TripleString.class);
    }
}
