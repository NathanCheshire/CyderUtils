package cyder.network;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Range;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.threads.CyderThreadFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * An encapsulator class for operations on ports, specifically within the range
 * supported by Cyder, that of {@link #portRange}.
 */
public class Port {
    /**
     * The range a general computer port must fall into.
     */
    public static final Range<Integer> portRange = Range.closed(0, 65535);

    /**
     * The default port available timeout.
     */
    private static final Duration DEFAULT_PORT_AVAILABLE_TIMEOUT = Duration.ofMillis(400);

    /**
     * The encapsulated port.
     */
    private final int port;

    /**
     * The executor service used by {@link #isAvailable()}.
     */
    private final ExecutorService portAvailableFinderService;

    /**
     * The time to wait for this local port to bind to determine whether it is available.
     */
    private Duration portAvailableTimeout = DEFAULT_PORT_AVAILABLE_TIMEOUT;

    /**
     * Constructs a new Port object from the provided {@link CommonServicePort}.
     *
     * @param commonServicePort the common service port
     * @throws NullPointerException if the provided common service port is null
     */
    public static Port from(CommonServicePort commonServicePort) {
        Preconditions.checkNotNull(commonServicePort);

        return new Port(commonServicePort.getPort());
    }

    /**
     * Constructs a new Port object from the provided port number.
     *
     * @param port the port number
     * @throws IllegalArgumentException if the provided port is outside the range of {@link #portRange}.
     */
    public Port(int port) {
        Preconditions.checkArgument(portRange.contains(port));

        this.port = port;
        CyderThreadFactory threadFactory = new CyderThreadFactory("Port isAvailable, port=" + port);
        this.portAvailableFinderService = Executors.newSingleThreadExecutor(threadFactory);
    }

    /**
     * Returns the port.
     *
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the time to wait for this local port to bind to determine whether it is available.
     *
     * @param portAvailableTimeout the time to wait for this local port to bind to determine whether it is available
     * @return this Port object
     */
    @CanIgnoreReturnValue
    public Port setPortAvailableTimeout(Duration portAvailableTimeout) {
        Preconditions.checkNotNull(portAvailableTimeout);
        Preconditions.checkArgument(!portAvailableTimeout.isNegative());

        this.portAvailableTimeout = portAvailableTimeout;
        return this;
    }

    /**
     * Returns the time to wait for this local port to bind to determine whether it is available.
     *
     * @return the time to wait for this local port to bind to determine whether it is available
     */
    public Duration getPortAvailableTimeout() {
        return portAvailableTimeout;
    }

    /**
     * Returns whether this port is locally available and able to be bound to.
     * Note this method is blocking and will hold the current thread until this
     * port is found to be either bound or not bound.
     *
     * @return whether this port is locally available and able to be bound to
     */
    public synchronized boolean isAvailable() {
        Future<Boolean> futureIsAvailable = portAvailableFinderService.submit(
                () -> {
                    try (ServerSocket socket = new ServerSocket(port)) {
                        return true;
                    } catch (IOException ignored) {
                        return false;
                    }
                }
        );
        Stopwatch timer = Stopwatch.createStarted();
        while (!futureIsAvailable.isDone() && timer.elapsed().compareTo(portAvailableTimeout) < 0) Thread.onSpinWait();
        futureIsAvailable.cancel(true);
        if (futureIsAvailable.isDone()) {
            try {
                return futureIsAvailable.get();
            } catch (Exception ignored) {}
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Port{"
                + "port=" + port
                + ", portAvailableTimeout=" + portAvailableTimeout
                + "}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = Integer.hashCode(port);
        ret = 31 * ret + portAvailableTimeout.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Port)) {
            return false;
        }

        Port other = (Port) o;
        return other.port == port
                && other.portAvailableTimeout.equals(portAvailableTimeout);
    }
}
