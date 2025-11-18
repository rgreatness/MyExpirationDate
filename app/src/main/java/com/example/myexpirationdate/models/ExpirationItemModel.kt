package com.example.myexpirationdate.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson

data class AcceptableXdate(
    val months: Int,
    val days: Int
)

class AcceptableXdateConverter {
    @TypeConverter
    fun fromObject(value: AcceptableXdate): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toObject(value: String): AcceptableXdate {
        return Gson().fromJson(value, AcceptableXdate::class.java)
    }
}


@Entity(tableName = "expiration_items")
@TypeConverters(AcceptableXdateConverter::class)
data class ExpirationItem(
    @PrimaryKey val id: String,
    val name: String,
    val acceptableXdate: AcceptableXdate,
    val isDonatable: Boolean,
    val isUnopened: Boolean
)
