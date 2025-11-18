package com.example.myexpirationdate.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myexpirationdate.models.ExpirationItem
import com.example.myexpirationdate.models.Photo

@Database(
    entities = [Photo::class, ExpirationItem::class],
    version = 2
)
abstract class MyDatabase: RoomDatabase() {

    abstract fun photoDao() : PhotoDao
    abstract fun expirationItemDao() : ExpirationItemDao

    companion object {
        @Volatile
        private var INSTANCE: MyDatabase? = null

        fun getDatabase(context: Context): MyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyDatabase::class.java,
                    "my_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
