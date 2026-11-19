package com.example.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.TranslationHistoryEntity
import com.example.data.local.TranslationHistoryRepository
import com.example.domain.ExportDelimiter
import com.example.domain.ExportEncoding
import com.example.domain.I18nStrings
import com.example.domain.TranslatorSettings
import com.example.domain.UiLanguage
import com.example.ui.theme.WarbandTranslatorTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class SettingsAndHistoryRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun settingsScreen_allowsSelectingCsvDelimiterAndEncoding() {
    var selectedDelimiter: ExportDelimiter? = null
    var selectedEncoding: ExportEncoding? = null

    composeTestRule.setContent {
      WarbandTranslatorTheme {
        SettingsScreenContent(
            settings = TranslatorSettings(),
            cacheCount = 42,
            tr = { I18nStrings.get(UiLanguage.EN, it) },
            onSelectExportDelimiter = { selectedDelimiter = it },
            onSelectExportEncoding = { selectedEncoding = it },
            onSelectUiLanguage = {},
            onClearCache = {},
            onOpenHelp = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("settings_screen_content").assertIsDisplayed()
    composeTestRule.onNodeWithTag("export_delimiter_comma").performScrollTo().performClick()
    assertEquals(ExportDelimiter.COMMA, selectedDelimiter)

    composeTestRule.onNodeWithTag("export_delimiter_tab").performScrollTo().performClick()
    assertEquals(ExportDelimiter.TAB, selectedDelimiter)

    composeTestRule.onNodeWithTag("export_delimiter_semicolon").performScrollTo().performClick()
    assertEquals(ExportDelimiter.SEMICOLON, selectedDelimiter)

    composeTestRule.onNodeWithTag("export_encoding_utf8_bom").performScrollTo().performClick()
    assertEquals(ExportEncoding.UTF_8_BOM, selectedEncoding)

    composeTestRule.onNodeWithTag("export_encoding_utf16le").performScrollTo().performClick()
    assertEquals(ExportEncoding.UTF_16_LE, selectedEncoding)

    composeTestRule.onNodeWithTag("export_encoding_utf16be").performScrollTo().performClick()
    assertEquals(ExportEncoding.UTF_16_BE, selectedEncoding)
  }

  @Test
  fun settingsScreen_allowsConfiguringBatchRenamePrefixAndSuffix() {
    var currentPrefix = ""
    var currentSuffix = ""

    composeTestRule.setContent {
      var prefixState by remember { mutableStateOf("") }
      var suffixState by remember { mutableStateOf("") }
      WarbandTranslatorTheme {
        SettingsScreenContent(
            settings = TranslatorSettings(
                targetLang = "id",
                exportFilenamePrefix = prefixState,
                exportFilenameSuffix = suffixState
            ),
            cacheCount = 0,
            tr = { I18nStrings.get(UiLanguage.EN, it) },
            onSelectExportDelimiter = {},
            onSelectExportEncoding = {},
            onExportPrefixChange = {
              prefixState = it
              currentPrefix = it
            },
            onExportSuffixChange = {
              suffixState = it
              currentSuffix = it
            },
            onSelectUiLanguage = {},
            onClearCache = {},
            onOpenHelp = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("rename_preset_translated").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals("_translated", currentSuffix)

    composeTestRule.onNodeWithTag("rename_preset_lang").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals("_id", currentSuffix)

    composeTestRule.onNodeWithTag("rename_preset_original").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals("", currentPrefix)
    assertEquals("", currentSuffix)
  }

  @Test
  fun historyScreen_displaysEntriesAndHandlesReopenAndClear() {
    val entries = listOf(
        TranslationHistoryEntity(
            id = 10L,
            fileName = "dialogs.csv",
            timestamp = 1700000000000L,
            status = "DONE",
            totalLines = 150,
            processedLines = 150,
            sourceLang = "en",
            targetLang = "id",
            engineName = "Google Translate"
        )
    )
    var reopenedId: Long? = null
    var cleared = false

    composeTestRule.setContent {
      WarbandTranslatorTheme {
        HistoryScreenContent(
            historyEntries = entries,
            isRunning = false,
            tr = { I18nStrings.get(UiLanguage.EN, it) },
            onReopenEntry = { reopenedId = it.id },
            onDeleteEntry = {},
            onClearAll = { cleared = true },
            onOpenHelp = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("history_item_10").assertIsDisplayed()
    composeTestRule.onNodeWithTag("reopen_history_10").performClick()
    assertEquals(10L, reopenedId)

    composeTestRule.onNodeWithTag("clear_history_button").performClick()
    assertTrue(cleared)
  }

  @Test
  fun translationHistoryRepository_insertsReopensAndClearsInRoom() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    try {
      val repo = TranslationHistoryRepository(db.translationHistoryDao())
      val insertedId = repo.recordSession(
          TranslationHistoryEntity(
              fileName = "troops.csv",
              status = "DONE",
              totalLines = 20,
              processedLines = 20
          )
      )
      assertTrue(insertedId > 0L)
      val listAfterInsert = repo.allHistory.first()
      assertEquals(1, listAfterInsert.size)
      assertEquals("troops.csv", listAfterInsert.first().fileName)

      repo.clearAllHistory()
      val listAfterClear = repo.allHistory.first()
      assertTrue(listAfterClear.isEmpty())
    } finally {
      db.close()
    }
  }
}
