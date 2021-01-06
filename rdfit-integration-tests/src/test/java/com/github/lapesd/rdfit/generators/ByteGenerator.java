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

package com.github.lapesd.rdfit.generators;

import com.github.lapesd.rdfit.TripleSet;
import com.github.lapesd.rdfit.components.hdt.converters.HDTConverters.Triple2TripleString;
import com.github.lapesd.rdfit.components.jena.JenaHelpers;
import com.github.lapesd.rdfit.source.syntax.RDFLangs;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetGraph;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.rdf.TripleWriter;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static java.util.Arrays.asList;
import static org.apache.jena.riot.RDFFormat.JSONLD_FRAME_FLAT;
import static org.apache.jena.riot.RDFFormat.JSONLD_FRAME_PRETTY;

public class ByteGenerator implements SourceGenerator {
    private static final String EX = "http://example.org/";
    static final Set<RDFLang> CAN_GUESS = Collections.unmodifiableSet(new HashSet<>(asList(
            RDFLangs.HDT,
            RDFLangs.RDFXML,
            RDFLangs.OWL,
            RDFLangs.JSONLD,
            RDFLangs.TRIX,
            RDFLangs.NT,
            RDFLangs.NQ,
            RDFLangs.TTL,
            RDFLangs.TRIG
    )));
    private static List<RDFFormat> FORMATS = null;

    public static boolean hasQuads(RDFLang lang) {
        return lang.equals(RDFLangs.NQ) || lang.equals(RDFLangs.TRIG);
    }

    public static List<RDFFormat> getFormats() {
        if (FORMATS != null)
            return FORMATS;
        List<RDFFormat> list = new ArrayList<>();
        for (Field f : RDFFormat.class.getFields()) {
            if (!Modifier.isStatic(f.getModifiers()) || !Modifier.isPublic(f.getModifiers()))
                continue;
            Object object;
            try {
                object = f.get(null);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            if (object instanceof RDFFormat)
                list.add((RDFFormat) object);
        }
        return FORMATS = list;
    }

    @Override public boolean isReusable() {
        return true;
    }

    @Override public @Nonnull List<?> generate(@Nonnull TripleSet tripleSet,
                                               @Nonnull File tempDir) {
        List<ImmutablePair<byte[], RDFLang>> in = generateWithLang(tripleSet);
        List<byte[]> out = new ArrayList<>();
        for (ImmutablePair<byte[], RDFLang> pair : in) {
            if (CAN_GUESS.contains(pair.right))
                out.add(pair.left);
        }
        return out;
    }

    public @Nonnull List<ImmutablePair<byte[], RDFLang>> generateWithLang(@Nonnull TripleSet set) {
        List<ImmutablePair<byte[], RDFLang>> list = new ArrayList<>();
        List<RDFLang> langs = RDFLangs.getLangs();
        for (RDFLang lang : langs) {
            if (set.hasQuads() && !hasQuads(lang))
                continue;
            if (lang.equals(RDFLangs.HDT)) {
                String baseURI = "http://example.org/baseHDT";
                HDTSpecification spec = new HDTSpecification();
                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    try (TripleWriter writer = HDTManager.getHDTWriter(out, baseURI, spec)) {
                        for (Triple triple : set.export(Triple.class))
                            writer.addTriple(Triple2TripleString.INSTANCE.convert(triple));
                    }
                    list.add(ImmutablePair.of(out.toByteArray(), lang));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else {
                Lang jLang = JenaHelpers.toJenaLang(lang);
                if (jLang == null)
                    continue;
                for (RDFFormat fmt : getFormats()) {
                    if (!fmt.getLang().equals(jLang))
                        continue;
                    if (fmt.equals(JSONLD_FRAME_FLAT) || fmt.equals(JSONLD_FRAME_PRETTY))
                        continue;
                    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        if (set.hasQuads()) {
                            DatasetGraph dsg = set.toDatasetGraph();
                            dsg.getDefaultGraph().getPrefixMapping().setNsPrefix("ex", EX);
                            RDFDataMgr.write(out, dsg, fmt);
                        } else {
                            Graph graph = set.toGraph();
                            graph.getPrefixMapping().setNsPrefix("ex", EX);
                            RDFDataMgr.write(out, set.toGraph(), fmt);
                        }
                        list.add(ImmutablePair.of(out.toByteArray(), lang));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return list;
    }
}
