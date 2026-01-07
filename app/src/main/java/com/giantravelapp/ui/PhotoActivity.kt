package com.giantravelapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.giantravelapp.R

class PhotoActivity : AppCompatActivity() {
    
    private var tripId: Long = -1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)
        
        tripId = intent.getLongExtra("tripId", -1)
        
        // TODO: Implement photo gallery import
        // TODO: Extract EXIF geotag from photos
        // TODO: Display photos on map
        // TODO: Allow caption editing
    }
}
