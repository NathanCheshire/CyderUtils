package cyderutils.temperature;

import com.google.common.base.Preconditions;

/**
 * A common unit of temperature.
 */
public enum TemperatureUnit {
    /**
     * The Celsius unit of temperature, used by metric system countries.
     * On this scale, water freezes at 0 and boils at 100.
     */
    CELSIUS,

    /**
     * The Kelvin unit of temperature, the international scientific standard.
     * Zero degrees represents the theoretical limit at which the enthalpy and entropy
     * implies zero-point-energy induced particle motion. By the Heisenberg uncertainty principle,
     * this is technically impossible since we cannot know both the momentum and position of a particle.
     */
    KELVIN,

    /**
     * The Fahrenheit unit of temperature, used by English countries.
     * Water freezes at 32 and boils at 212.
     */
    FAHRENHEIT;

    /**
     * Converts a measurement of this temperature unit to the provided measurement.
     *
     * @param measurement   the measurement of this unit
     * @param toMeasurement the unit to convert to
     * @return the conversion
     * @throws NullPointerException if the provided toMeasurement is null
     * @throws ConversionException  if the provided measurement is below
     *                              <a href="https://en.wikipedia.org/wiki/Absolute_zero">absolute zero</a> or the toMeasurement
     *                              is an unsupported temperature unit
     */
    public double convertTo(float measurement, TemperatureUnit toMeasurement) {
        Preconditions.checkNotNull(toMeasurement);

        return switch (this) {
            case CELSIUS -> switch (toMeasurement) {
                case KELVIN -> TemperatureUtil.celsiusToKelvin(measurement);
                case FAHRENHEIT -> TemperatureUtil.celsiusToFahrenheit(measurement);
                case CELSIUS -> measurement;
            };
            case KELVIN -> switch (toMeasurement) {
                case CELSIUS -> TemperatureUtil.kelvinToCelsius(measurement);
                case KELVIN -> measurement;
                case FAHRENHEIT -> TemperatureUtil.kelvinToFahrenheit(measurement);
            };
            case FAHRENHEIT -> switch (toMeasurement) {
                case CELSIUS -> TemperatureUtil.fahrenheitToCelsius(measurement);
                case KELVIN -> TemperatureUtil.fahrenheitToKelvin(measurement);
                case FAHRENHEIT -> measurement;
            };
        };
    }
}
