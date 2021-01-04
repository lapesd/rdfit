package com.github.lapesd.rdfit.components.normalizers.impl;

import com.github.lapesd.rdfit.components.annotations.Accepts;
import com.github.lapesd.rdfit.components.normalizers.BaseSourceNormalizer;
import com.github.lapesd.rdfit.source.RDFFile;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Path;

@Accepts({File.class, Path.class})
public class FileNormalizer extends BaseSourceNormalizer {
    @Override public @Nonnull Object normalize(@Nonnull Object source) {
        if (source instanceof Path)
            source = ((Path)source).toFile();
        if (source instanceof File)
            return new RDFFile((File) source);
        return source;
    }
}
