package com.giantravelapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giantravelapp.model.Trip
import com.giantravelapp.model.LocationPoint
import com.giantravelapp.model.TripPhoto
import com.giantravelapp.model.DiaryEntry
import kotlinx.coroutines.launch

class TripViewModel : ViewModel() {
    
    private val _trips = MutableLiveData<List<Trip>>()
    val trips: LiveData<List<Trip>> = _trips
    
    private val _currentTrip = MutableLiveData<Trip?>()
    val currentTrip: LiveData<Trip?> = _currentTrip
    
    private val _locations = MutableLiveData<List<LocationPoint>>()
    val locations: LiveData<List<LocationPoint>> = _locations
    
    private val _photos = MutableLiveData<List<TripPhoto>>()
    val photos: LiveData<List<TripPhoto>> = _photos
    
    private val _diaryEntries = MutableLiveData<List<DiaryEntry>>()
    val diaryEntries: LiveData<List<DiaryEntry>> = _diaryEntries
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    fun updateTrips(trips: List<Trip>) {
        _trips.value = trips
    }
    
    fun setCurrentTrip(trip: Trip) {
        _currentTrip.value = trip
    }
    
    fun updateLocations(locations: List<LocationPoint>) {
        _locations.value = locations
    }
    
    fun updatePhotos(photos: List<TripPhoto>) {
        _photos.value = photos
    }
    
    fun updateDiaryEntries(entries: List<DiaryEntry>) {
        _diaryEntries.value = entries
    }
    
    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }
    
    fun setErrorMessage(message: String?) {
        _errorMessage.value = message
    }
}
