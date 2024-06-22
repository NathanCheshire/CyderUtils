package com.github.natche.cyderutils.weather;

import com.google.common.base.Preconditions;
import com.github.natche.cyderutils.constants.CyderUrls;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.props.Props;
import com.github.natche.cyderutils.strings.CyderStrings;
import com.github.natche.cyderutils.utils.SerializationUtil;
import com.github.natche.cyderutils.weather.parsers.WeatherData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;

/**
 * Utilities related to weather and the weather API, that of Open Weather Map, utilized by Cyder.
 */
public final class WeatherUtil {
    /**
     * The app id argument.
     */
    private static final String APP_ID = "&appid=";

    /**
     * The units argument for the weather data.
     */
    private static final String UNITS_ARG = "&units=";

    /**
     * Suppress default constructor.
     */
    private WeatherUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns the weather data object for the provided location string if available. Empty optional else.
     *
     * @param locationString the location string such as "Starkville,Ms,USA"
     * @return the weather data object for the provided location string if available. Empty optional else
     */
    public static Optional<WeatherData> getWeatherData(String locationString) {
        Preconditions.checkNotNull(locationString);
        Preconditions.checkArgument(!locationString.isEmpty());
        Preconditions.checkState(Props.weatherKey.valuePresent());

        String weatherKey = Props.weatherKey.getValue();
        if (weatherKey.isEmpty()) return Optional.empty();

        String OpenString = CyderUrls.OPEN_WEATHER_BASE + locationString + APP_ID
                + weatherKey + UNITS_ARG + MeasurementScale.IMPERIAL.getWeatherDataRepresentation();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(OpenString).openStream()))) {
            return Optional.of(SerializationUtil.fromJson(reader, WeatherData.class));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }
}
