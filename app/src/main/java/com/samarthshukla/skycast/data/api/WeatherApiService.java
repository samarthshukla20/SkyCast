package com.samarthshukla.skycast.data.api;

import com.samarthshukla.skycast.model.ForecastResponse;
import com.samarthshukla.skycast.model.GeocodingResponse;
import com.samarthshukla.skycast.model.WeatherResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {
    @GET("data/2.5/weather") Call<WeatherResponse> getCurrentWeather(@Query("q") String city, @Query("appid") String apiKey, @Query("units") String units);
    @GET("data/2.5/weather") Call<WeatherResponse> getCurrentWeatherByCoordinates(@Query("lat") double lat, @Query("lon") double lon, @Query("appid") String apiKey, @Query("units") String units);
    @GET("data/2.5/forecast") Call<ForecastResponse> getFiveDayForecast(@Query("q") String city, @Query("appid") String apiKey, @Query("units") String units);
    @GET("data/2.5/forecast") Call<ForecastResponse> getFiveDayForecastByCoordinates(@Query("lat") double lat, @Query("lon") double lon, @Query("appid") String apiKey, @Query("units") String units);
    @GET("geo/1.0/direct") Call<List<GeocodingResponse>> searchCities(@Query("q") String query, @Query("limit") int limit, @Query("appid") String apiKey);
}