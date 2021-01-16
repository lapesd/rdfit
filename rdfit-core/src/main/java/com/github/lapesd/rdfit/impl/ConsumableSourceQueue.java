package com.github.lapesd.rdfit.impl;

import com.github.lapesd.rdfit.SourceQueue;

import javax.annotation.Nonnull;
import java.util.Iterator;

public interface ConsumableSourceQueue extends SourceQueue, Iterator<Object> {
    @Override @Nonnull Object next();
}
