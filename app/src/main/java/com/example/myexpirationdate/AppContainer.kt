package com.example.myexpirationdate

import android.content.Context
import androidx.room.Room
import com.example.myexpirationdate.data.PhotoDao
import com.example.myexpirationdate.data.MyDatabase

import kotlin.getValue

interface AppContainer {
    val photoDao : PhotoDao

}

class DefaultAppContainer(val context: Context) : AppContainer {

    private val db by lazy{
        Room.databaseBuilder(
            context,
            MyDatabase::class.java,
            "mydatabase.db"
        ).fallbackToDestructiveMigration(true) // drop old tables when the schema is updated
            .build()
    }

    override val photoDao: PhotoDao by lazy{
        db.photoDao
    }
}