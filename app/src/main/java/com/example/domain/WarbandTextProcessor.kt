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
  private val escapedBreakRegex = Regex("\\\\[nt]")
  private val printfRegex = Regex("%[sdif]")
  private val hexColorRegex = Regex("<#[0-9A-Fa-f]{6,8}>|@[0-9A-Fa-f]{6}")
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

    val sepIdx = findFirstDelimiterIndex(workingLine, sep)
    return if (sepIdx != -1) {
      val rawCode = workingLine.substring(0, sepIdx)
      val rawText = workingLine.substring(sepIdx + sep.length).trim()
      val code = if (sep != "|") unquoteCsvCellIfWrapped(rawCode.trim()) else rawCode
      val text = if (sep != "|") unquoteCsvCellIfWrapped(rawText) else rawText
      SplitLineResult(
          hasCode = true,
          codePart = code,
          textPart = text
      )
    } else {
      val trimmed = workingLine.trim()
      val text = if (sep != "|") unquoteCsvCellIfWrapped(trimmed) else trimmed
      SplitLineResult(
          hasCode = false,
          codePart = "",
          textPart = text
      )
    }
  }

  fun buildTranslatedLine(
      codePart: String,
      hasCode: Boolean,
      textPart: String,
      separator: String
  ): String {
    val sep = separator.ifEmpty { "|" }
    if (!hasCode) {
      return if (sep == "|") textPart else escapeCsvFieldIfNeeded(textPart, sep)
    }
    if (sep == "|") {
      return "$codePart|$textPart"
    }
    val safeCode = escapeCsvFieldIfNeeded(codePart, sep)
    val safeText = escapeCsvFieldIfNeeded(textPart, sep)
    return "$safeCode$sep$safeText"
  }

  private fun findFirstDelimiterIndex(line: String, sep: String): Int {
    if (sep.isEmpty()) return -1
    if (sep == "|") return line.indexOf(sep)
    var inQuotes = false
    var i = 0
    while (i < line.length) {
      val ch = line[i]
      if (ch == '"') {
        if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
          i += 2
          continue
        }
        inQuotes = !inQuotes
      } else if (!inQuotes && line.startsWith(sep, i)) {
        return i
      }
      i++
    }
    return -1
  }

  private fun unquoteCsvCellIfWrapped(cell: String): String {
    if (cell.length < 2 || !cell.startsWith('"') || !cell.endsWith('"')) return cell
    var i = 1
    while (i < cell.length - 1) {
      if (cell[i] == '"') {
        if (i + 1 < cell.length - 1 && cell[i + 1] == '"') {
          i += 2
          continue
        }
        // Closed before the end -> not a single outer quoted cell
        return cell
      }
      i++
    }
    return cell.substring(1, cell.length - 1).replace("\"\"", "\"")
  }

  fun countUnquotedDelimiter(line: String, delimiter: String): Int {
    if (delimiter.isEmpty()) return 0
    if (delimiter == "|") return countOccurrences(line, delimiter)
    var count = 0
    var inQuotes = false
    var i = 0
    while (i < line.length) {
      val ch = line[i]
      if (ch == '"') {
        if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
          i += 2
          continue
        }
        inQuotes = !inQuotes
      } else if (!inQuotes && line.startsWith(delimiter, i)) {
        count++
        i += delimiter.length
        continue
      }
      i++
    }
    return count
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
    protectedText = escapedBreakRegex.replace(protectedText, ::pushToken)
    protectedText = printfRegex.replace(protectedText, ::pushToken)
    protectedText = hexColorRegex.replace(protectedText, ::pushToken)

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
    escapedBreakRegex.findAll(text).forEach { tokens.add(it.value) }
    printfRegex.findAll(text).forEach { tokens.add(it.value) }
    hexColorRegex.findAll(text).forEach { tokens.add(it.value) }
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
      val split = splitLine(line, srcSep, "")
      if (!split.hasCode) {
        escapeCsvFieldIfNeeded(split.textPart, outSep)
      } else {
        val escapedCode = escapeCsvFieldIfNeeded(split.codePart, outSep)
        val escapedText = escapeCsvFieldIfNeeded(split.textPart, outSep)
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

  val builtInGlossaryPresets: List<GlossaryPreset> = listOf(
      GlossaryPreset(
          id = "factions",
          titleKey = "presetGlossFactions",
          termsText = """
            Swadia
            Vaegir
            Khergit
            Nord
            Rhodok
            Sarranid
            Calradia
            Praven
            Suno
            Uxkhal
            Dhirim
            Reyvadin
            Rivacheg
            Khudan
            Curaw
            Tulga
            Ichamur
            Narra
            Halmar
            Sargoth
            Tihr
            Wercheg
            Jelkala
            Veluca
            Yalen
            Shariz
            Durquba
            Ahmerrad
            Bariyye
          """.trimIndent()
      ),
      GlossaryPreset(
          id = "politics",
          titleKey = "presetGlossPolitics",
          termsText = """
            Marshal=Marsekal
            Vassal=Vasali
            Fief=Wilayah Kekuasaan
            Renown=Reputasi
            Right to Rule=Hak Memerintah
            Controversy=Kontroversi
            Sovereign=Penguasa
            Liege=Tuanku
            Claimant=Penuntut Takhta
            Banner=Panji
          """.trimIndent()
      ),
      GlossaryPreset(
          id = "military",
          titleKey = "presetGlossMilitary",
          termsText = """
            Recruit=Rekrut
            Militia=Milisi
            Skirmisher=Penyerang Ringan
            Footman=Prajurit Infanteri
            Knight=Kesatria
            Man-at-Arms=Kesatria Bersenjata
            Crossbowman=Pemanah Busur Silang
            Horse Archer=Pemanah Berkuda
            Lancer=Pasukan Tombak Berkuda
            Huscarl=Huskarl
          """.trimIndent()
      )
  )

  fun mergeGlossaryText(existingRaw: String, additionRaw: String): String {
    val existingLines = existingRaw.lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toMutableList()
    val seenKeys = existingLines.map { line ->
      val eq = line.indexOf('=')
      (if (eq > 0) line.substring(0, eq) else line).trim().lowercase()
    }.toHashSet()

    additionRaw.lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .forEach { line ->
          val eq = line.indexOf('=')
          val key = (if (eq > 0) line.substring(0, eq) else line).trim().lowercase()
          if (key.isNotEmpty() && seenKeys.add(key)) {
            existingLines.add(line)
          }
        }
    return existingLines.joinToString("\n")
  }

  private val spaceBeforePunctRegex = Regex("\\s+([,.!?;:])")
  private val multiSpaceRegex = Regex(" {2,}")
  private val indonesianToneRules: List<Pair<Regex, String>> = listOf(
      Regex("\\bTuanku saya\\b", RegexOption.IGNORE_CASE) to "Tuanku",
      Regex("\\bTuan saya\\b", RegexOption.IGNORE_CASE) to "Tuanku",
      Regex("\\bYang Mulia Anda\\b", RegexOption.IGNORE_CASE) to "Baginda",
      Regex("\\bYang Mulia Kamu\\b", RegexOption.IGNORE_CASE) to "Baginda",
      Regex("\\bKerajaan saya\\b", RegexOption.IGNORE_CASE) to "Kerajaanku",
      Regex("\\bpasukan saya\\b", RegexOption.IGNORE_CASE) to "pasukanku",
      Regex("\\bdesa saya\\b", RegexOption.IGNORE_CASE) to "desaku",
      Regex("\\bkastil saya\\b", RegexOption.IGNORE_CASE) to "kastilku",
      Regex("\\bkota saya\\b", RegexOption.IGNORE_CASE) to "kotaku",
      Regex("\\bbangsawan laki-laki\\b", RegexOption.IGNORE_CASE) to "kesatria"
  )
  private val englishToneRules: List<Pair<Regex, String>> = listOf(
      Regex("\\bmy lord\\b", RegexOption.IGNORE_CASE) to "My Lord",
      Regex("\\byour grace\\b", RegexOption.IGNORE_CASE) to "Your Grace",
      Regex("\\byour highness\\b", RegexOption.IGNORE_CASE) to "Your Highness"
  )

  fun applyMedievalTonePostProcessing(
      text: String,
      targetLang: String,
      enabled: Boolean
  ): String {
    if (!enabled || text.isBlank()) return text

    // Temporarily mask curly/caret/newline/color tokens so tone replacements never touch inside them
    val protectedTokens = mutableListOf<String>()
    fun maskMatch(m: MatchResult): CharSequence {
      val idx = protectedTokens.size
      protectedTokens.add(m.value)
      return "@@TOK${idx}@@"
    }
    var working = curlyVarRegex.replace(text, ::maskMatch)
    working = caretRegex.replace(working, ::maskMatch)
    working = escapedBreakRegex.replace(working, ::maskMatch)
    working = printfRegex.replace(working, ::maskMatch)
    working = hexColorRegex.replace(working, ::maskMatch)

    working = spaceBeforePunctRegex.replace(working, "$1")
    working = multiSpaceRegex.replace(working, " ")

    when (targetLang.lowercase()) {
      "id" -> {
        indonesianToneRules.forEach { (rx, replacement) ->
          working = rx.replace(working, replacement)
        }
      }
      "en" -> {
        englishToneRules.forEach { (rx, replacement) ->
          working = rx.replace(working, replacement)
        }
      }
    }

    working = working.trim()
    if (working.isNotEmpty() && working[0].isLowerCase() && !working.startsWith("@@TOK")) {
      working = working.replaceFirstChar { it.uppercase() }
    }

    val restorePlaceholder = Regex("@@TOK(\\d+)@@")
    return restorePlaceholder.replace(working) { m ->
      val idx = m.groupValues[1].toIntOrNull()
      if (idx != null && idx in protectedTokens.indices) protectedTokens[idx] else ""
    }
  }

  fun auditTranslatedFile(
      file: QueuedFileItem,
      separator: String,
      stripTag: String,
      skipRegex: String
  ): QaAuditSummary {
    val sep = separator.ifEmpty { "|" }
    val auditedRows = mutableListOf<QaAuditRow>()
    var issueRowsCount = 0

    file.lines.forEachIndexed { idx, origLine ->
      val transLine = file.results.getOrNull(idx) ?: origLine
      if (matchesSkipRegex(origLine, skipRegex)) {
        return@forEachIndexed
      }

      val origSplit = splitLine(origLine, sep, stripTag)
      val transSplit = splitLine(transLine, sep, "")

      val issues = mutableListOf<QaIssueType>()
      if (origSplit.hasCode && (!transSplit.hasCode || origSplit.codePart != transSplit.codePart)) {
        issues.add(QaIssueType.BROKEN_SEPARATOR)
      }

      val origDelimCount = countUnquotedDelimiter(origLine, sep)
      val transDelimCount = countUnquotedDelimiter(transLine, sep)
      if (origDelimCount != transDelimCount) {
        issues.add(QaIssueType.COLUMN_COUNT_MISMATCH)
      }

      if (transLine.count { it == '"' } % 2 != 0) {
        issues.add(QaIssueType.UNBALANCED_QUOTES)
      }

      val origTokens = extractProtectedTokens(origSplit.textPart)
      val transTokens = extractProtectedTokens(transSplit.textPart)
      val missingTokens = origTokens.filterNot { it in transTokens }
      val extraTokens = transTokens.filterNot { it in origTokens }

      if (missingTokens.isNotEmpty()) issues.add(QaIssueType.MISSING_TOKEN)
      if (extraTokens.isNotEmpty()) issues.add(QaIssueType.EXTRA_TOKEN)
      if (restoreTokenRegex.containsMatchIn(transSplit.textPart)) {
        issues.add(QaIssueType.LEFT_MARKER)
      }
      if (origSplit.textPart.length >= 16 &&
          origSplit.textPart.any { it.isLetter() } &&
          origSplit.textPart.equals(transSplit.textPart, ignoreCase = false)
      ) {
        issues.add(QaIssueType.UNTRANSLATED)
      }

      if (issues.isNotEmpty()) issueRowsCount++
      auditedRows.add(
          QaAuditRow(
              lineIndex = idx,
              codePart = origSplit.codePart,
              originalLine = origLine,
              translatedLine = transLine,
              missingTokens = missingTokens,
              extraTokens = extraTokens,
              issues = issues
          )
      )
    }

    return QaAuditSummary(
        fileId = file.id,
        fileName = file.name,
        totalRows = auditedRows.size,
        issueCount = issueRowsCount,
        rows = auditedRows
    )
  }

  fun autoFixMissingTokens(
      originalLine: String,
      translatedLine: String,
      separator: String,
      stripTag: String
  ): String {
    val sep = separator.ifEmpty { "|" }
    val origSplit = splitLine(originalLine, sep, stripTag)
    val transSplit = splitLine(translatedLine, sep, "")

    val origTokens = extractProtectedTokens(origSplit.textPart)
    val transTokens = extractProtectedTokens(transSplit.textPart)
    val missing = origTokens.filterNot { it in transTokens }

    var fixedText = transSplit.textPart.trim()
    if (missing.isNotEmpty()) {
      fixedText = if (fixedText.isEmpty()) missing.joinToString(" ") else "$fixedText ${missing.joinToString(" ")}"
    }

    return buildTranslatedLine(
        codePart = origSplit.codePart,
        hasCode = origSplit.hasCode,
        textPart = fixedText,
        separator = sep
    )
  }

  fun mergeModUpdateDiff(
      newModLines: List<String>,
      oldTranslatedLines: List<String>,
      separator: String,
      stripTag: String,
      skipRegex: String
  ): ModDiffMergeResult {
    val sep = separator.ifEmpty { "|" }
    val oldByCode = HashMap<String, String>(oldTranslatedLines.size)
    oldTranslatedLines.forEach { line ->
      val split = splitLine(line, sep, stripTag)
      if (split.hasCode && split.codePart.isNotBlank() && split.textPart.isNotEmpty()) {
        oldByCode[split.codePart] = split.textPart
      }
    }

    val seededCache = LinkedHashMap<String, String>()
    var carriedOver = 0
    var newOrChanged = 0
    var skipped = 0

    newModLines.forEach { newLine ->
      if (matchesSkipRegex(newLine, skipRegex)) {
        skipped++
        return@forEach
      }
      val newSplit = splitLine(newLine, sep, stripTag)
      if (newSplit.textPart.isEmpty()) {
        skipped++
        return@forEach
      }
      val reused = if (newSplit.hasCode) oldByCode[newSplit.codePart] else null
      if (!reused.isNullOrBlank()) {
        seededCache[newSplit.textPart] = reused
        carriedOver++
      } else {
        newOrChanged++
      }
    }

    return ModDiffMergeResult(
        mergedLines = newModLines,
        seededCachePairs = seededCache,
        carriedOverCount = carriedOver,
        newOrChangedCount = newOrChanged,
        skippedCount = skipped
    )
  }

  fun findAndReplaceInLines(
      lines: List<String>,
      findQuery: String,
      replaceWith: String,
      caseSensitive: Boolean,
      separator: String
  ): Pair<List<String>, Int> {
    if (findQuery.isEmpty()) return lines to 0
    val sep = separator.ifEmpty { "|" }
    val rx = Regex(
        Regex.escape(findQuery),
        if (caseSensitive) emptySet() else setOf(RegexOption.IGNORE_CASE)
    )
    var replacedRowsCount = 0
    val updated = lines.map { line ->
      val split = splitLine(line, sep, "")
      if (!split.hasCode) {
        val next = rx.replace(split.textPart, Regex.escapeReplacement(replaceWith))
        if (next != split.textPart) {
          replacedRowsCount++
          buildTranslatedLine("", false, next, sep)
        } else {
          line
        }
      } else {
        val nextText = rx.replace(split.textPart, Regex.escapeReplacement(replaceWith))
        if (nextText != split.textPart) {
          replacedRowsCount++
          buildTranslatedLine(split.codePart, true, nextText, sep)
        } else {
          line
        }
      }
    }
    return updated to replacedRowsCount
  }

  fun importGlossaryFromContent(rawContent: String, fileName: String): GlossaryImportResult {
    val clean = rawContent.removePrefix("\uFEFF").trim()
    if (clean.isEmpty()) {
      return GlossaryImportResult(importedLinesText = "", importedCount = 0, formatDetected = "EMPTY")
    }

    val lowerName = fileName.lowercase()
    val looksLikeJson = lowerName.endsWith(".json") || clean.startsWith("{") || clean.startsWith("[")
    if (looksLikeJson) {
      val parsedJson = parseGlossaryJson(clean)
      if (parsedJson != null) {
        return GlossaryImportResult(
            importedLinesText = parsedJson.joinToString("\n"),
            importedCount = parsedJson.size,
            formatDetected = "JSON"
        )
      }
    }

    val looksLikeCsv = lowerName.endsWith(".csv") || lowerName.endsWith(".tsv") ||
        (!clean.contains('=') && (clean.contains(',') || clean.contains(';') || clean.contains('\t')))
    if (looksLikeCsv) {
      val parsedCsv = parseGlossaryCsvOrTsv(clean)
      return GlossaryImportResult(
          importedLinesText = parsedCsv.joinToString("\n"),
          importedCount = parsedCsv.size,
          formatDetected = if (lowerName.endsWith(".tsv")) "TSV" else "CSV"
      )
    }

    val lines = clean.lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .map { line ->
          if (!line.contains('=') && line.contains('|')) {
            val idx = line.indexOf('|')
            val k = line.substring(0, idx).trim()
            val v = line.substring(idx + 1).trim()
            if (v.isEmpty()) k else "$k=$v"
          } else {
            line
          }
        }
    return GlossaryImportResult(
        importedLinesText = lines.joinToString("\n"),
        importedCount = lines.size,
        formatDetected = "TXT"
    )
  }

  private fun parseGlossaryJson(clean: String): List<String>? {
    return try {
      val result = mutableListOf<String>()
      if (clean.startsWith("[")) {
        val arr = org.json.JSONArray(clean)
        for (i in 0 until arr.length()) {
          val elem = arr.opt(i)
          when (elem) {
            is org.json.JSONObject -> {
              val key = elem.optString("source", "").ifBlank {
                elem.optString("key", "").ifBlank {
                  elem.optString("original", "").ifBlank { elem.optString("term", "") }
                }
              }.trim()
              val target = elem.optString("target", "").ifBlank {
                elem.optString("value", "").ifBlank {
                  elem.optString("translation", "").ifBlank { elem.optString("translated", "") }
                }
              }.trim()
              if (key.isNotEmpty()) {
                result.add(if (target.isEmpty()) key else "$key=$target")
              }
            }
            is String -> {
              val term = elem.trim()
              if (term.isNotEmpty()) result.add(term)
            }
          }
        }
      } else if (clean.startsWith("{")) {
        val obj = org.json.JSONObject(clean)
        val innerArray = obj.optJSONArray("entries") ?: obj.optJSONArray("glossary")
        if (innerArray != null) {
          return parseGlossaryJson(innerArray.toString())
        }
        val keys = obj.keys()
        while (keys.hasNext()) {
          val k = keys.next().trim()
          val v = obj.optString(k, "").trim()
          if (k.isNotEmpty()) {
            result.add(if (v.isEmpty()) k else "$k=$v")
          }
        }
      }
      result
    } catch (_: Exception) {
      null
    }
  }

  private fun parseGlossaryCsvOrTsv(clean: String): List<String> {
    val rawLines = clean.lines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }
    if (rawLines.isEmpty()) return emptyList()

    val firstLine = rawLines.first()
    val delimiter = when {
      firstLine.contains('\t') -> '\t'
      firstLine.contains(';') && !firstLine.contains(',') -> ';'
      firstLine.contains('|') && !firstLine.contains(',') -> '|'
      else -> ','
    }

    val headerNames = setOf("source", "key", "original", "term", "id", "english")
    val entries = mutableListOf<String>()
    rawLines.forEachIndexed { idx, line ->
      val cols = parseCsvFields(line, delimiter)
      if (cols.isEmpty()) return@forEachIndexed
      val c0 = cols[0].trim()
      val c1 = cols.getOrNull(1)?.trim().orEmpty()
      if (idx == 0 && c0.lowercase() in headerNames) {
        return@forEachIndexed
      }
      if (c0.isNotEmpty()) {
        entries.add(if (c1.isEmpty()) c0 else "$c0=$c1")
      }
    }
    return entries
  }

  private fun parseCsvFields(line: String, delimiter: Char): List<String> {
    val fields = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false
    var i = 0
    while (i < line.length) {
      val ch = line[i]
      when {
        ch == '"' -> {
          if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
            current.append('"')
            i++
          } else {
            inQuotes = !inQuotes
          }
        }
        ch == delimiter && !inQuotes -> {
          fields.add(current.toString())
          current.setLength(0)
        }
        else -> current.append(ch)
      }
      i++
    }
    fields.add(current.toString())
    return fields
  }

  fun validateCsvStructure(
      file: QueuedFileItem,
      separator: String,
      stripTag: String,
      skipRegex: String
  ): CsvValidationReport {
    val sep = separator.ifEmpty { "|" }
    if (file.lines.isEmpty()) {
      return CsvValidationReport(
          fileId = file.id,
          fileName = file.name,
          status = CsvValidationStatus.MALFORMED,
          totalLines = 0,
          separatorMatchedLines = 0,
          skippedLines = 0,
          alignedColumnsCount = 0,
          malformedLinesCount = 1,
          issues = listOf(CsvValidationIssue(1, "File kosong / tidak memiliki baris teks.", "")),
          summaryMessage = "File kosong atau tidak berisi baris valid."
      )
    }

    var skipped = 0
    var matchedSep = 0
    var alignedCols = 0
    var malformedCount = 0
    val issues = mutableListOf<CsvValidationIssue>()
    val columnCounts = HashMap<Int, Int>()

    file.lines.forEachIndexed { idx, rawLine ->
      val lineNo = idx + 1
      if (matchesSkipRegex(rawLine, skipRegex)) {
        skipped++
        return@forEachIndexed
      }

      val split = splitLine(rawLine, sep, stripTag)
      val sepOccurrences = countUnquotedDelimiter(rawLine, sep)
      columnCounts[sepOccurrences] = (columnCounts[sepOccurrences] ?: 0) + 1

      val hasOddQuotes = rawLine.count { it == '"' } % 2 != 0
      when {
        hasOddQuotes -> {
          malformedCount++
          if (issues.size < 12) {
            issues.add(CsvValidationIssue(lineNo, "Tanda kutip tidak berpasangan", rawLine.take(72)))
          }
        }
        !split.hasCode -> {
          malformedCount++
          if (issues.size < 12) {
            issues.add(CsvValidationIssue(lineNo, "Pemisah '$sep' tidak ditemukan (kolom tidak sejajar)", rawLine.take(72)))
          }
        }
        split.codePart.isBlank() -> {
          malformedCount++
          if (issues.size < 12) {
            issues.add(CsvValidationIssue(lineNo, "Kolom ID di kiri '$sep' kosong", rawLine.take(72)))
          }
        }
        else -> {
          matchedSep++
          if (sepOccurrences == 1) {
            alignedCols++
          } else {
            // Multiple delimiters in one line can indicate accidental unescaped separator in dialogue
            if (issues.size < 12) {
              issues.add(CsvValidationIssue(lineNo, "Ditemukan $sepOccurrences pemisah '$sep' pada satu baris", rawLine.take(72)))
            }
          }
        }
      }
    }

    val activeLines = (file.lines.size - skipped).coerceAtLeast(1)
    val matchRatio = matchedSep.toFloat() / activeLines.toFloat()
    val status = when {
      matchedSep == 0 && activeLines > 0 -> CsvValidationStatus.MALFORMED
      matchRatio < 0.6f -> CsvValidationStatus.MALFORMED
      malformedCount > 0 || issues.isNotEmpty() -> CsvValidationStatus.WARNING
      else -> CsvValidationStatus.VALID
    }

    val summaryMsg = when (status) {
      CsvValidationStatus.VALID -> "$matchedSep/$activeLines baris terstruktur rapi dengan pemisah '$sep'."
      CsvValidationStatus.WARNING -> "$matchedSep/$activeLines cocok, $malformedCount baris perlu perhatian struktur kolom."
      CsvValidationStatus.MALFORMED -> "Format mencurigakan: hanya $matchedSep/$activeLines baris cocok dengan pemisah '$sep'."
    }

    return CsvValidationReport(
        fileId = file.id,
        fileName = file.name,
        status = status,
        totalLines = file.lines.size,
        separatorMatchedLines = matchedSep,
        skippedLines = skipped,
        alignedColumnsCount = alignedCols,
        malformedLinesCount = malformedCount,
        issues = issues,
        summaryMessage = summaryMsg
    )
  }

  private fun countOccurrences(text: String, needle: String): Int {
    if (needle.isEmpty()) return 0
    var count = 0
    var idx = 0
    while (true) {
      val found = text.indexOf(needle, idx)
      if (found == -1) break
      count++
      idx = found + needle.length
    }
    return count
  }

  fun countWordsInLines(lines: List<String>, separator: String, stripTag: String, skipRegex: String): Int {
    val sep = separator.ifEmpty { "|" }
    var words = 0
    lines.forEach { line ->
      if (!matchesSkipRegex(line, skipRegex)) {
        val text = splitLine(line, sep, stripTag).textPart
        if (text.isNotBlank()) {
          words += text.trim().split(Regex("\\s+")).size
        }
      }
    }
    return words
  }

  fun computeDailyProductivityStats(
      history: List<com.example.data.local.TranslationHistoryEntity>,
      daysCount: Int = 7
  ): List<DailyProductivityStat> {
    val cal = java.util.Calendar.getInstance()
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
    cal.set(java.util.Calendar.MINUTE, 0)
    cal.set(java.util.Calendar.SECOND, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)

    val dayFormat = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())
    val dayBuckets = LinkedHashMap<Long, DailyProductivityStat>()
    for (offset in (daysCount - 1) downTo 0) {
      val c = (cal.clone() as java.util.Calendar)
      c.add(java.util.Calendar.DAY_OF_YEAR, -offset)
      val dayEpoch = c.timeInMillis
      dayBuckets[dayEpoch] = DailyProductivityStat(
          dayLabel = dayFormat.format(java.util.Date(dayEpoch)),
          dayEpoch = dayEpoch,
          wordsCount = 0,
          linesCount = 0,
          filesDone = 0,
          filesFailed = 0
      )
    }

    history.forEach { item ->
      val c = java.util.Calendar.getInstance()
      c.timeInMillis = item.timestamp
      c.set(java.util.Calendar.HOUR_OF_DAY, 0)
      c.set(java.util.Calendar.MINUTE, 0)
      c.set(java.util.Calendar.SECOND, 0)
      c.set(java.util.Calendar.MILLISECOND, 0)
      val bucketKey = c.timeInMillis
      val existing = dayBuckets[bucketKey] ?: return@forEach
      val derivedWords = if (item.wordsProcessed > 0) {
        item.wordsProcessed
      } else {
        item.processedLines * 6
      }
      dayBuckets[bucketKey] = existing.copy(
          wordsCount = existing.wordsCount + derivedWords,
          linesCount = existing.linesCount + item.processedLines,
          filesDone = existing.filesDone + if (item.status == "DONE") 1 else 0,
          filesFailed = existing.filesFailed + if (item.status != "DONE") 1 else 0
      )
    }

    return dayBuckets.values.toList()
  }
}
