package cyder.network.ipdataco;

import com.google.common.base.Preconditions;
import cyder.files.FileUtil;
import cyder.network.IpDataBaseUrl;
import cyder.parsers.ip.IpData;
import cyder.utils.SerializationUtil;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * A manager for storing and refreshing {@link IpData} objects queried from ipdata.co.
 * See <a href="https://dashboard.ipdata.co/sign-up.html">this link</a>
 * for creating an ipdata account and acquiring a key.
 */
public final class IpDataManager {
    /**
     * The byte read from a buffered reader which indicates the key used to query the ipdata API was invalid.
     */
    private static final int INVALID_KEY = -1;

    /**
     * The IP data key this manager uses to query ipdata.co.
     */
    private final String ipDataKey;

    /**
     * The base URl this manager use.
     */
    private IpDataBaseUrl baseUrl;

    /**
     * The most recent IpData object.
     */
    private IpData ipData;

    /**
     * Constructs a new IpDataManager which uses the provided key.
     * A base URL of {@link IpDataBaseUrl#STANDARD} is used.
     *
     * @param ipDataKey the ip data key
     * @throws NullPointerException     if the provided ipDataKey is null
     * @throws IllegalArgumentException if the provided ipDataKey is empty or not a valid key
     */
    public IpDataManager(String ipDataKey) {
        Preconditions.checkNotNull(ipDataKey);
        Preconditions.checkArgument(!ipDataKey.trim().isEmpty());
        Preconditions.checkArgument(isValidIpDataKey(ipDataKey, IpDataBaseUrl.STANDARD.getBaseUrl()));

        this.ipDataKey = ipDataKey;
        this.baseUrl = IpDataBaseUrl.STANDARD;
    }

    /**
     * Constructs a new IpDataManager which uses the provided key.
     *
     * @param ipDataKey the ip data key
     * @throws NullPointerException     if the provided ipDataKey or baseUrl is null
     * @throws IllegalArgumentException if the provided ipDataKey is empty or not a valid key
     */
    public IpDataManager(String ipDataKey, IpDataBaseUrl baseUrl) {
        Preconditions.checkNotNull(ipDataKey);
        Preconditions.checkNotNull(baseUrl);
        Preconditions.checkArgument(!ipDataKey.trim().isEmpty());
        Preconditions.checkArgument(isValidIpDataKey(ipDataKey, baseUrl.getBaseUrl()));

        this.ipDataKey = ipDataKey;
        this.baseUrl = baseUrl;
    }

    /**
     * Sets the base url this manager will use.
     *
     * @param baseUrl the base url this manager will use
     */
    public void setBaseUrl(IpDataBaseUrl baseUrl) {
        Preconditions.checkNotNull(baseUrl);

        this.baseUrl = baseUrl;
    }

    /**
     * Returns this manager's current IpData object. If the object has not
     * yet been initialized, it shall be before being returned.
     *
     * @return this manager's IpData object.
     * @throws IOException if the refresh fails
     */
    public IpData getIpData() throws IOException {
        if (ipData == null) refreshIpData();
        return ipData;
    }

    /**
     * Refreshes this manager's stored IpData object.
     *
     * @throws IOException if a BufferedReader cannot be created for reading from ipdata.co
     */
    public void refreshIpData() throws IOException {
        String queryUrl = baseUrl.getBaseUrl() + ipDataKey;
        try (BufferedReader reader = FileUtil.bufferedReaderForUrl(queryUrl)) {
            ipData = SerializationUtil.fromJson(reader, IpData.class);
        }
    }

    /**
     * Returns whether the provided IP data key is valid.
     *
     * @param ipDataKey the IP data key to validate
     * @param ipDataBaseUrl the base url to use for validation
     * @return whether the provided IP data key is valid
     */
    private static boolean isValidIpDataKey(String ipDataKey, String ipDataBaseUrl) {
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
