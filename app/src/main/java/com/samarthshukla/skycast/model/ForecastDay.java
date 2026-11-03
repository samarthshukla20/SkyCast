package com.samarthshukla.skycast.model;

public class ForecastDay {

    private final String day;
    private final String weatherIcon;
    private final String condition;
    private final int maxTemp;
    private final int minTemp;
    private final int precipitationProbability;

    public ForecastDay(
            String day,
            String weatherIcon,
            String condition,
            int maxTemp,
            int minTemp,
            int precipitationProbability
    ) {
        this.day = day;
        this.weatherIcon = weatherIcon;
        this.condition = condition;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.precipitationProbability = precipitationProbability;
    }

    public String getDay() {
        return day;
    }

    public String getWeatherIcon() {
        return weatherIcon;
    }

    public String getCondition() {
        return condition;
    }

    public int getMaxTemp() {
        return maxTemp;
    }

    public int getMinTemp() {
        return minTemp;
    }

    public int getPrecipitationProbability() {
        return precipitationProbability;
    }
}