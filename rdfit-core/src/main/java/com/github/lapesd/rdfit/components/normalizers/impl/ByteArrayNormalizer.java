package com.github.lapesd.rdfit.components.normalizers.impl;

import com.github.lapesd.rdfit.components.annotations.Accepts;
import com.github.lapesd.rdfit.components.normalizers.BaseSourceNormalizer;
import com.github.lapesd.rdfit.source.RDFInputStream;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;

@Accepts(byte[].class)
public class ByteArrayNormalizer extends BaseSourceNormalizer {
    @Override public @Nonnull Object normalize(@Nonnull Object source) {
        if (!(source instanceof byte[]))
            return false;
        ByteArrayInputStream is = new ByteArrayInputStream((byte[]) source);
        return new RDFInputStream(is);
    }
}
