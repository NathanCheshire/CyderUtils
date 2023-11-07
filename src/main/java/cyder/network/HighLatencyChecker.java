package cyder.network;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

/**
 * An object which pings a provided ip:port and determines a status based on a provided tolerance.
 */
public final class HighLatencyChecker {
    /**
     * The range a general computer port must fall into.
     */
    public static final Range<Integer> portRange = Range.closed(1024, 65535);

    /**
     * The IP address this checker will ping.
     */
    private final String ipAddress;

    /**
     * The port this checker will ping.
     */
    private final int port;

    /**
     * The categorizer to categorize the determined latency of this checker.
     */
    private final LatencyCategorizer latencyCategorizer;

    private HighLatencyChecker(Builder builder) {
        this.ipAddress = builder.ipAddress;
        this.port = builder.port;
        this.latencyCategorizer = builder.latencyCategorizer;
    }

    /**
     * A builder for constructing instances of {@link HighLatencyChecker}.
     */
    public static final class Builder {
        private static final String DEFAULT_IP_ADDRESS = "172.217.4.78";
        private static final int DEFAULT_PORT = 80;
        private static final String DEFAULT_REMOTE_NAME = "Google";
        private static final LatencyCategorizer DEFAULT_LATENCY_CATEGORIZER
                = new LatencyCategorizer(ImmutableMap.of(
                        200, "Low",
                        1000, "Moderate",
                        1500, "High"),
                DEFAULT_REMOTE_NAME + " unreachable");

        private String ipAddress;
        private int port;
        private LatencyCategorizer latencyCategorizer;
        private String remoteName;

        /**
         * Constructs a new instance of a Builder for a {@link HighLatencyChecker}.
         * All defaults are used for all internal parameters.
         */
        public Builder() {
            this.ipAddress = DEFAULT_IP_ADDRESS;
            this.port = DEFAULT_PORT;
            this.remoteName = DEFAULT_REMOTE_NAME;
            this.latencyCategorizer = DEFAULT_LATENCY_CATEGORIZER;
        }
    }
}
