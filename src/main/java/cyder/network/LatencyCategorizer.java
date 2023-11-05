package cyder.network;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedMap;

import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

public final class LatencyCategorizer {
    // todo should this be customizable?
    private static final String UNREACHABLE = "Unreachable";
    private final NavigableMap<Integer, String> latencyLevels;

    /**
     * Constructs a LatencyCategorizer with the provided latency levels.
     *
     * @param latencyLevels a map where keys represent the maximum latency (exclusive)
     *                      and values represent the labels for those latencies
     * @throws NullPointerException if the latencyLevels map is null
     * @throws IllegalArgumentException if the latencyLevels map contains fewer than two entries,
     *                                  or if the latency labels are not unique
     */
    public LatencyCategorizer(Map<Integer, String> latencyLevels) {
        Preconditions.checkNotNull(latencyLevels);
        Preconditions.checkArgument(latencyLevels.size() > 1);
        Set<String> labelSet = new HashSet<>(latencyLevels.values());
        Preconditions.checkArgument(labelSet.size() == latencyLevels.size());

        this.latencyLevels = ImmutableSortedMap.copyOf(latencyLevels);
    }

    /**
     * Categorizes the given latency.
     *
     * @param latency rhe latency time in milliseconds
     * @return the label of the latency level, or "UNREACHABLE" if higher than any defined level
     * @throws IllegalArgumentException if the provided latency is less than zero
     */
    public String categorize(int latency) {
        Preconditions.checkArgument(latency >= 0);

        var entry = latencyLevels.higherEntry(latency);
        if (entry != null) return entry.getValue();
        return UNREACHABLE;
    }
}
