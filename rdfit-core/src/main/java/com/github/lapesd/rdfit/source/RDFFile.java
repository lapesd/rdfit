package com.github.lapesd.rdfit.source;

import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import com.github.lapesd.rdfit.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class RDFFile extends RDFInputStream {
    private static final Logger logger = LoggerFactory.getLogger(RDFFile.class);

    private final @Nonnull File file;
    private boolean deleteOnClose;

    public RDFFile(@Nonnull File file) {
        this(file, false);
    }

    public RDFFile(@Nonnull File file, @Nullable RDFLang lang) {
        this(file, lang, false);
    }

    public RDFFile(@Nonnull File file, boolean deleteOnClose) {
        this(file, null, deleteOnClose);
    }
    public RDFFile(@Nonnull File file, @Nullable RDFLang lang, boolean deleteOnClose) {
        super(null, lang,
              file.toURI().toString().replaceFirst("^file:", "file://"), true);
        this.file = file;
        this.deleteOnClose = deleteOnClose;
    }

    public @Nonnull RDFFile setDeleteOnClose() {
        return setDeleteOnClose(true);
    }

    public @Nonnull RDFFile setDeleteOnClose(boolean value) {
        deleteOnClose = value;
        return this;
    }

    @Override public @Nonnull InputStream getInputStream() {
        if (inputStream == null) {
            try {
                inputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new RDFItException(file, e);
            }
        }
        return inputStream;
    }

    @Override public @Nonnull String toString() {
        return String.format("%s{syntax=%s,file=%s}", Utils.toString(this), lang, file);
    }

    @Override public void close() {
        try {
            super.close();
        } finally {
            if (deleteOnClose) {
                if (!file.delete())
                    logger.error("{}.close(): failed to delete {}", this, file.getAbsolutePath());
            }
        }
    }
}
