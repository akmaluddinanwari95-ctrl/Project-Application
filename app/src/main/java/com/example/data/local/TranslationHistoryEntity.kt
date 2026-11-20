package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translation_history")
data class TranslationHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val fileName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String,
    val uriString: String? = null,
    val totalLines: Int = 0,
    val processedLines: Int = 0,
    val sourceLang: String = "en",
    val targetLang: String = "id",
    val engineName: String = "Google Translate",
    val sourceSnapshot: String = "",
    val translatedSnapshot: String = "",
    val wordsProcessed: Int = 0,
    val durationMs: Long = 0L
)
