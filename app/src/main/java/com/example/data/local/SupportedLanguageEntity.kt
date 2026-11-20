package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "supported_languages")
data class SupportedLanguageEntity(
    @PrimaryKey val code: String,
    val label: String,
    val sortOrder: Int = 100,
    val isCustom: Boolean = false
)
