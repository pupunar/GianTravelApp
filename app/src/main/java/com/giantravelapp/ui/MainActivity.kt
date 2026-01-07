package com.giantravelapp.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.giantravelapp.R
import com.giantravelapp.adapter.TripAdapter
import com.giantravelapp.db.AppDatabase
import com.giantravelapp.model.Trip
import com.giantravelapp.service.LocationTrackingService
import com.giantravelapp.viewmodel.TripViewModel
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TripAdapter
    private lateinit var btnNewTrip: MaterialButton
    private lateinit var viewModel: TripViewModel
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    companion object {
        private const val PERMISSION_REQUEST = 100
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize UI
        recyclerView = findViewById(R.id.recyclerViewTrips)
        btnNewTrip = findViewById(R.id.btnNewTrip)
        
        // Setup RecyclerView
        adapter = TripAdapter { trip ->
            onTripSelected(trip)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // Setup ViewModel
        viewModel = ViewModelProvider(this).get(TripViewModel::class.java)
        
        // Load trips
        loadTrips()
        
        // New Trip Button
        btnNewTrip.setOnClickListener {
            showNewTripDialog()
        }
        
        // Request permissions
        requestPermissions()
    }
    
    private fun loadTrips() {
        scope.launch {
            val trips = AppDatabase.getInstance(this@MainActivity)
                .tripDao().getAllTrips()
            adapter.submitList(trips)
        }
    }
    
    private fun showNewTripDialog() {
        val input = TextInputEditText(this)
        
        MaterialAlertDialogBuilder(this)
            .setTitle("New Trip")
            .setMessage("Enter trip name")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val tripName = input.text.toString()
                if (tripName.isNotEmpty()) {
                    createNewTrip(tripName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun createNewTrip(name: String) {
        scope.launch {
            val trip = Trip(
                name = name,
                description = "Trip to discover new places",
                isActive = true,
                shareCode = generateShareCode()
            )
            val tripId = AppDatabase.getInstance(this@MainActivity)
                .tripDao().insert(trip)
            
            // Start location tracking
            startLocationTracking(tripId)
            
            // Refresh list
            loadTrips()
        }
    }
    
    private fun generateShareCode(): String {
        return "SHARE_" + System.currentTimeMillis().toString().takeLast(8)
    }
    
    private fun onTripSelected(trip: Trip) {
        val intent = Intent(this, TripDetailActivity::class.java)
        intent.putExtra("tripId", trip.id)
        startActivity(intent)
    }
    
    private fun startLocationTracking(tripId: Long) {
        val intent = Intent(this, LocationTrackingService::class.java)
        intent.putExtra("tripId", tripId)
        startService(intent)
    }
    
    private fun requestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
        
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
