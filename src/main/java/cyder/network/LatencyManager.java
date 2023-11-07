package cyder.network;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import cyder.exceptions.FatalException;
import cyder.files.FileUtil;
import cyder.props.Props;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.threads.CyderThreadRunner;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Optional;

/**
 * A manager for the host computer's network latency.
 */
public enum LatencyManager {
    /**
     * The latency manager instance.
     */
    INSTANCE;

    /**
     * The default timeout to use when pinging the latency ip to determine a user's latency.
     */
    public static final int DEFAULT_LATENCY_TIMEOUT = 2000;

    /**
     * The maximum possible ping for Cyder to consider a user's connection "decent."
     */
    public static final int DECENT_PING_MAXIMUM_LATENCY = 5000;

    /**
     * The slash slash for urls.
     */
    private static final String slashSlash = "//";

    /**
     * The prefix for http urls.
     */
    private static final String HTTP = "http";

    /**
     * The unknown string.
     */
    private static final String UNKNOWN = "Unknown";

    /**
     * The default ip to use when pinging determine a user's latency.
     * DNS changing for this would be highly unlikely.
     */
    private static final String defaultLatencyIp = "172.217.4.78";

    /**
     * The default port to use when pinging the default latency ip to determine a user's latency.
     */
    private static final int defaultLatencyPort = 80;

    /**
     * The default name of the latency ip and port.
     */
    private static final String defaultLatencyName = "Google";

    /**
     * The currently set latency ip.
     */
    private String latencyIp;

    /**
     * The currently set latency port.
     */
    private int latencyPort;

    /**
     * The currently set latency host name.
     */
    private String latencyHostName;

    LatencyManager() {
        initializeLatencyIpPortName();
    }

    /**
     * Returns the latency of the host system to {@link #latencyIp}.
     *
     * @param timeout the time in ms to wait before timing out
     * @return the latency in ms between the host system and the latency ip
     * @throws IOException if an exception occurs when attempting to connect to the latency ip
     */
    public long getLatency(int timeout) throws IOException {
        Preconditions.checkArgument(timeout > 0);

        Socket socket = new Socket();
        SocketAddress address = new InetSocketAddress(latencyIp, latencyPort);

        Stopwatch stopwatch = Stopwatch.createStarted();
        socket.connect(address, timeout);
        long latency = stopwatch.elapsed().toMillis();
        stopwatch.stop();

        FileUtil.closeIfNotNull(socket);

        return latency;
    }

    /**
     * Pings {@link #defaultLatencyIp} to find the latency.
     *
     * @return the latency of the local internet connection to the latency ip
     */
    public long getLatency() {
        return getLatency(DEFAULT_LATENCY_TIMEOUT);
    }

    /**
     * Determines if the connection to the internet is usable by pinging {@link #latencyIp}.
     *
     * @return if the connection to the internet is usable
     * @throws IOException if an exception occurs trying to connect to the latency ip
     */
    public boolean currentConnectionHasDecentPing() throws IOException {
        return getLatency(DECENT_PING_MAXIMUM_LATENCY) < DECENT_PING_MAXIMUM_LATENCY;
    }

    /**
     * Initializes the set latency ip, port, and name from the props/default values.
     */
    private void initializeLatencyIpPortName() {
        boolean customLatencyIpPresent = Props.latencyIp.customValuePresent();
        boolean customLatencyPortPreset = Props.latencyPort.customValuePresent();
        boolean customLatencyNamePresent = Props.latencyName.customValuePresent();

        if (customLatencyIpPresent) {
            latencyIp = Props.latencyIp.getValue();
            latencyPort = customLatencyPortPreset
                    ? Props.latencyPort.getValue()
                    : defaultLatencyPort;

            if (customLatencyNamePresent) {
                latencyHostName = Props.latencyName.getValue();
            } else {
                CyderThreadRunner.submit(() -> {
                    latencyHostName = UNKNOWN;

                    try {
                        String getTitleUrl = HTTP + CyderStrings.colon + slashSlash
                                + latencyIp + CyderStrings.colon + latencyPort;
                        Optional<String> latencyHostNameOptional = NetworkUtil.getUrlTitle(getTitleUrl);
                        if (latencyHostNameOptional.isEmpty()) {
                            throw new FatalException("Failed to get latency host name using JSoup");
                        } else {
                            latencyHostName = StringUtil.capsFirstWords(latencyHostNameOptional.get());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();

                        try {
                            latencyHostName = InetAddress.getByName(latencyIp).getHostName();
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                }, "todo remove me, or make me customization");
            }
        } else {
            latencyIp = defaultLatencyIp;
            latencyPort = defaultLatencyPort;
            latencyHostName = defaultLatencyName;
        }
    }
}
