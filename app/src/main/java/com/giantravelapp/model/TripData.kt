package com.giantravelapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val isActive: Boolean = true,
    val shareCode: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class LocationPoint(
    val id: Long = 0,
    val tripId: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val accuracy: Float = 0f,
    val speed: Float = 0f,
    val bearing: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val temperature: Double? = null,
    val humidity: Int? = null,
    val weatherCondition: String? = null
)

data class TripPhoto(
    val id: Long = 0,
    val tripId: Long,
    val filePath: String,
    val latitude: Double?,
    val longitude: Double?,
    val timestamp: Long = System.currentTimeMillis(),
    val caption: String = ""
)

data class DiaryEntry(
    val id: Long = 0,
    val tripId: Long,
    val title: String,
    val content: String,
    val latitude: Double?,
    val longitude: Double?,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String? = null
)

data class Comment(
    val id: Long = 0,
    val tripId: Long,
    val friendName: String,
    val friendAvatar: String? = null,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double?,
    val longitude: Double?
)

data class Friend(
    val id: Long = 0,
    val userId: String,
    val name: String,
    val email: String,
    val avatar: String? = null,
    val isFollowing: Boolean = false
)

data class WeatherInfo(
    val latitude: Double,
    val longitude: Double,
    val temperature: Double,
    val condition: String,
    val humidity: Int,
    val windSpeed: Double,
    val icon: String,
    val timestamp: Long = System.currentTimeMillis()
)
