package com.github.natche.cyderutils.network.ipdataco;

import com.google.common.base.Preconditions;
import com.github.natche.cyderutils.files.FileUtil;
import com.github.natche.cyderutils.network.ipdataco.models.IpData;
import com.github.natche.cyderutils.threads.CyderThreadRunner;
import com.github.natche.cyderutils.threads.ThreadUtil;
import com.github.natche.cyderutils.utils.SerializationUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A manager for storing and refreshing {@link IpData} objects queried from ipdata.co.
 * See <a href="https://dashboard.ipdata.co/sign-up.html">this link</a>
 * for creating an ipdata account and acquiring a key.
 */
public final class IpDataManager {
    /**
     * The byte read from a buffered reader which indicates the key used to query the ipdata API was invalid.
     */
    private static final int INVALID_KEY_RESPONSE_READ = -1;

    /**
     * The default timeout between state refreshes.
     */
    private static final Duration DEFAULT_REFRESH_DURATION = Duration.ofMinutes(15);

    /**
     * The frequency at which to check {@link #lastRefreshTime} against {@link Instant#now()}
     * to determine whether {@link #refreshIpData()} should be invoked.
     */
    private static final Duration REFRESH_FREQUENCY_UPDATED_CHECK_FREQUENCY = Duration.ofMillis(500);

    /**
     * Whether the refresher task is running.
     */
    private final AtomicBoolean refresherRunning = new AtomicBoolean();

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
     * The last time {@link #ipData} was refreshed at.
     */
    private Instant lastRefreshTime;

    /**
     * The refresh frequency for re-pulling IP data.
     */
    private Duration refreshFrequency = DEFAULT_REFRESH_DURATION;

    /**
     * Constructs a new IpDataManager which uses the provided key.
     * A base URL of {@link IpDataBaseUrl#STANDARD} is used.
     *
     * @param ipDataKey the ip data key
     * @throws NullPointerException     if the provided ipDataKey is null
     * @throws IllegalArgumentException if the provided ipDataKey is empty or not a valid key
     * @throws IOException if an exception occurs pulling the IP data
     */
    public IpDataManager(String ipDataKey) throws IOException {
        Preconditions.checkNotNull(ipDataKey);
        Preconditions.checkArgument(!ipDataKey.trim().isEmpty());
        Preconditions.checkArgument(isValidIpDataKey(ipDataKey, IpDataBaseUrl.STANDARD.getBaseUrl()));

        this.ipDataKey = ipDataKey;
        this.baseUrl = IpDataBaseUrl.STANDARD;

        refreshIpData();
        startRefresher();
    }

    /**
     * Constructs a new IpDataManager which uses the provided key.
     *
     * @param ipDataKey the ip data key
     * @throws NullPointerException     if the provided ipDataKey or baseUrl is null
     * @throws IllegalArgumentException if the provided ipDataKey is empty or not a valid key
     * @throws IOException if an exception occurs pulling the IP data
     */
    public IpDataManager(String ipDataKey, IpDataBaseUrl baseUrl) throws IOException {
        Preconditions.checkNotNull(ipDataKey);
        Preconditions.checkNotNull(baseUrl);
        Preconditions.checkArgument(!ipDataKey.trim().isEmpty());
        Preconditions.checkArgument(isValidIpDataKey(ipDataKey, baseUrl.getBaseUrl()));

        this.ipDataKey = ipDataKey;
        this.baseUrl = baseUrl;

        refreshIpData();
        startRefresher();
    }

    /**
     * Sets the refresh frequency of this ip data manager.
     *
     * @param refreshFrequency the refresh frequency of this ip data manager
     * @throws NullPointerException     if the provided duration is null
     * @throws IllegalArgumentException if the provided duration is negative
     */
    public void setRefreshFrequency(Duration refreshFrequency) {
        Preconditions.checkNotNull(refreshFrequency);
        Preconditions.checkArgument(!refreshFrequency.isNegative());

        this.refreshFrequency = refreshFrequency;
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
        if (!ipDataAvailable()) refreshIpData();
        return ipData;
    }

    /**
     * Refreshes this manager's stored IpData object.
     *
     * @throws IOException if a BufferedReader cannot be created for reading from ipdata.co
     */
    public synchronized void refreshIpData() throws IOException {
        String queryUrl = baseUrl.getBaseUrl() + ipDataKey;
        try (BufferedReader reader = FileUtil.bufferedReaderForUrl(queryUrl)) {
            ipData = SerializationUtil.fromJson(reader, IpData.class);
            lastRefreshTime = Instant.now();
        }
    }

    /**
     * Refreshes the internal {@link IpData} object if it has been longer
     * than the provided duration since refreshed.
     *
     * @param duration the duration
     * @throws NullPointerException if the provided duration object is null
     * @throws IOException          if a refresh call is invoked and fails
     */
    public void refreshIfLongerThan(Duration duration) throws IOException {
        Preconditions.checkNotNull(duration);

        long now = Instant.now().toEpochMilli();
        long lastRefresh = lastRefreshTime.toEpochMilli();
        long difference = now - lastRefresh;
        if (difference > duration.toMillis()) refreshIpData();
    }

    /**
     * Returns whether the IP data is presently available.
     *
     * @return whether the IP data is presently available
     */
    public boolean ipDataAvailable() {
        return ipData != null;
    }

    /**
     * Returns the last time the internal {@link IpData} object was refreshed at.
     *
     * @return the last time the internal {@link IpData} object was refreshed at
     */
    public Instant getLastRefreshTime() {
        return lastRefreshTime;
    }

    /**
     * Returns whether the provided IP data key is valid.
     *
     * @param ipDataKey     the IP data key to validate
     * @param ipDataBaseUrl the base url to use for validation
     * @return whether the provided IP data key is valid
     */
    private static boolean isValidIpDataKey(String ipDataKey, String ipDataBaseUrl) {
        Preconditions.checkNotNull(ipDataKey);
        Preconditions.checkArgument(!ipDataKey.isEmpty());

        String remote = ipDataBaseUrl + ipDataKey;
        try (BufferedReader reader = FileUtil.bufferedReaderForUrl(remote)) {
            int result = reader.read();
            return result != INVALID_KEY_RESPONSE_READ;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Starts the refresher task which will re-pull the ip data stats whenever
     * now minus {@link #lastRefreshTime} exceeds that of {@link #refreshFrequency}.
     * Changing {@link #refreshFrequency} will either prolong the refresh, or result in it being performed immediately.
     *
     * @throws IllegalStateException if this method is already running
     */
    private synchronized void startRefresher() {
        Preconditions.checkState(!refresherRunning.get());

        refresherRunning.set(true);

        String threadName = "IpDataManager refresh task, object created: " + Instant.now().toEpochMilli();
        CyderThreadRunner.submit(() -> {
            while (refresherRunning.get()) {
                try {
                    refreshIfLongerThan(refreshFrequency);
                } catch (IOException ignored) {}
                ThreadUtil.sleep(REFRESH_FREQUENCY_UPDATED_CHECK_FREQUENCY);
            }
        }, threadName);
    }
}
