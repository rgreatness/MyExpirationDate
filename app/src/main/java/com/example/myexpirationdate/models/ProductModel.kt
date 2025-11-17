package com.example.myexpirationdate.models

import android.content.Context
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

data class ExpirationItem(
    val id: String,
    val product_name: String,
    val acceptableXdate: AcceptableXdate,
    val isDonateable: Boolean,
    val isUnopened: Boolean
)
