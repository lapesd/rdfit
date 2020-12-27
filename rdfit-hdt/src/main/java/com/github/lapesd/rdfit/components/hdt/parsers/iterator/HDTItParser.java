package com.github.lapesd.rdfit.components.hdt.parsers.iterator;

import com.github.lapesd.rdfit.components.parsers.BaseItParser;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.iterator.IterationElement;
import com.github.lapesd.rdfit.iterator.PlainRDFIt;
import com.github.lapesd.rdfit.iterator.RDFIt;
import com.github.lapesd.rdfit.source.RDFFile;
import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.source.syntax.RDFLangs;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TripleString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

public class HDTItParser extends BaseItParser {
    private static final Logger logger = LoggerFactory.getLogger(HDTItParser.class);
    private static final @Nonnull Set<Class<?>> CLASSES = unmodifiableSet(new HashSet<>(asList(
            String.class, File.class, Path.class, URI.class, URL.class,
            RDFInputStream.class, HDT.class
    )));

    public HDTItParser() {
        super(CLASSES, TripleString.class, IterationElement.TRIPLE);
    }

    private @Nullable Object sanitizeInput(@Nonnull Object source) {
        if (source instanceof String) {
            String str = (String) source;
            if (str.startsWith("file:")) {
                try {
                    return Paths.get(new URI(str)).toFile();
                } catch (URISyntaxException e) {
                    File file = new File(str.replaceFirst("^file:(//)?", ""));
                    if (file.exists() && file.isFile() && file.canRead())
                        return file;
                    logger.info("Rejecting bad \"file:\" String {}: not a readable file", source);
                    return null;
                }
            } else {
                File file = new File(str);
                if (file.isFile() && file.exists() && file.canRead())
                    return file;
                logger.info("Rejecting String {}: not name a readable ", source);
                return null;
            }
        } else if (source instanceof URI) {
            URI uri = (URI) source;
            if (!uri.getScheme().startsWith("file")) return null;
            return Paths.get(uri).toFile();
        } else if (source instanceof URL) {
            URL url = (URL) source;
            if (!url.getProtocol().startsWith("file")) return null;
            try {
                return Paths.get(url.toURI()).toFile();
            } catch (URISyntaxException e) {
                return new File(url.toString().replaceFirst("^file:(//)?", ""));
            }
        } else if (source instanceof RDFInputStream) {
            RDFInputStream file = (RDFInputStream) source;
            try {
                return RDFLangs.HDT.equals(file.getOrDetectLang()) ? source : null;
            } catch (IOException e) {
                logger.info("IOException guessing syntax of {}. Accepting as a source", source);
                return source;
            }
        } else if (source instanceof Path) {
            return ((Path) source).toFile();
        } else if (source instanceof File || source instanceof HDT) {
            return source;
        }
        return null;
    }

    @Override public boolean canParse(@Nonnull Object source) {
        if (!super.canParse(source)) return false;

        source = sanitizeInput(source);
        if (source instanceof File) {
            File file = (File) source;
            if (file.isDirectory())
                return false; // we do not handle dirs, maybe someone else does
            try (RDFFile rdfFile = new RDFFile(file)) {
                return RDFLangs.HDT.equals(rdfFile.getOrDetectLang());
            } catch (IOException e) {
                logger.info("IOException guessing lang of {}. Accepting as a source", source);
                return true;
            }
        }
        return source != null;
    }

    @Override public @Nonnull <T> RDFIt<T> parse(@Nonnull Object source) {
        Object input = sanitizeInput(source);

        HDT hdt;
        if (input instanceof RDFInputStream || input instanceof File) {
            if (input instanceof File)
                input = new RDFFile((File) input);
            try (RDFInputStream ris = (RDFInputStream) input) {
                hdt = HDTManager.loadHDT(ris.getBufferedInputStream());
            } catch (Throwable t) {
                throw new RDFItException(source, "Problem reading input " + input, t);
            }
        } else if (input instanceof HDT) {
            hdt = (HDT) input;
        } else {
            throw new IllegalArgumentException("parse() called with unsupported source type");
        }

        IteratorTripleString it;
        try {
            it = hdt.search(null, null, null);
        } catch (NotFoundException e) {
            throw new RDFItException(source, "Unexpected exception  search()ing "+source, e);
        }
        return new PlainRDFIt<>(TripleString.class, IterationElement.TRIPLE, it, source);
    }
}
