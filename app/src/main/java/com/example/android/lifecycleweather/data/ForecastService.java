package com.example.android.lifecycleweather.data;

import com.example.android.lifecycleweather.data.FiveDayForecast;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

//"https://api.openweathermap.org/data/2.5/forecast"
public interface ForecastService {

    @GET("/data/2.5/forecast")
    Call<FiveDayForecast> searchForecast(
            @Query("q") String location,
            @Query("units") String units,
            @Query("appid") String key);
}
