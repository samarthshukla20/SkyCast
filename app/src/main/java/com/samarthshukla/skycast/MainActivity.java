package com.samarthshukla.skycast;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.LinearLayout;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.samarthshukla.skycast.data.api.RetrofitClient;
import com.samarthshukla.skycast.data.api.WeatherApiService;
import com.samarthshukla.skycast.model.ForecastDay;
import com.samarthshukla.skycast.model.GeocodingResponse;
import com.samarthshukla.skycast.model.ForecastResponse;
import com.samarthshukla.skycast.model.WeatherResponse;
import com.samarthshukla.skycast.ui.forecast.ForecastAdapter;
import com.samarthshukla.skycast.ui.forecast.CitySuggestionAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SkyCastForecast";

    private TextView tvCity;
    private TextView tvTemperature;
    private TextView tvCondition;
    private LottieAnimationView tvWeatherIcon;
    private TextView tvHighLow;
    private TextView tvFeelsLike;
    private TextView tvHumidity;
    private TextView tvWind;
    private TextView tvPressure;

    private RecyclerView forecastRecyclerView;
    private ForecastAdapter forecastAdapter;
    private List<ForecastDay> forecastList;

    private WeatherApiService weatherApiService;
    private EditText etCitySearch;
    private Button btnSearchCity;
    private LinearLayout citySuggestionsContainer;
    private RecyclerView citySuggestionsRecyclerView;
    private CitySuggestionAdapter citySuggestionAdapter;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private Call<List<GeocodingResponse>> citySearchCall;
    private Call<WeatherResponse> currentWeatherCall;
    private Call<ForecastResponse> forecastCall;
    private boolean selectingSuggestion = false;
    private String selectedCityLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        weatherApiService = RetrofitClient.getWeatherApi();
        setupForecast();
        setupCitySuggestions();
        setupCitySearch();

        fetchWeather("Lucknow");
        fetchFiveDayForecast("Lucknow");
    }

    private void initializeViews() {
        tvCity = findViewById(R.id.tvCity);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvCondition = findViewById(R.id.tvCondition);
        tvHighLow = findViewById(R.id.tvHighLow);
        tvWeatherIcon = findViewById(R.id.tvWeatherIcon);
        tvFeelsLike = findViewById(R.id.tvFeelsLike);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWind = findViewById(R.id.tvWind);
        tvPressure = findViewById(R.id.tvPressure);
        etCitySearch = findViewById(R.id.etCitySearch);
        btnSearchCity = findViewById(R.id.btnSearchCity);
        citySuggestionsContainer = findViewById(R.id.citySuggestionsContainer);
        citySuggestionsRecyclerView = findViewById(R.id.citySuggestionsRecyclerView);
    }

    // FORECAST RECYCLER VIEW

    private void setupForecast() {
        forecastRecyclerView = findViewById(R.id.forecastRecyclerView);
        forecastList = new ArrayList<>();
        forecastAdapter = new ForecastAdapter(forecastList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
        );

        forecastRecyclerView.setLayoutManager(layoutManager);
        forecastRecyclerView.setAdapter(forecastAdapter);
    }

    private void setupCitySuggestions() {
        citySuggestionAdapter = new CitySuggestionAdapter(this::onCitySelected);
        citySuggestionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        citySuggestionsRecyclerView.setAdapter(citySuggestionAdapter);
        citySuggestionsContainer.setVisibility(View.GONE);
    }

    private void setupCitySearch() {
        etCitySearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (selectingSuggestion) return;
                String query = s.toString().trim();
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                if (citySearchCall != null) citySearchCall.cancel();
                if (query.length() < 2) { hideCitySuggestions(); return; }
                searchRunnable = () -> requestCitySuggestions(query);
                searchHandler.postDelayed(searchRunnable, 400);
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        btnSearchCity.setOnClickListener(v -> searchCityFromButton());
        etCitySearch.setOnEditorActionListener((v, actionId, event) -> {
            boolean enter = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN;
            if (actionId == EditorInfo.IME_ACTION_SEARCH || enter) { searchCityFromButton(); return true; }
            return false;
        });
    }

    private void requestCitySuggestions(String query) {
        citySearchCall = weatherApiService.searchCities(query, 5, BuildConfig.WEATHER_API_KEY);
        citySearchCall.enqueue(new Callback<List<GeocodingResponse>>() {
            @Override public void onResponse(Call<List<GeocodingResponse>> call, Response<List<GeocodingResponse>> response) {
                if (call.isCanceled() || !query.equals(etCitySearch.getText().toString().trim())) return;
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    citySuggestionAdapter.setCities(response.body());
                    citySuggestionsContainer.setVisibility(View.VISIBLE);
                } else hideCitySuggestions();
            }
            @Override public void onFailure(Call<List<GeocodingResponse>> call, Throwable t) {
                if (!call.isCanceled()) { Log.e(TAG, "City suggestions failed", t); hideCitySuggestions(); }
            }
        });
    }

    private void searchCityFromButton() {
        String query = etCitySearch.getText().toString().trim();
        if (query.isEmpty()) { etCitySearch.setError("Please enter a city name"); return; }
        hideKeyboard();
        if (citySearchCall != null) citySearchCall.cancel();
        citySearchCall = weatherApiService.searchCities(query, 5, BuildConfig.WEATHER_API_KEY);
        citySearchCall.enqueue(new Callback<List<GeocodingResponse>>() {
            @Override public void onResponse(Call<List<GeocodingResponse>> call, Response<List<GeocodingResponse>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    Toast.makeText(MainActivity.this, "No matching city found", Toast.LENGTH_SHORT).show(); return;
                }
                GeocodingResponse chosen = response.body().get(0);
                for (GeocodingResponse city : response.body()) {
                    if (city.getName() != null && city.getName().equalsIgnoreCase(query)) { chosen = city; break; }
                }
                onCitySelected(chosen);
            }
            @Override public void onFailure(Call<List<GeocodingResponse>> call, Throwable t) {
                if (!call.isCanceled()) Toast.makeText(MainActivity.this, "Unable to search for city", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onCitySelected(GeocodingResponse city) {
        if (city == null) return;
        selectingSuggestion = true;
        etCitySearch.setText(city.getName());
        selectingSuggestion = false;
        hideKeyboard();
        etCitySearch.clearFocus();
        hideCitySuggestions();
        String label = city.getName();
        if (!city.getSubtitle().isEmpty()) label += ", " + city.getSubtitle();
        selectedCityLabel = label;
        tvCity.setText(label);
        fetchWeatherByCoordinates(city.getLat(), city.getLon());
        fetchForecastByCoordinates(city.getLat(), city.getLon());
    }

    private void hideCitySuggestions() {
        if (citySuggestionsContainer != null) citySuggestionsContainer.setVisibility(View.GONE);
        if (citySuggestionAdapter != null) citySuggestionAdapter.setCities(new ArrayList<>());
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(etCitySearch.getWindowToken(), 0);
    }

    // CURRENT WEATHER API

    private void fetchWeather(String city) {
        if (currentWeatherCall != null) currentWeatherCall.cancel();
        currentWeatherCall = weatherApiService.getCurrentWeather(
                city, BuildConfig.WEATHER_API_KEY, "metric"
        );
        currentWeatherCall.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(
                    Call<WeatherResponse> call,
                    Response<WeatherResponse> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    updateWeatherUI(response.body());
                } else {
                    Toast.makeText(
                            MainActivity.this,
                            "Unable to fetch weather. Error: " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable throwable) {
                Log.e(TAG, "Current weather request failed", throwable);

                Toast.makeText(
                        MainActivity.this,
                        "Network error: " + throwable.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void fetchWeatherByCoordinates(double lat, double lon) {
        if (currentWeatherCall != null) currentWeatherCall.cancel();
        currentWeatherCall = weatherApiService.getCurrentWeatherByCoordinates(lat, lon, BuildConfig.WEATHER_API_KEY, "metric");
        currentWeatherCall.enqueue(new Callback<WeatherResponse>() {
            @Override public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (call.isCanceled()) return;
                if (response.isSuccessful() && response.body() != null) updateWeatherUI(response.body());
                else Toast.makeText(MainActivity.this, "Unable to fetch weather: " + response.code(), Toast.LENGTH_SHORT).show();
            }
            @Override public void onFailure(Call<WeatherResponse> call, Throwable t) {
                if (!call.isCanceled()) Log.e(TAG, "Coordinate weather request failed", t);
            }
        });
    }

    private void updateWeatherUI(WeatherResponse weather) {
        tvCity.setText(selectedCityLabel != null ? selectedCityLabel : weather.getCityName());

        tvTemperature.setText(
                Math.round(weather.getMain().getTemperature()) + "°"
        );

        tvHighLow.setText(
                "H: " + Math.round(weather.getMain().getMaxTemperature())
                        + "°    L: "
                        + Math.round(weather.getMain().getMinTemperature())
                        + "°"
        );

        tvFeelsLike.setText(
                "Feels Like\n"
                        + Math.round(weather.getMain().getFeelsLike()) + "°C"
        );

        tvHumidity.setText(
                "Humidity\n" + weather.getMain().getHumidity() + "%"
        );

        tvWind.setText(
                "Wind\n" + weather.getWind().getSpeed() + " m/s"
        );

        tvPressure.setText(
                "Pressure\n" + weather.getMain().getPressure() + " hPa"
        );

        if (weather.getWeather() != null && !weather.getWeather().isEmpty()) {
            String condition = weather.getWeather().get(0).getDescription();
            String iconCode = weather.getWeather().get(0).getIcon();

            if (condition != null && !condition.isEmpty()) {
                condition = condition.substring(0, 1).toUpperCase(Locale.getDefault())
                        + condition.substring(1);
            }

            tvCondition.setText(condition != null ? condition : "Unknown");
            playWeatherAnimation(tvWeatherIcon, iconCode);
        }
    }

    // FIVE-DAY FORECAST API

    private void fetchFiveDayForecast(String city) {
        if (forecastCall != null) forecastCall.cancel();
        forecastCall = weatherApiService.getFiveDayForecast(
                city, BuildConfig.WEATHER_API_KEY, "metric"
        );
        forecastCall.enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(
                    Call<ForecastResponse> call,
                    Response<ForecastResponse> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ForecastResponse.ForecastEntry> entries =
                            response.body().getForecastEntries();

                    if (entries != null && !entries.isEmpty()) {
                        for (int i = 0; i < Math.min(entries.size(), 5); i++) {
                            ForecastResponse.ForecastEntry entry = entries.get(i);

                            Log.d(TAG, "Forecast date: " + entry.getDateTime());
                            Log.d(TAG, "Rain probability: "
                                    + entry.getRainPercentage() + "%");
                            Log.d(TAG, "Raw forecast entry: "
                                    + new com.google.gson.Gson().toJson(entry));
                        }

                        buildDailyForecast(entries);
                    } else {
                        Log.e(TAG, "Forecast response contains no entries");

                        Toast.makeText(
                                MainActivity.this,
                                "No forecast data available",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                } else {
                    Log.e(TAG, "Forecast API error: " + response.code());

                    Toast.makeText(
                            MainActivity.this,
                            "Unable to fetch forecast: " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<ForecastResponse> call,
                    Throwable throwable
            ) {
                Log.e(TAG, "Forecast request failed", throwable);

                Toast.makeText(
                        MainActivity.this,
                        "Forecast network error",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    // CONVERT 3-HOURLY DATA INTO DAILY FORECAST

    private void fetchForecastByCoordinates(double lat, double lon) {
        if (forecastCall != null) forecastCall.cancel();
        forecastCall = weatherApiService.getFiveDayForecastByCoordinates(lat, lon, BuildConfig.WEATHER_API_KEY, "metric");
        forecastCall.enqueue(new Callback<ForecastResponse>() {
            @Override public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                if (call.isCanceled()) return;
                if (response.isSuccessful() && response.body() != null) {
                    List<ForecastResponse.ForecastEntry> entries = response.body().getForecastEntries();
                    if (entries != null && !entries.isEmpty()) buildDailyForecast(entries);
                    else Toast.makeText(MainActivity.this, "No forecast data available", Toast.LENGTH_SHORT).show();
                } else Toast.makeText(MainActivity.this, "Unable to fetch forecast: " + response.code(), Toast.LENGTH_SHORT).show();
            }
            @Override public void onFailure(Call<ForecastResponse> call, Throwable t) {
                if (!call.isCanceled()) Log.e(TAG, "Coordinate forecast request failed", t);
            }
        });
    }

    private void buildDailyForecast(List<ForecastResponse.ForecastEntry> entries) {
        Map<String, DailyWeather> dailyMap = new TreeMap<>();

        SimpleDateFormat dateKeyFormat =
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        SimpleDateFormat dayFormat =
                new SimpleDateFormat("EEE", Locale.getDefault());

        SimpleDateFormat hourFormat =
                new SimpleDateFormat("HH", Locale.getDefault());

        for (ForecastResponse.ForecastEntry entry : entries) {
            if (entry.getMain() == null) {
                continue;
            }

            Date date = new Date(entry.getTimestamp() * 1000L);
            String dateKey = dateKeyFormat.format(date);

            DailyWeather daily = dailyMap.get(dateKey);

            if (daily == null) {
                daily = new DailyWeather();
                daily.date = date;
                daily.minTemp = entry.getMain().getMinTemperature();
                daily.maxTemp = entry.getMain().getMaxTemperature();
                dailyMap.put(dateKey, daily);
            } else {
                daily.minTemp = Math.min(
                        daily.minTemp,
                        entry.getMain().getMinTemperature()
                );

                daily.maxTemp = Math.max(
                        daily.maxTemp,
                        entry.getMain().getMaxTemperature()
                );
            }

            // Select the weather condition closest to noon.
            int hour = Integer.parseInt(hourFormat.format(date));
            int noonDistance = Math.abs(hour - 12);

            if (noonDistance < daily.closestNoonDistance) {
                daily.closestNoonDistance = noonDistance;

                if (entry.getWeather() != null && !entry.getWeather().isEmpty()) {
                    daily.condition = entry.getWeather().get(0).getDescription();
                    daily.iconCode = entry.getWeather().get(0).getIcon();
                }
            }

            // Keep the highest precipitation probability for the day.
            int rainProbability = entry.getRainPercentage();
            daily.precipitationProbability = Math.max(
                    daily.precipitationProbability,
                    rainProbability
            );

            Log.d(TAG, "Rain probability: "
                    + entry.getPrecipitationProbability());
        }

        forecastList.clear();
        int index = 0;

        for (DailyWeather daily : dailyMap.values()) {
            String dayName = index == 0 ? "Today" : dayFormat.format(daily.date);

            // ForecastDay now stores the Lottie asset filename, not an emoji.
            String animationFile = getAnimationFile(daily.iconCode);

            int maxTemp = (int) Math.round(daily.maxTemp);
            int minTemp = (int) Math.round(daily.minTemp);

            ForecastDay forecastDay = new ForecastDay(
                    dayName,
                    animationFile,
                    daily.condition != null ? daily.condition : "Unknown",
                    maxTemp,
                    minTemp,
                    daily.precipitationProbability
            );

            forecastList.add(forecastDay);
            index++;
        }

        forecastAdapter.notifyDataSetChanged();
        Log.d(TAG, "Daily forecast loaded: " + forecastList.size());
    }

    // LOTTIE WEATHER ANIMATIONS

    /**
     * Returns the matching JSON filename from app/src/main/assets.
     * Mist/fog (50x) intentionally uses the cloudy fallback animation.
     */
    private String getAnimationFile(String iconCode) {
        if (iconCode == null || iconCode.length() < 2) {
            return "few_clouds.json";
        }

        String conditionCode = iconCode.substring(0, 2);

        switch (conditionCode) {
            case "01":
                return iconCode.endsWith("n")
                        ? "clear_night.json"
                        : "clear_day.json";

            case "02":
                return "few_clouds.json";

            case "03":
            case "04":
            case "50":
                return "cloudy.json";

            case "09":
            case "10":
                return "rain.json";

            case "11":
                return "thunderstorm.json";

            case "13":
                return "snow.json";

            default:
                return "few_clouds.json";
        }
    }

    private void playWeatherAnimation(
            LottieAnimationView animationView,
            String iconCode
    ) {
        if (animationView == null) {
            return;
        }

        String animationFile = getAnimationFile(iconCode);

        animationView.cancelAnimation();
        animationView.setAnimation(animationFile);
        animationView.setRepeatCount(-1);
        animationView.playAnimation();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (tvWeatherIcon != null) {
            tvWeatherIcon.cancelAnimation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
        if (citySearchCall != null) citySearchCall.cancel();
        if (currentWeatherCall != null) currentWeatherCall.cancel();
        if (forecastCall != null) forecastCall.cancel();
    }

    // DAILY FORECAST DATA HOLDER

    private static class DailyWeather {
        Date date;
        double minTemp;
        double maxTemp;
        String condition;
        String iconCode;
        int precipitationProbability = 0;
        int closestNoonDistance = Integer.MAX_VALUE;
    }
}
