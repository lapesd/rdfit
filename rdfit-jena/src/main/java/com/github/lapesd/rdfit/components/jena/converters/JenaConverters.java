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

package com.github.lapesd.rdfit.components.jena.converters;

import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.components.Converter;
import com.github.lapesd.rdfit.components.annotations.Accepts;
import com.github.lapesd.rdfit.components.annotations.Outputs;
import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.components.converters.DetachedBaseConverter;
import com.github.lapesd.rdfit.errors.ConversionException;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.sparql.core.Quad;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.Arrays.asList;

public class JenaConverters {
    public static final @Nonnull List<Converter> CONVERTERS = asList(
            Quad2Triple.INSTANCE,
            Statement2Triple.INSTANCE,
            Triple2Statement.INSTANCE,
            Quad2Statement.INSTANCE,
            Triple2Quad.INSTANCE,
            Statement2Quad.INSTANCE
    );

    public static void registerAll(@Nonnull ConversionManager mgr) {
        for (Converter converter : CONVERTERS) mgr.register(converter);
    }

    public static void registerAll(@Nonnull RDFItFactory factory) {
        registerAll(factory.getConversionManager());
    }

    public static void unregisterAll(@Nonnull ConversionManager mgr) {
        for (Converter converter : CONVERTERS) mgr.unregister(converter);
    }

    public static void unregisterAll(@Nonnull RDFItFactory factory) {
        unregisterAll(factory.getConversionManager());
    }

    @Accepts(Quad.class) @Outputs(Triple.class)
    public static class Quad2Triple extends DetachedBaseConverter {
        public static final @Nonnull Quad2Triple INSTANCE = new Quad2Triple();
        @Override public @Nonnull Triple convert(@Nonnull Object input) {
            return ((Quad) input).asTriple();
        }
    }

    @Accepts(Statement.class) @Outputs(Triple.class)
    public static class Statement2Triple extends DetachedBaseConverter {
        public static final @Nonnull Statement2Triple INSTANCE = new Statement2Triple();
        @Override public @Nonnull Triple convert(@Nonnull Object input) {
            return ((Statement)input).asTriple();
        }
    }

    @Accepts(Triple.class) @Outputs(Statement.class)
    public static class Triple2Statement extends DetachedBaseConverter {
        public static final @Nonnull Triple2Statement INSTANCE = new Triple2Statement();

        @Override public boolean canConvert(@Nonnull Object input) {
            if (!(input instanceof Triple)) return false;
            Triple t = (Triple) input;
            return t.getSubject().isConcrete() && t.getPredicate().isConcrete()
                                               && t.getObject().isConcrete();
        }

        @Override public @Nonnull Statement convert(@Nonnull Object input) throws ConversionException {
            if (!canConvert(input))
                throw new ConversionException(input, this, "triple has variables");
            Triple t = (Triple) input;
            ResourceImpl s = new ResourceImpl(t.getSubject(), null);
            PropertyImpl p = new PropertyImpl(t.getPredicate(), null);
            RDFNode o;
            if (t.getObject().isLiteral())
                o = new LiteralImpl(t.getObject(), null);
            else
                o = new ResourceImpl(t.getObject(), null);
            return ResourceFactory.createStatement(s, p, o);
        }
    }

    @Accepts(Quad.class) @Outputs(Statement.class)
    public static class Quad2Statement extends DetachedBaseConverter {
        public static final @Nonnull Quad2Statement INSTANCE = new Quad2Statement();

        @Override public boolean canConvert(@Nonnull Object input) {
            if (!(input instanceof Quad)) return false;
            return Triple2Statement.INSTANCE.canConvert(((Quad)input).asTriple());
        }

        @Override public @Nonnull Statement convert(@Nonnull Object in) throws ConversionException {
            return Triple2Statement.INSTANCE.convert(((Quad)in).asTriple());
        }
    }

    @Accepts(Triple.class) @Outputs(Quad.class)
    public static class Triple2Quad extends DetachedBaseConverter {
        public static final @Nonnull Triple2Quad INSTANCE = new Triple2Quad();
        @Override public @Nonnull Quad convert(@Nonnull Object input) {
            return new Quad(Quad.defaultGraphIRI, (Triple)input);
        }
    }

    @Accepts(Statement.class) @Outputs(Quad.class)
    public static class Statement2Quad extends DetachedBaseConverter {
        public static final @Nonnull Statement2Quad INSTANCE = new Statement2Quad();
        @Override public @Nonnull Quad convert(@Nonnull Object input) {
            return new Quad(Quad.defaultGraphIRI, ((Statement)input).asTriple());
        }
    }
}
