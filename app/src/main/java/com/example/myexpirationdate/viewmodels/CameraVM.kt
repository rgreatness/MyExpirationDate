package com.example.myexpirationdate.viewmodels

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myexpirationdate.TAG
import com.example.myexpirationdate.data.PhotoDao
import com.example.myexpirationdate.models.Photo
import com.example.myexpirationdate.repos.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CameraVM(application: Application) : AndroidViewModel(application) {

    private val photoDao: PhotoDao by lazy {
        RoomVM.getInstance().photoDao
    }

    private val context: Context by lazy {
        getApplication<Application>().applicationContext
    }

    val photos: StateFlow<List<Photo>> = photoDao.getAllPhotos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onTakePhoto(bitmap: Bitmap) {
        Log.i(TAG, "Photo taken, processing upload")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val remoteUrl = ImageRepository.uploadAndSave(context, bitmap)

                if (remoteUrl != null) {
                    val photo = Photo(
                        name = "Photo ${photos.value.size + 1}",
                        imagePath = remoteUrl
                    )
                    photoDao.upsertPhoto(photo)
                    Log.i(TAG, "Photo saved with remote URL: $remoteUrl")
                } else {
                    Log.e(TAG, "Failed to upload photo to ImgBB - no URL returned")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading photo to ImgBB", e)
            }
        }
    }

    fun clearAllPhotos() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                photoDao.deleteAllPhotos()
                Log.d(TAG, "All photos cleared")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing photos", e)
            }
        }
    }
}
