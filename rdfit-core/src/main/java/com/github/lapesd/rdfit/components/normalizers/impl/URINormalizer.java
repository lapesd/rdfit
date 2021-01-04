package com.github.lapesd.rdfit.components.normalizers.impl;

import com.github.lapesd.rdfit.components.annotations.Accepts;
import com.github.lapesd.rdfit.components.normalizers.BaseSourceNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;

@Accepts(URI.class)
public class URINormalizer extends BaseSourceNormalizer {
    private static final Logger logger = LoggerFactory.getLogger(URINormalizer.class);

    @Override public @Nonnull Object normalize(@Nonnull Object source) {
        if (!(source instanceof URI))
            return source;
        URI uri = (URI) source;
        try {
            if (uri.isAbsolute())
                return uri.toURL();
        } catch (MalformedURLException e) {
            logger.warn("URI {} cannot be converted to an URL", source, e);
        } catch (IllegalArgumentException e) {
            logger.warn("Ignoring unexpected exception from URI.toURL() for {}", source, e);
        }

        if (!uri.isAbsolute()) {
            File file = new File(uri.getPath());
            if (file.exists())
                return file;
            file = new File(uri.getRawPath());
            if (file.exists())
                return file;
            logger.warn("Relative URI {} does not denote an existing local file", uri);
        }
        File file = new File(source.toString());
        return file.exists() ? file : source;
    }
}
