/*
 *    Copyright 2021 Alexis Armin Huf
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.lapesd.rdfit.components.parsers;

import com.github.lapesd.rdfit.components.Parser;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import com.github.lapesd.rdfit.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Default implementations for some methods in {@link Parser}
 */
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

    @Override public @Nonnull Set<RDFLang> parsedLangs() {
        return Collections.emptySet();
    }

    @Override public void attachTo(@Nonnull ParserRegistry registry) {
        if (this.parserRegistry != null && !this.parserRegistry.equals(registry)) {
            logger.debug("Swapping old parserRegistry from {} to {} in {}",
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
