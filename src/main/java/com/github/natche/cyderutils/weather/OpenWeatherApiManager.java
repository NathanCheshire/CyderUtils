package com.github.natche.cyderutils.weather;

import com.github.natche.cyderutils.threads.CyderThreadRunner;
import com.github.natche.cyderutils.threads.ThreadUtil;
import com.github.natche.cyderutils.utils.SerializationUtil;
import com.github.natche.cyderutils.weather.parsers.WeatherData;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CheckReturnValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

// todo allow stopping/starting

/** A manager for querying and updating weather data from the Open Weather API service. */
public final class OpenWeatherApiManager {
    /** The location used to validate keys. */
    private static final String KEY_VALIDATION_LOCATION = "Key West, FL, USA";

    /** The frequency to check the condition to refresh the internal data. */
    private static final Duration CHECK_REFRESH_INSTANT_FREQUENCY = Duration.ofMillis(500);

    /** The default refresh frequency of five minutes. */
    private static final Duration DEFAULT_REFRESH_FREQUENCY = Duration.ofMinutes(5);

    /** The open weather url to get weather data from. */
    private static final String baseUrl = "https://api.openweathermap.org/data/2.5/weather?q=";

    /** The units argument for the weather data. */
    private static final String UNITS_ARG = "&units=";

    /** Whether the refresher task is running. */
    private final AtomicBoolean refresherRunning = new AtomicBoolean();

    /** The API key this manager uses. */
    private final String apiKey;

    /** The location this manager queries. */
    private final String location;

    /** The measurement scale this */
    private final MeasurementScale measurementScale;

    /** The last fetched weather data. */
    private WeatherData weatherData;

    /** The last instant this manager refreshed the data at. */
    private Instant lastRefreshInstant;

    /** The frequency to refresh at. */
    private Duration refreshFrequency = DEFAULT_REFRESH_FREQUENCY;

    /**
     * Constructs a new OpenWeatherApiManager.
     *
     * @param apiKey the API key
     * @param location the location to query
     * @param measurementScale the measurement scale to use, that of Imperial or Metric
     * @throws IOException if an exception occurs when fetching the initial data
     * @throws NullPointerException if either the API key, location, or measurement scale are null
     * @throws IllegalArgumentException if either the API key or location is empty or the key is not valid
     */
    public OpenWeatherApiManager(String apiKey,
                                 String location,
                                 MeasurementScale measurementScale) throws IOException {
        Preconditions.checkNotNull(apiKey);
        Preconditions.checkNotNull(location);
        Preconditions.checkNotNull(measurementScale);
        Preconditions.checkArgument(!apiKey.isEmpty());
        Preconditions.checkArgument(!location.isEmpty());
        Preconditions.checkArgument(validateKey(apiKey));

        this.apiKey = apiKey;
        this.location = location;
        this.measurementScale = measurementScale;

        refreshWeatherData();
        startRefresher();
    }

    /**
     * Returns whether Open Weather API data is present.
     *
     * @return whether Open Weather API data is present
     */
    public boolean isDataAvailable() {
        return weatherData != null;
    }

    /**
     * Sets the frequency at which to refresh the internal data.
     *
     * @param refreshFrequency the frequency at which to refresh the internal data
     * @throws NullPointerException if the provided duration is null
     * @throws IllegalArgumentException if the provided duration is negative
     */
    public void setRefreshFrequency(Duration refreshFrequency) {
        Preconditions.checkNotNull(refreshFrequency);
        Preconditions.checkArgument(!refreshFrequency.isNegative());

        this.refreshFrequency = refreshFrequency;
    }

    /**
     * Returns the location this manager queries the API for data on.
     *
     * @return the location this manager queries the API for data on
     */
    public String getLocation() {
        return location;
    }

    /**
     * Returns the last instant at which the data was refreshed at.
     *
     * @return the last instant at which the data was refreshed at
     */
    @CheckReturnValue
    public Instant getLastRefreshInstant() {
        return lastRefreshInstant;
    }

    /**
     * Validates the provided OpenWeather API key.
     *
     * @param weatherDataApiKey an OpenWeather API key
     * @return whether the provided key is valid
     */
    @CheckReturnValue
    public static boolean validateKey(String weatherDataApiKey) {
        Preconditions.checkNotNull(weatherDataApiKey);
        Preconditions.checkArgument(!weatherDataApiKey.isEmpty());

        String queryString = getQueryUrl(weatherDataApiKey, KEY_VALIDATION_LOCATION, MeasurementScale.IMPERIAL);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new URL(queryString).openStream()))) {
            reader.readLine();
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    /**
     * Refreshes the data if it has been longer than {@link #refreshFrequency}.
     *
     * @throws IOException if an exception occurs refreshing the data
     */
    public void refreshIfLongerThan() throws IOException {
        long now = Instant.now().toEpochMilli();
        long lastRefresh = lastRefreshInstant.toEpochMilli();
        long difference = now - lastRefresh;
        if (difference > refreshFrequency.toMillis()) refreshWeatherData();
    }

    /**
     * Refreshes the internal weather data.
     *
     * @throws IOException if an exception occurs reading from the URL
     */
    private void refreshWeatherData() throws IOException {
        String queryString = getQueryUrl(apiKey, location, measurementScale);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(queryString).openStream()))) {
            weatherData = SerializationUtil.fromJson(reader, WeatherData.class);
            lastRefreshInstant = Instant.now();
        }
    }

    /**
     * Starts the refresher.
     *
     * @throws IllegalStateException if the refresher is already running.
     */
    private void startRefresher() {
        Preconditions.checkState(!refresherRunning.get());

        refresherRunning.set(true);

        String threadName = "OpenWeatherApiManager refresh task, object created: " + Instant.now().toEpochMilli();
        CyderThreadRunner.submit(() -> {
            while (refresherRunning.get()) {
                try {
                    refreshIfLongerThan();
                } catch (IOException ignored) {}
                ThreadUtil.sleep(CHECK_REFRESH_INSTANT_FREQUENCY.toMillis());
            }
        }, threadName);
    }

    /**
     * Returns the URL to query given the provided parameters.
     *
     * @param key      the API key
     * @param location the location string
     * @param scale    the unit scale
     * @return the URL to query
     */
    private static String getQueryUrl(String key, String location, MeasurementScale scale) {
        //noinspection SpellCheckingInspection
        return baseUrl + URLEncoder.encode(location, StandardCharsets.UTF_8)
                + "&appid=" + key + UNITS_ARG + scale.getUnitName();
    }
}
