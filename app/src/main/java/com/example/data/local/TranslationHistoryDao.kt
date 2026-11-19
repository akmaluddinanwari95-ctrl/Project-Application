package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationHistoryDao {
  @Query("SELECT * FROM translation_history ORDER BY timestamp DESC")
  fun getAllHistory(): Flow<List<TranslationHistoryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(entry: TranslationHistoryEntity): Long

  @Query("DELETE FROM translation_history WHERE id = :id")
  suspend fun deleteById(id: Long)

  @Query("DELETE FROM translation_history")
  suspend fun clearAll()
}
