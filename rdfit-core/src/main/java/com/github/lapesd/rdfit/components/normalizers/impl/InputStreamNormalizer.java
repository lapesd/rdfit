package com.github.lapesd.rdfit.components.normalizers.impl;

import com.github.lapesd.rdfit.components.annotations.Accepts;
import com.github.lapesd.rdfit.components.normalizers.BaseSourceNormalizer;
import com.github.lapesd.rdfit.source.RDFInputStream;

import javax.annotation.Nonnull;
import java.io.InputStream;

@Accepts(InputStream.class)
public class InputStreamNormalizer extends BaseSourceNormalizer {
    @Override public @Nonnull Object normalize(@Nonnull Object source) {
        if (source instanceof InputStream)
            return new RDFInputStream((InputStream) source);
        return source;
    }
}
