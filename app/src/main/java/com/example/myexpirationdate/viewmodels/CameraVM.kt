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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.cancellation.CancellationException

class CameraVM(application: Application) : AndroidViewModel(application) {

    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    val photos: StateFlow<List<Photo>> = _photos

    private val photoDao: PhotoDao by lazy {
        RoomVM.getInstance().photoDao
    }

    private val context: Context by lazy {
        getApplication<Application>().applicationContext
    }

    init {
        loadPhotos()
    }

    private fun loadPhotos() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                photoDao.getAllPhotos().collect { photoList ->
                    _photos.update { photoList }
                    Log.d(TAG, "Loaded ${photoList.size} photos from database")
                }
            } catch (e: CancellationException) {
                Log.d(TAG, "Photo loading cancelled")
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error loading photos", e)
            }
        }
    }

    fun onTakePhoto(bitmap: Bitmap) {
        Log.i(TAG, "Photo taken, processing upload/save")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val remoteUrl = try {
                    ImageRepository.uploadAndSave(context, bitmap)
                } catch (e: Exception) {
                    Log.w(TAG, "Image upload failed, will save locally", e)
                    null
                }

                if (!remoteUrl.isNullOrEmpty()) {
                    val photo = Photo(
                        name = "Photo ${_photos.value.size + 1}",
                        imagePath = remoteUrl
                    )
                    photoDao.upsertPhoto(photo)
                    Log.d(TAG, "Photo saved with remote URL: $remoteUrl")
                } else {
                    val fileName = "photo_${System.currentTimeMillis()}.jpg"
                    val file = File(context.filesDir, fileName)

                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }

                    val photo = Photo(
                        name = "Photo ${_photos.value.size + 1}",
                        imagePath = file.absolutePath
                    )

                    photoDao.upsertPhoto(photo)
                    Log.d(TAG, "Photo saved locally to ${file.absolutePath}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving photo", e)
            }
        }
    }

    fun clearAllPhotos() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _photos.value.forEach { photo ->
                    if (photo.imagePath.startsWith(context.filesDir.absolutePath)) {
                        val file = File(photo.imagePath)
                        if (file.exists()) file.delete()
                    }
                }

                photoDao.deleteAllPhotos()
                Log.d(TAG, "All photos cleared")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing photos", e)
            }
        }
    }
}
