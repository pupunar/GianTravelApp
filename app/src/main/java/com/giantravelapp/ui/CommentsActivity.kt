package com.giantravelapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.giantravelapp.R

class CommentsActivity : AppCompatActivity() {
    
    private var tripId: Long = -1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
        
        tripId = intent.getLongExtra("tripId", -1)
        
        // TODO: Display comments from friends
        // TODO: Add new comment with location
        // TODO: Sync comments with backend
        // TODO: Real-time comment updates
        // TODO: Comment notifications
    }
}
