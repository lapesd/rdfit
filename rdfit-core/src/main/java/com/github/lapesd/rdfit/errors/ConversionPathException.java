package com.github.lapesd.rdfit.errors;

import com.github.lapesd.rdfit.components.converters.ConversionPath;

import javax.annotation.Nonnull;

public class ConversionPathException extends ConversionException {
    public ConversionPathException(@Nonnull ConversionPath path, @Nonnull ConversionException e) {
        super(e.getInput(), e.getConverter(), e.getTargetClass(),
              e.getReason() + " ConversionPath="+path, e);
    }
}
