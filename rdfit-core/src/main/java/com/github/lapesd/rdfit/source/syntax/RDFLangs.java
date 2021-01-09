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

package com.github.lapesd.rdfit.source.syntax;

import com.github.lapesd.rdfit.source.syntax.impl.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class RDFLangs {
    private static @Nonnull List<RDFLang> LANGS;
    private static final @Nonnull List<RDFLang> BUILTIN_LANGS;
    private static final @Nonnull MultiLangDetector LANG_DETECTOR = new MultiLangDetector();
    private static final @Nonnull Map<RDFLang, LangDetector> EXTRA_DETECTORS
            = Collections.synchronizedMap(new HashMap<>());

    public static final @Nonnull RDFLang NT      = new SimpleRDFLang("N-Triples",        asList("nt",  "ntriples"),  "application/n-triples", false);
    public static final @Nonnull RDFLang NQ      = new SimpleRDFLang("N-Quads",          asList("nq",  "nquads"  ), "application/n-quads", false);
    public static final @Nonnull RDFLang TTL     = new SimpleRDFLang("Turtle",           asList("ttl", "turtle"  ), "text/turtle", false);
    public static final @Nonnull RDFLang TRIG    = new SimpleRDFLang("TriG",      singletonList("trig"           ), "application/trig", false);
    public static final @Nonnull RDFLang RDFXML  = new SimpleRDFLang("RDF/XML",          asList("rdf", "xml"     ), "application/rdf+xml", false);
    public static final @Nonnull RDFLang RDFA    = new SimpleRDFLang("RDFa",            asList("html", "xhtml"   ), "application/xhtml+xml", false);
    public static final @Nonnull RDFLang OWL     = new SimpleRDFLang("OWL/XML",          asList("owl", "xml"     ), "application/owl+xml", false);
    public static final @Nonnull RDFLang TRIX    = new SimpleRDFLang("TRIX",      singletonList("trix"           ), "text/xml", false);
    public static final @Nonnull RDFLang JSONLD  = new SimpleRDFLang("JSON-LD",   singletonList("jsonld"         ), "application/ld+json", false);
    public static final @Nonnull RDFLang RDFJSON = new SimpleRDFLang("RDF/JSON",  singletonList("rj"             ), "application/rdf+json", false);
    public static final @Nonnull RDFLang THRIFT  = new SimpleRDFLang("Thrift",           asList("trdf", "rt"     ), "application/rdf+thrift", true);
    public static final @Nonnull RDFLang BRF     = new SimpleRDFLang("BinaryRDF", singletonList("brf"            ), "*/*", true);
    public static final @Nonnull RDFLang HDT     = new SimpleRDFLang("HDT",       singletonList("hdt"            ), " application/vnd.hdt ", true);
    public static final @Nonnull RDFLang UNKNOWN = new UnknownRDFLang();

    static {
        List<RDFLang> list = new ArrayList<>();
        list.add(NT);
        list.add(NQ);
        list.add(TTL);
        list.add(TRIG);
        list.add(RDFXML);
        list.add(RDFA);
        list.add(OWL);
        list.add(TRIX);
        list.add(JSONLD);
        list.add(RDFJSON);
        list.add(THRIFT);
        list.add(BRF);
        list.add(HDT);
        LANGS = BUILTIN_LANGS = Collections.unmodifiableList(list);

        CookiesLangDetector cd = new CookiesLangDetector();
        cd.addCookie(Cookie.builder("<?xml").strict()
                           .then("<rdf:").ignoreCase().save().build(), RDFXML);
        cd.addCookie(Cookie.builder("<?xml").strict()
                           .then("<Ontology").ignoreCase().save().build(), OWL);
        cd.addCookie(Cookie.builder("<rdf:").strict().ignoreCase().build(), RDFXML);
        cd.addCookie(Cookie.builder("<Ontology").strict().ignoreCase().build(), OWL);
        cd.addCookie(Cookie.builder("{").skipWhitespace().strict().build(), JSONLD);
        cd.addCookie(Cookie.builder("[").skipWhitespace().strict()
                           .then("{").skipWhitespace().strict().save()
                           .build(), JSONLD);
//        cd.addCookie(Cookie.builder(new byte[]{0x1c, 0x18}).includeBOM().strict().build(), THRIFT);
        cd.addCookie(Cookie.builder("$HDT").includeBOM().strict().build(), HDT);
        cd.addCookie(Cookie.builder("<TriX").ignoreCase().build(), TRIX);
        cd.addCookie(Cookie.builder("<?xml").strict()
                           .then("<TriX").ignoreCase().save().build(), TRIX);

        LANG_DETECTOR.addFallback(cd);
        LANG_DETECTOR.addFallback(new TurtleFamilyDetector());
    }

    public static boolean isKnown(@Nullable RDFLang lang) {
        return lang != null && !UNKNOWN.equals(lang);
    }

    public static boolean isTriGSubset(@Nullable RDFLang lang) {
        return lang != null && (lang.equals(NT) || lang.equals(TTL) || lang.equals(TRIG));
    }

    public static boolean isNQSubset(@Nullable RDFLang lang) {
        return lang != null && (lang.equals(NT) || lang.equals(NQ));
    }

    /**
     * Get all {@link RDFLang} instances registered
     * @return non-null and non-empty list of languages
     */
    public static @Nonnull List<RDFLang> getLangs() {
        return LANGS;
    }

    /**
     * Add a new {@link RDFLang}
     *
     * @param lang new {@link RDFLang} to be returned in {@link #getLangs()}
     * @param detector if non-null will be used by
     */
    public static void registerLang(@Nonnull RDFLang lang, @Nullable LangDetector detector) {
        unregisterLang(lang);
        ArrayList<RDFLang> list = new ArrayList<>(LANGS);
        list.add(lang);
        LANGS = Collections.unmodifiableList(list);
    }

    /**
     * Remove an extra {@link RDFLang} instance previously registered with
     * {@link #registerLang(RDFLang, LangDetector)}, together with its optional
     * {@link LangDetector}.
     *
     * @param lang the {@link RDFLang} to remove
     * @return true iff the {@link RDFLang} was removed
     */
    @SuppressWarnings("UnusedReturnValue")
    public static boolean unregisterLang(@Nonnull RDFLang lang) {
        if (LANGS.contains(lang)) {
            if (BUILTIN_LANGS.contains(lang))
                throw new IllegalArgumentException("Cannot remove built-in RDFLang from RDFLangs");
            ArrayList<RDFLang> list = new ArrayList<>(LANGS);
            list.remove(lang);
            LANGS = Collections.unmodifiableList(list);
            LangDetector langDetector = EXTRA_DETECTORS.getOrDefault(lang, null);
            if (langDetector != null)
                LANG_DETECTOR.remove(langDetector);
            return true;
        }
        return false;
    }

    public static @Nonnull LangDetector getLangDetector() {
        return LANG_DETECTOR;
    }

    public static @Nonnull RDFLang guess(@Nonnull InputStream is, int maxBytes) throws IOException {
        LangDetector.State state = getLangDetector().createState();
        for (int i = 0, value; i < maxBytes && (value = is.read()) != -1; ++i) {
            RDFLang lang = state.feedByte((byte) value);
            if (isKnown(lang))
                return lang;
        }
        RDFLang lang = state.end();
        return lang != null ? lang : UNKNOWN;
    }
}
