package com.giantravelapp.export

import android.content.Context
import android.os.Environment
import com.giantravelapp.model.Trip
import com.giantravelapp.model.LocationPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object GPXExporter {
    
    fun exportToGPX(
        context: Context,
        trip: Trip,
        locations: List<LocationPoint>
    ): String {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOCUMENTS
        )
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
        
        val fileName = "${trip.name}_${System.currentTimeMillis()}.gpx"
        val file = File(downloadsDir, fileName)
        
        val gpxContent = buildGPXContent(trip, locations)
        file.writeText(gpxContent)
        
        return file.absolutePath
    }
    
    private fun buildGPXContent(trip: Trip, locations: List<LocationPoint>): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        
        val sb = StringBuilder()
        
        // GPX Header
        sb.append("""<?xml version="1.0" encoding="UTF-8"?>
""")
        sb.append("""<gpx version="1.1" creator="GianTravelApp"
""")
        sb.append("""xmlns="http://www.topografix.com/GPX/1/1"
""")
        sb.append("""xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
""")
        sb.append("""xsi:schemaLocation="http://www.topografix.com/GPX/1/1">
""")
        
        // Metadata
        sb.append("""  <metadata>
""")
        sb.append("""    <name>${escapeXml(trip.name)}</name>
""")
        sb.append("""    <desc>${escapeXml(trip.description)}</desc>
""")
        sb.append("""    <time>${sdf.format(Date(trip.startDate))}</time>
""")
        sb.append("""  </metadata>
""")
        
        // Track
        sb.append("""  <trk>
""")
        sb.append("""    <name>${escapeXml(trip.name)}</name>
""")
        sb.append("""    <trkseg>
""")
        
        // Track Points
        for (location in locations) {
            sb.append("""      <trkpt lat="${location.latitude}" lon="${location.longitude}">
""")
            if (location.altitude > 0) {
                sb.append("""        <ele>${location.altitude}</ele>
""")
            }
            sb.append("""        <time>${sdf.format(Date(location.timestamp))}</time>
""")
            if (location.accuracy > 0) {
                sb.append("""        <accuracy>${location.accuracy}</accuracy>
""")
            }
            sb.append("""      </trkpt>
""")
        }
        
        sb.append("""    </trkseg>
""")
        sb.append("""  </trk>
""")
        sb.append("""</gpx>""")
        
        return sb.toString()
    }
    
    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
