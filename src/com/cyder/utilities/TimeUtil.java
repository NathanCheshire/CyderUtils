package com.cyder.utilities;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimeUtil {

    private GeneralUtil timeGeneralUtil;
    private int gmtOffset;
    private JFrame consoleFrame;
    private InternetProtocolUtil InternetProtocolUtil;

    public TimeUtil() {
        timeGeneralUtil = new GeneralUtil();
        InternetProtocolUtil = new InternetProtocolUtil();

        initGMTOffset();
    }

    public String formatDate(LocalDateTime now, DateTimeFormatter dtf) {
        return now.format(dtf);
    }

    public String formatCurrentDate(DateTimeFormatter dtf) {
        return dtf.format(LocalDate.now());
    }

    public LinkedList<String> getTimezoneIDs() {
        TimeZone tz = TimeZone.getDefault();

        int offset = gmtOffset * 1000;
        String[] availableIDs = tz.getAvailableIDs(offset);

        LinkedList<String> timezoneIDs = new LinkedList<>();

        Collections.addAll(timezoneIDs, availableIDs);

        return timezoneIDs;
    }

    //returns gmt offset for current loc in seconds
    public int getGMTOffsetSeconds() {
        return gmtOffset;
    }

    //returns gmt offset for current loc, so slidell is -6, miami is -5
    public int getGMTOffsetHours() {
        return gmtOffset / 60 / 60;
    }

    public String weatherTime() {
        Calendar cal = Calendar.getInstance();
        Date Time = cal.getTime();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm:ss aa EEEEEEEEEEEEE MMMMMMMMMMMMMMMMMM dd, yyyy");
        return dateFormatter.format(Time);
    }

    private void initGMTOffset() {
        try {
            String OpenString = "https://api.openweathermap.org/data/2.5/weather?q=" +
                    InternetProtocolUtil.getUserCity() + "," + InternetProtocolUtil.getUserState() + "," +
                    InternetProtocolUtil.getUserCountry() + "&appid=" + timeGeneralUtil.getWeatherKey() + "&units=imperial";

            URL URL = new URL(OpenString);
            BufferedReader WeatherReader = new BufferedReader(new InputStreamReader(URL.openStream()));
            String[] Fields = {"", ""};
            String Line;

            while ((Line = WeatherReader.readLine()) != null) {
                String[] LineArray = Line.replace("{", "").replace("}", "")
                        .replace(":", "").replace("\"", "").replace("[", "")
                        .replace("]", "").replace(":", "").split(",");

                Fields = new StringUtil().combineArrays(Fields, LineArray);
            }

            WeatherReader.close();

            for (String field : Fields) {
                if (field.contains("timezone")) {
                    gmtOffset = Integer.parseInt(field.replaceAll("[^0-9\\-]", ""));
                }
            }
        }

        catch (Exception e) {
            timeGeneralUtil.handle(e);
        }
    }

    public void closeAtHourMinute(int Hour, int Minute, JFrame consoleFrame) {
        Calendar CloseCalendar = Calendar.getInstance();

        this.consoleFrame = consoleFrame;

        CloseCalendar.add(Calendar.DAY_OF_MONTH, 0);
        CloseCalendar.set(Calendar.HOUR_OF_DAY, Hour);
        CloseCalendar.set(Calendar.MINUTE, Minute);
        CloseCalendar.set(Calendar.SECOND, 0);
        CloseCalendar.set(Calendar.MILLISECOND, 0);

        long HowMany = (CloseCalendar.getTimeInMillis() - System.currentTimeMillis());
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.schedule(this::exit,HowMany, TimeUnit.MILLISECONDS);
    }

    private void exit() {
        new FrameAnimations().closeAnimation(consoleFrame);
        System.exit(0);
    }
}
