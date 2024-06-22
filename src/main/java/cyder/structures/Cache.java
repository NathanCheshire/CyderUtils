package cyder.structures;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.function.Supplier;

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
     * Whether this cache allows null values.
     */
    private final boolean allowNull;

    /**
     * The function to invoke to update the currently cached value if requested.
     */
    private Supplier<T> cachedValueUpdater;

    /**
     * Constructs a new cache with an initial value of null.
     */
    public Cache() {
        this(null, true);
    }

    /**
     * Constructs a new cache.
     *
     * @param initialValue the initial value cached
     * @param allowNull    whether null is allowed as a cached value
     * @throws NullPointerException if the provided initial value is null when allowNull is false
     */
    public Cache(T initialValue, boolean allowNull) {
        if (!allowNull) Preconditions.checkNotNull(initialValue);
        this.cachedValue = initialValue;
        this.allowNull = allowNull;
    }

    /**
     * Sets the cache value.
     *
     * @param newCache the new cache value
     * @throws NullPointerException if newCache is null and null is not allowed
     */
    public void setCache(T newCache) {
        if (!allowNull) Preconditions.checkNotNull(newCache);
        cachedValue = newCache;
    }

    /**
     * Returns the current cached value.
     *
     * @return the current cache value
     */
    public T getCache() {
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
     * @throws NullPointerException if the new value is null and null is not allowed
     */
    @CanIgnoreReturnValue
    public boolean cacheIfNotPresent(T newValue) {
        if (!allowNull) Preconditions.checkNotNull(newValue);
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
    public void setCachedValueUpdater(Supplier<T> function) {
        cachedValueUpdater = Preconditions.checkNotNull(function);
    }

    /**
     * Refreshes the cached value by invoking the cached value updater function.
     *
     * @throws IllegalStateException if the cached value updater function has not been set
     */
    public void refreshCachedValue() {
        Preconditions.checkState(cachedValueUpdater != null);
        cachedValue = cachedValueUpdater.get();
    }

    /**
     * Returns whether this cache allows null.
     *
     * @return whether this cache allows null
     */
    public boolean isNullAllowed() {
        return allowNull;
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
        return other.cachedValue.equals(cachedValueUpdater)
                && other.allowNull == allowNull
                && other.cachedValueUpdater.equals(cachedValueUpdater);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Cache{"
                + "cachedValue=" + cachedValue
                + "allowNull=" + allowNull
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
        ret = 31 * ret + Boolean.hashCode(allowNull);
        return ret;
    }
}
