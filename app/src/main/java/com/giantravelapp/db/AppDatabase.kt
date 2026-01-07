package com.giantravelapp.db

import android.content.Context
import androidx.room.*
import com.giantravelapp.model.Trip
import com.giantravelapp.model.LocationPoint
import com.giantravelapp.model.TripPhoto
import com.giantravelapp.model.DiaryEntry
import com.giantravelapp.model.Comment

@Database(
    entities = [Trip::class, LocationPoint::class, TripPhoto::class, DiaryEntry::class, Comment::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun tripDao(): TripDao
    abstract fun locationPointDao(): LocationPointDao
    abstract fun tripPhotoDao(): TripPhotoDao
    abstract fun diaryEntryDao(): DiaryEntryDao
    abstract fun commentDao(): CommentDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "giantravelapp_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

@Dao
interface TripDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trip: Trip): Long
    
    @Update
    suspend fun update(trip: Trip)
    
    @Delete
    suspend fun delete(trip: Trip)
    
    @Query("SELECT * FROM trips ORDER BY startDate DESC")
    suspend fun getAllTrips(): List<Trip>
    
    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getTripById(id: Long): Trip?
    
    @Query("SELECT * FROM trips WHERE isActive = 1")
    suspend fun getActiveTrip(): Trip?
}

@Dao
interface LocationPointDao {
    @Insert
    suspend fun insert(point: LocationPoint): Long
    
    @Insert
    suspend fun insertAll(points: List<LocationPoint>)
    
    @Query("SELECT * FROM location_point WHERE tripId = :tripId ORDER BY timestamp ASC")
    suspend fun getLocationsByTripId(tripId: Long): List<LocationPoint>
    
    @Query("DELETE FROM location_point WHERE tripId = :tripId")
    suspend fun deleteByTripId(tripId: Long)
}

@Dao
interface TripPhotoDao {
    @Insert
    suspend fun insert(photo: TripPhoto): Long
    
    @Query("SELECT * FROM trip_photo WHERE tripId = :tripId ORDER BY timestamp DESC")
    suspend fun getPhotosByTripId(tripId: Long): List<TripPhoto>
    
    @Query("DELETE FROM trip_photo WHERE id = :id")
    suspend fun deletePhoto(id: Long)
}

@Dao
interface DiaryEntryDao {
    @Insert
    suspend fun insert(entry: DiaryEntry): Long
    
    @Update
    suspend fun update(entry: DiaryEntry)
    
    @Query("SELECT * FROM diary_entry WHERE tripId = :tripId ORDER BY timestamp DESC")
    suspend fun getEntriesByTripId(tripId: Long): List<DiaryEntry>
    
    @Query("DELETE FROM diary_entry WHERE id = :id")
    suspend fun deleteEntry(id: Long)
}

@Dao
interface CommentDao {
    @Insert
    suspend fun insert(comment: Comment): Long
    
    @Query("SELECT * FROM comment WHERE tripId = :tripId ORDER BY timestamp DESC")
    suspend fun getCommentsByTripId(tripId: Long): List<Comment>
}
