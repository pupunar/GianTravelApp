package com.giantravelapp.api

import com.giantravelapp.model.WeatherInfo
import retrofit2.http.GET
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName

interface WeatherService {
    
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}

data class WeatherResponse(
    @SerializedName("coord")
    val coordinates: Coordinates,
    @SerializedName("main")
    val main: MainWeatherData,
    @SerializedName("weather")
    val weather: List<WeatherDescription>,
    @SerializedName("wind")
    val wind: Wind,
    @SerializedName("clouds")
    val clouds: Clouds,
    @SerializedName("dt")
    val timestamp: Long
)

data class Coordinates(
    val lon: Double,
    val lat: Double
)

data class MainWeatherData(
    val temp: Double,
    @SerializedName("feels_like")
    val feelsLike: Double,
    @SerializedName("temp_min")
    val tempMin: Double,
    @SerializedName("temp_max")
    val tempMax: Double,
    val pressure: Int,
    val humidity: Int
)

data class WeatherDescription(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Double,
    val deg: Int?,
    val gust: Double?
)

data class Clouds(
    val all: Int
)
