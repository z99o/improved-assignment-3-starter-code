package com.example.android.lifecycleweather.utils;

import android.net.Uri;

import com.example.android.lifecycleweather.data.FiveDayForecast;
import com.example.android.lifecycleweather.data.ForecastData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class OpenWeatherUtils {
    private static final String FIVE_DAY_FORECAST_BASE_URL = "https://api.openweathermap.org/data/2.5/forecast";
    private static final String FIVE_DAY_FORECAST_CITY_PARAM = "q";
    private static final String FIVE_DAY_FORECAST_UNITS_PARAM = "units";
    private static final String FIVE_DAY_FORECAST_APPID_PARAM = "appid";

    private final static String FIVE_DAY_FORECAST_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final static String FIVE_DAY_FORECAST_TIMEZONE = "UTC";
    private final static String TIMEZONE_OFFSET_FORMAT_STR = "GMT%0+3d:%02d";

    private final static String FIVE_DAY_FORECAST_ICON_URL_FORMAT_STR = "https://openweathermap.org/img/wn/%s@4x.png";

    /**
     * Builds a URL to query the OpenWeather API to fetch the 5 day/3 hour forecast for a specified
     * city.
     *
     * @param city The name of the city for which to fetch the forecast, e.g. "Corvallis,OR,US".
     * @param units String specifying the type of units of measurement to fetch.  Can be
     *              "imperial", "metric", or "standard".
     * @param appId The OpenWeather API key to use to fetch the forecast.
     *
     * @return Returns a String version of the OpenWeather API URL constructed using the provided
     * parameters.
     */
    public static String buildFiveDayForecastUrl(String city, String units, String appId) {
        return Uri.parse(FIVE_DAY_FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(FIVE_DAY_FORECAST_CITY_PARAM, city)
                .appendQueryParameter(FIVE_DAY_FORECAST_UNITS_PARAM, units)
                .appendQueryParameter(FIVE_DAY_FORECAST_APPID_PARAM, appId)
                .build()
                .toString();
    }

    /**
     * Parses the JSON response from the OpenWeather API's 5 day/3 hour forecast API.
     *
     * @param dailyForecastJson The raw JSON response from the OpenWeather 5 day/3 hour API.
     *
     * @return Returns a FiveDayForecast object representing the fetched forecast.
     */
    public static FiveDayForecast parseFiveDayForecastResponse(String dailyForecastJson) {
        Gson gson = new GsonBuilder().setDateFormat(FIVE_DAY_FORECAST_DATE_FORMAT).create();
        OpenWeatherForecastResponse forecast = gson.fromJson(
                dailyForecastJson,
                OpenWeatherForecastResponse.class
        );
        ArrayList<ForecastData> forecastDataList = new ArrayList<>();
        for (OpenWeatherForecastListItem item : forecast.forecastList) {
            forecastDataList.add(item.toForecastData(forecast.city.timezoneOffsetTotalSec));
        }
        return new FiveDayForecast(forecastDataList, forecast.city.name, forecast.city.coord.lat,
                forecast.city.coord.lon);
    }

    /**********************************************************************************************
     **
     ** The classes below are used in conjunction with Gson to parse the OpenWeather API's JSON
     ** response from the 5 day/3 hour forecast API.  Don't modify these classes unless you want
     ** to change the data being used from the OpenWeather API.
     **
     **********************************************************************************************/

    /**
     * This class represents the top-level response from the OpenWeather 5 day/3 hour forecast API.
     */
    private static class OpenWeatherForecastResponse {
        @SerializedName("list")
        public ArrayList<OpenWeatherForecastListItem> forecastList;

        public OpenWeatherForecastCity city;
    }

    /**
     * This class represents the `city` field in the response from the OpenWeather 5 day/3 hour
     * forecast API.
     */
    private static class OpenWeatherForecastCity {
        public String name;
        public OpenWeatherForecastCityCoords coord;

        @SerializedName("timezone")
        public int timezoneOffsetTotalSec;
    }

    /**
     * This class represents the `city.coord` field in the response from the OpenWeather
     * 5 day/3 hour forecast API.
     */
    private static class OpenWeatherForecastCityCoords {
        public double lat;
        public double lon;
    }

    /**
     * This class represents one item in the the `list` field in the response from the OpenWeather
     * 5 day/3 hour forecast API.
     */
    private static class OpenWeatherForecastListItem {
        public OpenWeatherForecastListItemMain main;
        public ArrayList<OpenWeatherForecastListItemWeather> weather;
        public OpenWeatherForecastListItemClouds clouds;
        public OpenWeatherForecastListItemWind wind;
        public double pop;

        @SerializedName("dt")
        public long timestamp;

        /**
         * This method is used to generate a `ForecastData` object from this
         * `OpenWeatherForecastListItem` object.
         *
         * @param timezoneOffsetTotalSec The timezone offset in seconds returned by the OpenWeather
         *                               API.
         */
        public ForecastData toForecastData(int timezoneOffsetTotalSec) {
            /*
             * Convert timezone seconds offset to GMT+/-hours:minutes offset string and convert
             * forecast time to correct timezone.
             */
            Calendar date = Calendar.getInstance(TimeZone.getTimeZone(FIVE_DAY_FORECAST_TIMEZONE));
            date.setTimeInMillis(this.timestamp * 1000L);
            int timezoneOffsetHours = timezoneOffsetTotalSec / 3600;
            int timezoneOffsetMin = (Math.abs(timezoneOffsetTotalSec) % 3600) / 60;
            String localTimezoneId = String.format(TIMEZONE_OFFSET_FORMAT_STR, timezoneOffsetHours,
                    timezoneOffsetMin);
            date.setTimeZone(TimeZone.getTimeZone(localTimezoneId));

            OpenWeatherForecastListItemWeather weatherItem = this.weather.get(0);

            return new ForecastData(
                    date,
                    (int)Math.round(this.main.highTemp),
                    (int)Math.round(this.main.lowTemp),
                    (int)Math.round(this.pop * 100),
                    this.clouds.coveragePercent,
                    (int)Math.round(this.wind.speed),
                    this.wind.deg,
                    weatherItem.description,
                    String.format(FIVE_DAY_FORECAST_ICON_URL_FORMAT_STR, weatherItem.icon)
            );
        }
    }

    /**
     * This class represents the `list.main` field in the response from the OpenWeather
     * 5 day/3 hour forecast API.
     */
    private static class OpenWeatherForecastListItemMain {
        @SerializedName("temp_min")
        public double lowTemp;

        @SerializedName("temp_max")
        public double highTemp;
    }

    /**
     * This class represents one item in the the `list.weather` field in the response from the
     * OpenWeather 5 day/3 hour forecast API.
     */
    private static class OpenWeatherForecastListItemWeather {
        public String description;
        public String icon;
    }

    /**
     * This class represents the `list.clouds` field in the response from the OpenWeather
     * 5 day/3 hour forecast API.
     */
    private static class OpenWeatherForecastListItemClouds {
        @SerializedName("all")
        public int coveragePercent;
    }

    /**
     * This class represents the `list.wind` field in the response from the OpenWeather
     * 5 day/3 hour forecast API.
     */
    private static class OpenWeatherForecastListItemWind {
        public double speed;
        public int deg;
    }
}
