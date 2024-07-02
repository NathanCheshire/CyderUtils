package com.github.natche.cyderutils.network;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedMap;

import java.util.*;

/**
 * A categorizer for categorizing latency levels provided a set
 * number of categories and their corresponding max (exclusive) values.
 */
public final class LatencyCategorizer {
    /** The default unreachable string. */
    private static final String DEFAULT_UNREACHABLE_STRING = "Unreachable";

    /** The string to return when a latency level is outside the maximum level of this categorizer. */
    private final String unreachableString;

    /** The latency levels for this categorizer. */
    private final NavigableMap<Long, String> latencyLevels;

    /**
     * Constructs a LatencyCategorizer with the provided latency levels.
     *
     * @param latencyLevels a map where keys represent the maximum latency (exclusive)
     *                      and values represent the labels for those latencies
     * @throws NullPointerException if the latencyLevels map is null
     * @throws IllegalArgumentException if the latencyLevels map contains fewer than two entries,
     *                                  or if the latency labels are not unique
     */
    public LatencyCategorizer(Map<Long, String> latencyLevels) {
        this(latencyLevels, DEFAULT_UNREACHABLE_STRING);
    }

    /**
     * Constructs a LatencyCategorizer with the provided latency levels.
     *
     * @param latencyLevels a map where keys represent the maximum latency (exclusive)
     *                      and values represent the labels for those latencies
     * @param unreachableString the string to return if categorize is called on a value
     *                         that is outside the range of this categorizer
     * @throws NullPointerException if the latencyLevels map is null or unreachableString
     * @throws IllegalArgumentException if the latencyLevels map contains fewer than two entries,
     *                                  or if the latency labels are not unique, or if unreachableString
     *                                  is empty
     */
    public LatencyCategorizer(Map<Long, String> latencyLevels, String unreachableString) {
        Preconditions.checkNotNull(latencyLevels);
        Preconditions.checkNotNull(unreachableString);
        Preconditions.checkArgument(latencyLevels.size() > 1);
        Set<String> labelSet = new HashSet<>(latencyLevels.values());
        Preconditions.checkArgument(labelSet.size() == latencyLevels.size());
        Preconditions.checkArgument(!unreachableString.trim().isEmpty());

        this.latencyLevels = ImmutableSortedMap.copyOf(latencyLevels);
        this.unreachableString = unreachableString;
    }

    /**
     * Categorizes the given latency.
     *
     * @param latency rhe latency time in milliseconds
     * @return the label of the latency level, or "UNREACHABLE" if higher than any defined level
     * @throws IllegalArgumentException if the provided latency is less than zero
     */
    public String categorize(long latency) {
        Preconditions.checkArgument(latency >= 0);

        var entry = latencyLevels.higherEntry(latency);
        if (entry != null) return entry.getValue();
        return unreachableString;
    }

    /**
     * Returns the unreachable string for this latency categorizer.
     *
     * @return the unreachable string for this latency categorizer
     */
    public String getUnreachableString() {
        return unreachableString;
    }

    /**
     * Returns the maximum delay in milliseconds this categorizer can categorize.
     *
     * @return the maximum delay in milliseconds this categorizer can categorize
     */
    public long getMaxLatencyLevel() {
        return Collections.max(latencyLevels.keySet());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "LatencyCategorizer{"
                + "latencyLevels=" + latencyLevels
                + ", unreachableString=" + unreachableString
                + "}";
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int ret = latencyLevels.hashCode();
        ret = 31 * ret + unreachableString.hashCode();
        return ret;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof LatencyCategorizer)) {
            return false;
        }

        LatencyCategorizer other = (LatencyCategorizer) o;
        return other.latencyLevels.equals(latencyLevels)
                && other.unreachableString.equals(unreachableString);
    }
}
