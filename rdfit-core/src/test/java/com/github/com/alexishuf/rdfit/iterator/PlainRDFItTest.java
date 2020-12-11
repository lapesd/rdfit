package com.github.com.alexishuf.rdfit.iterator;

import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.*;

public class PlainRDFItTest extends RDFItTestBase {
    @Override
    protected @Nonnull <T> RDFIt<T> createIt(@Nonnull Class<T> valueClass, @Nonnull List<?> data) {
        //noinspection unchecked
        return new PlainRDFIt<T>(valueClass, (Iterator<? extends T>) data.iterator());
    }
}