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
import com.github.lapesd.rdfit.components.Converter;
import com.github.lapesd.rdfit.components.annotations.Accepts;
import com.github.lapesd.rdfit.components.annotations.Outputs;
import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.components.converters.DetachedBaseConverter;
import org.apache.commons.rdf.api.Quad;
import org.apache.commons.rdf.api.Triple;
import org.apache.commons.rdf.api.TripleLike;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.eclipse.rdf4j.model.Statement;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * Helper class for registering {@link Converter}s between commons-rdf and RDF4J
 */
public class CommonsRDF4JConverters {
    private static final List<Converter> CONVERTERS = Arrays.asList(
            Statement2Triple.INSTANCE,
            TripleLike2Statement.INSTANCE,
            Statement2Quad.INSTANCE
    );
    private static final RDF4J R = new RDF4J();

    /**
     * Register all converters in the factory {@link ConversionManager}
     * @param factory the {@link RDFItFactory}
     */
    public static void registerAll(@Nonnull RDFItFactory factory) {
        registerAll(factory.getConversionManager());
    }

    /**
     * Register all converters at the given {@link ConversionManager}
     * @param manager the {@link ConversionManager}
     */
    public static void registerAll(@Nonnull ConversionManager manager) {
        for (Converter converter : CONVERTERS) manager.register(converter);
    }

    /**
     * Remove all converters registered by this class from the factory {@link ConversionManager}
     * @param factory the {@link RDFItFactory} whose {@link ConversionManager} will be used
     */
    public static void unregisterAll(@Nonnull RDFItFactory factory) {
        unregisterAll(factory.getConversionManager());
    }

    /**
     * Remove all converters registered by this class from the given {@link ConversionManager}
     * @param manager the {@link ConversionManager}
     */
    public static void unregisterAll(@Nonnull ConversionManager manager) {
        for (Converter converter : CONVERTERS) manager.unregister(converter);
    }

    /**
     * Convert from RDF4J Statement to commons-rdf Triple
     */
    @Accepts(Statement.class) @Outputs(Triple.class)
    public static class Statement2Triple extends DetachedBaseConverter {
        public static final Statement2Triple INSTANCE = new Statement2Triple();
        @Override public @Nonnull Triple convert(@Nonnull Object input) {
            return R.asTriple((Statement) input);
        }
    }


    /**
     * Convert from commons-rdf objects to an RDF4J Statement
     */
    @Accepts({Triple.class, Quad.class, TripleLike.class}) @Outputs(Statement.class)
    public static class TripleLike2Statement extends DetachedBaseConverter {
        public static final TripleLike2Statement INSTANCE = new TripleLike2Statement();
        @Override public @Nonnull Statement convert(@Nonnull Object input) {
            return R.asStatement((TripleLike) input);
        }
    }

    /**
     * Convert from RDF4J Statement to commons-rdf Quad
     */
    @Accepts(Statement.class) @Outputs(Quad.class)
    public static class Statement2Quad extends DetachedBaseConverter {
        public static final Statement2Quad INSTANCE = new Statement2Quad();
        @Override public @Nonnull Quad convert(@Nonnull Object input) {
            return R.asQuad((Statement) input);
        }
    }

}
