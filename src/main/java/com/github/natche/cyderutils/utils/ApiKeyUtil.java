package com.github.natche.cyderutils.utils;

import com.google.common.base.Preconditions;
import com.github.natche.cyderutils.constants.CyderUrls;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.props.Props;
import com.github.natche.cyderutils.strings.CyderStrings;
import com.github.natche.cyderutils.weather.MeasurementScale;
import com.github.natche.cyderutils.youtube.parsers.YouTubeSearchResultPage;
import com.github.natche.cyderutils.youtube.search.YouTubeSearchQuery;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;

/** Utilities related to validation of API keys. */
public final class ApiKeyUtil {
    /** The app id argument. */
    private static final String APP_ID_ARG = "&appid=";

    /** The units argument for the weather data. */
    private static final String UNITS_ARG = "&units=";

    /** Suppress default constructor. */
    private ApiKeyUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Validates the weather key.
     *
     * @return whether the provided weather key was valid
     */
    public static boolean validateWeatherKey(String weatherDataApiKey) {
        Preconditions.checkNotNull(weatherDataApiKey);
        Preconditions.checkArgument(!weatherDataApiKey.isEmpty());

        String openString = CyderUrls.OPEN_WEATHER_BASE
                + weatherDataApiKey
                + APP_ID_ARG + Props.weatherKey.getValue()
                + UNITS_ARG + MeasurementScale.IMPERIAL.getWeatherDataRepresentation();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new URL(openString).openStream()))) {
            reader.readLine();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Validates the YouTube api v3 key.
     *
     * @param youTubeApiKey the YouTube api v3 key
     * @return whether the YouTube key was valid
     */
    private static boolean validateYoutubeApiKey(String youTubeApiKey) {
        Preconditions.checkNotNull(youTubeApiKey);
        Preconditions.checkArgument(!youTubeApiKey.isEmpty());

        try {
            Optional<YouTubeSearchResultPage> optionalResults =
                    YouTubeSearchQuery.buildDefaultBuilder().setKey(youTubeApiKey).build().getResults();
            return optionalResults.isPresent();
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * validates the map quest api key.
     *
     * @param mapApikey the map quest api key
     * @return whether the key was valid
     */
    private static boolean validateMapApiKey(String mapApikey) {
        Preconditions.checkNotNull(mapApikey);
        Preconditions.checkArgument(!mapApikey.isEmpty());

        try {
            MapUtil.getMapView(new MapUtil.Builder(mapApikey).setFilterWaterMark(true));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
