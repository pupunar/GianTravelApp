package com.giantravelapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.giantravelapp.R
import com.giantravelapp.model.Trip
import java.text.SimpleDateFormat
import java.util.*

class TripAdapter(
    private val onTripClick: (Trip) -> Unit
) : ListAdapter<Trip, TripAdapter.TripViewHolder>(TripDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trip, parent, false)
        return TripViewHolder(view, onTripClick)
    }
    
    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class TripViewHolder(
        itemView: View,
        private val onTripClick: (Trip) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val tripNameTextView: TextView = itemView.findViewById(R.id.tripName)
        private val tripDescTextView: TextView = itemView.findViewById(R.id.tripDesc)
        private val tripDateTextView: TextView = itemView.findViewById(R.id.tripDate)
        private val tripStatusTextView: TextView = itemView.findViewById(R.id.tripStatus)
        
        fun bind(trip: Trip) {
            tripNameTextView.text = trip.name
            tripDescTextView.text = trip.description
            tripDateTextView.text = formatDate(trip.startDate)
            tripStatusTextView.text = if (trip.isActive) "🟢 Active" else "⏹️ Completed"
            
            itemView.setOnClickListener {
                onTripClick(trip)
            }
        }
        
        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
    
    private class TripDiffCallback : DiffUtil.ItemCallback<Trip>() {
        override fun areItemsTheSame(oldItem: Trip, newItem: Trip): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Trip, newItem: Trip): Boolean {
            return oldItem == newItem
        }
    }
}
