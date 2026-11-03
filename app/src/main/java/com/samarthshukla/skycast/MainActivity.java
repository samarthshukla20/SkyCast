package com.samarthshukla.skycast;

import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
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

import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SkyCastForecast";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private WeatherCache weatherCache;
    private String activeCacheKey = "lucknow";
    private TextView tvLastUpdated;

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
    private View btnCurrentLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private CancellationTokenSource locationCancellationTokenSource;
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
    private TextView btnThemeToggle;
    private SharedPreferences themePreferences;
    private static final String THEME_PREFS = "skycast_theme_preferences";
    private static final String KEY_NIGHT_MODE = "night_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applySavedTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        weatherApiService = RetrofitClient.getWeatherApi();
        weatherCache = new WeatherCache(this);
        tvLastUpdated = findViewById(R.id.tvLastUpdated);
        setupForecast();
        loadLastLocationCache();
        setupCitySuggestions();
        setupCitySearch();
        setupThemeToggle();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestCurrentLocation();
    }

    private void initializeViews() {
        btnThemeToggle = findViewById(R.id.btnThemeToggle);
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
        btnCurrentLocation = findViewById(R.id.btnCurrentLocation);
        citySuggestionsContainer = findViewById(R.id.citySuggestionsContainer);
        citySuggestionsRecyclerView = findViewById(R.id.citySuggestionsRecyclerView);
    }

    // THEME MANAGEMENT

    private void applySavedTheme() {

        themePreferences = getSharedPreferences(
                THEME_PREFS,
                MODE_PRIVATE
        );

        boolean darkMode = themePreferences.getBoolean(
                KEY_NIGHT_MODE,
                true
        );

        AppCompatDelegate.setDefaultNightMode(
                darkMode
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );
    }


    private void setupThemeToggle() {

        updateThemeToggleIcon();

        btnThemeToggle.setOnClickListener(v -> {

            boolean currentlyDark = themePreferences.getBoolean(
                    KEY_NIGHT_MODE,
                    true
            );

            boolean newDarkMode = !currentlyDark;

            // Save the user's selection.
            themePreferences.edit()
                    .putBoolean(KEY_NIGHT_MODE, newDarkMode)
                    .apply();

            // Apply the new theme.
            AppCompatDelegate.setDefaultNightMode(
                    newDarkMode
                            ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO
            );

        });
    }


    private void updateThemeToggleIcon() {

        boolean darkMode = themePreferences.getBoolean(
                KEY_NIGHT_MODE,
                true
        );

        if (darkMode) {
            btnThemeToggle.setText("\u2600\uFE0E");
            btnThemeToggle.setContentDescription("Switch to light mode");
        } else {
            btnThemeToggle.setText("\u263E\uFE0E");
            btnThemeToggle.setContentDescription("Switch to dark mode");
        }
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
        btnCurrentLocation.setOnClickListener(v -> requestCurrentLocation());
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
        activeCacheKey = WeatherCache.cityKey(label);
        loadCachedData(activeCacheKey);
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

    // CURRENT LOCATION

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCurrentLocation() {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
            return;
        }

        if (btnCurrentLocation != null) btnCurrentLocation.setEnabled(false);
        Toast.makeText(this, "Getting your location...", Toast.LENGTH_SHORT).show();

        // Prefer a fresh location; this avoids showing stale weather from an old location.
        if (locationCancellationTokenSource != null) locationCancellationTokenSource.cancel();
        locationCancellationTokenSource = new CancellationTokenSource();

        try {
            fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    locationCancellationTokenSource.getToken()
            ).addOnSuccessListener(this, location -> {
                if (btnCurrentLocation != null) btnCurrentLocation.setEnabled(true);
                if (location != null) {
                    loadWeatherForLocation(location);
                } else {
                    // Fall back to the last known location if a fresh fix is unavailable.
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(this, lastLocation -> {
                                if (lastLocation != null) loadWeatherForLocation(lastLocation);
                                else Toast.makeText(this,
                                        "Couldn't determine location. Check that Location is enabled and try again.",
                                        Toast.LENGTH_LONG).show();
                            })
                            .addOnFailureListener(this, error -> {
                                Log.e(TAG, "Last location lookup failed", error);
                                Toast.makeText(this, "Unable to get your location", Toast.LENGTH_SHORT).show();
                            });
                }
            }).addOnFailureListener(this, error -> {
                if (btnCurrentLocation != null) btnCurrentLocation.setEnabled(true);
                Log.e(TAG, "Current location lookup failed", error);
                Toast.makeText(this, "Unable to get your location. Try again.", Toast.LENGTH_SHORT).show();
            });
        } catch (SecurityException exception) {
            if (btnCurrentLocation != null) btnCurrentLocation.setEnabled(true);
            Log.e(TAG, "Location permission missing", exception);
        }
    }

    private void loadWeatherForLocation(Location location) {
        selectedCityLabel = null;
        activeCacheKey = WeatherCache.locationKey(location.getLatitude(), location.getLongitude());
        weatherCache.saveLastLocation(location.getLatitude(), location.getLongitude());
        loadCachedData(activeCacheKey); // Let the weather response supply the detected city name.
        hideCitySuggestions();
        tvCity.setText("Your location");
        fetchWeatherByCoordinates(location.getLatitude(), location.getLongitude());
        fetchForecastByCoordinates(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (hasLocationPermission()) {
                requestCurrentLocation();
            } else {
                Toast.makeText(this,
                        "Location permission denied. You can still search for any city.",
                        Toast.LENGTH_LONG).show();
                // Keep the app usable when the user declines location access.
                activeCacheKey = WeatherCache.cityKey("Lucknow");
                selectedCityLabel = "Lucknow";
                loadCachedData(activeCacheKey);
                fetchWeather("Lucknow");
                fetchFiveDayForecast("Lucknow");
            }
        }
    }

    // OFFLINE CACHE

    private void loadLastLocationCache() {
        double[] lastLocation = weatherCache.getLastLocation();
        if (lastLocation != null) {
            activeCacheKey = WeatherCache.locationKey(lastLocation[0], lastLocation[1]);
            loadCachedData(activeCacheKey);
        }
    }

    private void loadCachedData(String cacheKey) {
        WeatherResponse cachedWeather = weatherCache.getCurrentWeather(cacheKey);
        ForecastResponse cachedForecast = weatherCache.getForecast(cacheKey);
        boolean found = false;

        if (cachedWeather != null) {
            updateWeatherUI(cachedWeather);
            found = true;
        }
        if (cachedForecast != null && cachedForecast.getForecastEntries() != null
                && !cachedForecast.getForecastEntries().isEmpty()) {
            buildDailyForecast(cachedForecast.getForecastEntries());
            found = true;
        }

        if (found) updateCacheTimestamp(cacheKey);
        else if (tvLastUpdated != null) tvLastUpdated.setText("No saved weather data");
    }

    private void updateCacheTimestamp(String cacheKey) {
        if (tvLastUpdated == null) return;
        long savedAt = weatherCache.getLastUpdated(cacheKey);
        if (savedAt <= 0) {
            tvLastUpdated.setText("Waiting for weather data...");
            return;
        }
        String formatted = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                .format(new Date(savedAt));
        tvLastUpdated.setText("Last updated: " + formatted);
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
                if (call.isCanceled()) return;
                if (response.isSuccessful() && response.body() != null) {
                    weatherCache.saveCurrentWeather(activeCacheKey, response.body());
                    updateCacheTimestamp(activeCacheKey);
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
                if (response.isSuccessful() && response.body() != null) {
                    weatherCache.saveCurrentWeather(activeCacheKey, response.body());
                    updateCacheTimestamp(activeCacheKey);
                    updateWeatherUI(response.body());
                }
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
                if (call.isCanceled()) return;
                if (response.isSuccessful() && response.body() != null) {
                    weatherCache.saveForecast(activeCacheKey, response.body());
                    updateCacheTimestamp(activeCacheKey);
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
                    weatherCache.saveForecast(activeCacheKey, response.body());
                    updateCacheTimestamp(activeCacheKey);
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
        if (locationCancellationTokenSource != null) locationCancellationTokenSource.cancel();
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