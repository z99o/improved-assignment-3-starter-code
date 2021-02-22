package com.example.android.lifecycleweather;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.android.lifecycleweather.data.FiveDayForecast;
import com.example.android.lifecycleweather.data.ForecastCity;
import com.example.android.lifecycleweather.data.ForecastData;
import com.example.android.lifecycleweather.data.ForecastViewModel;
import com.example.android.lifecycleweather.data.Status;
import com.example.android.lifecycleweather.utils.OpenWeatherUtils;

public class MainActivity extends AppCompatActivity implements ForecastAdapter.OnForecastItemClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    /*
     * To use your own OpenWeather API key, create a file called `gradle.properties` in your
     * GRADLE_USER_HOME directory (this will usually be `$HOME/.gradle/` in MacOS/Linux and
     * `$USER_HOME/.gradle/` in Windows), and add the following line:
     *
     *   OPENWEATHER_API_KEY="<put_your_own_OpenWeather_API_key_here>"
     *
     * The Gradle build for this project is configured to automatically grab that value and store
     * it in the field `BuildConfig.OPENWEATHER_API_KEY` that's used below.  You can read more
     * about this setup on the following pages:
     *
     *   https://developer.android.com/studio/build/gradle-tips#share-custom-fields-and-resource-values-with-your-app-code
     *
     *   https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties
     *
     * Alternatively, you can just hard-code your API key below 🤷‍.  If you do hard code your API
     * key below, make sure to get rid of the following line (line 18) in build.gradle:
     *
     *   buildConfigField("String", "OPENWEATHER_API_KEY", OPENWEATHER_API_KEY)
     */
    private static final String OPENWEATHER_APPID = BuildConfig.OPENWEATHER_API_KEY;

    private ForecastAdapter forecastAdapter;
    private RequestQueue requestQueue;

    private ForecastCity forecastCity;

    private RecyclerView forecastListRV;
    private ProgressBar loadingIndicatorPB;
    private TextView errorMessageTV;

    private Toast errorToast;

    private ForecastViewModel mForecastViewModel;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.loadingIndicatorPB = findViewById(R.id.pb_loading_indicator);
        this.errorMessageTV = findViewById(R.id.tv_error_message);
        this.forecastListRV = findViewById(R.id.rv_forecast_list);
        this.forecastListRV.setLayoutManager(new LinearLayoutManager(this));
        this.forecastListRV.setHasFixedSize(true);

        this.forecastAdapter = new ForecastAdapter(this);
        this.forecastListRV.setAdapter(this.forecastAdapter);

        this.requestQueue = Volley.newRequestQueue(this);
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mForecastViewModel = new ViewModelProvider(this)
                .get(ForecastViewModel.class);

        mForecastViewModel.getSearchResults()
                .observe(this, new Observer<FiveDayForecast>() {
                    @Override
                    public void
                    onChanged(@Nullable FiveDayForecast fiveDayForecast) {
                        // Update UI with new search result data...
                        if(fiveDayForecast == null){
                            return;
                        }
                        Log.d("Results", String.valueOf(fiveDayForecast.getForecastCity()));
                        forecastCity = fiveDayForecast.getForecastCity();
                        forecastAdapter.updateForecastData(fiveDayForecast);

                    }
                });

       mForecastViewModel.getLoadingStatus()
                .observe(this, new Observer<Status>() {
                    @Override
                    public void onChanged(@Nullable Status status) {
                        switch(status){
                            case ERROR:
                                doLoadingError(mForecastViewModel.getLastError());
                                break;
                            case LOADING:
                                doLoadingLoading();
                                break;
                            default:
                                doLoadingSuccess();
                        }
                    }
                });

        this.fetchFiveDayForecast("Corvallis,OR,US", "imperial");
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.fetchFiveDayForecast("Corvallis,OR,US", "imperial");
    }

    @Override
    public void onForecastItemClick(ForecastData forecastData) {
        Intent intent = new Intent(this, ForecastDetailActivity.class);

        intent.putExtra(ForecastDetailActivity.EXTRA_FORECAST_DATA, forecastData);
        intent.putExtra(ForecastDetailActivity.EXTRA_FORECAST_CITY, this.forecastCity);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_map:
                viewForecastCityInMap();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    /**
     * This function uses Volley to fetch the 5 day/3 hour forecast from the OpenWeather API for
     * a specified city.  Updates this activity's UI to reflect the status of the request to the
     * OpenWeather API.  For more information about Volley, see the documentation here:
     *
     *   https://developer.android.com/training/volley
     *
     * @param city The name of the city for which to fetch the forecast, e.g. "Corvallis,OR,US".
     * @param units String specifying the type of units of measurement to fetch.  Can be
     *              "imperial", "metric", or "standard".
     */
    private void fetchFiveDayForecast(String city, String units) {
        String preference_location = sharedPreferences.getString(getString(R.string.pref_location_key),"Corvallis");
        String preference_units = sharedPreferences.getString(getString(R.string.pref_unit_key),getString(R.string.pref_unit_default));
        mForecastViewModel.loadSearchResults(preference_location,preference_units,OPENWEATHER_APPID);
        return;
        /*
        String forecastUrl = OpenWeatherUtils.buildFiveDayForecastUrl(city, units, OPENWEATHER_APPID);
        Log.d(TAG, "Request URL: " + forecastUrl);

        StringRequest req = new StringRequest(
                Request.Method.GET,
                forecastUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        FiveDayForecast forecast =
                                OpenWeatherUtils.parseFiveDayForecastResponse(response);
                        forecastAdapter.updateForecastData(forecast);
                        forecastCity = forecast.getForecastCity();

                        forecastListRV.setVisibility(View.VISIBLE);
                        loadingIndicatorPB.setVisibility(View.INVISIBLE);
                        errorMessageTV.setVisibility(View.INVISIBLE);

                        ActionBar actionBar = getSupportActionBar();
                        actionBar.setTitle(forecastCity.getName());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        errorMessageTV.setText(getString(R.string.loading_error, error.getMessage()));
                        errorMessageTV.setVisibility(View.VISIBLE);
                        loadingIndicatorPB.setVisibility(View.INVISIBLE);
                        forecastListRV.setVisibility(View.INVISIBLE);
                    }
                }
        );
        loadingIndicatorPB.setVisibility(View.VISIBLE);
        this.requestQueue.add(req);

         */
    }

    public void doLoadingSuccess(){
        forecastListRV.setVisibility(View.VISIBLE);
        loadingIndicatorPB.setVisibility(View.INVISIBLE);
        errorMessageTV.setVisibility(View.INVISIBLE);
    }

    public void doLoadingLoading(){
        loadingIndicatorPB.setVisibility(View.VISIBLE);
    }

    public void doLoadingError(String error){
        errorMessageTV.setText(getString(R.string.loading_error, error));
        errorMessageTV.setVisibility(View.VISIBLE);
        loadingIndicatorPB.setVisibility(View.INVISIBLE);
        forecastListRV.setVisibility(View.INVISIBLE);
    }



    /**
     * This function uses an implicit intent to view the forecast city in a map.
     */
    private void viewForecastCityInMap() {
        if (this.forecastCity != null) {
            Uri forecastCityGeoUri = Uri.parse(getString(
                    R.string.geo_uri,
                    this.forecastCity.getLatitude(),
                    this.forecastCity.getLongitude(),
                    12
            ));
            Intent intent = new Intent(Intent.ACTION_VIEW, forecastCityGeoUri);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                if (this.errorToast != null) {
                    this.errorToast.cancel();
                }
                this.errorToast = Toast.makeText(
                        this,
                        getString(R.string.action_map_error),
                        Toast.LENGTH_LONG
                );
                this.errorToast.show();
            }
        }
    }
}