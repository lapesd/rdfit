package com.github.lapesd.rdfit.iterator;

import com.github.lapesd.rdfit.SourceQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BaseImportingRDFIt<T> implements RDFIt<T> {
    private static final Logger logger = LoggerFactory.getLogger(BaseImportingRDFIt.class);
    private final @Nonnull RDFIt<T> delegate;

    public BaseImportingRDFIt(@Nonnull RDFIt<T> delegate) {
        this.delegate = delegate;
    }

    @Override public @Nonnull Class<? extends T> valueClass() {
        return delegate.valueClass();
    }

    @Override public @Nonnull IterationElement itElement() {
        return delegate.itElement();
    }

    @Override public @Nonnull SourceQueue getSourceQueue() {
        return delegate.getSourceQueue();
    }

    @Override public @Nonnull Object getSource() {
        return delegate.getSource();
    }

    @Override public void close() {
        delegate.close();
    }

    @Override public boolean hasNext() {
        return delegate.hasNext();
    }

    @Override public T next() {
        T next = delegate.next();
        String iri = getImportIRI(next);
        if (iri != null) {
            SourceQueue queue = getSourceQueue();
            if (queue.isClosed())
                logger.error("Cannot process import of {}: {} is closed", iri, queue);
            else
                queue.add(SourceQueue.When.Soon, iri);
        }
        return next;
    }

    protected abstract @Nullable String getImportIRI(@Nonnull Object tripleOrQuad);
}
