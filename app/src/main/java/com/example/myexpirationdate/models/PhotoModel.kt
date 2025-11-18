package com.example.myexpirationdate.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Photo(
    @PrimaryKey(autoGenerate = true)
    val photo_id: Int = 0,
    val name: String,
    val imagePath: String?,
    val expirationDate: String? = null
)
