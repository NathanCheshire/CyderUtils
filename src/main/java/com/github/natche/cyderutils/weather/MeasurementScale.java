package com.github.natche.cyderutils.weather;

/** Acceptable measurement scales accepted by the OpenWeatherMap API. */
public enum MeasurementScale {
    /** The imperial measurement scale. */
    IMPERIAL("imperial"),

    /** The metric measurement scale. */
    METRIC("metric");

    private final String unitName;

    MeasurementScale(String unitName) {
        this.unitName = unitName;
    }

    /**
     * Returns the unit name for this measurement scale.
     *
     * @return the unit name for this measurement scale
     */
    public String getUnitName() {
        return unitName;
    }
}