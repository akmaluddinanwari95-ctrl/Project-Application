package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SupportedLanguageDao {
  @Query("SELECT * FROM supported_languages ORDER BY sortOrder ASC, label ASC")
  fun getAllTargetLanguages(): Flow<List<SupportedLanguageEntity>>

  @Query("SELECT COUNT(*) FROM supported_languages")
  suspend fun count(): Int

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertAllIgnore(items: List<SupportedLanguageEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(item: SupportedLanguageEntity)

  @Query("DELETE FROM supported_languages WHERE code = :code AND isCustom = 1")
  suspend fun deleteCustomByCode(code: String)
}
