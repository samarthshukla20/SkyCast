package com.samarthshukla.skycast.data.api;

import com.samarthshukla.skycast.model.ForecastResponse;
import com.samarthshukla.skycast.model.WeatherResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {

    @GET("data/2.5/weather")
    Call<WeatherResponse> getCurrentWeather(
            @Query("q") String city,
            @Query("appid") String apiKey,
            @Query("units") String units
    );

    @GET("data/2.5/forecast")
    Call<ForecastResponse> getFiveDayForecast(
            @Query("q") String city,
            @Query("appid") String apiKey,
            @Query("units") String units
    );
}