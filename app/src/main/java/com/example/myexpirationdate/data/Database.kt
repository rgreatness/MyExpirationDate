package com.example.myexpirationdate.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myexpirationdate.models.Photo

@Database(
    entities = [Photo::class],
    version = 2
)
abstract class MyDatabase: RoomDatabase() {

    abstract val photoDao : PhotoDao
}