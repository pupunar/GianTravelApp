package com.giantravelapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.giantravelapp.R
import com.giantravelapp.db.AppDatabase
import com.giantravelapp.export.PDFExporter
import com.giantravelapp.export.GPXExporter
import kotlinx.coroutines.launch

class TripDetailActivity : AppCompatActivity() {
    
    private lateinit var googleMap: GoogleMap
    private var tripId: Long = -1
    private var polyline: Polyline? = null
    private val db by lazy { AppDatabase.getInstance(this) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_detail)
        
        tripId = intent.getLongExtra("tripId", -1)
        
        // Initialize Map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync { map ->
            googleMap = map
            loadAndDrawTrip()
        }
        
        // Setup Buttons
        setupButtons()
    }
    
    private fun setupButtons() {
        findViewById<MaterialButton>(R.id.btnAddPhoto).setOnClickListener {
            val intent = Intent(this, PhotoActivity::class.java)
            intent.putExtra("tripId", tripId)
            startActivity(intent)
        }
        
        findViewById<MaterialButton>(R.id.btnDiary).setOnClickListener {
            val intent = Intent(this, DiaryActivity::class.java)
            intent.putExtra("tripId", tripId)
            startActivity(intent)
        }
        
        findViewById<MaterialButton>(R.id.btnShare).setOnClickListener {
            val intent = Intent(this, ShareTripActivity::class.java)
            intent.putExtra("tripId", tripId)
            startActivity(intent)
        }
        
        findViewById<MaterialButton>(R.id.btnExportPDF).setOnClickListener {
            exportToPDF()
        }
        
        findViewById<MaterialButton>(R.id.btnExportGPX).setOnClickListener {
            exportToGPX()
        }
        
        findViewById<MaterialButton>(R.id.btnComments).setOnClickListener {
            val intent = Intent(this, CommentsActivity::class.java)
            intent.putExtra("tripId", tripId)
            startActivity(intent)
        }
    }
    
    private fun loadAndDrawTrip() {
        lifecycleScope.launch {
            val locations = db.locationPointDao().getLocationsByTripId(tripId)
            
            if (locations.isNotEmpty()) {
                // Draw polyline
                val polylineOptions = PolylineOptions()
                    .color(android.graphics.Color.BLUE)
                    .width(5f)
                
                for (location in locations) {
                    polylineOptions.add(
                        LatLng(location.latitude, location.longitude)
                    )
                }
                
                polyline = googleMap.addPolyline(polylineOptions)
                
                // Center map on first location
                val firstLocation = locations.first()
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(firstLocation.latitude, firstLocation.longitude),
                        13f
                    )
                )
                
                // Add markers for start and end
                val lastLocation = locations.last()
                googleMap.addMarker(
                    com.google.android.gms.maps.model.MarkerOptions()
                        .position(LatLng(firstLocation.latitude, firstLocation.longitude))
                        .title("Start")
                )
                googleMap.addMarker(
                    com.google.android.gms.maps.model.MarkerOptions()
                        .position(LatLng(lastLocation.latitude, lastLocation.longitude))
                        .title("Current Location")
                )
                
                // Add photos
                loadPhotosOnMap()
            }
        }
    }
    
    private fun loadPhotosOnMap() {
        lifecycleScope.launch {
            val photos = db.tripPhotoDao().getPhotosByTripId(tripId)
            for (photo in photos) {
                if (photo.latitude != null && photo.longitude != null) {
                    googleMap.addMarker(
                        com.google.android.gms.maps.model.MarkerOptions()
                            .position(LatLng(photo.latitude!!, photo.longitude!!))
                            .title(photo.caption)
                            .snippet("Photo")
                    )
                }
            }
        }
    }
    
    private fun exportToPDF() {
        lifecycleScope.launch {
            val trip = db.tripDao().getTripById(tripId) ?: return@launch
            val locations = db.locationPointDao().getLocationsByTripId(tripId)
            val photos = db.tripPhotoDao().getPhotosByTripId(tripId)
            val diaryEntries = db.diaryEntryDao().getEntriesByTripId(tripId)
            
            val pdfPath = PDFExporter.exportTripToPDF(
                this@TripDetailActivity,
                trip,
                locations,
                photos,
                diaryEntries
            )
            
            // Show success message
            android.widget.Toast.makeText(
                this@TripDetailActivity,
                "PDF exported to $pdfPath",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
    
    private fun exportToGPX() {
        lifecycleScope.launch {
            val locations = db.locationPointDao().getLocationsByTripId(tripId)
            val trip = db.tripDao().getTripById(tripId) ?: return@launch
            
            val gpxPath = GPXExporter.exportToGPX(
                this@TripDetailActivity,
                trip,
                locations
            )
            
            android.widget.Toast.makeText(
                this@TripDetailActivity,
                "GPX exported to $gpxPath",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
}
