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
import com.github.lapesd.rdfit.RIt;
import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.components.converters.util.ConversionCache;
import com.github.lapesd.rdfit.components.converters.util.ConversionPathSingletonCache;
import com.github.lapesd.rdfit.components.jena.converters.JenaConverters;
import com.github.lapesd.rdfit.components.jena.iterators.JenaImportingRDFIt;
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

    public static @Nonnull Model toModel(@Nonnull Model model, @Nonnull RDFIt<?> inIt,
                                         boolean fetchImports) {
        ConversionManager mgr = DefaultConversionManager.INSTANCE;
        try (RDFIt<?> it = fetchImports ? new JenaImportingRDFIt<>(inIt) : inIt) {
            ConversionCache cache = ConversionPathSingletonCache.createCache(mgr, Statement.class);
            while (it.hasNext()) {
                Object next = it.next();
                model.add((Statement) cache.convert(it.getSource(), next));
            }
        }
        return model;
    }
    public static @Nonnull Model toModelImporting(@Nonnull Model model, @Nonnull RDFIt<?> it) {
        return toModel(model, it, true);
    }
    public static @Nonnull Model toModelImporting(@Nonnull Model model, @Nonnull Object... sources) {
        if (sources.length == 1 && sources[0] instanceof RDFIt)
            return toModelImporting(model, (RDFIt<?>) sources[0]);
        return toModel(model, RIt.iterateTriples(Statement.class, sources), true);
    }
    public static @Nonnull Model toModel(@Nonnull Model model, @Nonnull RDFIt<?> it) {
        return toModel(model, it, false);
    }
    public static @Nonnull Model toModel(@Nonnull Model model, @Nonnull Object... sources) {
        if (sources.length == 1 && sources[0] instanceof RDFIt)
            return toModel(model, (RDFIt<?>) sources[0]);
        return toModel(model, RIt.iterateTriples(Statement.class, sources), false);
    }
    public static @Nonnull Model toModel(@Nonnull RDFIt<?> it, boolean fetchImports) {
        return toModel(ModelFactory.createDefaultModel(), it, fetchImports);
    }
    public static @Nonnull Model toModelImporting(@Nonnull RDFIt<?> it) {
        return toModel(it, true);
    }
    public static @Nonnull Model toModelImporting(@Nonnull Object... sources) {
        if (sources.length == 1 && sources[0] instanceof RDFIt)
            return toModelImporting((RDFIt<?>) sources[0]);
        return toModel(RIt.iterateTriples(Statement.class, sources), true);
    }
    public static @Nonnull Model toModel(@Nonnull RDFIt<?> it) {
        return toModel(it, false);
    }
    public static @Nonnull Model toModel(@Nonnull Object... sources) {
        if (sources.length == 1 && sources[0] instanceof RDFIt)
            return toModel((RDFIt<?>) sources[0]);
        return toModel(RIt.iterateTriples(Statement.class, sources), false);
    }

    public static @Nonnull Graph toGraph(@Nonnull Graph graph, @Nonnull RDFIt<?> inIt,
                                         boolean fetchImports) {
        ConversionManager mgr = DefaultConversionManager.INSTANCE;
        try (RDFIt<?> it = fetchImports ? new JenaImportingRDFIt<>(inIt) : inIt) {
            ConversionCache cache = ConversionPathSingletonCache.createCache(mgr, Triple.class);
            while (it.hasNext()) {
                Object next = it.next();
                graph.add((Triple) cache.convert(it.getSource(), next));
            }
        }
        return graph;
    }
    public static @Nonnull Graph toGraphImporting(@Nonnull Graph graph, @Nonnull RDFIt<?> it) {
        return toGraph(graph, it, true);
    }
    public static @Nonnull Graph toGraphImporting(@Nonnull Graph graph, @Nonnull Object... sources) {
        if (sources.length == 1 && sources[0] instanceof RDFIt)
            return toGraphImporting(graph, (RDFIt<?>) sources[0]);
        return toGraph(graph, RIt.iterateTriples(Triple.class, sources), true);
    }
    public static @Nonnull Graph toGraph(@Nonnull Graph graph, @Nonnull RDFIt<?> it) {
        return toGraph(graph, it, false);
    }
    public static @Nonnull Graph toGraph(@Nonnull Graph graph, @Nonnull Object... sources) {
        if (sources.length == 1 && sources[0] instanceof RDFIt)
            return toGraph(graph, (RDFIt<?>) sources[0]);
        return toGraph(graph, RIt.iterateTriples(Triple.class, sources), false);
    }
    public static @Nonnull Graph toGraph(@Nonnull RDFIt<?> it, boolean fetchImports) {
        return toGraph(GraphFactory.createDefaultGraph(), it, fetchImports);
    }
    public static @Nonnull Graph toGraphImporting(@Nonnull RDFIt<?> it) {
        return toGraph(it, true);
    }
    public static @Nonnull Graph toGraphImporting(@Nonnull Object... sources) {
        if (sources.length == 1 && sources[0] instanceof RDFIt)
            return toGraphImporting((RDFIt<?>) sources[0]);
        return toGraph(RIt.iterateTriples(Triple.class, sources), true);
    }
    public static @Nonnull Graph toGraph(@Nonnull RDFIt<?> it) {
        return toGraph(it, false);
    }
    public static @Nonnull Graph toGraph(@Nonnull Object... sources) {
        if (sources.length == 1 && sources[0] instanceof RDFIt)
            return toGraph((RDFIt<?>) sources[0]);
        return toGraph(RIt.iterateTriples(Triple.class, sources), false);
    }

    public static @Nonnull GraphFeeder graphFeeder(@Nonnull Graph graph) {
        return new GraphFeeder(graph);
    }
    public static @Nonnull ModelFeeder modelFeeder(@Nonnull Model model) {
        return new ModelFeeder(model);
    }
    public static @Nonnull DatasetFeeder modelFeeder(@Nonnull Dataset ds) {
        return new DatasetFeeder(ds);
    }
    public static @Nonnull DatasetGraphFeeder graphFeeder(@Nonnull DatasetGraph dsg) {
        return new DatasetGraphFeeder(dsg);
    }

    public static void registerAll(@Nonnull RDFItFactory factory) {
        JenaConverters.registerAll(factory);
        JenaModelParsers.registerAll(factory);
    }
}
