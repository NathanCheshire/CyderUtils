package cyder.network;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import cyder.constants.CyderRegexPatterns;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An object which pings a provided ip:port and determines a status based on a provided tolerance.
 */
public final class HighLatencyChecker {
    /**
     * The IP address this checker will ping.
     */
    private final String ipAddress;

    /**
     * The port this checker will ping.
     */
    private final Port port;

    /**
     * The name of the ipAddress:port combo this latency checker pings.
     */
    private final String remoteName;

    /**
     * The categorizer to categorize the determined latency of this checker.
     */
    private final LatencyCategorizer latencyCategorizer;

    /**
     * The delay between latency refreshes.
     */
    private final Duration pingDelay;

    /**
     * Whether this high latency checker is currently running
     */
    private final AtomicBoolean pinging = new AtomicBoolean(false);

    /**
     * The current status of this latency checker.
     */
    private String currentStatus;

    /**
     * The current latency of this latency checker.
     */
    private long currentLatency;

    private HighLatencyChecker(Builder builder) {
        this.ipAddress = builder.ipAddress;
        this.port = builder.port;
        this.latencyCategorizer = builder.latencyCategorizer;
        this.remoteName = builder.remoteName;
        this.pingDelay = builder.pingDelay;

        this.currentStatus = latencyCategorizer.getUnreachableString();
        this.currentLatency = Long.MAX_VALUE;
    }

    /**
     * Starts this latency checker if not running.
     *
     * @throws IllegalStateException if this checker is already running
     */
    public void start() {
        Preconditions.checkState(!pinging.get());
        pinging.set(true);

        CyderThreadRunner.submit(() -> {
            while (pinging.get()) {
                try {
                    // todo how to time this out if stop is called?
                    long newLatency = NetworkUtil.getLatency(ipAddress, port.getPort(),
                            (int) latencyCategorizer.getMaxLatencyLevel());
                    this.currentLatency = newLatency;
                    this.currentStatus = latencyCategorizer.categorize(newLatency);
                } catch (IOException e) {
                    this.currentLatency = Long.MAX_VALUE;
                    this.currentStatus = latencyCategorizer.getUnreachableString();
                }

                ThreadUtil.sleepWithChecks(pingDelay.toMillis(), 100, () -> !isRunning());
            }
        }, "todo customizable");
    }

    /**
     * Stops this latency checker if running.
     */
    public void stop() {
        pinging.set(false);
    }

    /**
     * Returns whether the latency checker is running.
     *
     * @return whether the latency checker is running
     */
    public boolean isRunning() {
        return pinging.get();
    }

    /**
     * Returns the current status of this latency checker.
     * If the checker has not yet ran, then {@link LatencyCategorizer#getUnreachableString()} will be returned.
     *
     * @return the current status of this latency checker
     */
    public String getCurrentStatus() {
        return currentStatus;
    }

    /**
     * Returns the current latency of this latency checker.
     * If the checker has not yet ran, then {@link Long#MAX_VALUE} will be returned.
     *
     * @return the current latency of this latency checker
     */
    public long getCurrentLatency() {
        return currentLatency;
    }

    /**
     * A builder for constructing instances of {@link HighLatencyChecker}.
     */
    public static final class Builder {
        private static final String DEFAULT_IP_ADDRESS = "172.217.4.78";
        private static final String DEFAULT_REMOTE_NAME = "Google";
        private static final LatencyCategorizer DEFAULT_LATENCY_CATEGORIZER
                = new LatencyCategorizer(ImmutableMap.of(
                        200L, "Low",
                        1000L, "Moderate",
                        1500L, "High"),
                DEFAULT_REMOTE_NAME + " unreachable");
        private static final Duration DEFAULT_PING_DELAY = Duration.ofSeconds(5);

        private String ipAddress;
        private Port port;
        private LatencyCategorizer latencyCategorizer;
        private String remoteName;
        private Duration pingDelay;

        /**
         * Constructs a new instance of a Builder for a {@link HighLatencyChecker}.
         * All defaults are used for all internal parameters.
         */
        public Builder() {
            this.ipAddress = DEFAULT_IP_ADDRESS;
            this.port = CommonServicePort.HTTP.constructPort();
            this.latencyCategorizer = DEFAULT_LATENCY_CATEGORIZER;
            this.remoteName = DEFAULT_REMOTE_NAME;
            this.pingDelay = DEFAULT_PING_DELAY;
        }

        /**
         * Sets the IP address to the provided address.
         *
         * @param ipAddress the IP address
         * @return this builder
         * @throws NullPointerException if the provided ipAddress is null
         * @throws IllegalArgumentException if the provided ipAddress is not formatted properly
         */
        public Builder setIpAddress(String ipAddress) {
            Preconditions.checkNotNull(ipAddress);
            Preconditions.checkArgument(CyderRegexPatterns.ipv4Pattern.matcher(ipAddress).matches());

            this.ipAddress = ipAddress;
            return this;
        }

        /**
         * Sets the port of this builder.
         *
         * @param port the port
         * @return this builder
         * @throws IllegalArgumentException if the provided port is invalid
         */
        public Builder setPort(int port) {
            Preconditions.checkArgument(Port.portRange.contains(port));
            this.port = new Port(port);
            return this;
        }

        /**
         * Sets the port of this builder.
         *
         * @param port the port
         * @return this builder
         * @throws NullPointerException if the provided port is null
         */
        public Builder setPort(Port port) {
            Preconditions.checkNotNull(port);
            this.port = port;
            return this;
        }

        /**
         * Sets the port of this builder.
         *
         * @param commonServicePort the common service port
         * @return this builder
         * @throws NullPointerException if the provided common service port is null
         */
        public Builder setPort(CommonServicePort commonServicePort) {
            Preconditions.checkNotNull(commonServicePort);
            this.port = commonServicePort.constructPort();
            return this;
        }

        /**
         * Sets the latency categorizer of this builder.
         *
         * @param latencyCategorizer the latency categorizer
         * @return this builder
         * @throws NullPointerException if the provided latency categorizer is null
         */
        public Builder setLatencyCategorizer(LatencyCategorizer latencyCategorizer) {
            Preconditions.checkNotNull(latencyCategorizer);
            this.latencyCategorizer = latencyCategorizer;
            return this;
        }

        /**
         * Sets the remote name of this builder.
         *
         * @param remoteName the remote name of this builder
         * @return this builder
         * @throws NullPointerException if the provided remote name is null
         * @throws IllegalArgumentException if the provided remote name is empty
         */
        public Builder setRemoteName(String remoteName) {
            Preconditions.checkNotNull(remoteName);
            Preconditions.checkArgument(!remoteName.trim().isEmpty());
            this.remoteName = remoteName;
            return this;
        }

        /**
         * Sets the ping delay of this builder.
         *
         * @param pingDelay the ping delay
         * @return this builder
         * @throws NullPointerException if the provided ping delay is null
         * @throws IllegalArgumentException if the provided ping delay is negative
         */
        public Builder setPingDelay(Duration pingDelay) {
            Preconditions.checkNotNull(pingDelay);
            Preconditions.checkArgument(!pingDelay.isNegative());
            this.pingDelay = pingDelay;
            return this;
        }
    }
}
