package cyder.network;

import com.google.common.base.Preconditions;
import cyder.files.FileUtil;
import cyder.parsers.ip.IpData;
import cyder.utils.SerializationUtil;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * A manager for
 */
public final class IpDataManager {
    /**
     * The IP data base url.
     */
    private static final String ipDataBaseUrl = "https://api.ipdata.co/?api-key=";

    /**
     * The byte read from a buffered reader which indicates the key used to query the ipdata API was invalid.
     */
    private static final int INVALID_KEY = -1;

    /**
     * The IP data key this manager uses to query ipdata.co.
     */
    private final String ipDataKey;

    /**
     * The most recent IpData object.
     */
    private IpData ipData;

    /**
     * Constructs a new IpDataManager which uses the provided key.
     *
     * @param ipDataKey the ip data key
     * @throws NullPointerException     if the provided ipDataKey is null
     * @throws IllegalArgumentException if the provided ipDataKey is empty or not a valid key
     */
    public IpDataManager(String ipDataKey) {
        Preconditions.checkNotNull(ipDataKey);
        Preconditions.checkArgument(!ipDataKey.trim().isEmpty());
        Preconditions.checkArgument(isValidIpDataKey(ipDataKey));

        this.ipDataKey = ipDataKey;
    }


    public IpData getIpData() throws IOException {
        if (ipData == null) refreshIpData();
        return ipData;
    }

    public void refreshIpData() throws IOException {
        String queryUrl = ipDataBaseUrl + ipDataKey;
        try (BufferedReader reader = FileUtil.bufferedReaderForUrl(queryUrl)) {
            ipData = SerializationUtil.fromJson(reader, IpData.class);
        }
    }

    /**
     * Returns whether the provided IP data key is valid.
     *
     * @param ipDataKey the IP data key to validate
     * @return whether the provided IP data key is valid
     */
    private static boolean isValidIpDataKey(String ipDataKey) {
        Preconditions.checkNotNull(ipDataKey);
        Preconditions.checkArgument(!ipDataKey.isEmpty());

        String remote = ipDataBaseUrl + ipDataKey;
        try (BufferedReader reader = FileUtil.bufferedReaderForUrl(remote)) {
            int result = reader.read();
            return result != INVALID_KEY;
        } catch (Exception ignored) {
            return false;
        }
    }
}
