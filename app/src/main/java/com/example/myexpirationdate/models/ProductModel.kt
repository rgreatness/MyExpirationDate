package com.example.myexpirationdate.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// define tables

@Entity(tableName = "product")
data class Product(
    @PrimaryKey
    var product_id: Int = 1,
    var name : String = "",
    var quantity: Int = 0,
    var unit_price : Double = 0.0
)

data class NameTupleProduct(
    @ColumnInfo(name= "name") val name: String?,
    @ColumnInfo(name = "unit_price") val unit_price: Double?
)