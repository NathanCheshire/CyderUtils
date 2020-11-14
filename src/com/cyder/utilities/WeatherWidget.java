package com.cyder.utilities;

import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderFrame;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class WeatherWidget {
    private JLabel locationLabel;
    private JFrame changeLocationFrame;
    private JLabel currentWeatherLabel;
    private JLabel changeLocationLabel;
    private JLabel temperatureLabel;
    private JLabel feelsLikeLabel;
    private JLabel windSpeedLabel;
    private JLabel windDirectionLabel;
    private JLabel humidityLabel;
    private JLabel pressureLabel;
    private JLabel sunsetLabel;
    private JLabel sunriseLabel;
    private JLabel timezoneLabel;

    private String sunrise;
    private String sunset;
    private String weatherIcon;
    private String weatherCondition;
    private String windSpeed;
    private String visibility;
    private String temperature;
    private String humidity;
    private String pressure;
    private String feelsLike;
    private String windBearing;

    private String locationString;

    private String userCity;
    private String userState;
    private String userStateAbr;
    private String isp;
    private String lat;
    private String lon;
    private String userCountry;
    private String userCountryAbr;
    private String userIP;
    private String userPostalCode;
    private String userFlagURL;
    private String gmtOffset;

    private JButton closeWeather;
    private JButton minimizeWeather;
    private JLabel currentTimeLabel;

    private CyderFrame weatherFrame;

    private boolean updateWeather;
    private boolean updateClock;
    private boolean useCustomLoc;

    private int timeOffset;
    private int currentLocationGMTOffset;
    private boolean GMTset;

    private Util weatherUtil = new Util();

    public WeatherWidget() {
        weatherStats();

        if (weatherFrame != null)
            weatherUtil.closeAnimation(weatherFrame);

        weatherFrame = new CyderFrame(500,608,new ImageIcon("src\\com\\cyder\\io\\pictures\\Weather.png"));
        weatherFrame.setTitlePosition(CyderFrame.CENTER_TITLE);
        weatherFrame.setTitle("Weather");

        currentTimeLabel = new JLabel();

        currentTimeLabel.setForeground(weatherUtil.vanila);

        currentTimeLabel.setFont(weatherUtil.weatherFontSmall);

        currentTimeLabel.setBounds(16, 50, 600, 30);

        currentTimeLabel.setText(weatherTime());

        weatherFrame.getContentPane().add(currentTimeLabel, SwingConstants.CENTER);

        locationLabel = new JLabel();

        locationLabel.setForeground(weatherUtil.vanila);

        locationLabel.setFont(weatherUtil.weatherFontSmall);

        locationLabel.setBounds(16, 85, 480, 30);

        locationLabel.setText(locationString);

        weatherFrame.getContentPane().add(locationLabel, SwingConstants.CENTER);

        JLabel currentWeatherIconLabel = new JLabel(new ImageIcon("src\\com\\cyder\\io\\pictures\\" + weatherIcon + ".png"));

        currentWeatherIconLabel.setBounds(16, 125, 100, 100);

        currentWeatherIconLabel.setBorder(new LineBorder(weatherUtil.navy,5,false));

        weatherFrame.getContentPane().add(currentWeatherIconLabel);

        currentWeatherLabel = new JLabel();

        currentWeatherLabel.setForeground(weatherUtil.vanila);

        currentWeatherLabel.setFont(weatherUtil.weatherFontSmall);

        currentWeatherLabel.setBounds(16, 255, 400, 30);

        currentWeatherLabel.setText(capsFirst(weatherCondition));

        weatherFrame.getContentPane().add(currentWeatherLabel);

        changeLocationLabel = new JLabel("Change Location");

        changeLocationLabel.setFont(weatherUtil.weatherFontSmall);

        changeLocationLabel.setForeground(weatherUtil.vanila);

        changeLocationLabel.setBounds(165, 220,200,30);

        changeLocationLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                changeLocationFrame = new JFrame();

                changeLocationFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                changeLocationFrame.setTitle("Change Location");

                changeLocationFrame.setIconImage(weatherUtil.getCyderIcon().getImage());

                JPanel parent = new JPanel();

                parent.setLayout(new BoxLayout(parent, BoxLayout.Y_AXIS));

                parent.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

                JLabel explenation = new JLabel("<html>Enter your city, state, and country code separated by a comma<br/>" +
                                                    "Example: \"New Orleans,LA,US\"<br/>If you don't have a state, don't worry about" +
                                                    " it, I'll figure it out.</html>");

                explenation.setFont(weatherUtil.weatherFontSmall);

                explenation.setForeground(weatherUtil.navy);

                JPanel a = new JPanel();

                a.add(explenation, SwingConstants.CENTER);

                parent.add(a);

                JTextField changeLocField = new JTextField(20);

                changeLocField.setBorder(new LineBorder(weatherUtil.navy,5,false));

                changeLocField.setForeground(weatherUtil.navy);

                changeLocField.setFont(weatherUtil.weatherFontSmall);

                CyderButton changeLoc = new CyderButton("Change Location");

                changeLoc.setBorder(new LineBorder(weatherUtil.navy,5,false));

                changeLocField.addActionListener(e1 -> changeLoc.doClick());

                JPanel b = new JPanel();

                b.add(changeLocField, SwingConstants.CENTER);

                parent.add(b);

                changeLoc.setFont(weatherUtil.weatherFontSmall);

                changeLoc.setForeground(weatherUtil.navy);

                changeLoc.setColors(weatherUtil.regularRed);

                changeLoc.setBackground(weatherUtil.regularRed);

                changeLoc.addActionListener(e12 -> {
                    try {
                        locationString = changeLocField.getText();

                        useCustomLoc = true;

                        weatherUtil.closeAnimation(changeLocationFrame);
                        weatherUtil.inform("Attempting to refresh and use the location \"" + locationString + "\" for weather.", "",400, 300);
                        refreshWeatherNow();
                    }

                    catch (Exception ex) {
                        weatherUtil.handle(ex);
                    }
                });

                JPanel c = new JPanel();

                c.add(changeLoc, SwingConstants.CENTER);

                parent.add(c);

                changeLocationFrame.add(parent);

                changeLocationFrame.setVisible(true);

                changeLocationFrame.pack();

                changeLocationFrame.setLocationRelativeTo(null);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                changeLocationLabel.setForeground(weatherUtil.navy);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                changeLocationLabel.setForeground(weatherUtil.vanila);
            }
        });

        weatherFrame.getContentPane().add(changeLocationLabel);

        temperatureLabel = new JLabel();

        temperatureLabel.setForeground(weatherUtil.vanila);

        temperatureLabel.setFont(weatherUtil.weatherFontSmall);

        temperatureLabel.setBounds(16, 300, 300, 30);

        temperatureLabel.setText("Temperature: " + temperature + "F");

        weatherFrame.getContentPane().add(temperatureLabel);

        feelsLikeLabel = new JLabel();

        feelsLikeLabel.setForeground(weatherUtil.vanila);

        feelsLikeLabel.setFont(weatherUtil.weatherFontSmall);

        feelsLikeLabel.setBounds(16, 345, 200, 30);

        feelsLikeLabel.setText("Feels like: " + feelsLike + "F");

        weatherFrame.getContentPane().add(feelsLikeLabel);

        windSpeedLabel = new JLabel();

        windSpeedLabel.setForeground(weatherUtil.vanila);

        windSpeedLabel.setFont(weatherUtil.weatherFontSmall);

        windSpeedLabel.setBounds(16, 390, 300, 30);

        windSpeedLabel.setText("Wind Speed: " + windSpeed + "mph");

        weatherFrame.getContentPane().add(windSpeedLabel);

        windDirectionLabel = new JLabel();

        windDirectionLabel.setForeground(weatherUtil.vanila);

        windDirectionLabel.setFont(weatherUtil.weatherFontSmall);

        windDirectionLabel.setBounds(16, 430, 400, 30);

        windDirectionLabel.setText("Wind Direction: " + windBearing + " Deg, " + weatherUtil.getWindDirection(windBearing));

        weatherFrame.getContentPane().add(windDirectionLabel);

        humidityLabel = new JLabel();

        humidityLabel.setForeground(weatherUtil.vanila);

        humidityLabel.setFont(weatherUtil.weatherFontSmall);

        humidityLabel.setBounds(16, 470, 300, 30);

        humidityLabel.setText("Humidity: " + humidity + "%");

        weatherFrame.getContentPane().add(humidityLabel, SwingConstants.CENTER);

        pressureLabel = new JLabel();

        pressureLabel.setForeground(weatherUtil.vanila);

        pressureLabel.setFont(weatherUtil.weatherFontSmall);

        pressureLabel.setBounds(16, 510, 300, 30);

        pressureLabel.setText("Pressure: " + Double.parseDouble(pressure) / 1000 + "atm");

        weatherFrame.getContentPane().add(pressureLabel, SwingConstants.CENTER);

        timezoneLabel = new JLabel();

        timezoneLabel.setForeground(weatherUtil.vanila);

        timezoneLabel.setFont(weatherUtil.weatherFontSmall);

        timezoneLabel.setBounds(16, 550, 400, 30);

        timezoneLabel.setText("Timezone stats: " + getTimezoneLabel());

        weatherFrame.getContentPane().add(timezoneLabel, SwingConstants.CENTER);

        sunriseLabel = new JLabel();

        sunriseLabel.setForeground(weatherUtil.vanila);

        sunriseLabel.setFont(weatherUtil.weatherFontSmall);

        sunriseLabel.setBounds(150, 187, 125, 30);

        sunriseLabel.setText(sunrise + "am");

        weatherFrame.getContentPane().add(sunriseLabel, SwingConstants.CENTER);

        sunsetLabel = new JLabel();

        sunsetLabel.setForeground(weatherUtil.vanila);

        sunsetLabel.setFont(weatherUtil.weatherFontSmall);

        sunsetLabel.setBounds(275, 189, 120, 30);

        sunsetLabel.setText(sunset + "pm");

        weatherFrame.getContentPane().add(sunsetLabel, SwingConstants.CENTER);

        weatherFrame.setVisible(true);

        weatherFrame.setLocationRelativeTo(null);

        weatherFrame.setIconImage(weatherUtil.getCyderIcon().getImage());

        updateClock = true;
        refreshClock();
        updateWeather = true;
        refreshWeather();
    }

    private void refreshClock() {
        Thread TimeThread = new Thread(() -> {
            try {
                while (updateClock) {
                    Thread.sleep(1000);
                    currentTimeLabel.setText(weatherTime());
                }
            } catch (Exception e) {
                weatherUtil.handle(e);
            }
        });

        TimeThread.start();
    }

    public String weatherTime() {
        Calendar cal = Calendar.getInstance();
        timeOffset = Integer.parseInt(gmtOffset) / 3600;
        cal.add(Calendar.HOUR, timeOffset);
        Date Time = cal.getTime();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm:ss aa EEEEEEEEEEEEE MMMMMMMMMMMMMMMMMM dd, yyyy");
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormatter.format(Time);
    }

    private void refreshWeather() {
        Thread WeatherThread = new Thread(() -> {
            try {
                while (updateWeather) {
                    Thread.sleep(1800000);

                    weatherStats();
                    locationLabel.setText(locationString);

                    currentWeatherLabel.setText(capsFirst(weatherCondition));
                    temperatureLabel.setText("Temperature: " + temperature + "F");
                    feelsLikeLabel.setText("Feels like: " + feelsLike);
                    windSpeedLabel.setText("Wind Speed: " + windSpeed + "mph");
                    windDirectionLabel.setText("Wind Direction: " + windBearing + " Deg, " + weatherUtil.getWindDirection(windBearing));
                    humidityLabel.setText("Humidity: " + humidity + "%");
                    pressureLabel.setText("Pressure: " + Double.parseDouble(pressure) / 1000 + "atm");
                    timezoneLabel.setText("Timezone stats: " + getTimezoneLabel());
                    sunriseLabel.setText(correctedSunTime(sunrise) + "am");
                    sunsetLabel.setText(correctedSunTime(sunset) + "pm");
                }
            } catch (Exception e) {
                weatherUtil.handle(e);
            }
        });

        WeatherThread.start();
    }

    private void refreshWeatherNow() {
        try {
            weatherStats();
            locationLabel.setText(locationString);

            currentWeatherLabel.setText(capsFirst(weatherCondition));
            temperatureLabel.setText("Temperature: " + temperature + "F");
            feelsLikeLabel.setText("Feels like: " + feelsLike);
            windSpeedLabel.setText("Wind Speed: " + windSpeed + "mph");
            windDirectionLabel.setText("Wind Direction: " + windBearing + " Deg, " + weatherUtil.getWindDirection(windBearing));
            humidityLabel.setText("Humidity: " + humidity + "%");
            pressureLabel.setText("Pressure: " + Double.parseDouble(pressure) / 1000 + "atm");
            timezoneLabel.setText("Timezone stats: " + getTimezoneLabel());
            sunriseLabel.setText(correctedSunTime(sunrise) + "am");
            sunsetLabel.setText(correctedSunTime(sunset) + "pm");
        }

        catch (Exception e) {
            weatherUtil.handle(e);
        }
    }

    private String getTimezoneLabel() {
        TimeZone tz = TimeZone.getDefault();

        int offset = Integer.parseInt(gmtOffset) * 1000;
        String[] availableIDs = tz.getAvailableIDs(offset);

        String one = "";
        String two = "";

        for (String availableID : availableIDs) {
            if (availableID.length() == 3) {
                one = availableID;
            }

            else if (availableID.contains("GMT"))
                two = availableID;
        }

        return one + "," + two;
    }

    private String correctedSunTime(String absoluteTime) {
        int hour = Integer.parseInt(absoluteTime.split(":")[0]);
        int minute = Integer.parseInt(absoluteTime.split(":")[1]);

        hour += (Integer.parseInt(gmtOffset) / 3600 - (currentLocationGMTOffset / 60 / 60));

        return hour + ":" + minute;
    }

    protected void weatherStats() {
        try {
            getIPData();

            if (!useCustomLoc)
                locationString = userCity + "," + userState + "," + userCountry;

            String OpenString = "";

            OpenString = "https://api.openweathermap.org/data/2.5/weather?q=" +
                        locationString + "&appid=" + weatherUtil.getWeatherKey() + "&units=imperial";

            URL URL = new URL(OpenString);
            BufferedReader WeatherReader = new BufferedReader(new InputStreamReader(URL.openStream()));
            String[] Fields = {"", ""};
            String Line;

            while ((Line = WeatherReader.readLine()) != null) {
                String[] LineArray = Line.replace("{", "").replace("}", "")
                        .replace(":", "").replace("\"", "").replace("[", "")
                        .replace("]", "").replace(":", "").split(",");

                Fields = weatherUtil.combineArrays(Fields, LineArray);
            }

            WeatherReader.close();

            for (String field : Fields) {
                if (field.contains("sunrise")) {
                    sunrise = field.replaceAll("[^\\d.]", "");
                }
                else if (field.contains("sunset")) {
                    sunset = field.replaceAll("[^\\d.]", "");
                }
                else if (field.contains("icon")) {
                    weatherIcon = field.replace("icon", "");
                }
                else if (field.contains("speed")) {
                    windSpeed = field.replaceAll("[^\\d.]", "");
                }
                else if (field.contains("deg")) {
                    windBearing = field.replaceAll("[^\\d.]", "");
                }
                else if (field.contains("description")) {
                    weatherCondition = field.replace("description", "");
                }
                else if (field.contains("visibility")) {
                    visibility = field.replaceAll("[^\\d.]", "");
                }
                else if (field.contains("feels_like")) {
                    feelsLike = field.replaceAll("[^\\d.]", "");
                }
                else if (field.contains("pressure")) {
                    pressure = field.replaceAll("[^\\d.]", "");
                    pressure = pressure.substring(0, Math.min(pressure.length(), 4));
                }
                else if (field.contains("humidity")) {
                    humidity = field.replaceAll("[^\\d.]", "");
                }
                else if (field.contains("temp")) {
                    temperature = field.replaceAll("[^\\d.]", "");
                }
                else if (field.contains("timezone")) {
                    gmtOffset = field.replaceAll("[^0-9\\-]", "");
                }
            }

            SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm");

            Date SunriseTime = new Date((long) Integer.parseInt(sunrise) * 1000);
            sunrise = dateFormatter.format(SunriseTime);

            Date SunsetTime = new Date((long) Integer.parseInt(sunset) * 1000);
            sunset = dateFormatter.format(SunsetTime);

            if (!GMTset) {
                currentLocationGMTOffset = Integer.parseInt(gmtOffset);
                GMTset = true;
            }

            Date Time = new Date();

            if (Time.getTime() > SunsetTime.getTime()) {
                weatherIcon = weatherIcon.replace("d", "n");
            }
        }

        catch (Exception e) {
            weatherUtil.handle(e);
        }
    }

    public String getWindDirection(String wb) {
        double bear = Double.parseDouble(wb);

        if (bear > 360)
            bear -= 360;

        String ret = "";

        if (bear > 270 || bear < 90)
            ret += "N";
        else if (bear == 270)
            return "W";
        else if (bear == 90)
            return "E";
        else
            ret += "S";

        if (bear > 0 && bear < 180)
            ret += "E";
        else if (bear == 180)
            return "S";
        else if (bear == 0 || bear == 360)
            return "N";
        else
            ret += "W";

        return ret;
    }

    public void getIPData() {
        try {
            String Key = weatherUtil.getIPKey();
            String url = "https://api.ipdata.co/?api-key=" + Key;

            URL Querry = new URL(url);

            BufferedReader BR = new BufferedReader(new InputStreamReader(Querry.openStream()));

            String CurrentLine;

            while ((CurrentLine = BR.readLine()) != null) {
                if (CurrentLine.contains("city")) {
                    userCity = (CurrentLine.replace("city", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"region\"")) {
                    userState = (CurrentLine.replace("region", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"region_code\"")) {
                    userStateAbr = (CurrentLine.replace("region_code", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("asn")) {
                    CurrentLine = BR.readLine();
                    CurrentLine = BR.readLine();
                    isp = (CurrentLine.replace("name", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"country_name\"")) {
                    userCountry = (CurrentLine.replace("country_name", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"country_code\"")) {
                    userCountryAbr = (CurrentLine.replace("country_code", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"latitude\"")) {
                    lat = (CurrentLine.replace("latitude", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"longitude\"")) {
                    lon = (CurrentLine.replace("longitude", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"ip\"")) {
                    userIP = (CurrentLine.replace("ip", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"flag\"")) {
                    userFlagURL = (CurrentLine.replace("\"flag\"", "").replace("\"","").replace(",", "").trim()).replaceFirst(":","");
                }

                else if (CurrentLine.contains("postal")) {
                    userPostalCode = (CurrentLine.replace("\"postal\"", "").replace("\"","").replace(",", "").replace(":", "").trim());
                }
            }
            BR.close();
        } catch (Exception e) {
            weatherUtil.handle(e);
        }
    }

    private String capsFirst(String Word) {
        StringBuilder SB = new StringBuilder(Word.length());
        String[] Words = Word.split(" ");

        for (String word : Words) {
            SB.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }

        return SB.toString();
    }
}
