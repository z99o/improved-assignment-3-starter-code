package com.example.android.lifecycleweather.data;

import android.os.Debug;
import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.android.lifecycleweather.MainActivity;
import com.example.android.lifecycleweather.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.ContentValues.TAG;

public class ForecastRepository {
    private MutableLiveData<FiveDayForecast> mSearchResults;
    private ForecastService forecastService;
    private static String Base_URL = "https://api.openweathermap.org";


    private MutableLiveData<Status> mLoadingStatus;
    private MutableLiveData<String> mlastLoadError;

    private FiveDayForecast mLastResults;
    private String mCurrentQuery;



    public ForecastRepository(){
        mSearchResults = new MutableLiveData<>();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ForecastData.class, new ForecastData.JsonDeserializer())
                .registerTypeAdapter(ForecastCity.class, new ForecastCity.JsonDeserializer())
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Base_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        this.forecastService = retrofit.create(ForecastService.class);
        mSearchResults.setValue(null);
        mLoadingStatus = new MutableLiveData<>();
        mlastLoadError = new MutableLiveData<>();
        mlastLoadError.setValue(null);
        mLoadingStatus.setValue(Status.SUCCESS);
        mCurrentQuery = null;

    }

    private boolean shouldExecuteSearch(String query) {
        return !TextUtils.equals(query, mCurrentQuery);
    }


    public LiveData<FiveDayForecast> getSearchResults(){
        return mSearchResults;
    }
    public LiveData<String> getLastError(){
        return mlastLoadError;
    }


    public void loadSearchResults(String location, String units, String key){
        Log.d(TAG, location + " vs " + mCurrentQuery);
        if (shouldExecuteSearch(location)) {
            mSearchResults.setValue(null);
            Call<FiveDayForecast> results = this.forecastService.searchForecast(location,units,key);
            mCurrentQuery = location;
            // Execute new search...
            // Execute search for query...
            mLoadingStatus.setValue(Status.LOADING);
            results.enqueue(new Callback<FiveDayForecast>() {
                @Override
                public void onResponse(Call<FiveDayForecast> call, Response<FiveDayForecast> response) {
                    Log.d("Response Code", String.valueOf(response.code()));
                    if(response.code() == 200) {
                        mSearchResults.setValue(response.body());
                        mLastResults = response.body();
                        Log.d("Response Body", String.valueOf(response.body()));
                        mLoadingStatus.setValue(Status.SUCCESS);
                    }
                    else{

                        Log.d("Response Body", String.valueOf(response.body()));
                        Log.d("Response Body", String.valueOf(response.toString()));
                    }
                }

                @Override
                public void onFailure(Call<FiveDayForecast> call,
                                      Throwable t) {
                    Log.d("Error", "");
                    t.printStackTrace();
                    mLoadingStatus.setValue(Status.ERROR);
                    mlastLoadError.setValue(t.getMessage());
                    mSearchResults.setValue(mLastResults);
                }
            });
        } else {
            Log.d(TAG, "using cached search results");
        }


    }

    public MutableLiveData<Status> getLoadingStatus() {
        return mLoadingStatus;
    }
}
