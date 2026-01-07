package com.giantravelapp.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.giantravelapp.R
import com.giantravelapp.model.Photo
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PhotoAdapter(
    private val onEditClick: (Photo) -> Unit,
    private val onDeleteClick: (Photo) -> Unit
) : ListAdapter<Photo, PhotoAdapter.PhotoViewHolder>(PhotoDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view, onEditClick, onDeleteClick)
    }
    
    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class PhotoViewHolder(
        itemView: View,
        private val onEditClick: (Photo) -> Unit,
        private val onDeleteClick: (Photo) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val photoImage: ImageView = itemView.findViewById(R.id.photo_image)
        private val captionText: TextView = itemView.findViewById(R.id.caption_text)
        private val coordsText: TextView = itemView.findViewById(R.id.coords_text)
        private val timeText: TextView = itemView.findViewById(R.id.time_text)
        private val editButton: ImageButton = itemView.findViewById(R.id.btn_edit)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.btn_delete)
        
        fun bind(photo: Photo) {
            // Carica immagine
            val file = File(photo.filePath)
            if (file.exists()) {
                photoImage.setImageURI(Uri.fromFile(file))
            }
            
            // Caption
            captionText.text = photo.caption.takeIf { it.isNotEmpty() } ?: "No caption"
            
            // Coordinate
            if (photo.latitude != null && photo.longitude != null) {
                coordsText.text = String.format(
                    "📍 %.4f, %.4f",
                    photo.latitude,
                    photo.longitude
                )
                coordsText.visibility = View.VISIBLE
            } else {
                coordsText.visibility = View.GONE
            }
            
            // Timestamp
            val sdf = SimpleDateFormat("HH:mm, dd/MM", Locale.getDefault())
            timeText.text = sdf.format(Date(photo.timestamp))
            
            // Buttons
            editButton.setOnClickListener { onEditClick(photo) }
            deleteButton.setOnClickListener { onDeleteClick(photo) }
        }
    }
    
    class PhotoDiffCallback : DiffUtil.ItemCallback<Photo>() {
        override fun areItemsTheSame(oldItem: Photo, newItem: Photo) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Photo, newItem: Photo) = oldItem == newItem
    }
}
