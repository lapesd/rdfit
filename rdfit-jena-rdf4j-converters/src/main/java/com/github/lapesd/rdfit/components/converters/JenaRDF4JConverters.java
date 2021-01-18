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

package com.github.lapesd.rdfit.components.converters;

import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.components.Converter;
import com.github.lapesd.rdfit.components.annotations.Accepts;
import com.github.lapesd.rdfit.components.annotations.Outputs;
import com.github.lapesd.rdfit.errors.ConversionException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_Triple;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.sparql.core.Quad;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static org.apache.jena.sparql.core.Quad.defaultGraphIRI;

public class JenaRDF4JConverters {
    private static final List<Converter> INSTANCES = Arrays.asList(
            Triple2RDF4J.INSTANCE, Quad2RDF4J.INSTANCE, Statement2RDF4J.INSTANCE,
            RDF4J2Triple.INSTANCE, RDF4J2Quad.INSTANCE, RDF4J2Statement.INSTANCE
    );

    public static void registerAll(@Nonnull ConversionManager conversionManager) {
        for (Converter c : INSTANCES) conversionManager.register(c);
    }
    public static void registerAll(@Nonnull RDFItFactory factory) {
        registerAll(factory.getConversionManager());
    }

    public static void unregisterAll(@Nonnull ConversionManager conversionManager) {
        for (Converter c : INSTANCES) conversionManager.unregister(c);
    }
    public static void unregisterAll(@Nonnull RDFItFactory factory) {
        unregisterAll(factory.getConversionManager());
    }

    private static final @Nonnull SimpleValueFactory VF = SimpleValueFactory.getInstance();

    public static Value node2value(@Nullable Node node) throws ConversionException {
        if (node == null) {
            return null;
        } else if (node.isBlank()) {
            return VF.createBNode(node.getBlankNodeLabel());
        } else if (node.isURI()) {
            return VF.createIRI(node.getURI());
        } else if (node instanceof Node_Triple) {
            Triple triple = ((Node_Triple) node).get();
            Statement stmt = Triple2RDF4J.INSTANCE.convert(triple);
            return VF.createTriple(stmt.getSubject(), stmt.getPredicate(), stmt.getObject());
        } else if (node.isLiteral()) {
            String lang = node.getLiteralLanguage();
            if (lang != null && !lang.isEmpty()) {
                return VF.createLiteral(node.getLiteralLexicalForm(), lang);
            } else {
                RDFDatatype dt = node.getLiteralDatatype();
                String uri = (dt == null ? XSDDatatype.XSDstring : dt).getURI();
                return VF.createLiteral(node.getLiteralLexicalForm(), VF.createIRI(uri));
            }
        } else {
            throw new IllegalArgumentException("Cannot convert Jena "+node+" to RDF4J");
        }
    }

    public static Node value2node(@Nullable Value value) throws ConversionException {
        return value2node(value, null);
    }

    public static Node value2node(@Nullable Value value, Node fallback) throws ConversionException {
        if (value == null) {
            return fallback;
        } else if (value.isBNode()) {
            return NodeFactory.createBlankNode(((BNode)value).getID());
        } else if (value.isIRI()) {
            return NodeFactory.createURI(value.toString());
        } else if (value.isLiteral()) {
            Literal l = (Literal) value;
            if (l.getLanguage().isPresent()) {
                return NodeFactory.createLiteral(l.getLabel(), l.getLanguage().get());
            } else {
                String uri = l.getDatatype().toString();
                RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(uri);
                return NodeFactory.createLiteral(l.getLabel(), dt);
            }
        } else if (value.isTriple()) {
            org.eclipse.rdf4j.model.Triple t = (org.eclipse.rdf4j.model.Triple) value;
            Statement stmt = VF.createStatement(t.getSubject(), t.getPredicate(), t.getObject());
            return new Node_Triple(RDF4J2Triple.INSTANCE.convert(stmt));
        }
        throw new IllegalArgumentException("Unexpected Value type "+value.getClass());
    }

    @Accepts(Triple.class) @Outputs(Statement.class)
    public static class Triple2RDF4J extends DetachedBaseConverter {
        public static final Triple2RDF4J INSTANCE = new Triple2RDF4J();

        @Override public boolean canConvert(@Nonnull Object input) {
            return super.canConvert(input) && ((Triple)input).isConcrete();
        }

        @Override public @Nonnull Statement convert(@Nonnull Object input) throws ConversionException {
            Triple t = (Triple) input;
            Value s = node2value(t.getSubject());
            Value p = node2value(t.getPredicate());
            Value o = node2value(t.getObject());
            return VF.createStatement((Resource) s, (IRI) p, o);
        }
    }

    @Accepts(Quad.class) @Outputs(Statement.class)
    public static class Quad2RDF4J extends DetachedBaseConverter {
        public static final Quad2RDF4J INSTANCE = new Quad2RDF4J();

        @Override public boolean canConvert(@Nonnull Object input) {
            return super.canConvert(input) && ((Quad)input).isConcrete();
        }

        @Override public @Nonnull Statement convert(@Nonnull Object input) throws ConversionException {
            Quad q = (Quad) input;
            Node qg = q.getGraph();
            Value g = qg.equals(defaultGraphIRI) || qg.equals(Quad.defaultGraphNodeGenerated)
                    ? null : node2value(qg);
            Value s = node2value(q.getSubject());
            Value p = node2value(q.getPredicate());
            Value o = node2value(q.getObject());
            return VF.createStatement((Resource) s, (IRI) p, o, (Resource) g);
        }
    }

    @Accepts(org.apache.jena.rdf.model.Statement.class) @Outputs(Statement.class)
    public static class Statement2RDF4J extends DetachedBaseConverter {
        public static final Statement2RDF4J INSTANCE = new Statement2RDF4J();

        @Override public @Nonnull Statement convert(@Nonnull Object input) throws ConversionException {
            Triple t = ((org.apache.jena.rdf.model.Statement) input).asTriple();
            return Triple2RDF4J.INSTANCE.convert(t);
        }
    }

    @Accepts(Statement.class) @Outputs(Triple.class)
    public static class RDF4J2Triple extends DetachedBaseConverter {
        public static final RDF4J2Triple INSTANCE = new RDF4J2Triple();

        @Override public @Nonnull Triple convert(@Nonnull Object input) throws ConversionException {
            Statement stmt = (Statement) input;
            return new Triple(value2node(stmt.getSubject()), value2node(stmt.getPredicate()),
                              value2node(stmt.getObject()));
        }
    }

    @Accepts(Statement.class) @Outputs(Quad.class)
    public static class RDF4J2Quad extends DetachedBaseConverter {
        public static final RDF4J2Quad INSTANCE = new RDF4J2Quad();

        @Override public @Nonnull Quad convert(@Nonnull Object input) throws ConversionException {
            Statement s = (Statement) input;
            return new Quad(value2node(s.getContext(), defaultGraphIRI), value2node(s.getSubject()),
                            value2node(s.getPredicate()), value2node(s.getObject()));
        }
    }

    @Accepts(Statement.class) @Outputs(org.apache.jena.rdf.model.Statement.class)
    public static class RDF4J2Statement extends DetachedBaseConverter {
        public static final RDF4J2Statement INSTANCE = new RDF4J2Statement();

        @Override public @Nonnull org.apache.jena.rdf.model.Statement
        convert(@Nonnull Object input) throws ConversionException {
            Statement stmt = (Statement) input;
            ResourceImpl s = new ResourceImpl(value2node(stmt.getSubject()), null);
            Property p = new PropertyImpl(value2node(stmt.getPredicate()), null);
            Node on = value2node(stmt.getObject());
            RDFNode o = on.isLiteral() ? new LiteralImpl(on, null)
                                       : new ResourceImpl(on, null);
            return ResourceFactory.createStatement(s, p, o);
        }
    }
}
