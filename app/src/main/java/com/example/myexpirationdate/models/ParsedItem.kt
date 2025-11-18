package com.example.myexpirationdate.models

import androidx.room.TypeConverter
import com.google.gson.Gson

data class ParsedItem(
    val name: String,
    val months: Int,
    val days: Int,
    val isDonatable: Boolean
)

fun parseExpirationItem(raw: String): ParsedItem? {
    return try {
        val nameRegex = """name=([^,]+)""".toRegex()
        val monthsRegex = """months=(\d+)""".toRegex()
        val daysRegex = """days=(\d+)""".toRegex()
        val donateRegex = """isDonateable=(true|false)""".toRegex()

        val name = nameRegex.find(raw)?.groupValues?.get(1)?.trim() ?: return null
        val months = monthsRegex.find(raw)?.groupValues?.get(1)?.toInt() ?: 0
        val days = daysRegex.find(raw)?.groupValues?.get(1)?.toInt() ?: 0
        val isDonatable = donateRegex.find(raw)?.groupValues?.get(1)?.toBoolean() ?: false

        ParsedItem(name, months, days, isDonatable)
    } catch (e: Exception) {
        null
    }
}
