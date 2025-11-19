package com.example.myexpirationdate

import android.content.Context
import androidx.room.Room
import com.example.myexpirationdate.data.ExpirationItemDao
import com.example.myexpirationdate.data.PhotoDao
import com.example.myexpirationdate.data.MyDatabase

import kotlin.getValue

interface AppContainer {
    val photoDao : PhotoDao
    val expirationItemDao : ExpirationItemDao

}

class DefaultAppContainer(context: Context) : AppContainer {
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
}
