package com.example.data.local

import androidx.room.Entity

@Entity(
    tableName = "translation_cache",
    primaryKeys = ["targetLang", "sourceText"]
)
data class TranslationCacheEntity(
    val targetLang: String,
    val sourceText: String,
    val translatedText: String,
    val updatedAt: Long = System.currentTimeMillis()
)
