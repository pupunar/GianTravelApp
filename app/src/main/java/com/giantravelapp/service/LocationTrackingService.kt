package com.giantravelapp.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.giantravelapp.R
import com.giantravelapp.db.AppDatabase
import com.giantravelapp.model.LocationPoint
import com.giantravelapp.repository.WeatherRepository
import kotlinx.coroutines.*

class LocationTrackingService : Service() {
    
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var currentTripId: Long = -1
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var weatherRepository: WeatherRepository
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "location_tracking"
    }
    
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationCallback()
        
        // Inizializza weather repository
        // Nota: Aggiungi OPENWEATHER_API_KEY in strings.xml
        val apiKey = getString(R.string.openweather_api_key)
        weatherRepository = WeatherRepository(this, apiKey)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        currentTripId = intent?.getLongExtra("tripId", -1) ?: -1
        startLocationUpdates()
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }
    
    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    saveLocation(location)
                }
            }
        }
    }
    
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000 // Update ogni 5 secondi
        ).apply {
            setMinUpdateDistanceMeters(10f) // Update se mosso 10 metri
        }.build()
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
    
    private fun saveLocation(location: android.location.Location) {
        if (currentTripId <= 0) return
        
        scope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance(this@LocationTrackingService)
                
                // Crea punto di localizzazione
                var locationPoint = LocationPoint(
                    tripId = currentTripId,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitude = location.altitude,
                    accuracy = location.accuracy,
                    speed = location.speed,
                    bearing = location.bearing,
                    timestamp = System.currentTimeMillis()
                )
                
                // Arricchisci con dati meteo (ogni 20 punti per evitare rate limiting)
                if (System.currentTimeMillis() % 20 == 0L) {
                    locationPoint = weatherRepository.enrichLocationWithWeather(
                        locationPoint,
                        currentTripId
                    )
                }
                
                db.locationPointDao().insert(locationPoint)
                
                // Aggiorna Firebase se necessario
                updateFirebaseLocation(locationPoint)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private suspend fun updateFirebaseLocation(locationPoint: LocationPoint) {
        // TODO: Implementare sincronizzazione Firebase
        // Questo permetterà agli amici di seguire real-time via web
        withContext(Dispatchers.IO) {
            try {
                // val firebaseDatabase = FirebaseDatabase.getInstance()
                // val tripRef = firebaseDatabase.getReference("trips/$currentTripId/locations")
                // tripRef.child(locationPoint.id.toString()).setValue(locationPoint)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun createNotification(): android.app.Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GianTravelApp")
            .setContentText("🌍 Tracking your journey with weather...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        weatherRepository.clearCache()
        scope.cancel()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
