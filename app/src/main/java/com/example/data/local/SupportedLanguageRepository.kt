package com.example.data.local

import com.example.domain.LanguageOption
import com.example.domain.SupportedLanguages
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SupportedLanguageRepository(private val dao: SupportedLanguageDao) {
  val targetLanguagesFlow: Flow<List<LanguageOption>> =
      dao.getAllTargetLanguages().map { rows ->
        if (rows.isEmpty()) {
          SupportedLanguages.targetLanguages
        } else {
          rows.map { LanguageOption(code = it.code, label = it.label) }
        }
      }

  suspend fun ensureSeeded() {
    if (dao.count() == 0) {
      dao.insertAllIgnore(defaultLanguageEntities())
    }
  }

  suspend fun addCustomLanguage(code: String, label: String) {
    val cleanCode = code.trim().lowercase().take(10)
    if (cleanCode.isEmpty()) return
    val cleanLabel = label.trim().ifEmpty { "${cleanCode.uppercase()} ($cleanCode)" }
    dao.upsert(
        SupportedLanguageEntity(
            code = cleanCode,
            label = cleanLabel,
            sortOrder = 999,
            isCustom = true
        )
    )
  }

  suspend fun removeCustomLanguage(code: String) {
    dao.deleteCustomByCode(code)
  }

  companion object {
    fun defaultLanguageEntities(): List<SupportedLanguageEntity> =
        SupportedLanguages.targetLanguages.mapIndexed { idx, opt ->
          SupportedLanguageEntity(
              code = opt.code,
              label = opt.label,
              sortOrder = idx,
              isCustom = false
          )
        }
  }
}
