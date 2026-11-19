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
}
