package com.github.lapesd.rdfit.source;

import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import static java.lang.String.format;

public class RDFInputStreamSupplier extends RDFInputStream {
    private static final Logger logger = LoggerFactory.getLogger(RDFInputStreamSupplier.class);

    private final @Nonnull Callable<InputStream> supplier;
    private @Nullable Callable<?> closeCallable;
    private @Nullable Runnable closeRunnable;
    private @Nullable String name;

    public RDFInputStreamSupplier(@Nonnull Supplier<InputStream> supplier) {
        this((Callable<InputStream>) supplier::get);
    }

    public RDFInputStreamSupplier(@Nonnull Callable<InputStream> supplier) {
        super(null, null, null, true);
        this.supplier = supplier;
    }

    public @Nonnull RDFInputStreamSupplier setName(@Nonnull String name) {
        this.name = name;
        return this;
    }

    public @Nonnull RDFInputStreamSupplier onClose(@Nonnull Callable<?> callable) {
        this.closeCallable = callable;
        this.closeRunnable = null;
        return this;
    }

    public @Nonnull RDFInputStreamSupplier onCLose(@Nonnull Runnable runnable) {
        this.closeRunnable = runnable;
        this.closeCallable = null;
        return this;
    }

    @Override public @Nonnull InputStream getInputStream() {
        if (inputStream == null) {
            try {
                inputStream = supplier.call();
            } catch (Exception e) {
                throw new RDFItException(this, this+" InputStream supplier failed", e);
            }
            if (inputStream == null)
                throw new RDFItException(this, this+" InputStream supplier returned null");
        }
        return inputStream;
    }

    @Override public @Nonnull String toString() {
        if (name != null)
            return format("%s{syntax=%s,name=%s}", Utils.toString(this), lang, name);
        return format("%s{syntax=%s,supplier=%s}", Utils.toString(this), lang, supplier);
    }

    @Override public void close() {
        try {
            super.close();
        } finally {
            if (closeCallable != null) {
                try {
                    closeCallable.call();
                } catch (Exception e) {
                    logger.error("{}.close(): closeCallable {} failed", this, closeCallable, e);
                }
            } else if (closeRunnable != null) {
                try {
                    closeRunnable.run();
                } catch (RuntimeException e) {
                    logger.error("{}.close(): closeRunnable {} failed", this, closeRunnable, e);
                }
            }

        }
    }
}
