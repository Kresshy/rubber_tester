package com.kresshy.rubbertester.weather;

public interface WeatherListener {
    void weatherDataReceived(WeatherData weatherData);

    void measurementReceived(WeatherMeasurement weatherMeasurement);
}
