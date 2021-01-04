package com.github.lapesd.rdfit.components;

import com.github.lapesd.rdfit.components.parsers.ParserRegistry;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;

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
     * Collection of RDF languages this parser actively provides support for.
     *
     * Model-style parsers (e.g. for query results or in-memory graphs) should return an
     * empty collection.
     */
    @Nonnull Set<RDFLang> parsedLangs();

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
