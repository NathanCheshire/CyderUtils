package com.github.natche.cyderutils.weather;

/**
 * Possible measurement scales, that of imperial or metric.
 */
public enum MeasurementScale {
    /**
     * The imperial measurement scale.
     */
    IMPERIAL("imperial"),

    /**
     * The metric measurement scale.
     */
    METRIC("metric");

    private final String weatherDataRepresentation;

    MeasurementScale(String weatherDataRepresentation) {
        this.weatherDataRepresentation = weatherDataRepresentation;
    }

    /**
     * Returns the weather data representation for this measurement scale.
     *
     * @return the weather data representation for this measurement scale
     */
    public String getWeatherDataRepresentation() {
        return weatherDataRepresentation;
    }
}