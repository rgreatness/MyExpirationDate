package com.example.myexpirationdate.models

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun loadExpirationList(context: Context): List<ExpirationItem> {
    val json = context.assets.open("expirationDates.json")
        .bufferedReader()
        .use { it.readText() }
    val type = object : TypeToken<List<ExpirationItem>>() {}.type
    return Gson().fromJson(json, type)
}

data class AcceptableXdate(
    val months: Int,
    val days: Int
)

class AcceptableXdateListConverter {
    @TypeConverter
    fun fromList(value: List<AcceptableXdate>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toList(value: String): List<AcceptableXdate> {
        val type = object : TypeToken<List<AcceptableXdate>>() {}.type
        return Gson().fromJson(value, type)
    }
}

@Entity(tableName = "expiration_items")
@TypeConverters(AcceptableXdateListConverter::class)
data class ExpirationItem(
    @PrimaryKey val id: String,
    val name: String,
    val acceptableXdate: List<AcceptableXdate>,
    val isDonateable: Boolean,
    val isUnopened: Boolean
)
