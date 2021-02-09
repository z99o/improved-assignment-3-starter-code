package com.example.android.lifecycleweather.data;

import java.util.ArrayList;

public class FiveDayForecast {
    private ArrayList<ForecastData> forecastDataList;
    private ForecastCity forecastCity;

    public FiveDayForecast(ArrayList<ForecastData> forecastDataList, String cityName,
                           double cityLatitude, double cityLongitude) {
        this.forecastDataList = forecastDataList;
        this.forecastCity = new ForecastCity(cityName, cityLatitude, cityLongitude);
    }

    public ArrayList<ForecastData> getForecastDataList() {
        return forecastDataList;
    }

    public ForecastCity getForecastCity() {
        return forecastCity;
    }
}
