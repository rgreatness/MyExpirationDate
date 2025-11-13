package com.example.myexpirationdate.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myexpirationdate.models.Product

@Database(
    entities = [Product::class],
    version = 1
)
abstract class MyDatabase: RoomDatabase() {

    abstract val productDao : ProductDao
}