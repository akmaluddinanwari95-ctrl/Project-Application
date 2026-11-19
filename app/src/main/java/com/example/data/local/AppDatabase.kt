package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TranslationCacheEntity::class, TranslationHistoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun translationCacheDao(): TranslationCacheDao
  abstract fun translationHistoryDao(): TranslationHistoryDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    val MIGRATION_1_2 = object : Migration(1, 2) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `translation_history` (
              `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
              `fileName` TEXT NOT NULL,
              `timestamp` INTEGER NOT NULL,
              `status` TEXT NOT NULL,
              `uriString` TEXT,
              `totalLines` INTEGER NOT NULL,
              `processedLines` INTEGER NOT NULL,
              `sourceLang` TEXT NOT NULL,
              `targetLang` TEXT NOT NULL,
              `engineName` TEXT NOT NULL,
              `sourceSnapshot` TEXT NOT NULL,
              `translatedSnapshot` TEXT NOT NULL
            )
            """.trimIndent()
        )
      }
    }

    fun getInstance(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "warband_translator.db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
            .also { INSTANCE = it }
      }
    }
  }
}
