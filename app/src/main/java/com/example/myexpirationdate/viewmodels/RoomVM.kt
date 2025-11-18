package com.example.myexpirationdate.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myexpirationdate.data.ExpirationItemDao
import com.example.myexpirationdate.data.PhotoDao
import com.example.myexpirationdate.medApp
import com.example.myexpirationdate.models.AcceptableXdate
import com.example.myexpirationdate.models.ExpirationItem
import com.example.myexpirationdate.models.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.IOException


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
    private val expirationItemDao: ExpirationItemDao
) : ViewModel() {

    var photo_state by mutableStateOf(PhotoState())

    val photos = photoDao.getAllPhotos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    init {
        viewModelScope.launch {
            populateExpirationItemsFromAsset()
        }
    }

    private suspend fun populateExpirationItemsFromAsset() {
        withContext(Dispatchers.IO) {
            try {
                val existingItems = expirationItemDao.getAllExpirationItems().first()
                if (existingItems.isEmpty()) {
                    val context = medApp.getApp().applicationContext
                    val jsonString = context.assets.open("expirationDates.json")
                        .bufferedReader()
                        .use { it.readText() }

                    val jsonArray = JSONArray(jsonString)

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val id = jsonObject.getString("id")
                        val name = jsonObject.getString("name")
                        val isDonateable = jsonObject.getBoolean("isDonatable")
                        val isUnopened = jsonObject.getBoolean("isUnopened")

                        val xdateObject = jsonObject.getJSONObject("acceptableXdate")
                        val months = xdateObject.getInt("months")
                        val days = xdateObject.getInt("days")
                        val acceptableXdate = AcceptableXdate(months, days)

                        val item = ExpirationItem(
                            id = id,
                            name = name,
                            acceptableXdate = acceptableXdate,
                            isDonatable = isDonateable,
                            isUnopened = isUnopened
                        )

                        expirationItemDao.insertExpirationItem(item)
                    }

                    Log.d("RoomVM", "Database populated with ${jsonArray.length()} items from expirationDates.json")
                } else {
                    Log.d("RoomVM", "Database already contains ${existingItems.size} items")
                }
            } catch (e: IOException) {
                Log.e("RoomVM", "Error reading expirationDates.json: ${e.message}", e)
            } catch (e: Exception) {
                Log.e("RoomVM", "Error populating database: ${e.message}", e)
            }
        }
    }

    companion object {
        private var INSTANCE: RoomVM? = null

        fun getInstance(): RoomVM {
            return INSTANCE ?: synchronized(this) {
                val instance = RoomVM(
                    medApp.getApp().container.photoDao,
                    medApp.getApp().container.expirationItemDao
                )
                INSTANCE = instance
                instance
            }
        }
    }
}
