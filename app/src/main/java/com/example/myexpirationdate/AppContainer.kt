package com.example.myexpirationdate

import android.content.Context
import androidx.room.Room
import com.example.myexpirationdate.data.ExpirationItemDao
import com.example.myexpirationdate.data.PhotoDao
import com.example.myexpirationdate.data.MyDatabase
import com.example.myexpirationdate.models.ExpirationItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.getValue

interface AppContainer {
    val photoDao : PhotoDao
    val expirationItemDao : ExpirationItemDao
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val database: MyDatabase by lazy {
        Room.databaseBuilder(
            context,
            MyDatabase::class.java,
            "your_database_name"
        ).fallbackToDestructiveMigration().build()
    }

    override val photoDao: PhotoDao by lazy {
        database.photoDao()
    }

    override val expirationItemDao: ExpirationItemDao by lazy {
        database.expirationItemDao()
    }

    fun loadExpirationItemsFromJson() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val jsonString = context.assets.open("expirationDates.json").bufferedReader().use { it.readText() }
                val gson = Gson()
                val itemType = object : TypeToken<List<ExpirationItem>>() {}.type
                val items: List<ExpirationItem> = gson.fromJson(jsonString, itemType)

                items.forEach { item ->
                    expirationItemDao.upsertExpirationItem(item)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
