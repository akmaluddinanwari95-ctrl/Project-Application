package com.example.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.data.local.TranslationCacheEntity
import com.example.domain.I18nStrings
import com.example.domain.ModDiffMergeResult
import com.example.domain.QaAuditRow
import com.example.domain.QaAuditSummary
import com.example.domain.QaIssueType
import com.example.domain.UiLanguage
import com.example.ui.theme.WarbandTranslatorTheme
import com.example.util.AndroidFileHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class QaEditorAndDiffMergeRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun fullEditorAndQaDialog_displaysIssuesTriggersFindReplaceAndAutoFix() {
    var autoFixedFileId: String? = null
    var replacedArgs: List<Any>? = null

    val summary = QaAuditSummary(
        fileId = "file-1",
        fileName = "dialogs.csv",
        totalRows = 2,
        issueCount = 1,
        rows = listOf(
            QaAuditRow(
                lineIndex = 0,
                codePart = "str_ok",
                originalLine = "str_ok|Hello {playername}",
                translatedLine = "str_ok|Halo {playername}",
                missingTokens = emptyList(),
                extraTokens = emptyList(),
                issues = emptyList()
            ),
            QaAuditRow(
                lineIndex = 1,
                codePart = "str_pay",
                originalLine = "str_pay|Pay {reg1} gold to {s0}",
                translatedLine = "str_pay|Bayar {reg1} emas",
                missingTokens = listOf("{s0}"),
                extraTokens = emptyList(),
                issues = listOf(QaIssueType.MISSING_TOKEN)
            )
        )
    )

    composeTestRule.setContent {
      WarbandTranslatorTheme {
        FullEditorAndQaDialog(
            summary = summary,
            tr = { I18nStrings.get(UiLanguage.EN, it) },
            onSaveLine = { _, _, _ -> },
            onFindAndReplace = { fileId, find, replace, caseSensitive ->
              replacedArgs = listOf(fileId, find, replace, caseSensitive)
            },
            onAutoFixMissingTokens = { autoFixedFileId = it },
            onDismiss = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("qa_editor_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("qa_status_banner").assertIsDisplayed()
    composeTestRule.onNodeWithTag("qa_row_1").assertIsDisplayed()

    composeTestRule.onNodeWithTag("qa_auto_fix_button").performClick()
    assertEquals("file-1", autoFixedFileId)

    composeTestRule.onNodeWithTag("qa_find_input").performTextInput("emas")
    composeTestRule.onNodeWithTag("qa_replace_input").performTextInput("denar")
    composeTestRule.onNodeWithTag("qa_replace_all_button").performClick()
    assertEquals(listOf("file-1", "emas", "denar", false), replacedArgs)
  }

  @Test
  fun modDiffMergeDialog_displaysSummaryAndApplies() {
    var applied = false
    val diffPreview = ModDiffMergeResult(
        mergedLines = listOf("str_a|A", "str_b|B"),
        seededCachePairs = mapOf("A" to "Terjemahan A"),
        carriedOverCount = 12,
        newOrChangedCount = 3,
        skippedCount = 1
    )

    composeTestRule.setContent {
      WarbandTranslatorTheme {
        ModDiffMergeDialog(
            visible = true,
            newModName = "new_dialogs.csv",
            oldTransName = "old_dialogs_id.csv",
            diffPreview = diffPreview,
            tr = { I18nStrings.get(UiLanguage.EN, it) },
            onPickNewModFile = {},
            onPickOldTranslatedFile = {},
            onApplyDiff = { applied = true },
            onDismiss = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("mod_diff_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mod_diff_summary_box").assertIsDisplayed()
    composeTestRule.onNodeWithTag("diff_apply_button").performClick()
    assertTrue(applied)
  }

  @Test
  fun cacheBackupJson_roundTripsCleanly() {
    val entries = listOf(
        TranslationCacheEntity(
            targetLang = "id",
            sourceText = "Swadian Knight",
            translatedText = "Kesatria Swadia",
            updatedAt = 123456789L
        ),
        TranslationCacheEntity(
            targetLang = "id",
            sourceText = "Hail {playername}!",
            translatedText = "Salam {playername}!",
            updatedAt = 987654321L
        )
    )

    val json = AndroidFileHelper.serializeCacheBackupJson(entries)
    val parsed = AndroidFileHelper.parseCacheBackupJson(json)
    assertEquals(2, parsed.size)
    assertEquals("Swadian Knight", parsed[0].sourceText)
    assertEquals("Kesatria Swadia", parsed[0].translatedText)
    assertEquals("id", parsed[1].targetLang)
  }
}
