// Kotlin
package com.example.myexpirationdate.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myexpirationdate.data.PhotoDao
import com.example.myexpirationdate.medApp
import com.example.myexpirationdate.models.Photo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


data class PhotoState(
    var id: String = "",
    var name: String = "",
    var image: String = ""
){
    fun toPhoto(): Photo {
        return Photo(
            photo_id = id.toIntOrNull() ?: 0,
            name = name,
            imagePath = image
        )
    }
}

class RoomVM(
    val photoDao: PhotoDao,
) : ViewModel() {

    var photo_state by mutableStateOf(PhotoState())

    val photos = photoDao.getAllPhotos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun deletePhoto(photo: Photo) {
        viewModelScope.launch {
            photoDao.deletePhoto(photo)
        }
    }

    fun addPhoto() {
        viewModelScope.launch {
            photoDao.upsertPhoto(photo_state.toPhoto())
        }
    }

    companion object {
        private var INSTANCE: RoomVM? = null

        fun getInstance(): RoomVM {
            val vm = INSTANCE ?: synchronized(this) {
                RoomVM(
                    medApp.Companion.getApp().container.photoDao,
                ).also {
                    INSTANCE = it
                }
            }
            return vm
        }
    }
}
