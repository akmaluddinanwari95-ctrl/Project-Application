package com.example.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WarbandTextProcessorTest {

  @Test
  fun parseGlossary_parsesNoTranslateAndForcedPairsSortedByLength() {
    val raw = """
      # comment
      Swadia
      Calradia
      Vassal=Vasali
      Fief=Tanah Wilayah
    """.trimIndent()

    val rules = WarbandTextProcessor.parseGlossary(raw)
    assertEquals(listOf("Calradia", "Swadia"), rules.noTranslate)
    assertEquals(
        listOf(
            GlossaryPair("Vassal", "Vasali"),
            GlossaryPair("Fief", "Tanah Wilayah")
        ),
        rules.forcedPairs
    )
    assertEquals(4, rules.totalCount)
  }

  @Test
  fun protectAndRestoreTokens_preservesVariablesCaretsAndGlossary() {
    val rules = WarbandTextProcessor.parseGlossary("Swadia\nVassal=Vasali")
    val input = "Hail {playername}! Swadia has {reg1} knights^and Vassal loyalty %s."

    val protectedResult = WarbandTextProcessor.protectWarbandTokens(input, rules)
    assertFalse(protectedResult.protectedText.contains("{playername}"))
    assertFalse(protectedResult.protectedText.contains("{reg1}"))
    assertFalse(protectedResult.protectedText.contains("Swadia"))

    val simulatedTranslation = protectedResult.protectedText
        .replace("Hail", "Salam")
        .replace("has", "memiliki")
        .replace("knights", "ksatria")
        .replace("and", "dan")
        .replace("loyalty", "kesetiaan")

    val restored = WarbandTextProcessor.restoreWarbandTokens(
        simulatedTranslation,
        protectedResult.tokens,
        rules
    )

    assertTrue(restored.contains("{playername}"))
    assertTrue(restored.contains("{reg1}"))
    assertTrue(restored.contains("Swadia"))
    assertTrue(restored.contains("Vasali"))
    assertTrue(restored.contains("^"))
    assertTrue(restored.contains("%s"))
  }

  @Test
  fun splitLine_stripsTagAndPreservesCodeId() {
    val line = "str_greeting|{***}Hello {s0}, welcome!"
    val split = WarbandTextProcessor.splitLine(line, "|", "{***}")
    assertTrue(split.hasCode)
    assertEquals("str_greeting", split.codePart)
    assertEquals("Hello {s0}, welcome!", split.textPart)
  }

  @Test
  fun skipRegex_matchesNumberOnlyLines() {
    assertTrue(WarbandTextProcessor.matchesSkipRegex("12345", "^\\d+$"))
    assertFalse(WarbandTextProcessor.matchesSkipRegex("str_id|Hello", "^\\d+$"))
    assertFalse(WarbandTextProcessor.isValidRegex("[unclosed"))
    assertTrue(WarbandTextProcessor.isValidRegex("^\\d+$"))
  }

  @Test
  fun buildCsvStructurePreview_limitsToTenRowsAndExtractsColumnsAndTokens() {
    val lines = (1..15).map { idx ->
      if (idx == 2) "99999"
      else "str_line_$idx|{***}Line $idx for {playername} with {s0}"
    }
    val item = QueuedFileItem(id = "f1", name = "dialogs.csv", lines = lines)

    val preview = WarbandTextProcessor.buildCsvStructurePreview(
        file = item,
        separator = "|",
        stripTag = "{***}",
        skipRegex = "^\\d+$",
        maxRows = 10
    )

    assertEquals(10, preview.rows.size)
    assertEquals(9, preview.matchedSeparatorCount)
    assertEquals(1, preview.skippedCount)

    val row1 = preview.rows[0]
    assertEquals(1, row1.rowNumber)
    assertTrue(row1.hasSeparator)
    assertEquals("str_line_1", row1.columnKey)
    assertEquals("Line 1 for {playername} with {s0}", row1.columnText)
    assertEquals(listOf("{playername}", "{s0}"), row1.protectedTokens)
    assertFalse(row1.isSkippedByRegex)

    val row2 = preview.rows[1]
    assertEquals(2, row2.rowNumber)
    assertFalse(row2.hasSeparator)
    assertTrue(row2.isSkippedByRegex)
  }

  @Test
  fun formatExportContent_appliesCommaTabSemicolonAndQuoting() {
    val lines = listOf(
        "str_a|Hello, Calradia!",
        "str_b|Swadia"
    )

    val commaOut = WarbandTextProcessor.formatExportContent(lines, "|", ExportDelimiter.COMMA)
    assertEquals("str_a,\"Hello, Calradia!\"\r\nstr_b,Swadia\r\n", commaOut)

    val tabOut = WarbandTextProcessor.formatExportContent(lines, "|", ExportDelimiter.TAB)
    assertEquals("str_a\tHello, Calradia!\r\nstr_b\tSwadia\r\n", tabOut)

    val semiOut = WarbandTextProcessor.formatExportContent(lines, "|", ExportDelimiter.SEMICOLON)
    assertEquals("str_a;Hello, Calradia!\r\nstr_b;Swadia\r\n", semiOut)

    val utf8BomBytes = com.example.util.AndroidFileHelper.encodeExportContent(commaOut, ExportEncoding.UTF_8_BOM)
    assertEquals(0xEF.toByte(), utf8BomBytes[0])
    assertEquals(0xBB.toByte(), utf8BomBytes[1])
    assertEquals(0xBF.toByte(), utf8BomBytes[2])

    val utf16LeBytes = com.example.util.AndroidFileHelper.encodeExportContent(commaOut, ExportEncoding.UTF_16_LE)
    assertEquals(0xFF.toByte(), utf16LeBytes[0])
    assertEquals(0xFE.toByte(), utf16LeBytes[1])

    val utf16BeBytes = com.example.util.AndroidFileHelper.encodeExportContent(commaOut, ExportEncoding.UTF_16_BE)
    assertEquals(0xFE.toByte(), utf16BeBytes[0])
    assertEquals(0xFF.toByte(), utf16BeBytes[1])

    assertEquals(ExportEncoding.UTF_16_LE, ExportEncoding.fromId("utf16"))
    assertEquals(ExportEncoding.UTF_8_BOM, ExportEncoding.fromId("utf8_bom"))
    assertEquals(ExportEncoding.UTF_16_BE, ExportEncoding.fromId("utf16be"))
  }

  @Test
  fun buildExportFileName_appliesPrefixSuffixAndSanitizesUnsafeChars() {
    assertEquals(
        "dialogs.csv",
        WarbandTextProcessor.buildExportFileName("dialogs.csv", "", "")
    )
    assertEquals(
        "dialogs_translated.csv",
        WarbandTextProcessor.buildExportFileName("dialogs.csv", "", "_translated")
    )
    assertEquals(
        "id_dialogs_translated.csv",
        WarbandTextProcessor.buildExportFileName("dialogs.csv", "id_", "_translated")
    )
    assertEquals(
        "id_readme_translated",
        WarbandTextProcessor.buildExportFileName("readme", "id_", "_translated")
    )
    assertEquals(
        "idtroops_mod.csv",
        WarbandTextProcessor.buildExportFileName("troops.csv", "id/", "_mod\\:*?")
    )
  }

  @Test
  fun mergeGlossaryText_mergesPresetsWithoutDuplicates() {
    val factions = WarbandTextProcessor.builtInGlossaryPresets.first { it.id == "factions" }
    val merged1 = WarbandTextProcessor.mergeGlossaryText("Swadia\nCustomTerm=Istilah", factions.termsText)
    assertTrue(merged1.contains("CustomTerm=Istilah"))
    assertTrue(merged1.contains("Calradia"))
    val swadiaCount = merged1.lines().count { it.equals("Swadia", ignoreCase = true) }
    assertEquals(1, swadiaCount)
  }

  @Test
  fun applyMedievalTonePostProcessing_fixesIndonesianTitlesAndProtectsTokens() {
    val raw = "tuanku saya , {playername} memiliki pasukan saya di kastil saya ."
    val processed = WarbandTextProcessor.applyMedievalTonePostProcessing(raw, "id", enabled = true)
    assertTrue(processed.startsWith("Tuanku, {playername}"))
    assertTrue(processed.contains("pasukanku"))
    assertTrue(processed.contains("kastilku."))
    assertFalse(processed.contains(" ,"))

    val disabled = WarbandTextProcessor.applyMedievalTonePostProcessing(raw, "id", enabled = false)
    assertEquals(raw, disabled)
  }

  @Test
  fun auditTranslatedFile_detectsMissingTokensAndBrokenSeparatorAndAutoFixes() {
    val item = QueuedFileItem(
        id = "q1",
        name = "dialogs.csv",
        lines = listOf(
            "str_ok|Hello {playername}!",
            "str_missing_tok|Pay {reg1} denars to {s0}.",
            "str_broken|Visit Praven today."
        ),
        results = listOf(
            "str_ok|Halo {playername}!",
            "str_missing_tok|Bayar {reg1} denar.",
            "Visit Praven hari ini."
        )
    )

    val audit = WarbandTextProcessor.auditTranslatedFile(
        file = item,
        separator = "|",
        stripTag = "{***}",
        skipRegex = "^\\d+$"
    )
    assertEquals(3, audit.totalRows)
    assertEquals(2, audit.issueCount)
    assertTrue(QaIssueType.MISSING_TOKEN in audit.rows[1].issues)
    assertEquals(listOf("{s0}"), audit.rows[1].missingTokens)
    assertTrue(QaIssueType.BROKEN_SEPARATOR in audit.rows[2].issues)

    val fixedLine = WarbandTextProcessor.autoFixMissingTokens(
        originalLine = item.lines[1],
        translatedLine = item.results[1],
        separator = "|",
        stripTag = "{***}"
    )
    assertEquals("str_missing_tok|Bayar {reg1} denar. {s0}", fixedLine)
  }

  @Test
  fun mergeModUpdateDiff_matchesByCodeAndSeedsCachePairs() {
    val newModLines = listOf(
        "999",
        "str_welcome|{***}Welcome to Calradia!",
        "str_new_quest|{***}Defend the village of {s1}."
    )
    val oldTranslated = listOf(
        "str_welcome|Selamat datang di Calradia!",
        "str_removed|Teks lama"
    )

    val diff = WarbandTextProcessor.mergeModUpdateDiff(
        newModLines = newModLines,
        oldTranslatedLines = oldTranslated,
        separator = "|",
        stripTag = "{***}",
        skipRegex = "^\\d+$"
    )

    assertEquals(1, diff.carriedOverCount)
    assertEquals(1, diff.newOrChangedCount)
    assertEquals(1, diff.skippedCount)
    assertEquals("Selamat datang di Calradia!", diff.seededCachePairs["Welcome to Calradia!"])
  }

  @Test
  fun findAndReplaceInLines_replacesOnlyRightOfSeparator() {
    val lines = listOf(
        "str_king_harlaus|King Harlaus rules Swadia.",
        "str_king_yaroglek|King Yaroglek rules Vaegir."
    )
    val (updated, count) = WarbandTextProcessor.findAndReplaceInLines(
        lines = lines,
        findQuery = "king",
        replaceWith = "Raja",
        caseSensitive = false,
        separator = "|"
    )
    assertEquals(2, count)
    assertEquals("str_king_harlaus|Raja Harlaus rules Swadia.", updated[0])
    assertEquals("str_king_yaroglek|Raja Yaroglek rules Vaegir.", updated[1])
  }

  @Test
  fun importGlossaryFromContent_parsesCsvWithHeadersAndQuotesAndTsv() {
    val csv = """
      source,target
      "King Harlaus","Raja Harlaus"
      "Swadian Knight",Ksatria Swadia
      Calradia,
    """.trimIndent()

    val csvResult = WarbandTextProcessor.importGlossaryFromContent(csv, "terms.csv")
    assertEquals("CSV", csvResult.formatDetected)
    assertEquals(3, csvResult.importedCount)
    assertTrue(csvResult.importedLinesText.contains("King Harlaus=Raja Harlaus"))
    assertTrue(csvResult.importedLinesText.contains("Swadian Knight=Ksatria Swadia"))
    assertTrue(csvResult.importedLinesText.contains("Calradia"))

    val tsv = "source\ttarget\nPraven\tPraven\nVassal\tVasali"
    val tsvResult = WarbandTextProcessor.importGlossaryFromContent(tsv, "glossary.tsv")
    assertEquals("TSV", tsvResult.formatDetected)
    assertEquals(2, tsvResult.importedCount)
    assertTrue(tsvResult.importedLinesText.contains("Vassal=Vasali"))
  }

  @Test
  fun validateCsvStructure_detectsValidWarningAndMalformedFiles() {
    val validFile = QueuedFileItem(
        id = "v1",
        name = "dialogs.csv",
        lines = listOf(
            "dlga_1|Hail, {playername}!",
            "dlga_2|May Swadia endure."
        )
    )
    val validReport = WarbandTextProcessor.validateCsvStructure(validFile, "|", "{***}", "^\\d+$")
    assertEquals(CsvValidationStatus.VALID, validReport.status)
    assertEquals(0, validReport.malformedLinesCount)

    val warningFile = QueuedFileItem(
        id = "w1",
        name = "partial.csv",
        lines = listOf(
            "dlga_1|First valid line",
            "dlga_2|Second valid line",
            "dlga_3|Third | extra pipe line"
        )
    )
    val warningReport = WarbandTextProcessor.validateCsvStructure(warningFile, "|", "{***}", "^\\d+$")
    assertEquals(CsvValidationStatus.WARNING, warningReport.status)
    assertTrue(warningReport.issues.isNotEmpty())

    val malformedFile = QueuedFileItem(
        id = "m1",
        name = "broken.csv",
        lines = listOf(
            "comma_separated,no_pipe_here",
            "another_broken_row,text",
            "|empty_id_left"
        )
    )
    val malformedReport = WarbandTextProcessor.validateCsvStructure(malformedFile, "|", "{***}", "^\\d+$")
    assertEquals(CsvValidationStatus.MALFORMED, malformedReport.status)
    assertTrue(malformedReport.malformedLinesCount >= 3)
  }

  @Test
  fun countWordsAndDailyStats_aggregatesRecentDays() {
    val words = WarbandTextProcessor.countWordsInLines(
        lines = listOf("999", "str_a|Hello brave knights", "str_b|Swadia"),
        separator = "|",
        stripTag = "{***}",
        skipRegex = "^\\d+$"
    )
    assertEquals(4, words)

    val now = System.currentTimeMillis()
    val stats = WarbandTextProcessor.computeDailyProductivityStats(
        history = listOf(
            com.example.data.local.TranslationHistoryEntity(
                fileName = "a.csv",
                timestamp = now,
                status = "DONE",
                processedLines = 10,
                wordsProcessed = 65
            ),
            com.example.data.local.TranslationHistoryEntity(
                fileName = "b.csv",
                timestamp = now,
                status = "ERROR",
                processedLines = 2,
                wordsProcessed = 12
            )
        ),
        daysCount = 7
    )
    assertEquals(7, stats.size)
    val todayBucket = stats.last()
    assertEquals(77, todayBucket.wordsCount)
    assertEquals(1, todayBucket.filesDone)
    assertEquals(1, todayBucket.filesFailed)
  }
}
