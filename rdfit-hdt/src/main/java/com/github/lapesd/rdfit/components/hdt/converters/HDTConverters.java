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

package com.github.lapesd.rdfit.components.hdt.converters;

import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.components.Converter;
import com.github.lapesd.rdfit.components.annotations.Accepts;
import com.github.lapesd.rdfit.components.annotations.Outputs;
import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.components.converters.DetachedBaseConverter;
import com.github.lapesd.rdfit.components.jena.converters.JenaConverters;
import com.github.lapesd.rdfit.errors.ConversionException;
import com.github.lapesd.rdfit.util.Literal;
import com.github.lapesd.rdfit.util.LiteralParser;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.rdfhdt.hdt.triples.TripleString;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.regex.Pattern;

import static org.apache.jena.graph.NodeFactory.createBlankNode;
import static org.apache.jena.graph.NodeFactory.createLiteral;

/**
 * Registers/unregisters converters between Jena and HDT triples
 */
public class HDTConverters {
    private static final @Nonnull Pattern INT_RX = Pattern.compile("[+-]?[0-9]+");
    private static final @Nonnull Pattern DEC_RX = Pattern.compile("[+-]?[0-9]*\\.[0-9]+");
    private static final @Nonnull Pattern DOUBLE_RX = Pattern.compile(
            "([+-]?[0-9]|[+-]?\\.[0-9]+|[+-]?[0-9]+\\.[0-9]+)[eE]([+-]?[0-9]+)");
    private static final @Nonnull Map<String, String> PREFIXES;
    private static final @Nonnull List<Converter> CONVERTERS = Arrays.asList(
            TripleString2Triple.INSTANCE, Triple2TripleString.INSTANCE
    );

    static {
        Map<String, String> map = new HashMap<>();
        map.put("xsd", "http://www.w3.org/2001/XMLSchema#");
        map.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        map.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        PREFIXES = Collections.unmodifiableMap(map);
    }

    /**
     * Convert a string used in a {@link TripleString} to a jena {@link Node}
     * @param seq an RDF term
     * @return representation of the RDF term as a Jena {@link Node}
     */
    public static @Nonnull Node hdtStringToNode(@Nonnull CharSequence seq) {
        if (seq.length() == 0) return createBlankNode();
        char f = seq.charAt(0);
        String string = seq.toString();
        if (string.equalsIgnoreCase("false") || string.equalsIgnoreCase("true")) {
            return createLiteral(string.toLowerCase(), XSDDatatype.XSDboolean);
        } else  if (f=='"' || f=='\'' || f>='0' && f<='9' || f=='.' || f=='-' || f=='+') {
            Literal lit = new LiteralParser().parse(string);
            if (lit.isLang()) {
                return createLiteral(lit.getLexicalForm(), lit.getLangTag());
            } else if (!lit.isQuoted()) {
                String lex = lit.getLexicalForm();
                if (INT_RX.matcher(lex).matches()) {
                    return  createLiteral(lex, XSDDatatype.XSDinteger);
                } else if (DEC_RX.matcher(lex).matches()) {
                    return createLiteral(lex, XSDDatatype.XSDdecimal);
                } else if (DOUBLE_RX.matcher(lex).matches()) {
                    return createLiteral(lex, XSDDatatype.XSDdouble);
                } else {
                    throw new IllegalArgumentException("Literal "+lit+" is not valid Turtle");
                }
            } else if (lit.isTyped()) {
                String iri;
                if (lit.isIRITyped()) {
                    iri = lit.getTypeIRI();
                } else {
                    assert lit.isPrefixTyped();
                    String name = lit.getTypePrefixName();
                    if (name == null)
                        throw new IllegalArgumentException("null prefix name for datatype in " + lit);
                    name = name.trim().toLowerCase().replaceAll("[0-9_-]+$", "");
                    String prefix = PREFIXES.getOrDefault(name, null);
                    if (prefix == null)
                        throw new IllegalArgumentException("Unknown prefix " + name + " in " + lit);
                    iri = prefix + lit.getTypeLocalName();
                }
                assert iri != null;
                RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(iri);
                assert dt != null;
                return createLiteral(lit.getLexicalForm(), dt);
            } else { // plain literal (becomes xsd:string)
                return createLiteral(lit.getLexicalForm());
            }
        } else if (f == '_') {
            if (string.length() < 3)
                return createBlankNode();
            return createBlankNode(string.subSequence(2, string.length()).toString());
        } else {
            return NodeFactory.createURI(string);
        }
    }

    /**
     * Convert an HDT {@link TripleString} into a Jena {@link Triple}
     */
    @Accepts(TripleString.class) @Outputs(Triple.class)
    public static class TripleString2Triple extends DetachedBaseConverter {
        public static final @Nonnull TripleString2Triple INSTANCE = new TripleString2Triple();

        /**
         * Convert a single term of a {@link TripleString}
         * @param ts the {@link TripleString} of the term
         * @param term a member of the {@link TripleString}
         * @param requireResource fail if the result Node is not a blank node or URI
         * @return a jena {@link Node} representing term
         * @throws ConversionException If something goes wrong
         */
        protected @Nonnull Node convertTerm(@Nonnull TripleString ts, @Nonnull CharSequence term,
                                            boolean requireResource) throws ConversionException {
            try {
                Node node = hdtStringToNode(term);
                if (requireResource && node.isLiteral())
                    throw new ConversionException(ts, this, "Expected term "+term+" to be a resource");
                return node;
            } catch (IllegalArgumentException e) {
                throw new ConversionException(ts, this,
                        "Cannot convert term "+term+": "+e.getMessage());
            }
        }

        @Override public @Nonnull Triple convert(@Nonnull Object input) throws ConversionException {
            TripleString ts = (TripleString) input;
            Node s = convertTerm(ts, ts.getSubject(), true);
            Node p = convertTerm(ts, ts.getPredicate(), true);
            Node o = convertTerm(ts, ts.getObject(), false);
            return new Triple(s, p, o);
        }
    }

    /**
     * Convers Jena {@link Triple}s into {@link TripleString}s
     */
    @Accepts(Triple.class) @Outputs(TripleString.class)
    public static class Triple2TripleString extends DetachedBaseConverter {
        public static final @Nonnull Triple2TripleString INSTANCE = new Triple2TripleString();

        /**
         * Convert a single jena {@link Node} into a String for use in a {@link TripleString}
         * @param node the input node
         * @param in the triple that contains the node
         * @return the HDT string to be used in a {@link TripleString}
         * @throws ConversionException if something goes wrong.
         */
        private @Nonnull String toHDTString(@Nonnull Node node,
                                            @Nonnull Triple in) throws ConversionException {
            if (node.isBlank()) {
                return "_:"+node.toString();
            } else if (node.isURI()) {
                return node.getURI();
            } else if (node.isLiteral()) {
                String lang = node.getLiteralLanguage();
                String lex = node.getLiteralLexicalForm().replace("\"", "\\\"")
                                 .replace("\n", "\\n").replaceAll("\r", "\\r");
                if (lang != null && !lang.isEmpty()) {
                    return String.format("\"%s\"@%s", lex, lang);
                } else {
                    String uri = node.getLiteralDatatypeURI();
                    if (uri == null || uri.isEmpty())
                        uri = XSDDatatype.XSDstring.getURI();
                    return String.format("\"%s\"^^<%s>", lex, uri);
                }
            } else {
                throw new ConversionException(in, this, "non-concrete node "+node);
            }
        }

        @Override public @Nonnull TripleString convert(@Nonnull Object input)
                throws ConversionException {
            Triple t = (Triple) input;
            return new TripleString(
                    toHDTString(t.getSubject(), t),
                    toHDTString(t.getPredicate(), t),
                    toHDTString(t.getObject(), t)
            );
        }
    }

    /**
     * Add all HDT/jena converters to the given {@link ConversionManager}
     * @param mgr the {@link ConversionManager}
     */
    public static void registerAll(@Nonnull ConversionManager mgr) {
        for (Converter c : CONVERTERS) mgr.register(c);
        JenaConverters.registerAll(mgr);
    }

    /**
     * Calls {@link #registerAll(ConversionManager)} with {@link RDFItFactory#getConversionManager()}
     * @param factory the {@link RDFItFactory}
     */
    public static void registerAll(@Nonnull RDFItFactory factory) {
        registerAll(factory.getConversionManager());
    }

    /**
     * Removes all converters that might have been added by {@link #registerAll(ConversionManager)}
     * from the given {@link ConversionManager}.
     * @param mgr the {@link ConversionManager}
     */
    public static void unregisterAll(@Nonnull ConversionManager mgr) {
        for (Converter c : CONVERTERS) mgr.register(c);
        JenaConverters.unregisterAll(mgr);
    }

    /**
     * Calls {@link #unregisterAll(ConversionManager)} with
     * {@link RDFItFactory#getConversionManager()}
     * @param factory the {@link RDFItFactory}
     */
    public static void unregisterAll(@Nonnull RDFItFactory factory) {
        registerAll(factory.getConversionManager());
    }
}
