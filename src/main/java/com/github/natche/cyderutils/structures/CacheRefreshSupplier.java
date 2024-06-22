package com.github.natche.cyderutils.structures;

/**
 * A supplier for refreshing a {@link Cache}.
 *
 * @param <T> the type returned by this refresh supplier
 */
public interface CacheRefreshSupplier<T> {
    /**
     * Returns the cache value.
     *
     * @return the cache value
     */
    T get();

    /**
     * Returns a {@link String} representation of this refresh supplier.
     *
     * @return a {@link String} representation of this refresh supplier
     */
    @Override
    String toString();
}
