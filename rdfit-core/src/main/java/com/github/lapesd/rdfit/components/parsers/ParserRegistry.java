package com.github.lapesd.rdfit.components.parsers;

import com.github.lapesd.rdfit.components.ItParser;
import com.github.lapesd.rdfit.components.ListenerParser;
import com.github.lapesd.rdfit.components.Parser;
import com.github.lapesd.rdfit.components.converters.ConversionManager;
import com.github.lapesd.rdfit.iterator.IterationElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

public interface ParserRegistry {
    @Nonnull ConversionManager getConversionManager();

    void setConversionManager(@Nonnull ConversionManager mgr);

    /**
     * Register a parser.
     */
    void register(@Nonnull Parser parser);

    /**
     * Removes a specific {@link Parser} instance previously registered.
     */
    void unregister(@Nonnull Parser parser);

    /**
     * Remove all {@link Parser} instances that satisfy the given predicate
     */
    void unregisterIf(@Nonnull Predicate<? super Parser> predicate);

    default void unregisterAll(@Nonnull Class<? extends Parser> aClass) {
        unregisterIf(aClass::isInstance);
    }

    /**
     * Get the first {@link ItParser} instance whose {@link Parser#canParse(Object)} accepts
     * source and whose {@link ItParser#iterationElement()} matches
     *
     * @param source the source to be parsed
     * @return a {@link ItParser} or null if no registered {@link ItParser} accepts the source.
     */
    @Nullable ItParser getItParser(@Nonnull Object source, @Nullable IterationElement itElem);

    /**
     * Same as {@link #getItParser(Object, IterationElement)} with null {@link IterationElement};
     */
    default @Nullable ItParser getItParser(@Nonnull Object source) {
        return getItParser(source, null);
    }

    /**
     * Get a {@link ListenerParser} instance whose {@link Parser#canParse(Object)} accepts source.
     *
     * @param source the source to be parsed
     * @return a {@link ListenerParser} or null if no registered {@link ListenerParser}
     *         accepts the source.
     */
    @Nullable ListenerParser getCallbackParser(@Nonnull Object source);
}
