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
import com.github.lapesd.rdfit.errors.ConversionException;
import org.apache.commons.rdf.api.Quad;
import org.apache.commons.rdf.api.Triple;
import org.apache.commons.rdf.jena.JenaRDF;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Triple;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ReifiedStatementImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.Arrays.asList;

public class CommonsJenaConverters {
    private static final JenaRDF JR = new JenaRDF();
    private static final List<Converter> CONVERTERS = asList(
            JenaTriple2Triple.INSTANCE,
            JenaTriple2Quad.INSTANCE,
            JenaStatement2Triple.INSTANCE,
            JenaStatement2Quad.INSTANCE,
            JenaQuad2Quad.INSTANCE,
            JenaQuad2Triple.INSTANCE,
            Quad2JenaQuad.INSTANCE,
            Quad2JenaTriple.INSTANCE,
            Quad2JenaStatement.INSTANCE,
            Triple2JenaTriple.INSTANCE,
            Triple2JenaStatement.INSTANCE,
            Triple2JenaQuad.INSTANCE
    );

    public static void registerAll(@Nonnull RDFItFactory factory) {
        registerAll(factory.getConversionManager());
    }
    public static void registerAll(@Nonnull ConversionManager manager) {
        for (Converter converter : CONVERTERS) manager.register(converter);
    }
    public static void unregisterAll(@Nonnull RDFItFactory factory) {
        unregisterAll(factory.getConversionManager());
    }
    public static void unregisterAll(@Nonnull ConversionManager manager) {
        for (Converter converter : CONVERTERS) manager.unregister(converter);
    }

    @Accepts(org.apache.jena.graph.Triple.class) @Outputs(Triple.class)
    public static class JenaTriple2Triple extends DetachedBaseConverter {
        public static final JenaTriple2Triple INSTANCE = new JenaTriple2Triple();
        @Override public @Nonnull Triple convert(@Nonnull Object input) throws ConversionException {
            try {
                return JR.asTriple((org.apache.jena.graph.Triple)input);
            } catch (org.apache.commons.rdf.jena.ConversionException e) {
                throw new ConversionException(input, this, outputClass(), "Non-concrete triple");
            }
        }
    }

    @Accepts(Statement.class) @Outputs(Triple.class)
    public static class JenaStatement2Triple extends DetachedBaseConverter {
        public static final JenaStatement2Triple INSTANCE = new JenaStatement2Triple();
        @Override public @Nonnull Triple convert(@Nonnull Object input) throws ConversionException {
            try {
                return JR.asTriple(((Statement)input).asTriple());
            } catch (org.apache.commons.rdf.jena.ConversionException e) {
                throw new ConversionException(input, this, outputClass(), "Non-concrete triple");
            }
        }
    }

    @Accepts(org.apache.jena.graph.Triple.class) @Outputs(Quad.class)
    public static class JenaTriple2Quad extends DetachedBaseConverter {
        public static final JenaTriple2Quad INSTANCE = new JenaTriple2Quad();
        @Override public @Nonnull Quad convert(@Nonnull Object input) throws ConversionException {
            try {
                org.apache.jena.sparql.core.Quad quad = new org.apache.jena.sparql.core.Quad(
                        org.apache.jena.sparql.core.Quad.defaultGraphIRI,
                        (org.apache.jena.graph.Triple) input
                );
                return JR.asQuad(quad);
            } catch (org.apache.commons.rdf.jena.ConversionException e) {
                throw new ConversionException(input, this, outputClass(), "Non-concrete triple");
            }
        }
    }

    @Accepts(Statement.class) @Outputs(Quad.class)
    public static class JenaStatement2Quad extends DetachedBaseConverter {
        public static final JenaStatement2Quad INSTANCE = new JenaStatement2Quad();
        @Override public @Nonnull Quad convert(@Nonnull Object input) throws ConversionException {
            try {
                org.apache.jena.graph.Triple triple = ((Statement) input).asTriple();
                return JenaTriple2Quad.INSTANCE.convert(triple);
            } catch (org.apache.commons.rdf.jena.ConversionException e) {
                throw new ConversionException(input, this, outputClass(), "Non-concrete triple");
            }
        }
    }

    @Accepts(org.apache.jena.sparql.core.Quad.class) @Outputs(Quad.class)
    public static class JenaQuad2Quad extends DetachedBaseConverter {
        public static final JenaQuad2Quad INSTANCE = new JenaQuad2Quad();
        @Override public @Nonnull Quad convert(@Nonnull Object input) throws ConversionException {
            try {
                return JR.asQuad(((org.apache.jena.sparql.core.Quad)input));
            } catch (org.apache.commons.rdf.jena.ConversionException e) {
                throw new ConversionException(input, this, outputClass(), "Non-concrete quad");
            }
        }
    }

    @Accepts(org.apache.jena.sparql.core.Quad.class) @Outputs(Triple.class)
    public static class JenaQuad2Triple extends DetachedBaseConverter {
        public static final JenaQuad2Triple INSTANCE = new JenaQuad2Triple();
        @Override public @Nonnull Triple convert(@Nonnull Object input) throws ConversionException {
            try {
                return JR.asTriple(((org.apache.jena.sparql.core.Quad)input).asTriple());
            } catch (org.apache.commons.rdf.jena.ConversionException e) {
                throw new ConversionException(input, this, outputClass(), "Non-concrete quad");
            }
        }
    }

    @Accepts(Quad.class) @Outputs(org.apache.jena.sparql.core.Quad.class)
    public static class Quad2JenaQuad extends DetachedBaseConverter {
        public static final Quad2JenaQuad INSTANCE = new Quad2JenaQuad();
        @Override public @Nonnull org.apache.jena.sparql.core.Quad convert(@Nonnull Object input) {
            return JR.asJenaQuad((Quad) input);
        }
    }

    @Accepts(Quad.class) @Outputs(org.apache.jena.graph.Triple.class)
    public static class Quad2JenaTriple extends DetachedBaseConverter {
        public static final Quad2JenaTriple INSTANCE = new Quad2JenaTriple();
        @Override public @Nonnull org.apache.jena.graph.Triple convert(@Nonnull Object input) {
            return JR.asJenaTriple(((Quad) input).asTriple());
        }
    }

    @Accepts(Quad.class) @Outputs(Statement.class)
    public static class Quad2JenaStatement extends DetachedBaseConverter {
        public static final Quad2JenaStatement INSTANCE = new Quad2JenaStatement();
        @Override public @Nonnull Statement convert(@Nonnull Object input) {
            return Triple2JenaStatement.INSTANCE.convert(((Quad)input).asTriple());
        }
    }

    @Accepts(Triple.class) @Outputs(org.apache.jena.graph.Triple.class)
    public static class Triple2JenaTriple extends DetachedBaseConverter {
        public static final Triple2JenaTriple INSTANCE = new Triple2JenaTriple();
        @Override public @Nonnull org.apache.jena.graph.Triple convert(@Nonnull Object input) {
            return JR.asJenaTriple((Triple) input);
        }
    }

    @Accepts(Triple.class) @Outputs(Statement.class)
    public static class Triple2JenaStatement extends DetachedBaseConverter {
        public static final Triple2JenaStatement INSTANCE = new Triple2JenaStatement();
        @Override public @Nonnull Statement convert(@Nonnull Object input) {
            return wrap(JR.asJenaTriple((Triple) input));
        }

        private static @Nonnull Statement wrap(@Nonnull org.apache.jena.graph.Triple triple) {
            Resource s = wrap(triple.getSubject()).asResource();
            Property p = new PropertyImpl(triple.getPredicate(), null);
            RDFNode o = wrap(triple.getObject());
            return ResourceFactory.createStatement(s, p, o);
        }
        private static @Nonnull RDFNode wrap(@Nonnull Node node) {
            if (node.isLiteral()) {
                return new LiteralImpl(node, null);
            } else if (node instanceof Node_Triple) {
                Statement stmt = wrap(((Node_Triple) node).get());
                return ReifiedStatementImpl.create(null, node, stmt);
            } else {
                return new ResourceImpl(node, null);
            }
        }
    }

    @Accepts(Triple.class) @Outputs(org.apache.jena.sparql.core.Quad.class)
    public static class Triple2JenaQuad extends DetachedBaseConverter {
        public static final Triple2JenaQuad INSTANCE = new Triple2JenaQuad();
        @Override public @Nonnull org.apache.jena.sparql.core.Quad convert(@Nonnull Object input) {
            org.apache.jena.graph.Triple t = Triple2JenaTriple.INSTANCE.convert(input);
            return new org.apache.jena.sparql.core.Quad(
                    org.apache.jena.sparql.core.Quad.defaultGraphIRI, t);
        }
    }

}
