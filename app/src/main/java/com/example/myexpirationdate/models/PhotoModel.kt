package com.example.myexpirationdate.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson

@Entity(tableName = "photo")
@TypeConverters(Converters::class)
data class Photo(
    @PrimaryKey(autoGenerate = true) val photo_id: Int? = null,
    val name: String,
    val imagePath: String?,
    val expirationDate: String? = null,
    val acceptableXdate: AcceptableXdate = AcceptableXdate(0, 0),
    val isDonatable: Boolean = false,
    val hash: String? = null
)
