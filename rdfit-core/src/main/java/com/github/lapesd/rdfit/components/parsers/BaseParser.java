package com.github.lapesd.rdfit.components.parsers;

import com.github.lapesd.rdfit.components.Parser;
import com.github.lapesd.rdfit.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public abstract class BaseParser implements Parser {
    private static final Logger logger = LoggerFactory.getLogger(BaseParser.class);
    protected final @Nonnull Collection<Class<?>> acceptedClasses;
    protected @Nullable ParserRegistry parserRegistry;

    public BaseParser(@Nonnull Collection<Class<?>> acceptedClasses) {
        this.acceptedClasses = acceptedClasses;
    }

    @Override public @Nonnull Collection<Class<?>> acceptedClasses() {
        return acceptedClasses;
    }

    @Override public void attachTo(@Nonnull ParserRegistry registry) {
        if (this.parserRegistry != null && !this.parserRegistry.equals(registry)) {
            logger.info("Swapping old parserRegistry from {} to {} in {}",
                        this.parserRegistry, registry, this);
        }
        this.parserRegistry = registry;
    }

    @Override public boolean canParse(@Nonnull Object source) {
        for (Class<?> c : acceptedClasses()) {
            if (c.isInstance(source)) return true;
        }
        return false;
    }

    @Override public @Nonnull String toString() {
        return Utils.toString(this);
    }
}
