package com.example.myexpirationdate.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.myexpirationdate.models.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    // add queries here : examples below

//     query 1
    @Query("SELECT * FROM product")
    fun getAllProduct() : Flow<List<Product>>

    @Query("SElECT * FROM product WHERE product_id IN (:listIds)")
    fun getProductWithIds(listIds: Array<Int>): Flow<List<Product>>


    // mix of insert and update
    // Step 8: Enable Upsert
    @Upsert
    suspend fun upsertProduct(product: Product)

    // Step 9: Enable Delete
    @Delete
    suspend fun deleteProduct(product: Product)
}