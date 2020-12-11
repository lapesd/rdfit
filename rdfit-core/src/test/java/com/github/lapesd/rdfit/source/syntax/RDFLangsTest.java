package com.github.lapesd.rdfit.source.syntax;

import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import com.google.common.collect.Lists;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.rdf.TripleWriter;
import org.rdfhdt.hdt.triples.TripleString;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

public class RDFLangsTest {
    static {
        JenaSystem.init();
    }

    private static final @Nonnull String EX = "http://example.org/";
    private static final @Nonnull Resource S = ResourceFactory.createResource(EX+"S1");
    private static final @Nonnull Resource B = ResourceFactory.createResource();
    private static final @Nonnull Property P = ResourceFactory.createProperty(EX+"P1");
    private static final @Nonnull Resource O = ResourceFactory.createResource(EX+"O1");
    private static final @Nonnull Literal PL = ResourceFactory.createPlainLiteral("\"pl\"a\"in@");
    private static final @Nonnull Literal SL = ResourceFactory.createTypedLiteral(" \" st@r\"^^\"");
    private static final @Nonnull Literal LL = ResourceFactory.createLangLiteral("'l \"a\"n@g \"", "en");
    private static final @Nonnull Literal IL = ResourceFactory.createTypedLiteral(1);
    private static final @Nonnull Literal BL = ResourceFactory.createTypedLiteral(false);



    private static final List<RDFFormat> JENA_LANGS = asList(
            RDFFormat.NT, RDFFormat.TTL, RDFFormat.TURTLE_PRETTY,
            RDFFormat.TRIG, RDFFormat.TRIG_PRETTY,
            RDFFormat.JSONLD, RDFFormat.JSONLD_COMPACT_PRETTY, RDFFormat.JSONLD_FLATTEN_FLAT,
            RDFFormat.RDFXML, RDFFormat.TRIX
    );
    private static final List<RDFLang> RDFIT_LANGS = asList(
            RDFLangs.TRIG, RDFLangs.TRIG, RDFLangs.TRIG,
            RDFLangs.TRIG, RDFLangs.TRIG,
            RDFLangs.JSONLD, RDFLangs.JSONLD, RDFLangs.JSONLD,
            RDFLangs.RDFXML, RDFLangs.TRIX
    );

    @DataProvider public @Nonnull Object[][] guessData() throws Exception {
        List<List<Object>> rows = new ArrayList<>();

        List<Boolean> booleans = asList(false, true);
        List<RDFNode> subs = asList(S, B);
        List<RDFNode> preds = asList(P, RDF.type);
        List<RDFNode> objs = asList(O, PL, SL, LL, IL, BL);
        for (List<Object> nodes : Lists.cartesianProduct(booleans, subs, preds, objs)) {
            Model m = ModelFactory.createDefaultModel();
            if (nodes.get(0).equals(Boolean.TRUE))
                m.setNsPrefix("ex", EX);
            m.add((Resource)nodes.get(1), (Property)nodes.get(2), (RDFNode)nodes.get(3));
            for (int i = 0, size = JENA_LANGS.size(); i < size; i++) {
                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    RDFDataMgr.write(out, m, JENA_LANGS.get(i));
                    //noinspection StringOperationCanBeSimplified
                    String input = new String(out.toByteArray(), UTF_8);
                    rows.add(asList(input, RDFIT_LANGS.get(i)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        for (List<RDFNode> nodes : Lists.cartesianProduct(subs, preds, objs)) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                HDTSpecification hdtSpecification = new HDTSpecification();
                try (TripleWriter writer = HDTManager.getHDTWriter(out, EX, hdtSpecification)) {
                    String sub = nodes.get(0).toString(), pred = nodes.get(1).toString();
                    Node o = nodes.get(2).asNode();
                    String obj = o.toString(true);

                    if (o.isLiteral() && o.getLiteralLanguage().isEmpty()
                                      && o.getLiteralDatatypeURI() != null) {
                        obj = obj.replaceAll("([^\\\\]\")\\^\\^([^^]+)$", "$1^^<$2>");
                    }
                    TripleString tripleString = new TripleString(sub, pred, obj);
                    writer.addTriple(tripleString);
                }
                rows.add(asList(out.toByteArray(), RDFLangs.HDT));
            }
        }

        return rows.stream().map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "guessData")
    public void testGuess(@Nonnull Object input, @Nonnull RDFLang expected) throws IOException {
        byte[] bs = input instanceof byte[] ? (byte[]) input : input.toString().getBytes(UTF_8);
        try (ByteArrayInputStream is = new ByteArrayInputStream(bs)) {
            assertEquals(RDFLangs.guess(is, Integer.MAX_VALUE), expected);
        }
    }

}