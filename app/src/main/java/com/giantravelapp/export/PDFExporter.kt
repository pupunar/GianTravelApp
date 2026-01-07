package com.giantravelapp.export

import android.content.Context
import android.os.Environment
import com.giantravelapp.model.Trip
import com.giantravelapp.model.LocationPoint
import com.giantravelapp.model.TripPhoto
import com.giantravelapp.model.DiaryEntry
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.TextAlignment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object PDFExporter {
    
    fun exportTripToPDF(
        context: Context,
        trip: Trip,
        locations: List<LocationPoint>,
        photos: List<TripPhoto>,
        diaryEntries: List<DiaryEntry>
    ): String {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOCUMENTS
        )
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
        
        val fileName = "Trip_${trip.name}_${System.currentTimeMillis()}.pdf"
        val file = File(downloadsDir, fileName)
        
        val pdfWriter = PdfWriter(file)
        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument)
        
        // Title
        document.add(
            Paragraph(trip.name)
                .setBold()
                .setFontSize(24f)
                .setTextAlignment(TextAlignment.CENTER)
        )
        
        // Trip Info
        document.add(
            Paragraph("Trip Information")
                .setBold()
                .setFontSize(14f)
        )
        
        val tripTable = Table(floatArrayOf(150f, 300f))
        tripTable.addCell("Trip Name:")
        tripTable.addCell(trip.name)
        tripTable.addCell("Description:")
        tripTable.addCell(trip.description)
        tripTable.addCell("Start Date:")
        tripTable.addCell(formatDate(trip.startDate))
        tripTable.addCell("Total Distance:")
        tripTable.addCell("${calculateDistance(locations)} km")
        tripTable.addCell("Total Waypoints:")
        tripTable.addCell(locations.size.toString())
        document.add(tripTable)
        
        // Diary Entries
        if (diaryEntries.isNotEmpty()) {
            document.add(Paragraph("\nTravel Diary").setBold().setFontSize(14f))
            for (entry in diaryEntries) {
                document.add(
                    Paragraph("${entry.title} - ${formatDate(entry.timestamp)}")
                        .setBold()
                )
                document.add(Paragraph(entry.content))
                document.add(Paragraph(""))
            }
        }
        
        // Photos Summary
        if (photos.isNotEmpty()) {
            document.add(Paragraph("\nPhotos").setBold().setFontSize(14f))
            document.add(Paragraph("Total photos: ${photos.size}"))
            for (photo in photos.take(5)) { // Show first 5 photos
                document.add(
                    Paragraph("${photo.caption} - ${formatDate(photo.timestamp)}")
                )
            }
            if (photos.size > 5) {
                document.add(Paragraph("... and ${photos.size - 5} more photos"))
            }
        }
        
        // Statistics
        document.add(Paragraph("\nStatistics").setBold().setFontSize(14f))
        val statsTable = Table(floatArrayOf(200f, 200f))
        statsTable.addCell("Metric")
        statsTable.addCell("Value")
        statsTable.addCell("Total Duration")
        statsTable.addCell(calculateDuration(trip))
        statsTable.addCell("Highest Altitude")
        statsTable.addCell("${locations.maxByOrNull { it.altitude }?.altitude ?: 0} m")
        statsTable.addCell("Average Speed")
        statsTable.addCell("${calculateAverageSpeed(locations)} km/h")
        document.add(statsTable)
        
        document.close()
        return file.absolutePath
    }
    
    private fun calculateDistance(locations: List<LocationPoint>): String {
        if (locations.size < 2) return "0"
        
        var distance = 0.0
        for (i in 1 until locations.size) {
            val lat1 = locations[i-1].latitude
            val lon1 = locations[i-1].longitude
            val lat2 = locations[i].latitude
            val lon2 = locations[i].longitude
            
            distance += calculateHaversineDistance(lat1, lon1, lat2, lon2)
        }
        
        return String.format("%.2f", distance)
    }
    
    private fun calculateHaversineDistance(
        lat1: Double, lon1: Double, lat2: Double, lon2: Double
    ): Double {
        val R = 6371 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon/2) * Math.sin(dLon/2)
        val c = 2 * Math.asin(Math.sqrt(a))
        return R * c
    }
    
    private fun calculateAverageSpeed(locations: List<LocationPoint>): String {
        if (locations.isEmpty()) return "0"
        val avgSpeed = locations.map { it.speed }.average()
        return String.format("%.2f", avgSpeed)
    }
    
    private fun calculateDuration(trip: Trip): String {
        val duration = System.currentTimeMillis() - trip.startDate
        val hours = duration / (1000 * 60 * 60)
        return "$hours hours"
    }
    
    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
