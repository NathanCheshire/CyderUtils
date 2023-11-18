package cyder.structures;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.function.Function;

/**
 * A cache for a type.
 *
 * @param <T> the type of cache.
 */
public class Cache<T> {
    /**
     * The current cache value.
     */
    private T cachedValue;

    /**
     * The function to invoke to update the currently cached value if requested.
     */
    private Function<Void, T> cachedValueUpdater;

    /**
     * Constructs a new cache.
     */
    public Cache() {}

    /**
     * Constructs a new cache.
     *
     * @param initialValue the initial value cached
     * @throws NullPointerException if the provided initial value is null
     */
    public Cache(T initialValue) {
        cachedValue = Preconditions.checkNotNull(initialValue);
    }

    /**
     * Sets the cache value.
     *
     * @param newCache the new cache value
     * @throws NullPointerException if newCache is null
     */
    public void setCache(T newCache) {
        cachedValue = Preconditions.checkNotNull(newCache);
    }

    /**
     * Returns the current cache value.
     *
     * @return the current cache value
     * @throws IllegalStateException if the cache value is not present
     */
    public T getCache() {
        if (!isCachePresent() && cachedValueUpdater != null) refreshCachedValue();
        Preconditions.checkState(isCachePresent());
        return cachedValue;
    }

    /**
     * Clears the current cache value.
     */
    public void clear() {
        cachedValue = null;
    }

    /**
     * Returns whether the cache value is present.
     *
     * @return whether the cache value is present
     */
    public boolean isCachePresent() {
        return cachedValue != null;
    }

    /**
     * Caches the provided value if no cache is currently present.
     *
     * @return whether the new value was cached
     * @throws NullPointerException if the new value is null
     */
    @CanIgnoreReturnValue
    public boolean cacheIfNotPresent(T newValue) {
        Preconditions.checkNotNull(newValue);
        if (isCachePresent()) return false;
        cachedValue = newValue;
        return true;
    }

    /**
     * Sets the function to invoke to refresh the cache.
     *
     * @param function the function to invoke to refresh the cache
     * @throws NullPointerException if the provided function is null
     */
    public void setCachedValueUpdater(Function<Void, T> function) {
        cachedValueUpdater = Preconditions.checkNotNull(function);
    }

    /**
     * Refreshes the cached value by invoking the cached value updater function.
     *
     * @throws IllegalStateException if the cached value updater function has not been set
     */
    public void refreshCachedValue() {
        Preconditions.checkState(cachedValueUpdater != null);
        cachedValue = cachedValueUpdater.apply(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Cache<?>)) {
            return false;
        }

        Cache<?> other = (Cache<?>) o;
        return other.cachedValue == cachedValueUpdater
                && other.cachedValueUpdater == cachedValueUpdater;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Cache{"
                + "cachedValue=" + cachedValue
                + ", cachedValueUpdater=" + cachedValueUpdater
                + "}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = cachedValue.hashCode();
        ret = 31 * ret + cachedValueUpdater.hashCode();
        return ret;
    }
}
