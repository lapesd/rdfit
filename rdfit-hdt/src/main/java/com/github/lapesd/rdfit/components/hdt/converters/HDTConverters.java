package com.github.lapesd.rdfit.components.hdt.converters;

import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.components.Converter;
import com.github.lapesd.rdfit.components.annotations.Accepts;
import com.github.lapesd.rdfit.components.annotations.Outputs;
import com.github.lapesd.rdfit.components.converters.BaseConverter;
import com.github.lapesd.rdfit.components.converters.ConversionManager;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;
import static org.apache.jena.graph.NodeFactory.createBlankNode;
import static org.apache.jena.graph.NodeFactory.createLiteral;

public class HDTConverters {
    private static final @Nonnull Pattern INT_RX = Pattern.compile("[+-]?[0-9]+");
    private static final @Nonnull Pattern DEC_RX = Pattern.compile("[+-]?[0-9]*\\.[0-9]+");
    private static final @Nonnull Pattern DOUBLE_RX = Pattern.compile(
            "([+-]?[0-9]|[+-]?\\.[0-9]+|[+-]?[0-9]+\\.[0-9]+)[eE]([+-]?[0-9]+)");
    private static final @Nonnull Map<String, String> PREFIXES;
    private static final @Nonnull List<Converter> CONVERTERS
            = singletonList(TripleString2Triple.INSTANCE);

    static {
        Map<String, String> map = new HashMap<>();
        map.put("xsd", "http://www.w3.org/2001/XMLSchema#");
        map.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        map.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        PREFIXES = Collections.unmodifiableMap(map);
    }

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

    @Accepts(TripleString.class) @Outputs(Triple.class)
    public static class TripleString2Triple extends BaseConverter {
        public static final @Nonnull TripleString2Triple INSTANCE = new TripleString2Triple();

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

        @Override public @Nonnull Object convert(@Nonnull Object input) throws ConversionException {
            TripleString ts = (TripleString) input;
            Node s = convertTerm(ts, ts.getSubject(), true);
            Node p = convertTerm(ts, ts.getPredicate(), true);
            Node o = convertTerm(ts, ts.getObject(), false);
            return new Triple(s, p, o);
        }
    }

    public static void registerAll(@Nonnull ConversionManager mgr) {
        for (Converter c : CONVERTERS) mgr.register(c);
        JenaConverters.registerAll(mgr);
    }
    public static void registerAll(@Nonnull RDFItFactory factory) {
        registerAll(factory.getConverterManager());
    }

    public static void unregisterAll(@Nonnull ConversionManager mgr) {
        for (Converter c : CONVERTERS) mgr.register(c);
        JenaConverters.unregisterAll(mgr);
    }
    public static void unregisterAll(@Nonnull RDFItFactory factory) {
        registerAll(factory.getConverterManager());
    }
}
