package com.samarthshukla.skycast.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {

    @SerializedName("name")
    private String cityName;

    @SerializedName("main")
    private MainData main;

    @SerializedName("weather")
    private List<WeatherData> weather;

    @SerializedName("wind")
    private WindData wind;

    public String getCityName() {
        return cityName;
    }

    public MainData getMain() {
        return main;
    }

    public List<WeatherData> getWeather() {
        return weather;
    }

    public WindData getWind() {
        return wind;
    }

    public static class MainData {

        @SerializedName("temp")
        private double temperature;

        @SerializedName("feels_like")
        private double feelsLike;

        @SerializedName("temp_min")
        private double minTemperature;

        @SerializedName("temp_max")
        private double maxTemperature;

        @SerializedName("pressure")
        private int pressure;

        @SerializedName("humidity")
        private int humidity;

        public double getTemperature() {
            return temperature;
        }

        public double getFeelsLike() {
            return feelsLike;
        }

        public double getMinTemperature() {
            return minTemperature;
        }

        public double getMaxTemperature() {
            return maxTemperature;
        }

        public int getPressure() {
            return pressure;
        }

        public int getHumidity() {
            return humidity;
        }
    }

    public static class WeatherData {

        @SerializedName("description")
        private String description;

        @SerializedName("icon")
        private String icon;

        public String getDescription() {
            return description;
        }

        public String getIcon() {
            return icon;
        }
    }

    public static class WindData {

        @SerializedName("speed")
        private double speed;

        public double getSpeed() {
            return speed;
        }
    }
}