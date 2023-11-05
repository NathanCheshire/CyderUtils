package cyder.network;

import com.google.common.base.Preconditions;
import cyder.constants.CyderUrls;
import cyder.exceptions.FatalException;
import cyder.parsers.ip.IpData;
import cyder.props.Props;
import cyder.utils.SerializationUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A manager for this session's IP data.
 */
public enum IpDataManager {
    /**
     * The IpDataManager instance.
     */
    INSTANCE;

    IpDataManager() {}

    /**
     * The most recent IpData object.
     */
    private final AtomicReference<IpData> ipData = new AtomicReference<>();

    /**
     * Updates the ip data object encapsulated and returns it.
     *
     * @return the encapsulated ip data object
     * @throws IOException if an exception occurs reading from the query url
     */
    public IpData getIpData() throws IOException {
        IpData ret = ipData.get();
        if (ret != null) return ret;

        Optional<IpData> pulledData = pullIpData();
        if (pulledData.isPresent()) {
            ret = pulledData.get();
            ipData.set(ret);
            return ret;
        }

        throw new FatalException("Failed to fetch IP data");
    }

    /**
     * Pulls and serializes the ip data into an ip data object and
     * returns that object if successful. Empty optional else.
     *
     * @return the ip data object
     * @throws IOException if an exception occurs reading from the query url
     */
    private Optional<IpData> pullIpData() throws IOException {
        Preconditions.checkState(Props.ipKey.valuePresent());

        return pullIpData(Props.ipKey.getValue());
    }

    /**
     * Pulls and serializes the ip data into an ip data object
     * and returns that object if successful. Empty optional else.
     *
     * @param key the key to use for the ip data query
     * @return the ip data object
     * @throws NullPointerException     if the provided key is null
     * @throws IllegalArgumentException if the provided key is empty
     * @throws IOException              if an exception occurs reading from the query url
     */
    public Optional<IpData> pullIpData(String key) throws IOException {
        Preconditions.checkNotNull(key);
        Preconditions.checkArgument(!key.isEmpty());

        String queryUrl = CyderUrls.IPDATA_BASE + key;
        URL url = new URL(queryUrl);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return Optional.of(SerializationUtil.fromJson(reader, IpData.class));
        }
    }
}
