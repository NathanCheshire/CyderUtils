package com.github.natche.cyderutils.temperature;

import com.google.common.base.Preconditions;
import com.github.natche.cyderutils.annotations.CyderAuthor;
import com.github.natche.cyderutils.annotations.ForReadability;
import com.github.natche.cyderutils.annotations.Vanilla;
import com.github.natche.cyderutils.color.CyderColors;
import com.github.natche.cyderutils.font.CyderFonts;
import com.github.natche.cyderutils.ui.UiUtil;
import com.github.natche.cyderutils.ui.button.CyderModernButton;
import com.github.natche.cyderutils.ui.button.ThemeBuilder;
import com.github.natche.cyderutils.ui.field.CyderTextField;
import com.github.natche.cyderutils.ui.frame.CyderFrame;
import com.github.natche.cyderutils.ui.selection.CyderCheckbox;
import com.github.natche.cyderutils.ui.selection.CyderCheckboxGroup;

import javax.swing.*;
import java.text.DecimalFormat;
import java.util.Optional;

import static com.github.natche.cyderutils.temperature.TemperatureUtil.*;

/** A temperature conversion widget. */
@Vanilla
@CyderAuthor
public final class TemperatureWidget {
    /** The frame for this widget. */
    private CyderFrame temperatureFrame;

    /** The field to receive the temperature input. */
    private CyderTextField startingValueField;

    /** The old Fahrenheit checkbox. */
    private CyderCheckbox oldFahrenheit;

    /** The new Fahrenheit checkbox. */
    private CyderCheckbox newFahrenheit;

    /** The old celsius checkbox. */
    private CyderCheckbox oldCelsius;

    /** The new celsius checkbox. */
    private CyderCheckbox newCelsius;

    /** The old kelvin checkbox. */
    private CyderCheckbox oldKelvin;

    /** The new kelvin checkbox. */
    private CyderCheckbox newKelvin;

    /**
     * Returns a new instance of the temperature converter widget.
     *
     * @return a new instance of the temperature converter widget
     */
    public static TemperatureWidget getInstance() {
        return new TemperatureWidget();
    }

    /** Temperature converter widget to convert between kelvin, fahrenheit, and celsius. */
    private TemperatureWidget() {}

    /** The decimal formatter for the result. */
    private static final DecimalFormat resultFormatter = new DecimalFormat("#.####");

    /** The text for the reset values button. */
    private static final String RESET_VALUES = "Reset Values";

    /** The temperature units supported by Cyder for conversion. */
    private enum Unit {
        /** The imperial temperature unit. */
        FAHRENHEIT("Fahrenheit"),

        /** The SI temperature unit. */
        CELSIUS("Celsius"),

        /** The primary temperature uit */
        KELVIN("Kelvin");

        /** The name of this temperature unit. */
        private final String name;

        Unit(String name) {
            this.name = name;
        }

        /**
         * Returns the name of this temperature unit.
         *
         * @return the name of this temperature unit
         */
        public String getName() {
            return name;
        }
    }

    /** The text for the calculate button. */
    private static final String CALCULATE = "Calculate";

    /** The theme for the buttons. */
    private static final ThemeBuilder buttonTheme = new ThemeBuilder()
            .setBackgroundColor(CyderColors.regularRed)
            .setBorderLength(5)
            .setBorderColor(CyderColors.navy)
            .setForegroundColor(CyderColors.navy)
            .setFont(CyderFonts.SEGOE_20);

    /** The regex for the value field. */
    private static final String valueFieldRegex = "-?(([0-9]*)\\.?[0-9]*)";

    /** The measurement label text. */
    private static final String MEASUREMENT = "Measurement:";

    /** Shows a new TemperatureWidget. */
    public static void showGui() {
        getInstance().innerShowGui();
    }

    /** Shows the temperature widget GUI. */
    private void innerShowGui() {
        UiUtil.closeIfOpen(temperatureFrame);

        temperatureFrame = new CyderFrame.Builder()
                .setWidth(600)
                .setHeight(340)
                .setTitle("Temperature Converter")
                .build();

        JLabel valueLabel = new JLabel(MEASUREMENT);
        valueLabel.setFont(CyderFonts.SEGOE_20);
        valueLabel.setBounds(60, 40, 200, 30);
        temperatureFrame.getContentPane().add(valueLabel);

        startingValueField = new CyderTextField();
        startingValueField.setHorizontalAlignment(JTextField.CENTER);
        startingValueField.setKeyEventRegexMatcher(valueFieldRegex);
        startingValueField.setBounds(240, 40, 300, 35);
        temperatureFrame.getContentPane().add(startingValueField);

        JLabel oldFahrenheitLabel = new JLabel(Unit.FAHRENHEIT.getName());
        oldFahrenheitLabel.setFont(CyderFonts.SEGOE_30.deriveFont(22f));
        oldFahrenheitLabel.setForeground(CyderColors.navy);
        oldFahrenheitLabel.setBounds(140, 110, 250, 30);
        temperatureFrame.getContentPane().add(oldFahrenheitLabel);

        JLabel oldCelsiusLabel = new JLabel(Unit.CELSIUS.getName());
        oldCelsiusLabel.setFont(CyderFonts.SEGOE_30.deriveFont(22f));
        oldCelsiusLabel.setForeground(CyderColors.navy);
        oldCelsiusLabel.setBounds(140, 170, 250, 30);
        temperatureFrame.getContentPane().add(oldCelsiusLabel);

        JLabel oldKelvinLabel = new JLabel(Unit.KELVIN.getName());
        oldKelvinLabel.setFont(CyderFonts.SEGOE_30.deriveFont(22f));
        oldKelvinLabel.setForeground(CyderColors.navy);
        oldKelvinLabel.setBounds(140, 230, 250, 30);
        temperatureFrame.getContentPane().add(oldKelvinLabel);

        CyderCheckboxGroup oldGroup = new CyderCheckboxGroup();

        oldFahrenheit = new CyderCheckbox();
        oldFahrenheit.setBounds(80, 100, 50, 50);
        oldGroup.addCheckbox(oldFahrenheit);
        temperatureFrame.getContentPane().add(oldFahrenheit);

        oldCelsius = new CyderCheckbox();
        oldCelsius.setBounds(80, 160, 50, 50);
        oldGroup.addCheckbox(oldCelsius);
        temperatureFrame.getContentPane().add(oldCelsius);

        oldKelvin = new CyderCheckbox();
        oldKelvin.setBounds(80, 220, 50, 50);
        oldGroup.addCheckbox(oldKelvin);
        temperatureFrame.getContentPane().add(oldKelvin);

        JLabel conversionToLabel = new JLabel("-2-");
        conversionToLabel.setFont(CyderFonts.SEGOE_30.deriveFont(45f));
        conversionToLabel.setForeground(CyderColors.navy);
        conversionToLabel.setBounds(260, 150, 150, 60);
        temperatureFrame.getContentPane().add(conversionToLabel);

        JLabel newFahrenheitLabel = new JLabel(Unit.FAHRENHEIT.getName());
        newFahrenheitLabel.setFont(CyderFonts.SEGOE_30.deriveFont(22f));
        newFahrenheitLabel.setForeground(CyderColors.navy);
        newFahrenheitLabel.setBounds(430, 110, 250, 30);
        temperatureFrame.getContentPane().add(newFahrenheitLabel);

        JLabel newCelsiusLabel = new JLabel(Unit.CELSIUS.getName());
        newCelsiusLabel.setFont(CyderFonts.SEGOE_30.deriveFont(22f));
        newCelsiusLabel.setForeground(CyderColors.navy);
        newCelsiusLabel.setBounds(430, 170, 250, 30);
        temperatureFrame.getContentPane().add(newCelsiusLabel);

        JLabel newKelvinLabel = new JLabel(Unit.KELVIN.getName());
        newKelvinLabel.setFont(CyderFonts.SEGOE_30.deriveFont(22f));
        newKelvinLabel.setForeground(CyderColors.navy);
        newKelvinLabel.setBounds(430, 230, 250, 30);
        temperatureFrame.getContentPane().add(newKelvinLabel);

        CyderCheckboxGroup newGroup = new CyderCheckboxGroup();

        newFahrenheit = new CyderCheckbox();
        newFahrenheit.setBounds(370, 100, 50, 50);
        newGroup.addCheckbox(newFahrenheit);
        temperatureFrame.getContentPane().add(newFahrenheit);

        newCelsius = new CyderCheckbox();
        newCelsius.setBounds(370, 160, 50, 50);
        newGroup.addCheckbox(newCelsius);
        temperatureFrame.getContentPane().add(newCelsius);

        newKelvin = new CyderCheckbox();
        newKelvin.setBounds(370, 220, 50, 50);
        newGroup.addCheckbox(newKelvin);
        temperatureFrame.getContentPane().add(newKelvin);

        CyderModernButton calculate = new CyderModernButton(CALCULATE);
        calculate.addClickRunnable(this::calculateButtonAction);
        calculate.setTheme(buttonTheme);
        calculate.setBounds(140, 280, 150, 40);
        temperatureFrame.getContentPane().add(calculate);

        CyderModernButton resetValues = new CyderModernButton(RESET_VALUES);
        resetValues.addClickRunnable(this::reset);
        resetValues.setTheme(buttonTheme);
        resetValues.setBounds(300, 280, 150, 40);
        temperatureFrame.getContentPane().add(resetValues);

        reset();
        temperatureFrame.finalizeAndShow();
    }

    /** Performs the logic for when the calculate button is pressed. */
    @ForReadability
    private void calculateButtonAction() {
        String startingValueText = startingValueField.getTrimmedText();
        if (startingValueText.isEmpty()) return;

        double value;
        try {
            value = Double.parseDouble(startingValueText);
        } catch (NumberFormatException ex) {
            temperatureFrame.notify("Could not parse input");
            return;
        }

        Optional<Unit> oldUnitOptional = getOldUnit();
        Optional<Unit> newUnitOptional = getNewUnit();

        if (oldUnitOptional.isEmpty() || newUnitOptional.isEmpty()) {
            return;
        }

        Unit oldUnit = oldUnitOptional.get();
        Unit newUnit = newUnitOptional.get();

        if (newUnit == oldUnit) {
            temperatureFrame.notify("Get out of here with that, your value is already in "
                    + oldUnit.getName());
            return;
        }

        double oldValueInKelvin = toKelvin(value, oldUnit);

        switch (newUnit) {
            case FAHRENHEIT -> {
                double fahrenheitFromKelvin = kelvinToFahrenheit(oldValueInKelvin);
                startingValueField.setText(resultFormatter.format(fahrenheitFromKelvin));
                oldFahrenheit.setChecked();
            }
            case CELSIUS -> {
                double celsiusFromKelvin = kelvinToCelsius(oldValueInKelvin);
                startingValueField.setText(resultFormatter.format(celsiusFromKelvin));
                oldCelsius.setChecked();
            }
            case KELVIN -> {
                startingValueField.setText(resultFormatter.format(oldValueInKelvin));
                oldKelvin.setChecked();
            }
        }

        startingValueField.flashField();
    }

    /**
     * Converts the provided temperature to kelvin.
     *
     * @param value           the temperature value
     * @param temperatureUnit the temperature unit
     * @return the temperature converted to kelvin
     */
    @ForReadability
    private double toKelvin(double value, Unit temperatureUnit) {
        Preconditions.checkNotNull(temperatureUnit);

        return switch (temperatureUnit) {
            case FAHRENHEIT -> fahrenheitToKelvin(value);
            case CELSIUS -> celsiusToKelvin(value);
            case KELVIN -> value;
        };
    }

    /**
     * Converts the provided temperature to celsius.
     *
     * @param value           the temperature value
     * @param temperatureUnit the temperature unit
     * @return the temperature converted to fahrenheit
     */
    @ForReadability
    private double toFahrenheit(double value, Unit temperatureUnit) {
        Preconditions.checkNotNull(temperatureUnit);

        return switch (temperatureUnit) {
            case FAHRENHEIT -> value;
            case CELSIUS -> celsiusToFahrenheit(value);
            case KELVIN -> kelvinToFahrenheit(value);
        };
    }

    /**
     * Converts the provided temperature to celsius.
     *
     * @param value           the temperature value
     * @param temperatureUnit the temperature unit
     * @return the temperature converted to celsius
     */
    @ForReadability
    private double toCelsius(double value, Unit temperatureUnit) {
        Preconditions.checkNotNull(temperatureUnit);

        return switch (temperatureUnit) {
            case FAHRENHEIT -> fahrenheitToCelsius(value);
            case CELSIUS -> value;
            case KELVIN -> kelvinToCelsius(value);
        };
    }

    /**
     * Returns the old temperature unit if present.
     *
     * @return the old temperature unit if present
     */
    @ForReadability
    private Optional<Unit> getOldUnit() {
        if (oldFahrenheit.isChecked()) {
            return Optional.of(Unit.FAHRENHEIT);
        } else if (oldCelsius.isChecked()) {
            return Optional.of(Unit.CELSIUS);
        } else if (oldKelvin.isChecked()) {
            return Optional.of(Unit.KELVIN);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Returns the new temperature unit if present.
     *
     * @return the new temperature unit if present
     */
    @ForReadability
    private Optional<Unit> getNewUnit() {
        if (newFahrenheit.isChecked()) {
            return Optional.of(Unit.FAHRENHEIT);
        } else if (newCelsius.isChecked()) {
            return Optional.of(Unit.CELSIUS);
        } else if (newKelvin.isChecked()) {
            return Optional.of(Unit.KELVIN);
        } else {
            return Optional.empty();
        }
    }

    /** Clears the value field. */
    @ForReadability
    private void clearFieldInput() {
        startingValueField.setText("");
    }

    /** Resets the temperature widget state. */
    @ForReadability
    private void reset() {
        temperatureFrame.revokeAllNotifications();

        clearFieldInput();

        oldFahrenheit.setChecked();
        oldCelsius.setNotChecked();
        oldKelvin.setNotChecked();

        newFahrenheit.setChecked();
        newCelsius.setNotChecked();
        newKelvin.setNotChecked();
    }
}
