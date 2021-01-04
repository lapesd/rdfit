package com.github.lapesd.rdfit.components.rdf4j.parsers.listener;

import com.github.lapesd.rdfit.components.parsers.BaseListenerParser;
import com.github.lapesd.rdfit.components.rdf4j.listener.RDFListenerHandler;
import com.github.lapesd.rdfit.components.rdf4j.parsers.RDF4JFormat;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.listener.RDFListener;
import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.source.syntax.RDFLangs;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Arrays.asList;

public class RDF4JInputStreamParser extends BaseListenerParser {
    private static final Logger logger = LoggerFactory.getLogger(RDF4JInputStreamParser.class);
    public static final Set<RioSetting<?>> DEFAULT_NON_FATAL_ERRORS = new HashSet<>(asList(
            BasicParserSettings.VERIFY_RELATIVE_URIS,
            BasicParserSettings.VERIFY_DATATYPE_VALUES,
            BasicParserSettings.VERIFY_LANGUAGE_TAGS,
            BasicParserSettings.VERIFY_URI_SYNTAX
    ));
    private static final @Nonnull Set<RDFLang> PARSED_LANGS;
    static {
        Set<RDFLang> set = new HashSet<>();
        for (RDFLang lang : RDFLangs.getLangs()) {
            if (RDF4JFormat.toRDF4J(lang) != null)
                set.add(lang);
        }
        PARSED_LANGS = Collections.unmodifiableSet(set);
    }

    private final @Nonnull Set<RioSetting<?>> nonFatalErrors;

    public RDF4JInputStreamParser() {
        this(DEFAULT_NON_FATAL_ERRORS);
    }
    public RDF4JInputStreamParser(@Nonnull Collection<RioSetting<?>> nonFatalErrors) {
        super(Collections.singleton(RDFInputStream.class), Statement.class, Statement.class);
        this.nonFatalErrors = nonFatalErrors instanceof Set
                ? (Set<RioSetting<?>>)nonFatalErrors
                : new HashSet<>(nonFatalErrors);
    }

    @Override public @Nonnull Set<RDFLang> parsedLangs() {
        return PARSED_LANGS;
    }

    @Override public boolean canParse(@Nonnull Object source) {
        if (!super.canParse(source)) return false;
        try {
            RDFLang lang = ((RDFInputStream) source).getOrDetectLang();
            RDFFormat format = RDF4JFormat.toRDF4J(lang);
            if (format == null) return false;
            try {
                Rio.createParser(format);
                return true;
            } catch (UnsupportedRDFormatException e) {
                return false;
            }
        } catch (IOException e) {
            logger.info("IOException guessing lang of source {}. Will assume parsing", source);
            return true;
        }
    }

    private static class EndParseException extends RuntimeException {
    }

    @Override
    public void parse(@Nonnull Object source,
                      @Nonnull RDFListener<?, ?> listener) throws InterruptParsingException {
        try (RDFListenerHandler handler = new RDFListenerHandler(listener, source)) {
            RDFInputStream ris = (RDFInputStream) source;
            RDFFormat fmt = RDF4JFormat.toRDF4J(ris.getOrDetectLang());
            RDFParser parser = Rio.createParser(fmt);
            for (RioSetting<?> setting : nonFatalErrors)
                parser.getParserConfig().addNonFatalError(setting);
            parser.setRDFHandler(handler);
            parser.setParseErrorListener(new ParseErrorListener() {
                @Override public void warning(String msg, long l, long c) {
                    if (!listener.notifyParseWarning(format("line %d, col %d: %s.", l, c, msg)))
                        throw new EndParseException();
                }

                @Override public void error(String msg, long l, long c) {
                    if (!listener.notifyParseError(format("line %d, col %d: %s.", l, c, msg)))
                        throw new EndParseException();
                }

                @Override public void fatalError(String msg, long l, long c) {
                    msg = format("Fatal error at %d:%d in source %s: %s", l, c, source, msg);
                    if (!listener.notifySourceError(new RDFItException(source, msg)))
                        throw new InterruptParsingException();
                }
            });
            if (ris.hasBaseIri())
                parser.parse(ris.getInputStream(), ris.getBaseIRI());
            else
                parser.parse(ris.getInputStream());
        } catch (EndParseException ignored) {
        } catch (InterruptParsingException e) {
            throw e;
        } catch (Throwable t) {
            RDFItException ritException;
            if (t.getCause() instanceof EndParseException)
                return;
            if (t.getCause() instanceof InterruptParsingException)
                throw (RuntimeException) t.getCause();
            else if (t.getCause() instanceof RDFItException)
                ritException = (RDFItException) t.getCause();
            else if (t instanceof RDFItException)
                ritException = (RDFItException)t;
            else
                ritException = new RDFItException(source, "Failed to parse "+source, t);
            if (!listener.notifySourceError(ritException))
                throw new InterruptParsingException();
        }
    }
}
