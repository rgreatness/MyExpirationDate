package com.example.myexpirationdate.models

import android.util.Log
import androidx.room.TypeConverter
import com.example.myexpirationdate.TAG
import com.google.gson.Gson

data class ParsedItem(
    val name: String,
    val months: Int,
    val days: Int,
    val isDonatable: Boolean
)

fun parseExpirationItem(text: String): ParsedItem? {
    return try {
        val nameRegex = """name=([^,)]+)""".toRegex()
        val monthsRegex = """months=(\d+)""".toRegex()
        val daysRegex = """days=(\d+)""".toRegex()
        val isDonatableRegex = """isDonatable=(true|false)""".toRegex()

        val name = nameRegex.find(text)?.groupValues?.get(1)?.trim() ?: return null
        val months = monthsRegex.find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val days = daysRegex.find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val isDonatable = isDonatableRegex.find(text)?.groupValues?.get(1)?.toBoolean() ?: false

        Log.d(TAG, "Parsed name: $name")
        Log.d(TAG, "Parsed months: $months")
        Log.d(TAG, "Parsed days: $days")
        Log.d(TAG, "Parsed isDonatable: $isDonatable")

        ParsedItem(name, months, days, isDonatable)
    } catch (e: Exception) {
        Log.e(TAG, "Error parsing expiration item", e)
        null
    }
}

