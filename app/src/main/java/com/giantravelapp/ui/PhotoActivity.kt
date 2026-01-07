package com.giantravelapp.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.giantravelapp.R
import com.giantravelapp.adapter.PhotoAdapter
import com.giantravelapp.db.AppDatabase
import com.giantravelapp.model.Photo
import com.giantravelapp.util.ExifUtil
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class PhotoActivity : AppCompatActivity() {
    
    private lateinit var tripId: String
    private lateinit var photoAdapter: PhotoAdapter
    private lateinit var db: AppDatabase
    
    private lateinit var galleryButton: Button
    private lateinit var cameraButton: Button
    private lateinit var previewImage: ImageView
    private lateinit var captionInput: EditText
    private lateinit var saveButton: Button
    private lateinit var photosRecycler: RecyclerView
    private lateinit var progressBar: ProgressBar
    
    private var selectedImageUri: Uri? = null
    private var selectedPhoto: Photo? = null
    
    // Gallery picker
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        lifecycleScope.launch {
            uris.forEach { uri ->
                processImage(uri)
            }
        }
    }
    
    // Camera
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && selectedImageUri != null) {
            lifecycleScope.launch {
                processImage(selectedImageUri!!)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)
        
        tripId = intent.getStringExtra("tripId") ?: return
        db = AppDatabase.getDatabase(this)
        
        initViews()
        setupRecycler()
        loadPhotos()
    }
    
    private fun initViews() {
        galleryButton = findViewById(R.id.btn_gallery)
        cameraButton = findViewById(R.id.btn_camera)
        previewImage = findViewById(R.id.preview_image)
        captionInput = findViewById(R.id.caption_input)
        saveButton = findViewById(R.id.btn_save_photo)
        photosRecycler = findViewById(R.id.photos_recycler)
        progressBar = findViewById(R.id.progress_bar)
        
        galleryButton.setOnClickListener { openGallery() }
        cameraButton.setOnClickListener { openCamera() }
        saveButton.setOnClickListener { savePhoto() }
    }
    
    private fun setupRecycler() {
        photoAdapter = PhotoAdapter(
            onEditClick = { photo -> editPhoto(photo) },
            onDeleteClick = { photo -> deletePhoto(photo) }
        )
        photosRecycler.layoutManager = LinearLayoutManager(this)
        photosRecycler.adapter = photoAdapter
    }
    
    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }
    
    private fun openCamera() {
        val photoFile = File(getExternalFilesDir(null), "camera_${System.currentTimeMillis()}.jpg")
        selectedImageUri = Uri.fromFile(photoFile)
        cameraLauncher.launch(selectedImageUri)
    }
    
    private suspend fun processImage(uri: Uri) {
        progressBar.visibility = android.view.View.VISIBLE
        
        try {
            // Estrai metadata EXIF
            val metadata = ExifUtil.extractMetadata(this, uri)
            
            // Carica bitmap
            var bitmap = ExifUtil.loadBitmapFromUri(this, uri) ?: run {
                Toast.makeText(this, "Errore nel caricamento immagine", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Ruota bitmap in base all'orientamento
            bitmap = ExifUtil.rotateBitmap(bitmap, metadata.orientation)
            
            // Salva file compresso
            val fileName = "${UUID.randomUUID()}.jpg"
            val outputFile = File(getExternalFilesDir(null), fileName)
            ExifUtil.compressImage(bitmap, outputFile, quality = 85)
            
            // Mostra preview
            selectedImageUri = uri
            previewImage.setImageBitmap(bitmap)
            previewImage.visibility = android.view.View.VISIBLE
            
            // Prepara foto per salvataggio
            selectedPhoto = Photo(
                id = UUID.randomUUID().toString(),
                tripId = tripId,
                filePath = outputFile.absolutePath,
                caption = "",
                latitude = metadata.latitude,
                longitude = metadata.longitude,
                timestamp = metadata.timestamp,
                createdAt = System.currentTimeMillis()
            )
            
            Toast.makeText(this, "Foto caricata! Aggiungi caption e salva.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Errore: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            progressBar.visibility = android.view.View.GONE
        }
    }
    
    private fun savePhoto() {
        if (selectedPhoto == null) {
            Toast.makeText(this, "Seleziona una foto prima", Toast.LENGTH_SHORT).show()
            return
        }
        
        val caption = captionInput.text.toString()
        val photoToSave = selectedPhoto!!.copy(caption = caption)
        
        lifecycleScope.launch {
            db.photoDao().insert(photoToSave)
            
            // Pulisci form
            captionInput.text.clear()
            previewImage.visibility = android.view.View.GONE
            selectedPhoto = null
            selectedImageUri = null
            
            Toast.makeText(this@PhotoActivity, "Foto salvata!", Toast.LENGTH_SHORT).show()
            
            // Ricarica lista
            loadPhotos()
        }
    }
    
    private fun editPhoto(photo: Photo) {
        selectedPhoto = photo
        selectedImageUri = Uri.fromFile(File(photo.filePath))
        captionInput.setText(photo.caption)
        
        // Mostra preview
        ExifUtil.loadBitmapFromUri(this, selectedImageUri!!)?.let {
            previewImage.setImageBitmap(it)
            previewImage.visibility = android.view.View.VISIBLE
        }
        
        // Scroll al top
        photosRecycler.scrollToPosition(0)
    }
    
    private fun deletePhoto(photo: Photo) {
        lifecycleScope.launch {
            // Cancella da database
            db.photoDao().delete(photo)
            
            // Cancella file
            try {
                File(photo.filePath).delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            Toast.makeText(this@PhotoActivity, "Foto eliminata", Toast.LENGTH_SHORT).show()
            loadPhotos()
        }
    }
    
    private fun loadPhotos() {
        lifecycleScope.launch {
            val photos = db.photoDao().getPhotosByTrip(tripId)
            photoAdapter.submitList(photos)
        }
    }
}
