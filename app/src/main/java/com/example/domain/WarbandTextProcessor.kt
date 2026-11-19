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
}
