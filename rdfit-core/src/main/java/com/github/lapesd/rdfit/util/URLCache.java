package com.github.lapesd.rdfit.util;

import com.github.lapesd.rdfit.source.RDFInputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URL;
import java.util.function.Supplier;

/**
 * A Cache for {@link RDFInputStream}s of URLs.
 */
public interface URLCache {
    /**
     * Stores a {@link RDFInputStream} for the given URL.
     *
     * @param url URL to cache (fragments are ignored)
     * @param supplier A Supplier that returns a {@link RDFInputStream} with that URL content,
     *                 ideally without causing I/O operations.
     * @return the previously {@link Supplier} set to this url or null if this is the first
     *         put operation on the url.
     */
    @Nullable Supplier<RDFInputStream> put(@Nonnull URL url, @Nonnull Supplier<RDFInputStream> supplier);

    /**
     * Stores a {@link RDFInputStream} for the given URL
     * <strong>only if no other supplier was previously registered for the same URL</strong>.
     *
     * @param url URL to cache (fragments are ignored)
     * @param supplier A Supplier that returns a {@link RDFInputStream} with that URL content,
     *                 ideally without causing I/O operations.
     * @return Whether the supplier was stored (true) or if there was a previous
     * supplier for the url (false).
     */
    boolean putIfAbsent(@Nonnull URL url, @Nonnull Supplier<RDFInputStream> supplier);

    /**
     * Gets cached RDF data for the provided URL.
     *
     * @param url the url to query (fragments are ignored)
     * @return A {@link Supplier} that provides an {@link RDFInputStream} with the RDF
     *         data for that URL. Ownership of the {@link RDFInputStream} is given to the caller,
     *         which should close the object after consuming it.
     */
    @Nullable Supplier<RDFInputStream> get(@Nonnull URL url);
}
