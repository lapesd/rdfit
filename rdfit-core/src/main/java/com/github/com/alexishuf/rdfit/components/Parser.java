package com.github.com.alexishuf.rdfit.components;

import com.github.com.alexishuf.rdfit.parsers.ParserRegistry;

import javax.annotation.Nonnull;
import java.util.Collection;

public interface Parser extends Component {
    /**
     * Notify that the parser has been registered at the given registry.
     */
    void attachTo(@Nonnull ParserRegistry registry);

    /**
     * Set of classes accepted by the parser. {@link #canParse(Object)} may reject specific
     * instances of these classes.
     */
    @Nonnull Collection<Class<?>> acceptedClasses();

    /**
     * Indicates whether this {@link Parser} implementation can parse the given source.
     *
     * This method may perform I/O as part of such test. However, long running IO operations
     * and scanning whole files should be avoided.
     *
     * @param source the source to test
     * @return <code>true</code> if a subsequent call to a <code>parse</code> method will work
     *         (assuming the input is fully valid).
     */
    boolean canParse(@Nonnull Object source);
}
