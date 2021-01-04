package com.github.lapesd.rdfit.components.normalizers.impl;

import com.github.lapesd.rdfit.components.annotations.Accepts;
import com.github.lapesd.rdfit.components.normalizers.BaseSourceNormalizer;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

@Accepts(Supplier.class)
public class SupplierNormalizer extends BaseSourceNormalizer {
    @Override public @Nonnull Object normalize(@Nonnull Object source) {
        if (!(source instanceof Supplier))
            return source;
        try {
            return ((Supplier<?>) source).get();
        } catch (InterruptParsingException| RDFItException e) {
            throw e;
        } catch (Throwable t) {
            throw new RDFItException(source, "Supplier.get() failed", t);
        }
    }
}
