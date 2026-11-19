package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationCacheDao {
  @Query(
      "SELECT translatedText FROM translation_cache WHERE targetLang = :targetLang AND sourceText = :sourceText LIMIT 1"
  )
  suspend fun getTranslation(targetLang: String, sourceText: String): String?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(entry: TranslationCacheEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAll(entries: List<TranslationCacheEntity>)

  @Query("SELECT COUNT(*) FROM translation_cache")
  fun getCountFlow(): Flow<Int>

  @Query("DELETE FROM translation_cache")
  suspend fun clearAll()

  @Query(
      "DELETE FROM translation_cache WHERE rowid NOT IN (SELECT rowid FROM translation_cache ORDER BY updatedAt DESC LIMIT :maxRows)"
  )
  suspend fun trimOldEntries(maxRows: Int = 15000)
}
