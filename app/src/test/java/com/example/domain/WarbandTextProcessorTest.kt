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

    val utf16Bytes = commaOut.toByteArray(ExportEncoding.UTF_16.toCharset())
    assertEquals(0xFE.toByte(), utf16Bytes[0])
    assertEquals(0xFF.toByte(), utf16Bytes[1])
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
}
