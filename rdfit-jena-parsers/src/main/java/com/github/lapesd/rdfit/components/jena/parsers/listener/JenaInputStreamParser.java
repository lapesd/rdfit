package com.github.lapesd.rdfit.components.jena.parsers.listener;

import com.github.lapesd.rdfit.components.jena.JenaHelpers;
import com.github.lapesd.rdfit.components.jena.listener.InterruptJenaParsingException;
import com.github.lapesd.rdfit.components.jena.listener.ListenerStreamRDF;
import com.github.lapesd.rdfit.components.parsers.BaseListenerParser;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.listener.RDFListener;
import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.source.syntax.RDFLangs;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Quad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class JenaInputStreamParser extends BaseListenerParser {
    public static final Logger logger = LoggerFactory.getLogger(JenaInputStreamParser.class);
    public static final @Nonnull Set<RDFLang> PARSED_LANGS;

    static {
        Set<RDFLang> set = new HashSet<>();
        for (RDFLang lang : RDFLangs.getLangs()) {
            if (JenaHelpers.toJenaLang(lang) != null)
                set.add(lang);
        }
        PARSED_LANGS = Collections.unmodifiableSet(set);
    }

    public JenaInputStreamParser() {
        super(Collections.singleton(RDFInputStream.class), Triple.class, Quad.class);
    }

    @Override public @Nonnull Set<RDFLang> parsedLangs() {
        return PARSED_LANGS;
    }

    @Override public boolean canParse(@Nonnull Object source) {
        if (!super.canParse(source))
            return false;
        try {
            RDFLang lang = ((RDFInputStream) source).getOrDetectLang();
            return JenaHelpers.toJenaLang(lang) != null;
        } catch (IOException e) {
            logger.info("IOException when guessing lang of source {}. Will assume parsing", source);
            return true;
        }
    }

    @Override
    public void parse(@Nonnull Object source,
                      @Nonnull RDFListener<?, ?> listener) throws InterruptParsingException {
        boolean ok = false;
        try (RDFInputStream ris = (RDFInputStream) source) {
            RDFLang lang = ris.getOrDetectLang();
            Lang jLang = JenaHelpers.toJenaLang(lang);
            ListenerStreamRDF adaptor = new ListenerStreamRDF(listener, source);
            if (ris.hasBaseIri())
                RDFDataMgr.parse(adaptor, ris.getInputStream(), ris.getBaseIRI(), jLang);
            else
                RDFDataMgr.parse(adaptor, ris.getInputStream(), jLang);
            ok = true;
        } catch (IOException e) {
            RDFItException ex = new RDFItException(source, "Could not guess syntax of input " +
                                                           "due to IOException", e);
            if (!listener.notifySourceError(ex))
                throw new InterruptParsingException();
        } catch (InterruptJenaParsingException ignored) {
        } catch (InterruptParsingException|RDFItException e) {
            throw e;
        } catch (Throwable e) {
            throw new RDFItException(source, e);
        } finally {
            if (!ok)
                listener.finish(source);
        }
    }
}
