package com.giantravelapp.repository

import android.content.Context
import com.giantravelapp.api.WeatherService
import com.giantravelapp.db.AppDatabase
import com.giantravelapp.model.LocationPoint
import com.giantravelapp.model.WeatherInfo
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(
    private val context: Context,
    private val apiKey: String
) {
    
    private val weatherService: WeatherService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)
    }
    
    private val db = AppDatabase.getInstance(context)
    private val cache = mutableMapOf<String, WeatherInfo>()
    private val cacheExpiry = 600000L // 10 minuti
    
    suspend fun getWeatherForLocation(
        latitude: Double,
        longitude: Double,
        tripId: Long
    ): WeatherInfo? = withContext(Dispatchers.IO) {
        try {
            // Controlla cache
            val cacheKey = "${latitude}_${longitude}"
            val cached = cache[cacheKey]
            if (cached != null && System.currentTimeMillis() - cached.timestamp < cacheExpiry) {
                return@withContext cached
            }
            
            // Fetch da API
            val response = weatherService.getCurrentWeather(
                latitude = latitude,
                longitude = longitude,
                apiKey = apiKey
            )
            
            // Converti in WeatherInfo
            val weatherInfo = WeatherInfo(
                latitude = latitude,
                longitude = longitude,
                temperature = response.main.temp,
                condition = response.weather.firstOrNull()?.main ?: "Unknown",
                humidity = response.main.humidity,
                windSpeed = response.wind.speed,
                icon = response.weather.firstOrNull()?.icon ?: "01d",
                timestamp = System.currentTimeMillis()
            )
            
            // Salva in cache
            cache[cacheKey] = weatherInfo
            
            return@withContext weatherInfo
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
    
    suspend fun enrichLocationWithWeather(
        location: LocationPoint,
        tripId: Long
    ): LocationPoint = withContext(Dispatchers.IO) {
        try {
            val weather = getWeatherForLocation(
                location.latitude,
                location.longitude,
                tripId
            )
            
            return@withContext location.copy(
                temperature = weather?.temperature,
                humidity = weather?.humidity,
                weatherCondition = weather?.condition
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext location
        }
    }
    
    suspend fun getWeatherForTrip(tripId: Long): List<WeatherInfo> = withContext(Dispatchers.IO) {
        try {
            val locations = db.locationPointDao().getLocationsByTripId(tripId)
            val weatherList = mutableListOf<WeatherInfo>()
            
            // Fetch meteo ogni 20 punti per evitare rate limiting
            locations.forEachIndexed { index, location ->
                if (index % 20 == 0) {
                    val weather = getWeatherForLocation(
                        location.latitude,
                        location.longitude,
                        tripId
                    )
                    if (weather != null) {
                        weatherList.add(weather)
                    }
                }
            }
            
            return@withContext weatherList
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }
    
    fun clearCache() {
        cache.clear()
    }
}
