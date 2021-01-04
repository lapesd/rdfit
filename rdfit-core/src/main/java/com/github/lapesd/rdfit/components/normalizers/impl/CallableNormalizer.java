package com.github.lapesd.rdfit.components.normalizers.impl;

import com.github.lapesd.rdfit.components.annotations.Accepts;
import com.github.lapesd.rdfit.components.normalizers.BaseSourceNormalizer;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

@Accepts(Callable.class)
public class CallableNormalizer extends BaseSourceNormalizer {
    @Override public @Nonnull Object normalize(@Nonnull Object source) {
        if (!(source instanceof Callable))
            return source;
        try {
            return ((Callable<?>) source).call();
        } catch (InterruptParsingException | RDFItException e) {
            throw e;
        } catch (Throwable e) {
            throw new RDFItException(source, "Callable.call() failed:", e);
        }
    }
}
