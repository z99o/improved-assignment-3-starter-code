package com.example.android.lifecycleweather.data;

import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class ForecastViewModel extends ViewModel {
    private LiveData<FiveDayForecast> mSearchResults;
    private ForecastRepository mRepository;
    private LiveData<Status> mLoadingStatus;
    private LiveData<String> mLastError;
    private String mCurrentQuery;
    private boolean shouldExecuteSearch(String query) {
        return !TextUtils.equals(query, mCurrentQuery);
    }

    public ForecastViewModel(){
        mRepository = new ForecastRepository();
        mSearchResults = mRepository.getSearchResults();
        mLoadingStatus = mRepository.getLoadingStatus();
        mLastError = mRepository.getLastError();

    }

    public void loadSearchResults(String location, String units, String key){
        mRepository.loadSearchResults(location, units, key);

    }

    public LiveData<Status> getLoadingStatus() {

        return mLoadingStatus;
    }

    public LiveData<FiveDayForecast> getSearchResults(){
        return mSearchResults;
    }
    public String getLastError(){
        return mLastError.getValue();
    }
}
