package com.example.myexpirationdate.models

import androidx.room.TypeConverter
import com.google.gson.Gson

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromAcceptableXdate(value: AcceptableXdate?): String? =
        gson.toJson(value)

    @TypeConverter
    fun toAcceptableXdate(value: String?): AcceptableXdate? =
        value?.let { gson.fromJson(it, AcceptableXdate::class.java) }
}
