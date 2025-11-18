package com.example.myexpirationdate.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myexpirationdate.models.Converters
import com.example.myexpirationdate.models.ExpirationItem
import com.example.myexpirationdate.models.Photo

@Database(
    entities = [Photo::class, ExpirationItem::class],
    version = 2
)
@TypeConverters(Converters::class)
abstract class MyDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    abstract fun expirationItemDao(): ExpirationItemDao
}
