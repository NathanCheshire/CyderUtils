package com.github.natche.cyderutils.elevation;

/** The standard length units for an elevation query. */
public enum LengthUnit {
    /** The SI unit for length. */
    METERS("METERS", 1.0),

    /** The English unit for length. */
    FEET("FEET", 0.3048);

    /** The name of the length unit */
    private final String name;

    /** Conversion factor to meters. */
    private final double toMetersFactor;

    LengthUnit(String name, double toMetersFactor) {
        this.name = name;
        this.toMetersFactor = toMetersFactor;
    }

    /**
     * Returns the name of this length unit.
     *
     * @return the name of this length unit
     */
    public String getName() {
        return name;
    }

    /**
     * Converts a value from this unit to another unit.
     *
     * @param value      the value in this unit
     * @param targetUnit the target unit to convert to
     * @return the converted value
     */
    public double convert(double value, LengthUnit targetUnit) {
        double valueInMeters = value * this.toMetersFactor;
        return valueInMeters / targetUnit.toMetersFactor;
    }
}
