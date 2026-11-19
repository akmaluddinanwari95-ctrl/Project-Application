package com.example.domain

data class SplitLineResult(
    val hasCode: Boolean,
    val codePart: String,
    val textPart: String
)

data class ProtectedTextResult(
    val protectedText: String,
    val tokens: List<String>
)

object WarbandTextProcessor {
  private val curlyVarRegex = Regex("\\{[^}]+\\}")
  private val caretRegex = Regex("\\^\\^|\\^")
  private val printfRegex = Regex("%[sd]")
  private val restoreTokenRegex = Regex("\\s*ZXQ\\s*(\\d+)\\s*QXZ\\s*", RegexOption.IGNORE_CASE)

  fun parseGlossary(rawText: String): GlossaryRules {
    val cleanRaw = rawText.trim()
    if (cleanRaw.isEmpty()) return GlossaryRules()

    val noTranslate = mutableListOf<String>()
    val forcedPairs = mutableListOf<GlossaryPair>()

    cleanRaw.lines().forEach { line ->
      val clean = line.trim()
      if (clean.isEmpty() || clean.startsWith("#")) return@forEach
      val eqIdx = clean.indexOf('=')
      if (eqIdx > 0) {
        val src = clean.substring(0, eqIdx).trim()
        val dst = clean.substring(eqIdx + 1).trim()
        if (src.isNotEmpty()) {
          forcedPairs.add(GlossaryPair(source = src, target = dst))
        }
      } else {
        noTranslate.add(clean)
      }
    }

    return GlossaryRules(
        noTranslate = noTranslate.sortedByDescending { it.length },
        forcedPairs = forcedPairs.sortedByDescending { it.source.length }
    )
  }

  fun splitLine(rawLine: String, separator: String, stripTag: String): SplitLineResult {
    val sep = separator.ifEmpty { "|" }
    val workingLine = if (stripTag.isNotEmpty()) {
      rawLine.replace(stripTag, "")
    } else {
      rawLine
    }

    val sepIdx = workingLine.indexOf(sep)
    return if (sepIdx != -1) {
      SplitLineResult(
          hasCode = true,
          codePart = workingLine.substring(0, sepIdx),
          textPart = workingLine.substring(sepIdx + sep.length).trim()
      )
    } else {
      SplitLineResult(
          hasCode = false,
          codePart = "",
          textPart = workingLine.trim()
      )
    }
  }

  fun protectWarbandTokens(text: String, glossary: GlossaryRules): ProtectedTextResult {
    val tokens = mutableListOf<String>()

    fun pushToken(matchResult: MatchResult): CharSequence {
      val id = tokens.size
      tokens.add(matchResult.value)
      return " ZXQ${id}QXZ "
    }

    var protectedText = curlyVarRegex.replace(text, ::pushToken)
    protectedText = caretRegex.replace(protectedText, ::pushToken)
    protectedText = printfRegex.replace(protectedText, ::pushToken)

    glossary.noTranslate.forEach { term ->
      val rx = Regex("\\b${Regex.escape(term)}\\b", RegexOption.IGNORE_CASE)
      protectedText = rx.replace(protectedText, ::pushToken)
    }

    return ProtectedTextResult(protectedText = protectedText, tokens = tokens)
  }

  fun restoreWarbandTokens(
      translatedText: String,
      tokens: List<String>,
      glossary: GlossaryRules
  ): String {
    var result = restoreTokenRegex.replace(translatedText) { match ->
      val idx = match.groupValues.getOrNull(1)?.toIntOrNull()
      if (idx != null && idx in tokens.indices) {
        tokens[idx]
      } else {
        ""
      }
    }

    glossary.forcedPairs.forEach { pair ->
      val rx = Regex("\\b${Regex.escape(pair.source)}\\b", RegexOption.IGNORE_CASE)
      result = rx.replace(result, Regex.escapeReplacement(pair.target))
    }

    return result.trim()
  }

  fun isValidRegex(pattern: String): Boolean {
    val trimmed = pattern.trim()
    if (trimmed.isEmpty()) return true
    return try {
      Regex(trimmed)
      true
    } catch (_: Exception) {
      false
    }
  }

  fun matchesSkipRegex(line: String, pattern: String): Boolean {
    val trimmed = pattern.trim()
    if (trimmed.isEmpty()) return false
    return try {
      Regex(trimmed).containsMatchIn(line)
    } catch (_: Exception) {
      false
    }
  }

  fun extractProtectedTokens(text: String): List<String> {
    val tokens = mutableListOf<String>()
    curlyVarRegex.findAll(text).forEach { tokens.add(it.value) }
    caretRegex.findAll(text).forEach { tokens.add(it.value) }
    printfRegex.findAll(text).forEach { tokens.add(it.value) }
    return tokens.distinct()
  }

  fun buildCsvStructurePreview(
      file: QueuedFileItem,
      separator: String,
      stripTag: String,
      skipRegex: String,
      maxRows: Int = 10
  ): CsvStructurePreview {
    val sep = separator.ifEmpty { "|" }
    val sampleLines = file.lines.take(maxRows)
    var matchedSep = 0
    var skippedCount = 0

    val rows = sampleLines.mapIndexed { index, rawLine ->
      val skipped = matchesSkipRegex(rawLine, skipRegex)
      if (skipped) skippedCount++
      val split = splitLine(rawLine, sep, stripTag)
      if (split.hasCode) matchedSep++
      val tokens = extractProtectedTokens(split.textPart)
      CsvPreviewRow(
          rowNumber = index + 1,
          rawLine = rawLine,
          hasSeparator = split.hasCode,
          columnKey = split.codePart,
          columnText = split.textPart,
          isSkippedByRegex = skipped,
          protectedTokens = tokens
      )
    }

    return CsvStructurePreview(
        fileId = file.id,
        fileName = file.name,
        totalLines = file.lines.size,
        separator = sep,
        rows = rows,
        matchedSeparatorCount = matchedSep,
        skippedCount = skippedCount
    )
  }

  fun formatExportContent(
      lines: List<String>,
      sourceSeparator: String,
      exportDelimiter: ExportDelimiter
  ): String {
    val srcSep = sourceSeparator.ifEmpty { "|" }
    if (exportDelimiter == ExportDelimiter.SOURCE_PIPE && srcSep == "|") {
      return lines.joinToString("\r\n") + "\r\n"
    }
    val outSep = if (exportDelimiter == ExportDelimiter.SOURCE_PIPE) srcSep else exportDelimiter.symbol
    val formatted = lines.map { line ->
      val idx = line.indexOf(srcSep)
      if (idx == -1) {
        escapeCsvFieldIfNeeded(line, outSep)
      } else {
        val codePart = line.substring(0, idx)
        val textPart = line.substring(idx + srcSep.length)
        val escapedCode = escapeCsvFieldIfNeeded(codePart, outSep)
        val escapedText = escapeCsvFieldIfNeeded(textPart, outSep)
        "$escapedCode$outSep$escapedText"
      }
    }
    return formatted.joinToString("\r\n") + "\r\n"
  }

  private fun escapeCsvFieldIfNeeded(value: String, delimiter: String): String {
    if (delimiter == "|") return value
    val needsQuotes = value.contains(delimiter) || value.contains('"') || value.contains('\n') || value.contains('\r')
    return if (needsQuotes) {
      "\"${value.replace("\"", "\"\"")}\""
    } else {
      value
    }
  }

  private val illegalFilenameCharsRegex = Regex("[/\\\\:*?\"<>|\\r\\n\\t]")

  fun sanitizeFilenameAffix(raw: String, maxLength: Int = 24): String {
    return illegalFilenameCharsRegex.replace(raw, "").take(maxLength)
  }

  fun buildExportFileName(originalName: String, prefix: String, suffix: String): String {
    val cleanPrefix = sanitizeFilenameAffix(prefix).trim()
    val cleanSuffix = sanitizeFilenameAffix(suffix).trim()
    val safeOriginal = originalName.ifBlank { "output.csv" }
    if (cleanPrefix.isEmpty() && cleanSuffix.isEmpty()) return safeOriginal

    val dotIdx = safeOriginal.lastIndexOf('.')
    return if (dotIdx > 0) {
      val base = safeOriginal.substring(0, dotIdx)
      val ext = safeOriginal.substring(dotIdx)
      "$cleanPrefix$base$cleanSuffix$ext"
    } else {
      "$cleanPrefix$safeOriginal$cleanSuffix"
    }
  }
}
