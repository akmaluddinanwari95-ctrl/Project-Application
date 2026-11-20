package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.domain.SupportedLanguages

@Database(
    entities = [
      TranslationCacheEntity::class,
      TranslationHistoryEntity::class,
      SupportedLanguageEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun translationCacheDao(): TranslationCacheDao
  abstract fun translationHistoryDao(): TranslationHistoryDao
  abstract fun supportedLanguageDao(): SupportedLanguageDao

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

    val MIGRATION_2_3 = object : Migration(2, 3) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `translation_history` ADD COLUMN `wordsProcessed` INTEGER NOT NULL DEFAULT 0"
        )
        db.execSQL(
            "ALTER TABLE `translation_history` ADD COLUMN `durationMs` INTEGER NOT NULL DEFAULT 0"
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `supported_languages` (
              `code` TEXT NOT NULL PRIMARY KEY,
              `label` TEXT NOT NULL,
              `sortOrder` INTEGER NOT NULL,
              `isCustom` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        seedLanguagesSql(db)
      }
    }

    private fun seedLanguagesSql(db: SupportSQLiteDatabase) {
      SupportedLanguages.targetLanguages.forEachIndexed { idx, opt ->
        db.execSQL(
            "INSERT OR IGNORE INTO `supported_languages` (`code`, `label`, `sortOrder`, `isCustom`) VALUES (?, ?, ?, 0)",
            arrayOf<Any>(opt.code, opt.label, idx)
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
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .addCallback(object : Callback() {
              override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                seedLanguagesSql(db)
              }

              override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                seedLanguagesSql(db)
              }
            })
            .build()
            .also { INSTANCE = it }
      }
    }
  }
}
