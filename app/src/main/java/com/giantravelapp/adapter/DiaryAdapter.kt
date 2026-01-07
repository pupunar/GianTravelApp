package com.giantravelapp.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.giantravelapp.R
import com.giantravelapp.model.DiaryEntry
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiaryAdapter(
    private val context: Context,
    private val onEditClick: (DiaryEntry) -> Unit,
    private val onDeleteClick: (DiaryEntry) -> Unit
) : ListAdapter<DiaryEntry, DiaryAdapter.DiaryViewHolder>(DiaryDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_diary, parent, false)
        return DiaryViewHolder(view, context, onEditClick, onDeleteClick)
    }
    
    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class DiaryViewHolder(
        itemView: View,
        private val context: Context,
        private val onEditClick: (DiaryEntry) -> Unit,
        private val onDeleteClick: (DiaryEntry) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val moodEmoji: TextView = itemView.findViewById(R.id.mood_emoji)
        private val titleText: TextView = itemView.findViewById(R.id.diary_title_text)
        private val contentText: TextView = itemView.findViewById(R.id.diary_content_text)
        private val timeText: TextView = itemView.findViewById(R.id.diary_time_text)
        private val weatherIcon: TextView = itemView.findViewById(R.id.weather_icon)
        private val photoContainer: LinearLayout = itemView.findViewById(R.id.photo_container_diary)
        private val photoImage: ImageView = itemView.findViewById(R.id.diary_photo)
        private val coordsText: TextView = itemView.findViewById(R.id.diary_coords_text)
        private val editButton: ImageButton = itemView.findViewById(R.id.btn_edit_diary)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.btn_delete_diary)
        
        fun bind(entry: DiaryEntry) {
            // Mood emoji
            moodEmoji.text = entry.mood.take(1) // Prende la prima emoji
            
            // Titolo e contenuto
            titleText.text = entry.title
            contentText.text = entry.content
            
            // Timestamp
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ITALIAN)
            timeText.text = sdf.format(Date(entry.timestamp))
            
            // Meteo
            if (entry.weather.isNotEmpty()) {
                weatherIcon.text = "🌟 ${entry.weather}"
                weatherIcon.visibility = View.VISIBLE
            } else {
                weatherIcon.visibility = View.GONE
            }
            
            // Foto
            if (entry.photoPath != null) {
                val file = File(entry.photoPath)
                if (file.exists()) {
                    photoImage.setImageURI(Uri.fromFile(file))
                    photoContainer.visibility = View.VISIBLE
                }
            } else {
                photoContainer.visibility = View.GONE
            }
            
            // Coordinate
            if (entry.latitude != null && entry.longitude != null) {
                coordsText.text = String.format(
                    "📍 %.4f, %.4f",
                    entry.latitude,
                    entry.longitude
                )
                coordsText.visibility = View.VISIBLE
            } else {
                coordsText.visibility = View.GONE
            }
            
            // Buttons
            editButton.setOnClickListener { onEditClick(entry) }
            deleteButton.setOnClickListener { onDeleteClick(entry) }
        }
    }
    
    class DiaryDiffCallback : DiffUtil.ItemCallback<DiaryEntry>() {
        override fun areItemsTheSame(oldItem: DiaryEntry, newItem: DiaryEntry) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: DiaryEntry, newItem: DiaryEntry) = oldItem == newItem
    }
}
