package cyder.network;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;

public class Port {
    /**
     * The range a general computer port must fall into.
     */
    public static final Range<Integer> portRange = Range.closed(1024, 65535);

    /**
     * The encapsulated port.
     */
    private final int port;

    /**
     * Constructs a new Port object from the provided {@link CommonServicePort}.
     *
     * @param commonServicePort the common service port
     */
    public Port(CommonServicePort commonServicePort) {
        this.port = commonServicePort.getPort();
    }

    /**
     * Constructs a new Port object from the provided port number.
     *
     * @param port the port number
     * @throws IllegalArgumentException if the provided port is outside of the range {@link #portRange}.
     */
    public Port(int port) {
        Preconditions.checkArgument(portRange.contains(port));
        this.port = port;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Port{port=" + port + "}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(port);
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
        return other.port == port;
    }
}
