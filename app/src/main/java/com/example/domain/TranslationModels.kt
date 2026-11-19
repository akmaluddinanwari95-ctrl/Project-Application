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

enum class ExportDelimiter(val id: String, val symbol: String, val displayLabel: String) {
  SOURCE_PIPE("pipe", "|", "Warband Default (|)"),
  COMMA("comma", ",", "Comma (,)"),
  TAB("tab", "\t", "Tab (\\t)"),
  SEMICOLON("semicolon", ";", "Semicolon (;)");

  companion object {
    fun fromId(id: String): ExportDelimiter = entries.firstOrNull { it.id == id } ?: SOURCE_PIPE
  }
}

enum class ExportEncoding(val id: String, val charsetName: String, val displayLabel: String) {
  UTF_8("utf8", "UTF-8", "UTF-8 (No BOM)"),
  UTF_8_BOM("utf8_bom", "UTF-8 BOM", "UTF-8 with BOM"),
  UTF_16_LE("utf16le", "UTF-16 LE", "UTF-16 LE + BOM"),
  UTF_16_BE("utf16be", "UTF-16 BE", "UTF-16 BE + BOM");

  fun toCharset(): java.nio.charset.Charset = when (this) {
    UTF_8, UTF_8_BOM -> Charsets.UTF_8
    UTF_16_LE -> Charsets.UTF_16LE
    UTF_16_BE -> Charsets.UTF_16BE
  }

  companion object {
    fun fromId(id: String): ExportEncoding = when (id.lowercase()) {
      "utf8_bom" -> UTF_8_BOM
      "utf16", "utf16le", "utf-16le" -> UTF_16_LE
      "utf16be", "utf-16be" -> UTF_16_BE
      else -> UTF_8
    }
  }
}

data class BatchSizeOption(
    val size: Int,
    val titleKey: String
)

object BatchSizeOptions {
  val all = listOf(
      BatchSizeOption(1, "batchSize1"),
      BatchSizeOption(5, "batchSize5"),
      BatchSizeOption(10, "batchSize10")
  )
}

data class TranslatorSettings(
    val uiLanguage: UiLanguage = UiLanguage.ID,
    val engine: TranslationEngine = TranslationEngine.GOOGLE,
    val delayMs: Long = 200L,
    val batchSize: Int = 1,
    val autoFallback: Boolean = true,
    val medievalToneEnabled: Boolean = true,
    val sourceLang: String = "en",
    val targetLang: String = "id",
    val separator: String = "|",
    val stripTag: String = "{***}",
    val skipRegex: String = "^\\d+$",
    val glossaryText: String = "",
    val exportDelimiter: ExportDelimiter = ExportDelimiter.SOURCE_PIPE,
    val exportEncoding: ExportEncoding = ExportEncoding.UTF_8,
    val exportFilenamePrefix: String = "",
    val exportFilenameSuffix: String = ""
)

data class GlossaryPreset(
    val id: String,
    val titleKey: String,
    val termsText: String
)

enum class QaIssueType {
  MISSING_TOKEN,
  EXTRA_TOKEN,
  BROKEN_SEPARATOR,
  LEFT_MARKER,
  UNTRANSLATED
}

data class QaAuditRow(
    val lineIndex: Int,
    val codePart: String,
    val originalLine: String,
    val translatedLine: String,
    val missingTokens: List<String>,
    val extraTokens: List<String>,
    val issues: List<QaIssueType>
) {
  val hasIssue: Boolean get() = issues.isNotEmpty()
}

data class QaAuditSummary(
    val fileId: String,
    val fileName: String,
    val totalRows: Int,
    val issueCount: Int,
    val rows: List<QaAuditRow>
)

data class ModDiffMergeResult(
    val mergedLines: List<String>,
    val seededCachePairs: Map<String, String>,
    val carriedOverCount: Int,
    val newOrChangedCount: Int,
    val skippedCount: Int
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
    val uriString: String? = null,
    val lines: List<String>,
    val results: List<String> = emptyList(),
    val status: FileQueueStatus = FileQueueStatus.WAIT,
    val cachedLinesCount: Int = 0,
    val processedCount: Int = 0,
    val errorMessage: String? = null
) {
  val totalLines: Int get() = lines.size
  val resumePercentage: Int
    get() = if (lines.isEmpty()) 0 else ((maxOf(processedCount, cachedLinesCount).toFloat() / lines.size) * 100f).toInt().coerceIn(0, 100)
}

data class CsvPreviewRow(
    val rowNumber: Int,
    val rawLine: String,
    val hasSeparator: Boolean,
    val columnKey: String,
    val columnText: String,
    val isSkippedByRegex: Boolean,
    val protectedTokens: List<String>
)

data class CsvStructurePreview(
    val fileId: String,
    val fileName: String,
    val totalLines: Int,
    val separator: String,
    val rows: List<CsvPreviewRow>,
    val matchedSeparatorCount: Int,
    val skippedCount: Int
)

data class TelemetrySample(
    val elapsedSec: Float,
    val speedLinesPerSec: Float,
    val progressPercent: Float,
    val processedLines: Int
)

data class PreviewRow(
    val lineIndex: Int,
    val originalLine: String,
    val translatedLine: String
)

data class PendingExportPreviewItem(
    val fileId: String,
    val originalFileName: String,
    val outputFileName: String,
    val formattedContent: String,
    val lineCount: Int
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
    val etaText: String = "",
    val currentSpeedLinesPerSec: Float = 0f,
    val peakSpeedLinesPerSec: Float = 0f,
    val telemetrySamples: List<TelemetrySample> = emptyList()
) {
  val percentage: Int
    get() = if (totalLines > 0) ((processedLines.toFloat() / totalLines) * 100).toInt().coerceIn(0, 100) else 0
}
