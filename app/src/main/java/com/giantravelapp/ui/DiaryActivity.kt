package com.giantravelapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.giantravelapp.R

class DiaryActivity : AppCompatActivity() {
    
    private var tripId: Long = -1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary)
        
        tripId = intent.getLongExtra("tripId", -1)
        
        // TODO: Display diary entries list
        // TODO: Add new diary entry
        // TODO: Edit existing entries
        // TODO: Associate location with entry
        // TODO: Add photo to diary
    }
}
