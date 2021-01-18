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

package com.github.lapesd.rdfit.components.commonsrdf.converters;

import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.components.annotations.Accepts;
import com.github.lapesd.rdfit.components.annotations.Outputs;
import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.components.converters.DetachedBaseConverter;
import org.apache.commons.rdf.api.Quad;
import org.apache.commons.rdf.api.Triple;
import org.apache.commons.rdf.simple.SimpleRDF;

import javax.annotation.Nonnull;

public class CommonsConverters {
    private static final @Nonnull SimpleRDF SR = new SimpleRDF();

    public static void registerAll(@Nonnull RDFItFactory factory) {
        registerAll(factory.getConversionManager());
    }
    public static void registerAll(@Nonnull ConversionManager manager) {
        manager.register(Quad2Triple.INSTANCE);
        manager.register(Triple2Quad.INSTANCE);
    }
    public static void unregisterAll(@Nonnull RDFItFactory factory) {
        unregisterAll(factory.getConversionManager());
    }
    public static void unregisterAll(@Nonnull ConversionManager manager) {
        manager.unregister(Quad2Triple.INSTANCE);
        manager.unregister(Triple2Quad.INSTANCE);
    }

    @Accepts(Quad.class) @Outputs(Triple.class)
    public static class Quad2Triple extends DetachedBaseConverter {
        public static final @Nonnull Quad2Triple INSTANCE = new Quad2Triple();
        @Override public @Nonnull Triple convert(@Nonnull Object input) {
            return ((Quad)input).asTriple();
        }
    }

    @Accepts(Triple.class) @Outputs(Quad.class)
    public static class Triple2Quad extends DetachedBaseConverter {
        public static final @Nonnull Triple2Quad INSTANCE = new Triple2Quad();
        @Override public @Nonnull Quad convert(@Nonnull Object input) {
            Triple t = (Triple) input;
            return SR.createQuad(null, t.getSubject(), t.getPredicate(), t.getObject());
        }
    }
}
