package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.domain.I18nStrings
import com.example.domain.QueuedFileItem
import com.example.domain.TelemetrySample
import com.example.domain.TranslationProgressState
import com.example.domain.UiLanguage
import com.example.domain.WarbandTextProcessor
import com.example.ui.theme.WarbandTranslatorTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class BatchUiComponentsRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun csvPreviewerCard_displaysFirstTenRowsAndSwitchesFiles() {
    val file1 = QueuedFileItem(
        id = "f1",
        name = "dialogs.csv",
        lines = (1..15).map { "str_dlg_$it|Dialogue line $it {s0}" }
    )
    val file2 = QueuedFileItem(
        id = "f2",
        name = "troops.csv",
        lines = (1..5).map { "trp_$it|Swadian Knight $it" }
    )
    val preview = WarbandTextProcessor.buildCsvStructurePreview(
        file = file1,
        separator = "|",
        stripTag = "{***}",
        skipRegex = "^\\d+$",
        maxRows = 10
    )
    var selectedId = "f1"

    composeTestRule.setContent {
      WarbandTranslatorTheme {
        Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
          CsvPreviewerCard(
              fileQueue = listOf(file1, file2),
              preview = preview,
              tr = { I18nStrings.get(UiLanguage.EN, it) },
              onSelectFile = { selectedId = it },
              onOpenHelp = {}
          )
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("csv_structure_preview_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("csv_preview_row_1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("csv_preview_row_10").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("csv_preview_tab_1").performScrollTo().performClick()
    assertEquals("f2", selectedId)
  }

  @Test
  fun telemetryChartCard_displaysRealtimeSpeedAndProgress() {
    val progress = TranslationProgressState(
        isRunning = true,
        currentFileIndex = 1,
        totalPendingFiles = 2,
        currentFileName = "dialogs.csv",
        processedLines = 50,
        totalLines = 100,
        currentSpeedLinesPerSec = 4.2f,
        peakSpeedLinesPerSec = 6.8f,
        telemetrySamples = listOf(
            TelemetrySample(0f, 0f, 0f, 0),
            TelemetrySample(1f, 3.5f, 25f, 25),
            TelemetrySample(2f, 4.2f, 50f, 50)
        )
    )

    composeTestRule.setContent {
      WarbandTranslatorTheme {
        TelemetryChartCard(
            progress = progress,
            tr = { I18nStrings.get(UiLanguage.EN, it) },
            onOpenHelp = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("realtime_telemetry_chart_card").assertIsDisplayed()
  }
}
