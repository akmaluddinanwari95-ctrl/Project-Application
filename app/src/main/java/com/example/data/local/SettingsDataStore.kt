package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
    val AUTO_FALLBACK = booleanPreferencesKey("auto_fallback")
    val SRC_LANG = stringPreferencesKey("src_lang")
    val TGT_LANG = stringPreferencesKey("tgt_lang")
    val SEPARATOR = stringPreferencesKey("separator")
    val STRIP_TAG = stringPreferencesKey("strip_tag")
    val SKIP_REGEX = stringPreferencesKey("skip_regex")
    val GLOSSARY_TEXT = stringPreferencesKey("glossary_text")
  }

  val settingsFlow: Flow<TranslatorSettings> =
      context.settingsDataStore.data.map { prefs ->
        TranslatorSettings(
            uiLanguage = UiLanguage.fromCode(prefs[Keys.UI_LANG] ?: "id"),
            engine = TranslationEngine.fromId(prefs[Keys.ENGINE] ?: "google"),
            delayMs = prefs[Keys.DELAY_MS] ?: 200L,
            autoFallback = prefs[Keys.AUTO_FALLBACK] ?: true,
            sourceLang = prefs[Keys.SRC_LANG] ?: "en",
            targetLang = prefs[Keys.TGT_LANG] ?: "id",
            separator = prefs[Keys.SEPARATOR] ?: "|",
            stripTag = prefs[Keys.STRIP_TAG] ?: "{***}",
            skipRegex = prefs[Keys.SKIP_REGEX] ?: "^\\d+$",
            glossaryText = prefs[Keys.GLOSSARY_TEXT] ?: ""
        )
      }

  suspend fun updateSettings(transform: (TranslatorSettings) -> TranslatorSettings) {
    context.settingsDataStore.edit { prefs ->
      val current = TranslatorSettings(
          uiLanguage = UiLanguage.fromCode(prefs[Keys.UI_LANG] ?: "id"),
          engine = TranslationEngine.fromId(prefs[Keys.ENGINE] ?: "google"),
          delayMs = prefs[Keys.DELAY_MS] ?: 200L,
          autoFallback = prefs[Keys.AUTO_FALLBACK] ?: true,
          sourceLang = prefs[Keys.SRC_LANG] ?: "en",
          targetLang = prefs[Keys.TGT_LANG] ?: "id",
          separator = prefs[Keys.SEPARATOR] ?: "|",
          stripTag = prefs[Keys.STRIP_TAG] ?: "{***}",
          skipRegex = prefs[Keys.SKIP_REGEX] ?: "^\\d+$",
          glossaryText = prefs[Keys.GLOSSARY_TEXT] ?: ""
      )
      val updated = transform(current)
      prefs[Keys.UI_LANG] = updated.uiLanguage.code
      prefs[Keys.ENGINE] = updated.engine.id
      prefs[Keys.DELAY_MS] = updated.delayMs
      prefs[Keys.AUTO_FALLBACK] = updated.autoFallback
      prefs[Keys.SRC_LANG] = updated.sourceLang
      prefs[Keys.TGT_LANG] = updated.targetLang
      prefs[Keys.SEPARATOR] = updated.separator
      prefs[Keys.STRIP_TAG] = updated.stripTag
      prefs[Keys.SKIP_REGEX] = updated.skipRegex
      prefs[Keys.GLOSSARY_TEXT] = updated.glossaryText
    }
  }
}
