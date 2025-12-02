// kotlin
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
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

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

    // In-memory cache of hashes we've already processed (populated from DB)
    private val processedHashes = mutableSetOf<String>()

    init {
        // Keep processedHashes in sync with DB so duplicates are detected even after navigation
        viewModelScope.launch(Dispatchers.IO) {
            try {
                photoDao.getAllPhotos().collect { list ->
                    processedHashes.clear()
                    processedHashes.addAll(list.mapNotNull { it.hash })
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error collecting photos to populate processedHashes", e)
            }
        }
    }

    private fun bitmapHash(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val bytes = baos.toByteArray()
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun onTakePhoto(bitmap: Bitmap, parsedItem: ParsedItem? = null) {
        Log.i(TAG, "Photo taken, processing upload")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val hash = bitmapHash(bitmap)

                // Fast in-memory guard (survives composable navigation) before hitting DAO
                if (processedHashes.contains(hash)) {
                    Log.i(TAG, "Duplicate detected in-memory (hash=${hash.take(8)}...), skipping")
                    return@launch
                }

                // Double-check against DB (defensive)
                val existing = try { photoDao.getPhotoByHash(hash) } catch (e: Exception) { null }
                if (existing != null) {
                    Log.i(TAG, "Duplicate detected in DB (hash=${hash.take(8)}...), skipping")
                    processedHashes.add(hash)
                    return@launch
                }

                val remoteUrl = ImageRepository.uploadAndSave(context, bitmap)

                if (!remoteUrl.isNullOrBlank()) {
                    val photo = Photo(
                        name = parsedItem?.name ?: "Photo ${photos.value.size + 1}",
                        imagePath = remoteUrl,
                        acceptableXdate = parsedItem?.let { AcceptableXdate(it.months, it.days) }
                            ?: AcceptableXdate(0, 0),
                        isDonatable = parsedItem?.isDonatable ?: false,
                        hash = hash
                    )
                    photoDao.upsertPhoto(photo)
                    // mark processed so subsequent navigations won't re-save
                    processedHashes.add(hash)
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
                processedHashes.clear()
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
                var hash: String? = null

                if (photo != null) {
                    hash = bitmapHash(photo)
                    if (hash != null && processedHashes.contains(hash)) {
                        Log.i(TAG, "Duplicate manual photo detected - skipping save")
                        return@launch
                    }

                    val existing = if (hash != null) try { photoDao.getPhotoByHash(hash) } catch (e: Exception) { null } else null
                    if (existing != null) {
                        Log.i(TAG, "Duplicate manual photo detected in DB - skipping save")
                        existing.hash?.let { processedHashes.add(it) }
                        return@launch
                    }

                    val remoteUrl = ImageRepository.uploadAndSave(context, photo)
                    if (!remoteUrl.isNullOrBlank()) {
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
                    isDonatable = isDonatable,
                    hash = hash
                )
                photoDao.upsertPhoto(newPhoto)
                hash?.let { processedHashes.add(it) }
                Log.i(TAG, "Manual photo saved with imagePath: $imagePath")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving manual entry", e)
            }
        }
    }

}
