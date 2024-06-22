package cyderutils.temperature;

import cyderutils.exceptions.IllegalMethodException;
import cyderutils.strings.CyderStrings;

/**
 * Utility methods related to temperature conversions.
 */
public final class TemperatureUtil {
    /**
     * The value to add to Celsius measurements to convert them to Kelvin measurements.
     */
    private static final double kelvinAdditive = 273.15;

    /**
     * The freezing point of water on a Fahrenheit scale.
     */
    private static final double fahrenheitFreezingPoint = 32.0;

    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private TemperatureUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Converts the provided Fahrenheit value to Kelvin.
     *
     * @param fahrenheit the value
     * @return the provided Fahrenheit value in Kelvin
     */
    public static double fahrenheitToKelvin(double fahrenheit) {
        return (fahrenheit - fahrenheitFreezingPoint) * (5.0 / 9.0) + kelvinAdditive;
    }

    /**
     * Converts the provided Celsius value to Kelvin.
     *
     * @param celsius the value
     * @return the provided Celsius value in Kelvin
     */
    public static double celsiusToKelvin(double celsius) {
        return celsius + kelvinAdditive;
    }

    /**
     * Converts the provided Fahrenheit value to Celsius.
     *
     * @param fahrenheit the value
     * @return the provided Fahrenheit value in Celsius
     */
    public static double fahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - fahrenheitFreezingPoint) * (5.0 / 9.0);
    }

    /**
     * Converts the provided Kelvin value to Celsius.
     *
     * @param kelvin the value
     * @return the provided Kelvin value in Celsius
     */
    public static double kelvinToCelsius(double kelvin) {
        return kelvin - kelvinAdditive;
    }

    /**
     * Converts the provided Celsius value to Fahrenheit.
     *
     * @param celsius the value
     * @return the provided Celsius value in Fahrenheit
     */
    public static double celsiusToFahrenheit(double celsius) {
        return celsius * 1.8 + fahrenheitFreezingPoint;
    }

    /**
     * Converts the provided Kelvin value to Fahrenheit.
     *
     * @param kelvin the value
     * @return the provided Kelvin value in Fahrenheit
     */
    public static double kelvinToFahrenheit(double kelvin) {
        return 1.8 * (kelvin - kelvinAdditive) + fahrenheitFreezingPoint;
    }
}
