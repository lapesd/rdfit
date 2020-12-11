package com.github.com.alexishuf.rdfit.parsers;

import com.github.com.alexishuf.rdfit.components.CallbackParser;
import com.github.com.alexishuf.rdfit.components.ItParser;
import com.github.com.alexishuf.rdfit.components.Parser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

public interface ParserRegistry {
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
     * Get a {@link ItParser} instance whose {@link Parser#canParse(Object)} accepts source.
     *
     * @param source the source to be parsed
     * @return a {@link ItParser} or null if no registered {@link ItParser} accepts the source.
     */
    @Nullable ItParser getItParser(@Nonnull Object source);

    /**
     * Get a {@link CallbackParser} instance whose {@link Parser#canParse(Object)} accepts source.
     *
     * @param source the source to be parsed
     * @return a {@link CallbackParser} or null if no registered {@link CallbackParser}
     *         accepts the source.
     */
    @Nullable CallbackParser getCallbackParser(@Nonnull Object source);
}
