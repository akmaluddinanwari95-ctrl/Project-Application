package com.example.domain

enum class UiLanguage(val code: String, val label: String) {
  ID("id", "ID"),
  EN("en", "EN"),
  TR("tr", "TR");

  companion object {
    fun fromCode(code: String): UiLanguage = entries.firstOrNull { it.code == code } ?: ID
  }
}

enum class TranslationEngine(val id: String, val displayName: String) {
  GOOGLE("google", "Google Translate"),
  LINGVA("lingva", "Lingva Mirror"),
  MYMEMORY("mymemory", "MyMemory API");

  companion object {
    fun fromId(id: String): TranslationEngine = entries.firstOrNull { it.id == id } ?: GOOGLE
  }
}

enum class FileQueueStatus {
  WAIT,
  RUN,
  DONE,
  ERROR
}

enum class LogLevel {
  INFO,
  OK,
  WARN,
  ERROR
}

enum class ConnectionBadgeType {
  NEUTRAL,
  OK,
  WARN,
  ERROR
}

data class LanguageOption(
    val code: String,
    val label: String
)

object SupportedLanguages {
  val sourceLanguages = listOf(
      LanguageOption("en", "English (Inggris)"),
      LanguageOption("auto", "Auto-Detect"),
      LanguageOption("tr", "Türkçe (Turki)"),
      LanguageOption("ru", "Русский (Rusia)"),
      LanguageOption("zh", "中文 (Mandarin)"),
      LanguageOption("de", "Deutsch (Jerman)"),
      LanguageOption("fr", "Français (Prancis)"),
      LanguageOption("es", "Español (Spanyol)"),
      LanguageOption("ja", "日本語 (Jepang)")
  )

  val targetLanguages = listOf(
      LanguageOption("id", "Bahasa Indonesia (id)"),
      LanguageOption("ms", "Bahasa Melayu (ms)"),
      LanguageOption("en", "English (en)"),
      LanguageOption("tr", "Türkçe (tr)"),
      LanguageOption("ar", "العربية (ar)"),
      LanguageOption("ru", "Русский (ru)"),
      LanguageOption("zh", "中文 (zh)")
  )
}

data class DelayOption(
    val ms: Long,
    val titleKey: String
)

object DelayOptions {
  val all = listOf(
      DelayOption(80L, "spdFast"),
      DelayOption(200L, "spdNormal"),
      DelayOption(500L, "spdSafe")
  )
}

data class TranslatorSettings(
    val uiLanguage: UiLanguage = UiLanguage.ID,
    val engine: TranslationEngine = TranslationEngine.GOOGLE,
    val delayMs: Long = 200L,
    val autoFallback: Boolean = true,
    val sourceLang: String = "en",
    val targetLang: String = "id",
    val separator: String = "|",
    val stripTag: String = "{***}",
    val skipRegex: String = "^\\d+$",
    val glossaryText: String = ""
)

data class GlossaryPair(
    val source: String,
    val target: String
)

data class GlossaryRules(
    val noTranslate: List<String> = emptyList(),
    val forcedPairs: List<GlossaryPair> = emptyList()
) {
  val totalCount: Int get() = noTranslate.size + forcedPairs.size
}

data class QueuedFileItem(
    val id: String,
    val name: String,
    val lines: List<String>,
    val results: List<String> = emptyList(),
    val status: FileQueueStatus = FileQueueStatus.WAIT
)

data class PreviewRow(
    val lineIndex: Int,
    val originalLine: String,
    val translatedLine: String
)

data class LogEntry(
    val id: Long,
    val timestamp: String,
    val message: String,
    val level: LogLevel
)

data class ConnectionStatus(
    val text: String,
    val type: ConnectionBadgeType
)

data class ErrorAlert(
    val id: Long,
    val title: String,
    val detail: String,
    val lineInfo: String = ""
)

data class TranslationProgressState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val currentFileIndex: Int = 0,
    val totalPendingFiles: Int = 0,
    val currentFileName: String = "",
    val processedLines: Int = 0,
    val totalLines: Int = 0,
    val cacheHits: Int = 0,
    val etaText: String = ""
) {
  val percentage: Int
    get() = if (totalLines > 0) ((processedLines.toFloat() / totalLines) * 100).toInt().coerceIn(0, 100) else 0
}
