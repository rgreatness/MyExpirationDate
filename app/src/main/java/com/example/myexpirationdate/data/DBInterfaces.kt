package com.example.myexpirationdate.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.myexpirationdate.models.Photo
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Query("SELECT * FROM photo")
    fun getAllPhotos(): Flow<List<Photo>>

    @Query("SELECT * FROM photo WHERE photo_id IN (:listIds)")
    fun getPhotoWithIds(listIds: Array<Int>): Flow<List<Photo>>

    @Query("DELETE FROM photo")
    suspend fun deleteAllPhotos()

    @Upsert
    suspend fun upsertPhoto(photo: Photo)

    @Delete
    suspend fun deletePhoto(photo: Photo)
}
