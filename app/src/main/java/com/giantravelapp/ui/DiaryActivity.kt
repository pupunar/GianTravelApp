package com.giantravelapp.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.giantravelapp.R
import com.giantravelapp.adapter.DiaryAdapter
import com.giantravelapp.db.AppDatabase
import com.giantravelapp.model.DiaryEntry
import com.giantravelapp.util.ExifUtil
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class DiaryActivity : AppCompatActivity() {
    
    private lateinit var tripId: String
    private lateinit var diaryAdapter: DiaryAdapter
    private lateinit var db: AppDatabase
    
    private lateinit var titleInput: EditText
    private lateinit var contentInput: EditText
    private lateinit var moodSpinner: androidx.appcompat.widget.AppCompatSpinner
    private lateinit var photoButton: ImageButton
    private lateinit var photoPreview: ImageView
    private lateinit var weatherInput: EditText
    private lateinit var saveButton: Button
    private lateinit var diaryRecycler: RecyclerView
    private lateinit var emptyState: TextView
    private lateinit var progressBar: ProgressBar
    
    private var selectedPhotoUri: Uri? = null
    private var selectedEntry: DiaryEntry? = null
    private var currentLocationLat: Double? = null
    private var currentLocationLon: Double? = null
    
    // Gallery picker
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedPhotoUri = it }
        uri?.let { photoPreview.setImageURI(it) }
        photoPreview.visibility = android.view.View.VISIBLE
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary)
        
        tripId = intent.getStringExtra("tripId") ?: return
        currentLocationLat = intent.getDoubleExtra("latitude", 0.0).takeIf { it != 0.0 }
        currentLocationLon = intent.getDoubleExtra("longitude", 0.0).takeIf { it != 0.0 }
        
        db = AppDatabase.getDatabase(this)
        
        initViews()
        setupRecycler()
        setupMoodSpinner()
        loadDiaryEntries()
    }
    
    private fun initViews() {
        titleInput = findViewById(R.id.diary_title)
        contentInput = findViewById(R.id.diary_content)
        moodSpinner = findViewById(R.id.mood_spinner)
        photoButton = findViewById(R.id.btn_add_photo)
        photoPreview = findViewById(R.id.photo_preview_diary)
        weatherInput = findViewById(R.id.weather_input)
        saveButton = findViewById(R.id.btn_save_entry)
        diaryRecycler = findViewById(R.id.diary_recycler)
        emptyState = findViewById(R.id.empty_state)
        progressBar = findViewById(R.id.progress_bar)
        
        photoButton.setOnClickListener { selectPhoto() }
        saveButton.setOnClickListener { saveDiaryEntry() }
    }
    
    private fun setupRecycler() {
        diaryAdapter = DiaryAdapter(
            context = this,
            onEditClick = { entry -> editEntry(entry) },
            onDeleteClick = { entry -> deleteEntry(entry) }
        )
        diaryRecycler.layoutManager = LinearLayoutManager(this).apply {
            reverseLayout = true
            stackFromEnd = true
        }
        diaryRecycler.adapter = diaryAdapter
    }
    
    private fun setupMoodSpinner() {
        val moods = arrayOf("😀 Happy", "😂 Excited", "😘 Neutral", "😟 Sad", "😠 Angry")
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, moods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        moodSpinner.adapter = adapter
    }
    
    private fun selectPhoto() {
        galleryLauncher.launch("image/*")
    }
    
    private fun saveDiaryEntry() {
        val title = titleInput.text.toString().trim()
        val content = contentInput.text.toString().trim()
        val mood = moodSpinner.selectedItem.toString()
        val weather = weatherInput.text.toString().trim()
        
        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Titolo e contenuto sono obbligatori", Toast.LENGTH_SHORT).show()
            return
        }
        
        progressBar.visibility = android.view.View.VISIBLE
        
        lifecycleScope.launch {
            try {
                var photoPath: String? = null
                
                // Se c'è una foto, salva
                selectedPhotoUri?.let { uri ->
                    val bitmap = ExifUtil.loadBitmapFromUri(this@DiaryActivity, uri)
                    if (bitmap != null) {
                        val fileName = "diary_${UUID.randomUUID()}.jpg"
                        val outputFile = File(getExternalFilesDir(null), fileName)
                        ExifUtil.compressImage(bitmap, outputFile, quality = 85)
                        photoPath = outputFile.absolutePath
                    }
                }
                
                // Crea o aggiorna entry
                val entry = if (selectedEntry != null) {
                    selectedEntry!!.copy(
                        title = title,
                        content = content,
                        mood = mood,
                        weather = weather,
                        photoPath = photoPath ?: selectedEntry!!.photoPath,
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    DiaryEntry(
                        id = UUID.randomUUID().toString(),
                        tripId = tripId,
                        title = title,
                        content = content,
                        mood = mood,
                        weather = weather,
                        photoPath = photoPath,
                        latitude = currentLocationLat,
                        longitude = currentLocationLon,
                        timestamp = System.currentTimeMillis(),
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                }
                
                // Salva nel database
                db.diaryDao().upsert(entry)
                
                // Reset form
                clearForm()
                
                Toast.makeText(
                    this@DiaryActivity,
                    if (selectedEntry != null) "Entry aggiornata!" else "Entry creata!",
                    Toast.LENGTH_SHORT
                ).show()
                
                // Ricarica
                loadDiaryEntries()
                selectedEntry = null
                
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@DiaryActivity, "Errore: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = android.view.View.GONE
            }
        }
    }
    
    private fun editEntry(entry: DiaryEntry) {
        selectedEntry = entry
        titleInput.setText(entry.title)
        contentInput.setText(entry.content)
        weatherInput.setText(entry.weather)
        
        // Seleziona mood
        val moodIndex = when {
            entry.mood.contains("Happy") -> 0
            entry.mood.contains("Excited") -> 1
            entry.mood.contains("Neutral") -> 2
            entry.mood.contains("Sad") -> 3
            entry.mood.contains("Angry") -> 4
            else -> 0
        }
        moodSpinner.setSelection(moodIndex)
        
        // Mostra foto se esiste
        if (entry.photoPath != null) {
            val file = File(entry.photoPath)
            if (file.exists()) {
                photoPreview.setImageURI(Uri.fromFile(file))
                photoPreview.visibility = android.view.View.VISIBLE
            }
        }
        
        // Scroll al top
        diaryRecycler.scrollToPosition(0)
    }
    
    private fun deleteEntry(entry: DiaryEntry) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Elimina entry?")
            .setMessage("\"${entry.title}\" sarà eliminato permanentemente.")
            .setPositiveButton("Elimina") { _, _ ->
                lifecycleScope.launch {
                    db.diaryDao().delete(entry)
                    
                    // Cancella foto se esiste
                    entry.photoPath?.let { path ->
                        try {
                            File(path).delete()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    
                    Toast.makeText(this@DiaryActivity, "Entry eliminata", Toast.LENGTH_SHORT).show()
                    loadDiaryEntries()
                }
            }
            .setNegativeButton("Annulla", null)
            .show()
    }
    
    private fun clearForm() {
        titleInput.text.clear()
        contentInput.text.clear()
        weatherInput.text.clear()
        moodSpinner.setSelection(0)
        selectedPhotoUri = null
        photoPreview.visibility = android.view.View.GONE
    }
    
    private fun loadDiaryEntries() {
        lifecycleScope.launch {
            val entries = db.diaryDao().getEntriesByTrip(tripId)
            
            if (entries.isEmpty()) {
                emptyState.visibility = android.view.View.VISIBLE
                diaryRecycler.visibility = android.view.View.GONE
            } else {
                emptyState.visibility = android.view.View.GONE
                diaryRecycler.visibility = android.view.View.VISIBLE
                diaryAdapter.submitList(entries)
            }
        }
    }
}
