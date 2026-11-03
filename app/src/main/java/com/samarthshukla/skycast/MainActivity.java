package com.samarthshukla.skycast;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.samarthshukla.skycast.data.api.RetrofitClient;
import com.samarthshukla.skycast.data.api.WeatherApiService;
import com.samarthshukla.skycast.model.WeatherResponse;
import com.samarthshukla.skycast.model.ForecastResponse;
import com.samarthshukla.skycast.model.ForecastDay;
import com.samarthshukla.skycast.ui.forecast.ForecastAdapter;

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
    private TextView tvHighLow;
    private TextView tvFeelsLike;
    private TextView tvHumidity;
    private TextView tvWind;
    private TextView tvPressure;

    private RecyclerView forecastRecyclerView;
    private ForecastAdapter forecastAdapter;
    private List<ForecastDay> forecastList;

    private WeatherApiService weatherApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();

        weatherApiService = RetrofitClient.getWeatherApi();

        setupForecast();

        fetchWeather("Lucknow");
        fetchFiveDayForecast("Lucknow");
    }

    private void initializeViews() {

        tvCity = findViewById(R.id.tvCity);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvCondition = findViewById(R.id.tvCondition);
        tvHighLow = findViewById(R.id.tvHighLow);

        tvFeelsLike = findViewById(R.id.tvFeelsLike);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWind = findViewById(R.id.tvWind);
        tvPressure = findViewById(R.id.tvPressure);
    }

    // -----------------------------------------
    // FORECAST RECYCLER VIEW
    // -----------------------------------------

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

    // -----------------------------------------
    // CURRENT WEATHER API
    // -----------------------------------------

    private void fetchWeather(String city) {

        weatherApiService.getCurrentWeather(
                city,
                BuildConfig.WEATHER_API_KEY,
                "metric"
        ).enqueue(new Callback<WeatherResponse>() {

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
            public void onFailure(
                    Call<WeatherResponse> call,
                    Throwable throwable
            ) {

                Log.e(TAG, "Current weather request failed", throwable);

                Toast.makeText(
                        MainActivity.this,
                        "Network error: " + throwable.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void updateWeatherUI(WeatherResponse weather) {

        tvCity.setText(weather.getCityName());

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

        if (weather.getWeather() != null
                && !weather.getWeather().isEmpty()) {

            String condition = weather.getWeather()
                    .get(0)
                    .getDescription();

            if (condition != null && !condition.isEmpty()) {
                condition = condition.substring(0, 1).toUpperCase()
                        + condition.substring(1);
            }

            tvCondition.setText(condition);
        }
    }

    // -----------------------------------------
    // FIVE-DAY FORECAST API
    // -----------------------------------------

    private void fetchFiveDayForecast(String city) {

        weatherApiService.getFiveDayForecast(
                city,
                BuildConfig.WEATHER_API_KEY,
                "metric"
        ).enqueue(new Callback<ForecastResponse>() {

            @Override
            public void onResponse(
                    Call<ForecastResponse> call,
                    Response<ForecastResponse> response
            ) {

                if (response.isSuccessful() && response.body() != null) {

                    List<ForecastResponse.ForecastEntry> entries =
                            response.body().getForecastEntries();

                    if (entries != null && !entries.isEmpty()) {

                        // Inspect the first five forecast entries.
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

    // -----------------------------------------
    // CONVERT 3-HOURLY DATA INTO DAILY FORECAST
    // -----------------------------------------

    private void buildDailyForecast(
            List<ForecastResponse.ForecastEntry> entries
    ) {

        Map<String, DailyWeather> dailyMap = new TreeMap<>();

        SimpleDateFormat dateKeyFormat =
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        SimpleDateFormat dayFormat =
                new SimpleDateFormat("EEE", Locale.getDefault());

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
            int hour = Integer.parseInt(
                    new SimpleDateFormat("HH", Locale.getDefault())
                            .format(date)
            );

            int noonDistance = Math.abs(hour - 12);

            if (noonDistance < daily.closestNoonDistance) {

                daily.closestNoonDistance = noonDistance;

                if (entry.getWeather() != null
                        && !entry.getWeather().isEmpty()) {

                    daily.condition =
                            entry.getWeather().get(0).getDescription();

                    daily.iconCode =
                            entry.getWeather().get(0).getIcon();
                }
            }

            // Probability of precipitation as a percentage.
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

            String dayName;

            if (index == 0) {
                dayName = "Today";
            } else {
                dayName = dayFormat.format(daily.date);
            }

            String emoji = getWeatherEmoji(daily.iconCode);

            int maxTemp = (int) Math.round(daily.maxTemp);
            int minTemp = (int) Math.round(daily.minTemp);

            ForecastDay forecastDay = new ForecastDay(
                    dayName,
                    emoji,
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

    // -----------------------------------------
    // WEATHER ICON MAPPING
    // -----------------------------------------

    private String getWeatherEmoji(String iconCode) {

        if (iconCode == null) {
            return "🌤️";
        }

        if (iconCode.startsWith("01")) {
            return "☀️";
        }

        if (iconCode.startsWith("02")) {
            return "🌤️";
        }

        if (iconCode.startsWith("03") || iconCode.startsWith("04")) {
            return "☁️";
        }

        if (iconCode.startsWith("09") || iconCode.startsWith("10")) {
            return "🌧️";
        }

        if (iconCode.startsWith("11")) {
            return "⛈️";
        }

        if (iconCode.startsWith("13")) {
            return "❄️";
        }

        if (iconCode.startsWith("50")) {
            return "🌫️";
        }

        return "🌤️";
    }

    // -----------------------------------------
    // DAILY FORECAST DATA HOLDER
    // -----------------------------------------

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