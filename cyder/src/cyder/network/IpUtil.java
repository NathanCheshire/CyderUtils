package cyder.network;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.parsers.remote.ip.IpData;
import cyder.props.PropLoader;
import cyder.utils.SerializationUtil;
import cyder.utils.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility methods for ip data queries.
 */
public final class IpUtil {
    /**
     * Suppress default constructor.
     */
    private IpUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The most recent IpData object.
     */
    private static final AtomicReference<IpData> mostRecentIpData = new AtomicReference<>();

    /**
     * The key for the ip data.
     */
    private static final String IP_KEY = "ip_key";

    /**
     * Updates the ip data object encapsulated and returns it.
     *
     * @return the encapsulated ip data object
     */
    public static IpData getIpData() {
        Preconditions.checkState(PropLoader.propExists(IP_KEY));
        IpData mostRecent = mostRecentIpData.get();
        if (mostRecent != null) {
            return mostRecent;
        } else {
            Optional<IpData> optionalData = pullIpData();
            if (optionalData.isEmpty()) throw new FatalException("Could not get IP data");
            IpData data = optionalData.get();
            mostRecentIpData.set(data);
            return data;
        }
    }

    /**
     * Pulls and serializes ip data into an ip data object and returns that object if found. Empty optional else.
     *
     * @return an ip data object
     */
    private static Optional<IpData> pullIpData() {
        String key = PropLoader.getString(IP_KEY);
        Preconditions.checkState(!StringUtil.getTrimmedText(key).isEmpty());

        String url = CyderUrls.IPDATA_BASE + key;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            return Optional.of(SerializationUtil.fromJson(reader, IpData.class));
        } catch (IOException e) {
            ExceptionHandler.silentHandle(e);
        }

        return Optional.empty();
    }
}