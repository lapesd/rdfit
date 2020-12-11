package com.github.lapesd.rdfit.components.jena.parsers.listener;

import com.github.lapesd.rdfit.components.jena.JenaHelpers;
import com.github.lapesd.rdfit.components.jena.listener.InterruptJenaParsingException;
import com.github.lapesd.rdfit.components.jena.listener.StreamRDFListener;
import com.github.lapesd.rdfit.components.parsers.BaseListenerParser;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.listener.RDFListener;
import com.github.lapesd.rdfit.source.RDFInputStream;
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

public class JenaRDFInputStreamParser extends BaseListenerParser {
    public static final Logger logger = LoggerFactory.getLogger(JenaRDFInputStreamParser.class);

    public JenaRDFInputStreamParser() {
        super(Collections.singleton(RDFInputStream.class), Triple.class, Quad.class);
    }

    @Override public boolean canParse(@Nonnull Object source) {
        if (!super.canParse(source)) return false;
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
            StreamRDFListener adaptor = new StreamRDFListener(listener, source);
            if (ris.hasBaseIri())
                RDFDataMgr.parse(adaptor, ris.getInputStream(), ris.getBaseIRI(), jLang);
            else
                RDFDataMgr.parse(adaptor, ris.getInputStream(), jLang);
            ok = true;
        } catch (IOException e) {
            RDFItException ex = new RDFItException(source, "Could not guess syntax of input " +
                                                           "due to IOException", e);
            if (!listener.notifySourceError(source, ex))
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
