package com.example.myexpirationdate.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson

@Entity
data class Photo(
    @PrimaryKey(autoGenerate = true)
    val photo_id: Int = 0,
    val name: String,
    val imagePath: String?,
    val expirationDate: String? = null,
    val acceptableXdate: AcceptableXdate = AcceptableXdate(0, 0),
    val isDonatable: Boolean = false
)
