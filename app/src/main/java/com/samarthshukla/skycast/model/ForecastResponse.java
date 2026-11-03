package com.samarthshukla.skycast.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ForecastResponse {

    @SerializedName("list")
    private List<ForecastEntry> forecastEntries;

    @SerializedName("city")
    private CityData city;

    public List<ForecastEntry> getForecastEntries() {
        return forecastEntries;
    }

    public CityData getCity() {
        return city;
    }

    // -----------------------------------------
    // FORECAST ENTRY
    // -----------------------------------------

    public static class ForecastEntry {

        @SerializedName("dt")
        private long timestamp;

        @SerializedName("dt_txt")
        private String dateTime;

        @SerializedName("main")
        private MainData main;

        @SerializedName("weather")
        private List<WeatherData> weather;

        // Probability of precipitation: 0.0 to 1.0
        @SerializedName("pop")
        private double precipitationProbability;

        public long getTimestamp() {
            return timestamp;
        }

        public String getDateTime() {
            return dateTime;
        }

        public MainData getMain() {
            return main;
        }

        public List<WeatherData> getWeather() {
            return weather;
        }

        public double getPrecipitationProbability() {
            return precipitationProbability;
        }

        // Returns precipitation probability as percentage.
        public int getRainPercentage() {
            return (int) Math.round(
                    Math.max(0.0, Math.min(1.0, precipitationProbability)) * 100
            );
        }
    }

    // -----------------------------------------
    // MAIN WEATHER DATA
    // -----------------------------------------

    public static class MainData {

        @SerializedName("temp")
        private double temperature;

        @SerializedName("temp_min")
        private double minTemperature;

        @SerializedName("temp_max")
        private double maxTemperature;

        public double getTemperature() {
            return temperature;
        }

        public double getMinTemperature() {
            return minTemperature;
        }

        public double getMaxTemperature() {
            return maxTemperature;
        }
    }

    // -----------------------------------------
    // WEATHER CONDITION
    // -----------------------------------------

    public static class WeatherData {

        @SerializedName("main")
        private String main;

        @SerializedName("description")
        private String description;

        @SerializedName("icon")
        private String icon;

        public String getMain() {
            return main;
        }

        public String getDescription() {
            return description;
        }

        public String getIcon() {
            return icon;
        }
    }

    // -----------------------------------------
    // CITY DATA
    // -----------------------------------------

    public static class CityData {

        @SerializedName("name")
        private String name;

        public String getName() {
            return name;
        }
    }
}