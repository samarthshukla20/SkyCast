package com.samarthshukla.skycast;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.samarthshukla.skycast.model.ForecastResponse;
import com.samarthshukla.skycast.model.WeatherResponse;

/** Small persistent cache for the latest weather and forecast per city/location. */
public class WeatherCache {
    private static final String PREFS = "skycast_weather_cache";
    private static final String LAST_LAT = "last_lat";
    private static final String LAST_LON = "last_lon";
    private final SharedPreferences preferences;
    private final Gson gson = new Gson();

    public WeatherCache(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static String cityKey(String city) {
        return "city_" + (city == null ? "unknown" : city.trim().toLowerCase().replaceAll("[^a-z0-9]+", "_"));
    }

    public static String locationKey(double lat, double lon) {
        return String.format(java.util.Locale.US, "location_%.2f_%.2f", lat, lon);
    }

    private String key(String cacheKey, String suffix) { return cacheKey + "_" + suffix; }

    public void saveCurrentWeather(String cacheKey, WeatherResponse weather) {
        preferences.edit()
                .putString(key(cacheKey, "current"), gson.toJson(weather))
                .putLong(key(cacheKey, "updated"), System.currentTimeMillis())
                .apply();
    }

    public WeatherResponse getCurrentWeather(String cacheKey) {
        String json = preferences.getString(key(cacheKey, "current"), null);
        if (json == null) return null;
        try { return gson.fromJson(json, WeatherResponse.class); }
        catch (Exception ignored) { return null; }
    }

    public void saveForecast(String cacheKey, ForecastResponse forecast) {
        preferences.edit()
                .putString(key(cacheKey, "forecast"), gson.toJson(forecast))
                .putLong(key(cacheKey, "updated"), System.currentTimeMillis())
                .apply();
    }

    public ForecastResponse getForecast(String cacheKey) {
        String json = preferences.getString(key(cacheKey, "forecast"), null);
        if (json == null) return null;
        try { return gson.fromJson(json, ForecastResponse.class); }
        catch (Exception ignored) { return null; }
    }

    public long getLastUpdated(String cacheKey) {
        return preferences.getLong(key(cacheKey, "updated"), 0L);
    }

    public void saveLastLocation(double lat, double lon) {
        preferences.edit().putString(LAST_LAT, String.valueOf(lat))
                .putString(LAST_LON, String.valueOf(lon)).apply();
    }

    public double[] getLastLocation() {
        String lat = preferences.getString(LAST_LAT, null);
        String lon = preferences.getString(LAST_LON, null);
        if (lat == null || lon == null) return null;
        try { return new double[]{Double.parseDouble(lat), Double.parseDouble(lon)}; }
        catch (NumberFormatException ignored) { return null; }
    }
}
