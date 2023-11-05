package cyder.network;

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
     * A builder for constructing instances of {@link HighLatencyChecker}.
     */
    public static final class Builder {

    }
}
