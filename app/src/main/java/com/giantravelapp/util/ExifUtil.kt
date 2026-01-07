package com.giantravelapp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.IOException

data class PhotoMetadata(
    val latitude: Double?,
    val longitude: Double?,
    val timestamp: Long,
    val orientation: Int = 0
)

object ExifUtil {
    
    /**
     * Estrae i dati EXIF da una foto (geolocation, timestamp, orientamento)
     */
    fun extractMetadata(context: Context, uri: Uri): PhotoMetadata {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return PhotoMetadata(
                latitude = null,
                longitude = null,
                timestamp = System.currentTimeMillis()
            )
            
            val exif = ExifInterface(inputStream)
            inputStream.close()
            
            // Estrai GPS coordinates
            val latLng = exif.latLong
            val latitude = latLng?.get(0)
            val longitude = latLng?.get(1)
            
            // Estrai timestamp
            val dateTimeString = exif.getAttribute(ExifInterface.TAG_DATETIME)
            val timestamp = if (dateTimeString != null) {
                try {
                    val sdf = java.text.SimpleDateFormat("yyyy:MM:dd HH:mm:ss", java.util.Locale.US)
                    sdf.parse(dateTimeString)?.time ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }
            } else {
                System.currentTimeMillis()
            }
            
            // Estrai orientamento
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            
            PhotoMetadata(
                latitude = latitude,
                longitude = longitude,
                timestamp = timestamp,
                orientation = orientation
            )
        } catch (e: Exception) {
            e.printStackTrace()
            PhotoMetadata(
                latitude = null,
                longitude = null,
                timestamp = System.currentTimeMillis()
            )
        }
    }
    
    /**
     * Ruota un bitmap in base all'orientamento EXIF
     */
    fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = android.graphics.Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(-90f)
                matrix.postScale(-1f, 1f)
            }
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    
    /**
     * Comprime e salva immagine
     */
    fun compressImage(bitmap: Bitmap, outputFile: File, quality: Int = 85): File {
        val fos = java.io.FileOutputStream(outputFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos)
        fos.close()
        return outputFile
    }
    
    /**
     * Legge immagine da URI e la decodifica
     */
    fun loadBitmapFromUri(context: Context, uri: Uri, maxWidth: Int = 1024, maxHeight: Int = 1024): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            
            // Prima decodifichiamo le dimensioni
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()
            
            // Calcola sample size
            val sampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
            
            // Apri nuovamente lo stream e decodifica con sample size
            val inputStream2 = context.contentResolver.openInputStream(uri) ?: return null
            val finalOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val bitmap = BitmapFactory.decodeStream(inputStream2, null, finalOptions)
            inputStream2.close()
            
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = kotlin.math.round(height.toFloat() / reqHeight.toFloat()).toInt()
            val widthRatio = kotlin.math.round(width.toFloat() / reqWidth.toFloat()).toInt()
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        return inSampleSize
    }
}
