package com.example.android.lifecycleweather.data;

import java.io.Serializable;
import java.util.Calendar;

public class ForecastData implements Serializable {
    private Calendar date;
    private int highTemp;
    private int lowTemp;
    private int pop;
    private int cloudCoverage;
    private int windSpeed;
    private int windDirDeg;
    private String shortDescription;
    private String iconUrl;

    public ForecastData(Calendar date, int highTemp, int lowTemp, int pop,
                        int cloudCoverage, int windSpeed, int windDirDeg,
                        String shortDescription, String iconUrl) {
        this.date = date;
        this.highTemp = highTemp;
        this.lowTemp = lowTemp;
        this.pop = pop;
        this.cloudCoverage = cloudCoverage;
        this.windSpeed = windSpeed;
        this.windDirDeg = windDirDeg;
        this.shortDescription = shortDescription;
        this.iconUrl = iconUrl;
    }

    public Calendar getDate() {
        return date;
    }

    public int getHighTemp() {
        return highTemp;
    }

    public int getLowTemp() {
        return lowTemp;
    }

    public int getPop() {
        return pop;
    }

    public int getCloudCoverage() {
        return cloudCoverage;
    }

    public int getWindSpeed() {
        return windSpeed;
    }

    public int getWindDirDeg() {
        return windDirDeg;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getIconUrl() {
        return iconUrl;
    }
}
