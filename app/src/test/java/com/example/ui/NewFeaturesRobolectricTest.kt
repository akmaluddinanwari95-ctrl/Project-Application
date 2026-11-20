package com.example.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.SupportedLanguageEntity
import com.example.data.local.SupportedLanguageRepository
import com.example.data.local.TranslationHistoryEntity
import com.example.domain.CsvValidationIssue
import com.example.domain.CsvValidationReport
import com.example.domain.CsvValidationStatus
import com.example.domain.DailyProductivityStat
import com.example.domain.I18nStrings
import com.example.domain.SessionSummaryReport
import com.example.domain.TranslationErrorItem
import com.example.domain.TranslatorSettings
import com.example.domain.UiLanguage
import com.example.domain.WarbandTextProcessor
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
class NewFeaturesRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun jsonGlossaryImport_parsesObjectArrayAndNestedEntries() {
    val objJson = """{"Swadia":"Swadia","Vassal":"Vasali","Player":""}"""
    val objRes = WarbandTextProcessor.importGlossaryFromContent(objJson, "dict.json")
    assertEquals("JSON", objRes.formatDetected)
    assertEquals(3, objRes.importedCount)
    assertTrue(objRes.importedLinesText.contains("Vassal=Vasali"))
    assertTrue(objRes.importedLinesText.contains("Player"))

    val arrJson = """[{"source":"Praven","target":"Praven"},{"source":"Fief","target":"Tanah Wilayah"}]"""
    val arrRes = WarbandTextProcessor.importGlossaryFromContent(arrJson, "terms.json")
    assertEquals("JSON", arrRes.formatDetected)
    assertEquals(2, arrRes.importedCount)
    assertTrue(arrRes.importedLinesText.contains("Fief=Tanah Wilayah"))
  }

  @Test
  fun roomMigration2To3_preservesHistoryAndSeedsSupportedLanguages() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val dbName = "migration_test_${System.nanoTime()}.db"

    // Create a v2 database manually
    val helperConfig = SupportSQLiteOpenHelper.Configuration.builder(context)
        .name(dbName)
        .callback(object : SupportSQLiteOpenHelper.Callback(2) {
          override fun onCreate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `translation_cache` (
                  `targetLang` TEXT NOT NULL,
                  `sourceText` TEXT NOT NULL,
                  `translatedText` TEXT NOT NULL,
                  `updatedAt` INTEGER NOT NULL,
                  PRIMARY KEY(`targetLang`, `sourceText`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `translation_history` (
                  `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                  `fileName` TEXT NOT NULL,
                  `timestamp` INTEGER NOT NULL,
                  `status` TEXT NOT NULL,
                  `uriString` TEXT,
                  `totalLines` INTEGER NOT NULL,
                  `processedLines` INTEGER NOT NULL,
                  `sourceLang` TEXT NOT NULL,
                  `targetLang` TEXT NOT NULL,
                  `engineName` TEXT NOT NULL,
                  `sourceSnapshot` TEXT NOT NULL,
                  `translatedSnapshot` TEXT NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `translation_history`
                (`fileName`,`timestamp`,`status`,`uriString`,`totalLines`,`processedLines`,`sourceLang`,`targetLang`,`engineName`,`sourceSnapshot`,`translatedSnapshot`)
                VALUES ('legacy.csv', 1700000000000, 'DONE', NULL, 20, 20, 'en', 'id', 'Google Translate', '[]', '[]')
                """.trimIndent()
            )
          }

          override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
        })
        .build()

    FrameworkSQLiteOpenHelperFactory().create(helperConfig).writableDatabase.close()

    val migratedDb = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
        .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
        .allowMainThreadQueries()
        .build()
    try {
      val history = migratedDb.translationHistoryDao().searchHistory("legacy", "DONE").first()
      assertEquals(1, history.size)
      assertEquals("legacy.csv", history.first().fileName)

      val langRepo = SupportedLanguageRepository(migratedDb.supportedLanguageDao())
      val seeded = langRepo.targetLanguagesFlow.first()
      assertTrue(seeded.any { it.code == "id" })
      assertTrue(seeded.any { it.code == "tr" })

      langRepo.addCustomLanguage("uk", "Ukrainian (Custom)")
      val updatedEntities = migratedDb.supportedLanguageDao().getAllTargetLanguages().first()
      assertTrue(updatedEntities.any { it.code == "uk" && it.isCustom })
    } finally {
      migratedDb.close()
      context.deleteDatabase(dbName)
    }
  }

  @Test
  fun settingsScreen_medievalThemeSwitchTogglesState() {
    var themeEnabled = true

    composeTestRule.setContent {
      var checked by remember { mutableStateOf(true) }
      WarbandTranslatorTheme(medievalTheme = checked) {
        SettingsScreenContent(
            settings = TranslatorSettings(medievalThemeEnabled = checked),
            cacheCount = 12,
            tr = { I18nStrings.get(UiLanguage.EN, it) },
            onSelectExportDelimiter = {},
            onSelectExportEncoding = {},
            onSelectUiLanguage = {},
            onToggleMedievalTheme = {
              checked = it
              themeEnabled = it
            },
            onClearCache = {},
            onOpenHelp = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("medieval_theme_switch").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals(false, themeEnabled)
  }

  @Test
  fun historyScreen_searchAndFilterChipsUpdateCallbacksAndShowProductivityChart() {
    var lastQuery = ""
    var lastStatus = "ALL"

    val entries = listOf(
        TranslationHistoryEntity(
            id = 1L,
            fileName = "dialogs.csv",
            status = "DONE",
            totalLines = 100,
            processedLines = 100,
            wordsProcessed = 720,
            durationMs = 4500L
        )
    )
    val stats = listOf(
        DailyProductivityStat("19/11", 1L, 300, 40, 1, 0),
        DailyProductivityStat("20/11", 2L, 720, 100, 1, 0)
    )

    composeTestRule.setContent {
      var query by remember { mutableStateOf("") }
      WarbandTranslatorTheme {
        HistoryScreenContent(
            historyEntries = entries,
            isRunning = false,
            tr = { I18nStrings.get(UiLanguage.EN, it) },
            onReopenEntry = {},
            onDeleteEntry = {},
            onClearAll = {},
            onOpenHelp = {},
            searchQuery = query,
            onSearchQueryChange = {
              query = it
              lastQuery = it
            },
            statusFilter = "ALL",
            onStatusFilterChange = { lastStatus = it },
            dailyStats = stats,
            totalDoneCount = 1,
            totalFailedCount = 0
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("productivity_charts_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("productivity_words_bar_canvas").assertIsDisplayed()
    composeTestRule.onNodeWithTag("productivity_donut_canvas").assertIsDisplayed()

    composeTestRule.onNodeWithTag("history_search_input").performTextInput("dialogs")
    composeTestRule.waitForIdle()
    assertEquals("dialogs", lastQuery)

    composeTestRule.onNodeWithTag("history_filter_DONE").performClick()
    composeTestRule.waitForIdle()
    assertEquals("DONE", lastStatus)
  }

  @Test
  fun errorLogSection_inspectsAndRetriesFailedRows() {
    var retriedId: String? = null
    var retriedAll = false
    val entries = listOf(
        TranslationErrorItem(
            id = "e1",
            fileId = "f1",
            fileName = "dialogs.csv",
            lineIndex = 4,
            originalLine = "dlga_king|Hail to the king of Swadia!",
            errorMessage = "HTTP 503 Service Unavailable"
        )
    )

    composeTestRule.setContent {
      WarbandTranslatorTheme {
        ErrorLogSectionCard(
            entries = entries,
            isRunning = false,
            tr = { I18nStrings.get(UiLanguage.EN, it) },
            onRetryEntry = { retriedId = it },
            onRetryAllRowErrors = { retriedAll = true },
            onDismissEntry = {},
            onClearAll = {},
            onExportErrors = {},
            onOpenHelp = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("translation_error_log_section").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inspect_error_entry_0").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("inspected_error_line_0").assertIsDisplayed()

    composeTestRule.onNodeWithTag("retry_error_entry_0").performClick()
    assertEquals("e1", retriedId)

    composeTestRule.onNodeWithTag("retry_all_errors_button").performClick()
    assertTrue(retriedAll)
  }

  @Test
  fun sessionSummaryAndValidationDialog_displayMetricsAndHandleProceed() {
    var proceeded = false
    val summary = SessionSummaryReport(
        sessionId = 1L,
        completedAt = 1700000012500L,
        originalLines = 200,
        translatedLines = 180,
        skippedLines = 10,
        errorsEncountered = 2,
        totalDurationMs = 12500L,
        wordsProcessed = 1420,
        filesSucceeded = 2,
        filesFailed = 1,
        stoppedEarly = false
    )
    val stats = listOf(
        DailyProductivityStat("19/11", 1L, 500, 60, 1, 0),
        DailyProductivityStat("20/11", 2L, 1420, 180, 2, 1)
    )
    val reports = listOf(
        CsvValidationReport(
            fileId = "f1",
            fileName = "broken.csv",
            status = CsvValidationStatus.MALFORMED,
            totalLines = 10,
            separatorMatchedLines = 1,
            skippedLines = 0,
            alignedColumnsCount = 1,
            malformedLinesCount = 9,
            issues = listOf(CsvValidationIssue(2, "Delimiter '|' missing", "bad row")),
            summaryMessage = "Format suspicious"
        )
    )

    composeTestRule.setContent {
      WarbandTranslatorTheme {
        SessionSummaryDashboardCard(
            summary = summary,
            dailyStats = stats,
            tr = { I18nStrings.get(UiLanguage.EN, it) },
            onDismiss = {},
            onOpenHelp = {}
        )
        CsvValidationWarningDialog(
            visible = true,
            reports = reports,
            tr = { I18nStrings.get(UiLanguage.EN, it) },
            onProceedAnyway = { proceeded = true },
            onDismiss = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("session_summary_dashboard_card").assertIsDisplayed()
    composeTestRule.onNodeWithText("180 / 200 (90%)").assertIsDisplayed()
    composeTestRule.onNodeWithTag("csv_validation_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("validation_proceed_button").performClick()
    assertTrue(proceeded)
  }
}
