package com.example.myexpirationdate.viewmodels

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myexpirationdate.TAG
import com.example.myexpirationdate.data.PhotoDao
import com.example.myexpirationdate.models.AcceptableXdate
import com.example.myexpirationdate.models.ParsedItem
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

    fun onTakePhoto(bitmap: Bitmap, parsedItem: ParsedItem? = null) {
        Log.i(TAG, "Photo taken, processing upload")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val remoteUrl = ImageRepository.uploadAndSave(context, bitmap)

                if (remoteUrl != null) {
                    val photo = Photo(
                        name = parsedItem?.name ?: "Photo ${photos.value.size + 1}",
                        imagePath = remoteUrl,
                        acceptableXdate = parsedItem?.let { AcceptableXdate(it.months, it.days) }
                            ?: AcceptableXdate(0, 0),
                        isDonatable = parsedItem?.isDonatable ?: false
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

    fun onSaveManual(name: String, months: Int, days: Int, isDonatable: Boolean, photo: Bitmap?) {
        Log.i(TAG, "Saving manual entry: $name, $months months, $days days, Donatable: $isDonatable")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var imagePath: String = ""

                if (photo != null) {
                    val remoteUrl = ImageRepository.uploadAndSave(context, photo)
                    if (remoteUrl != null) {
                        imagePath = remoteUrl
                    } else {
                        Log.e(TAG, "Failed to upload manual photo to ImgBB - no URL returned")
                    }
                } else {
                    Log.w(TAG, "No photo provided for manual save - inserting record without image")
                }

                val newPhoto = Photo(
                    name = name,
                    imagePath = imagePath,
                    acceptableXdate = AcceptableXdate(months, days),
                    isDonatable = isDonatable
                )
                photoDao.upsertPhoto(newPhoto)
                Log.i(TAG, "Manual photo saved with imagePath: $imagePath")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving manual entry", e)
            }
        }
    }

}
