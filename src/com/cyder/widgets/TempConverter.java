package com.cyder.widgets;

import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderFrame;
import com.cyder.utilities.GeneralUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.text.DecimalFormat;

public class TempConverter {
    private CyderFrame temperatureFrame;
    private JTextField startingValue;
    private JRadioButton oldFahrenheit;
    private JRadioButton newFahrenheit;
    private JRadioButton oldCelsius;
    private JRadioButton newCelsius;
    private JRadioButton oldKelvin;
    private JRadioButton newKelvin;
    private ButtonGroup radioNewValueGroup;
    private ButtonGroup radioCurrentValueGroup;

    private GeneralUtil tempGeneralUtil = new GeneralUtil();

    public TempConverter() {
        if (temperatureFrame != null)
            temperatureFrame.closeAnimation();

        temperatureFrame = new CyderFrame(600,320,new ImageIcon("src/com/cyder/io/pictures/DebugBackground.png"));
        temperatureFrame.setTitle("Temperature Converter");

        JLabel ValueLabel = new JLabel("Measurement: ");
        ValueLabel.setFont(tempGeneralUtil.weatherFontSmall);
        startingValue = new JTextField(20);
        startingValue.setBorder(new LineBorder(tempGeneralUtil.navy,5,false));
        startingValue.setForeground(tempGeneralUtil.navy);
        startingValue.setSelectionColor(tempGeneralUtil.selectionColor);
        startingValue.setFont(tempGeneralUtil.weatherFontSmall);
        ValueLabel.setBounds(60,40, 200, 30);
        temperatureFrame.getContentPane().add(ValueLabel);
        startingValue.setBounds(240,40, 300, 35);
        temperatureFrame.getContentPane().add(startingValue);

        oldFahrenheit =  new JRadioButton("Fahrenheit");
        oldCelsius =  new JRadioButton("Celsius");
        oldKelvin = new JRadioButton("Kelvin");
        oldFahrenheit.setFont(tempGeneralUtil.weatherFontBig);
        oldCelsius.setFont(tempGeneralUtil.weatherFontBig);
        oldKelvin.setFont(tempGeneralUtil.weatherFontBig);
        radioCurrentValueGroup = new ButtonGroup();
        radioCurrentValueGroup.add(oldFahrenheit);
        radioCurrentValueGroup.add(oldCelsius);
        radioCurrentValueGroup.add(oldKelvin);
        oldFahrenheit.setBounds(80,100,300,30);
        oldCelsius.setBounds(80,150,200,30);
        oldKelvin.setBounds(80,200,200,30);
        oldFahrenheit.setOpaque(false);
        oldCelsius.setOpaque(false);
        oldKelvin.setOpaque(false);
        oldFahrenheit.setFocusPainted(false);
        oldCelsius.setFocusPainted(false);
        oldKelvin.setFocusPainted(false);
        temperatureFrame.getContentPane().add(oldFahrenheit);
        temperatureFrame.getContentPane().add(oldCelsius);
        temperatureFrame.getContentPane().add(oldKelvin);

        JLabel NewValue = new JLabel("-2-");
        NewValue.setFont(tempGeneralUtil.weatherFontBig.deriveFont(60f));
        NewValue.setBounds(260,150,150,60);
        temperatureFrame.getContentPane().add(NewValue);

        newFahrenheit =  new JRadioButton("Fahrenheit");
        newCelsius =  new JRadioButton("Celsius");
        newKelvin = new JRadioButton("Kelvin");
        newFahrenheit.setFont(tempGeneralUtil.weatherFontBig);
        newCelsius.setFont(tempGeneralUtil.weatherFontBig);
        newKelvin.setFont(tempGeneralUtil.weatherFontBig);
        radioNewValueGroup = new ButtonGroup();
        radioNewValueGroup.add(newFahrenheit);
        radioNewValueGroup.add(newCelsius);
        radioNewValueGroup.add(newKelvin);
        newFahrenheit.setBounds(370,100,300,30);
        newCelsius.setBounds(370,150,200,30);
        newKelvin.setBounds(370,200,200,30);
        newFahrenheit.setOpaque(false);
        newCelsius.setOpaque(false);
        newKelvin.setOpaque(false);
        newFahrenheit.setFocusPainted(false);
        newCelsius.setFocusPainted(false);
        newKelvin.setFocusPainted(false);
        temperatureFrame.getContentPane().add(newFahrenheit);
        temperatureFrame.getContentPane().add(newCelsius);
        temperatureFrame.getContentPane().add(newKelvin);

        CyderButton calculate = new CyderButton("Calculate");
        calculate.setBorder(new LineBorder(tempGeneralUtil.navy,5,false));

        calculate.addActionListener(e -> {
            try {
                DecimalFormat tempFormat = new DecimalFormat(".####");
                double CalculationValue = Double.parseDouble(startingValue.getText());

                if (oldKelvin.isSelected() && CalculationValue <= 0) {
                    temperatureFrame.inform("Temperatures below absolute zero are imposible.","", 400, 200);
                }

                else {
                    if (oldFahrenheit.isSelected()) {
                        if (newFahrenheit.isSelected())
                            temperatureFrame.inform("Get out of here with that. Your value is already in Fahrenheit.","", 400, 200);

                        else if (newCelsius.isSelected()) {
                            double CelsiusFromFahrenheit;

                            CelsiusFromFahrenheit = (CalculationValue - 32.0) / 1.8;

                            temperatureFrame.inform( CalculationValue + " Fahrenheit converted to Celsius equals: "
                                    + tempFormat.format(CelsiusFromFahrenheit),"", 400, 200);

                            startingValue.setText("");

                            radioCurrentValueGroup.clearSelection();

                            radioNewValueGroup.clearSelection();
                        }

                        else if (newKelvin.isSelected()) {
                            double KelvinFromFahrenheit;
                            KelvinFromFahrenheit = (CalculationValue +459.67) * 5/9;

                            if (KelvinFromFahrenheit >= 0) {
                                temperatureFrame.inform(CalculationValue + " Fahrenheit converted to Kelvin equals: "
                                        + tempFormat.format(KelvinFromFahrenheit),"", 400, 200);

                                startingValue.setText("");

                                radioCurrentValueGroup.clearSelection();

                                radioNewValueGroup.clearSelection();
                            }

                            else
                                temperatureFrame.inform("Temperatures below absolute zero are imposible.","", 400, 200);
                        }
                    }

                    else if (oldCelsius.isSelected()) {
                        if (newFahrenheit.isSelected()) {
                            double FahrenheitFromCelsius;

                            FahrenheitFromCelsius = (CalculationValue *1.8) + 32;

                            temperatureFrame.inform(CalculationValue + " Celsius converted to Fahrenheit equals: "
                                    + tempFormat.format(FahrenheitFromCelsius),"", 400, 200);

                            startingValue.setText("");

                            radioCurrentValueGroup.clearSelection();

                            radioNewValueGroup.clearSelection();
                        }

                        else if (newCelsius.isSelected())
                            temperatureFrame.inform("Get out of here with that. Your value is already in Celsius.","", 400, 200);

                        else if (newKelvin.isSelected()) {
                            double KelvinFromCelsius;

                            KelvinFromCelsius = CalculationValue + 273.15 ;

                            if (KelvinFromCelsius >= 0) {
                                temperatureFrame.inform(CalculationValue + " Celsius converted to Kelvin equals: "
                                        + tempFormat.format(KelvinFromCelsius),"", 400, 200);

                                startingValue.setText("");

                                radioCurrentValueGroup.clearSelection();

                                radioNewValueGroup.clearSelection();
                            }

                            else
                                temperatureFrame.inform("Temperatures below absolute zero are imposible.","", 400, 200);
                        }
                    }

                    else if (oldKelvin.isSelected()) {
                        if (newFahrenheit.isSelected()) {
                            double FahrenheitFromKelvin;

                            FahrenheitFromKelvin = CalculationValue * 1.8 - 459.67;

                            temperatureFrame.inform(CalculationValue + " Kelvin converted to Fahrenheit equals: "
                                    + tempFormat.format(FahrenheitFromKelvin),"", 400, 200);

                            startingValue.setText("");

                            radioCurrentValueGroup.clearSelection();

                            radioNewValueGroup.clearSelection();
                        }

                        else if (newCelsius.isSelected()) {
                            double CelsiusFromKelvin;

                            CelsiusFromKelvin = CalculationValue - 273.15;

                            temperatureFrame.inform( CalculationValue + " Kelvin converted to Celsius equals: "
                                    + tempFormat.format(CelsiusFromKelvin),"", 400, 200);

                            startingValue.setText("");

                            radioCurrentValueGroup.clearSelection();

                            radioNewValueGroup.clearSelection();
                        }

                        else if (newKelvin.isSelected())
                            temperatureFrame.inform("Get out of here with that. Your value is already in Kelvin","", 400, 200);
                    }

                    else
                        temperatureFrame.inform("Please select your current temperature unit and the one you want to convet to.","", 400, 200);
                }
            }

            catch (Exception ex) {
                temperatureFrame.inform("Your value must only contain numbers.","", 400, 200);
            }
        });

        CyderButton resetValues = new CyderButton("Reset Values");
        resetValues.setBorder(new LineBorder(tempGeneralUtil.navy,5,false));
        resetValues.setColors(tempGeneralUtil.regularRed);
        calculate.setColors(tempGeneralUtil.regularRed);
        resetValues.addActionListener(e -> {
            startingValue.setText("");
            radioCurrentValueGroup.clearSelection();
            radioNewValueGroup.clearSelection();
        });

        calculate.setBackground(tempGeneralUtil.regularRed);
        calculate.setFont(tempGeneralUtil.weatherFontSmall);
        resetValues.setFocusPainted(false);
        resetValues.setBackground(tempGeneralUtil.regularRed);
        resetValues.setFont(tempGeneralUtil.weatherFontSmall);

        calculate.setBounds(140,260,150,40);
        resetValues.setBounds(300,260,150,40);

        temperatureFrame.getContentPane().add(calculate);
        temperatureFrame.getContentPane().add(resetValues);
        temperatureFrame.setVisible(true);
        temperatureFrame.setLocationRelativeTo(null);
    }
}
