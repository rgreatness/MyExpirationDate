package com.example.myexpirationdate.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Upsert
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.myexpirationdate.models.ExpirationItem
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

@Dao
interface ExpirationItemDao {
    @Query("SELECT * FROM expiration_items")
    fun getAllExpirationItems(): Flow<List<ExpirationItem>>

    @Upsert
    suspend fun upsertExpirationItem(item: ExpirationItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpirationItem(item: ExpirationItem)

    @Delete
    suspend fun deleteExpirationItem(item: ExpirationItem)

    @RawQuery(observedEntities = [ExpirationItem::class])
    fun getItemsByQuery(query: SupportSQLiteQuery): List<ExpirationItem>
}
