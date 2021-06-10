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

package com.github.lapesd.rdfit.components.rdf4j.parsers;

import com.github.lapesd.rdfit.source.syntax.RDFLangs;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import org.eclipse.rdf4j.rio.RDFFormat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Helper for converting between {@link RDFLang} and {@link RDFFormat}.
 */
public class RDF4JFormat {

    /**
     * Convert a {@link RDFLang} into a {@link RDFFormat}
     * @param lang a {@link RDFLang}
     * @return a {@link RDFFormat} or null if there is no equivalent
     */
    public static @Nullable RDFFormat toRDF4J(@Nullable RDFLang lang) {
        if      (RDFLangs.RDFXML.equals(lang))  return RDFFormat.RDFXML;
        else if (RDFLangs.NT.equals(lang))      return RDFFormat.NTRIPLES;
        else if (RDFLangs.TTL.equals(lang))     return RDFFormat.TURTLE;
        else if (RDFLangs.TRIX.equals(lang))    return RDFFormat.TRIX;
        else if (RDFLangs.TRIG.equals(lang))    return RDFFormat.TRIG;
        else if (RDFLangs.BRF.equals(lang))     return RDFFormat.BINARY;
        else if (RDFLangs.NQ.equals(lang))      return RDFFormat.NQUADS;
        else if (RDFLangs.JSONLD.equals(lang))  return RDFFormat.JSONLD;
        else if (RDFLangs.RDFJSON.equals(lang)) return RDFFormat.RDFJSON;
        else if (RDFLangs.RDFA.equals(lang))    return RDFFormat.RDFA;
        else if (RDFLangs.HDT.equals(lang))     return RDFFormat.HDT;
        else                                    return null;
    }

    /**
     * Conver a {@link RDFFormat} into the equivalent {@link RDFLang}
     * @param format a {@link RDFFormat}
     * @return a {@link RDFLang} or null if there is no equivalent
     */
    public static @Nullable RDFLang fromRDF4j(@Nullable RDFFormat format) {
        if      (format == null)                      return null;
        else if (format.equals(RDFFormat.RDFXML))     return RDFLangs.RDFXML;
        else if (format.equals(RDFFormat.NTRIPLES))   return RDFLangs.NT;
        else if (format.equals(RDFFormat.TURTLE))     return RDFLangs.TTL;
        else if (format.equals(RDFFormat.TURTLESTAR)) return RDFLangs.TTL;
        else if (format.equals(RDFFormat.N3))         return RDFLangs.NT;
        else if (format.equals(RDFFormat.TRIX))       return RDFLangs.TRIX;
        else if (format.equals(RDFFormat.TRIG))       return RDFLangs.TRIG;
        else if (format.equals(RDFFormat.TRIGSTAR))   return RDFLangs.TRIG;
        else if (format.equals(RDFFormat.BINARY))     return RDFLangs.BRF;
        else if (format.equals(RDFFormat.NQUADS))     return RDFLangs.NQ;
        else if (format.equals(RDFFormat.JSONLD))     return RDFLangs.JSONLD;
        else if (format.equals(RDFFormat.RDFJSON))    return RDFLangs.RDFJSON;
        else if (format.equals(RDFFormat.RDFA))       return RDFLangs.RDFA;
        else if (format.equals(RDFFormat.HDT))        return RDFLangs.HDT;
        else                                          return null;
    }
}
