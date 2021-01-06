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

package com.github.lapesd.rdfit.components.jena;

import com.github.lapesd.rdfit.RDFItFactory;
import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.components.converters.util.ConversionCache;
import com.github.lapesd.rdfit.components.converters.util.ConversionPathSingletonCache;
import com.github.lapesd.rdfit.components.jena.converters.JenaConverters;
import com.github.lapesd.rdfit.iterator.RDFIt;
import com.github.lapesd.rdfit.source.syntax.RDFLangs;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.graph.GraphFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class JenaHelpers {
    public static @Nullable Lang toJenaLang(RDFLang lang) {
        if      (lang.equals(RDFLangs.NT     )) return Lang.NT;
        else if (lang.equals(RDFLangs.NQ     )) return Lang.NQ;
        else if (lang.equals(RDFLangs.TTL    )) return Lang.TTL;
        else if (lang.equals(RDFLangs.TRIG   )) return Lang.TRIG;
        else if (lang.equals(RDFLangs.RDFXML )) return Lang.RDFXML;
        else if (lang.equals(RDFLangs.OWL    )) return Lang.RDFXML;
        else if (lang.equals(RDFLangs.TRIX   )) return Lang.TRIX;
        else if (lang.equals(RDFLangs.JSONLD )) return Lang.JSONLD;
        else if (lang.equals(RDFLangs.RDFJSON)) return Lang.RDFJSON;
        else if (lang.equals(RDFLangs.THRIFT )) return Lang.RDFTHRIFT;
        else                                    return null;
    }

    public static @Nullable RDFLang fromJenaLang(Lang lang) {
        if      (lang.equals(Lang.NT       )) return RDFLangs.NT;
        else if (lang.equals(Lang.NQ       )) return RDFLangs.NQ;
        else if (lang.equals(Lang.TTL      )) return RDFLangs.TTL;
        else if (lang.equals(Lang.TRIG     )) return RDFLangs.TRIG;
        else if (lang.equals(Lang.RDFXML   )) return RDFLangs.RDFXML;
        else if (lang.equals(Lang.TRIX     )) return RDFLangs.TRIX;
        else if (lang.equals(Lang.JSONLD   )) return RDFLangs.JSONLD;
        else if (lang.equals(Lang.RDFJSON  )) return RDFLangs.RDFJSON;
        else if (lang.equals(Lang.RDFTHRIFT)) return RDFLangs.THRIFT;
        else                                  return null;
    }

    public static @Nonnull Model toModel(@Nonnull Model model, @Nonnull RDFIt<?> it) {
        ConversionManager mgr = DefaultConversionManager.INSTANCE;
        try {
            ConversionCache cache = ConversionPathSingletonCache.createCache(mgr, Statement.class);
            while (it.hasNext()) {
                Object next = it.next();
                model.add((Statement) cache.convert(it.getSource(), next));
            }
        } finally {
            it.close();
        }
        return model;
    }

    public static @Nonnull Model toModel(@Nonnull RDFIt<?> it) {
        return toModel(ModelFactory.createDefaultModel(), it);
    }

    public static @Nonnull Graph toGraph(@Nonnull Graph graph, @Nonnull RDFIt<?> it) {
        ConversionManager mgr = DefaultConversionManager.INSTANCE;
        try {
            ConversionCache cache = ConversionPathSingletonCache.createCache(mgr, Triple.class);
            while (it.hasNext()) {
                Object next = it.next();
                graph.add((Triple) cache.convert(it.getSource(), next));
            }
        } finally {
            it.close();
        }
        return graph;
    }

    public static @Nonnull Graph toGraph(@Nonnull RDFIt<?> it) {
        return toGraph(GraphFactory.createDefaultGraph(), it);
    }

    public static @Nonnull GraphFeeder createFeeder(@Nonnull Graph graph) {
        return new GraphFeeder(graph);
    }
    public static @Nonnull ModelFeeder creteFeeder(@Nonnull Model model) {
        return new ModelFeeder(model);
    }
    public static @Nonnull DatasetFeeder creteFeeder(@Nonnull Dataset ds) {
        return new DatasetFeeder(ds);
    }
    public static @Nonnull DatasetGraphFeeder createFeeder(@Nonnull DatasetGraph dsg) {
        return new DatasetGraphFeeder(dsg);
    }

    public static void registerAll(@Nonnull RDFItFactory factory) {
        JenaConverters.registerAll(factory);
        JenaModelParsers.registerAll(factory);
    }
}
