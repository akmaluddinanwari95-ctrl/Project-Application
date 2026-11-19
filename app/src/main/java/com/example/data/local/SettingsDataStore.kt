package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.ExportDelimiter
import com.example.domain.ExportEncoding
import com.example.domain.TranslationEngine
import com.example.domain.TranslatorSettings
import com.example.domain.UiLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "warband_translator_settings")

class SettingsDataStore(private val context: Context) {
  private object Keys {
    val UI_LANG = stringPreferencesKey("ui_lang")
    val ENGINE = stringPreferencesKey("engine")
    val DELAY_MS = longPreferencesKey("delay_ms")
    val BATCH_SIZE = intPreferencesKey("batch_size")
    val AUTO_FALLBACK = booleanPreferencesKey("auto_fallback")
    val MEDIEVAL_TONE = booleanPreferencesKey("medieval_tone")
    val SRC_LANG = stringPreferencesKey("src_lang")
    val TGT_LANG = stringPreferencesKey("tgt_lang")
    val SEPARATOR = stringPreferencesKey("separator")
    val STRIP_TAG = stringPreferencesKey("strip_tag")
    val SKIP_REGEX = stringPreferencesKey("skip_regex")
    val GLOSSARY_TEXT = stringPreferencesKey("glossary_text")
    val EXPORT_DELIMITER = stringPreferencesKey("export_delimiter")
    val EXPORT_ENCODING = stringPreferencesKey("export_encoding")
    val EXPORT_FILENAME_PREFIX = stringPreferencesKey("export_filename_prefix")
    val EXPORT_FILENAME_SUFFIX = stringPreferencesKey("export_filename_suffix")
  }

  val settingsFlow: Flow<TranslatorSettings> =
      context.settingsDataStore.data.map { prefs ->
        TranslatorSettings(
            uiLanguage = UiLanguage.fromCode(prefs[Keys.UI_LANG] ?: "id"),
            engine = TranslationEngine.fromId(prefs[Keys.ENGINE] ?: "google"),
            delayMs = prefs[Keys.DELAY_MS] ?: 200L,
            batchSize = (prefs[Keys.BATCH_SIZE] ?: 1).coerceIn(1, 10),
            autoFallback = prefs[Keys.AUTO_FALLBACK] ?: true,
            medievalToneEnabled = prefs[Keys.MEDIEVAL_TONE] ?: true,
            sourceLang = prefs[Keys.SRC_LANG] ?: "en",
            targetLang = prefs[Keys.TGT_LANG] ?: "id",
            separator = prefs[Keys.SEPARATOR] ?: "|",
            stripTag = prefs[Keys.STRIP_TAG] ?: "{***}",
            skipRegex = prefs[Keys.SKIP_REGEX] ?: "^\\d+$",
            glossaryText = prefs[Keys.GLOSSARY_TEXT] ?: "",
            exportDelimiter = ExportDelimiter.fromId(prefs[Keys.EXPORT_DELIMITER] ?: "pipe"),
            exportEncoding = ExportEncoding.fromId(prefs[Keys.EXPORT_ENCODING] ?: "utf8"),
            exportFilenamePrefix = prefs[Keys.EXPORT_FILENAME_PREFIX] ?: "",
            exportFilenameSuffix = prefs[Keys.EXPORT_FILENAME_SUFFIX] ?: ""
        )
      }

  suspend fun updateSettings(transform: (TranslatorSettings) -> TranslatorSettings) {
    context.settingsDataStore.edit { prefs ->
      val current = TranslatorSettings(
          uiLanguage = UiLanguage.fromCode(prefs[Keys.UI_LANG] ?: "id"),
          engine = TranslationEngine.fromId(prefs[Keys.ENGINE] ?: "google"),
          delayMs = prefs[Keys.DELAY_MS] ?: 200L,
          batchSize = (prefs[Keys.BATCH_SIZE] ?: 1).coerceIn(1, 10),
          autoFallback = prefs[Keys.AUTO_FALLBACK] ?: true,
          medievalToneEnabled = prefs[Keys.MEDIEVAL_TONE] ?: true,
          sourceLang = prefs[Keys.SRC_LANG] ?: "en",
          targetLang = prefs[Keys.TGT_LANG] ?: "id",
          separator = prefs[Keys.SEPARATOR] ?: "|",
          stripTag = prefs[Keys.STRIP_TAG] ?: "{***}",
          skipRegex = prefs[Keys.SKIP_REGEX] ?: "^\\d+$",
          glossaryText = prefs[Keys.GLOSSARY_TEXT] ?: "",
          exportDelimiter = ExportDelimiter.fromId(prefs[Keys.EXPORT_DELIMITER] ?: "pipe"),
          exportEncoding = ExportEncoding.fromId(prefs[Keys.EXPORT_ENCODING] ?: "utf8"),
          exportFilenamePrefix = prefs[Keys.EXPORT_FILENAME_PREFIX] ?: "",
          exportFilenameSuffix = prefs[Keys.EXPORT_FILENAME_SUFFIX] ?: ""
      )
      val updated = transform(current)
      prefs[Keys.UI_LANG] = updated.uiLanguage.code
      prefs[Keys.ENGINE] = updated.engine.id
      prefs[Keys.DELAY_MS] = updated.delayMs
      prefs[Keys.BATCH_SIZE] = updated.batchSize.coerceIn(1, 10)
      prefs[Keys.AUTO_FALLBACK] = updated.autoFallback
      prefs[Keys.MEDIEVAL_TONE] = updated.medievalToneEnabled
      prefs[Keys.SRC_LANG] = updated.sourceLang
      prefs[Keys.TGT_LANG] = updated.targetLang
      prefs[Keys.SEPARATOR] = updated.separator
      prefs[Keys.STRIP_TAG] = updated.stripTag
      prefs[Keys.SKIP_REGEX] = updated.skipRegex
      prefs[Keys.GLOSSARY_TEXT] = updated.glossaryText
      prefs[Keys.EXPORT_DELIMITER] = updated.exportDelimiter.id
      prefs[Keys.EXPORT_ENCODING] = updated.exportEncoding.id
      prefs[Keys.EXPORT_FILENAME_PREFIX] = updated.exportFilenamePrefix
      prefs[Keys.EXPORT_FILENAME_SUFFIX] = updated.exportFilenameSuffix
    }
  }
}
