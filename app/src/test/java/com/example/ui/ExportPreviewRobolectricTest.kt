package com.example.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.domain.ExportDelimiter
import com.example.domain.ExportEncoding
import com.example.domain.I18nStrings
import com.example.domain.PendingExportPreviewItem
import com.example.domain.UiLanguage
import com.example.domain.WarbandTextProcessor
import com.example.ui.theme.WarbandTranslatorTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class ExportPreviewRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun exportPreviewDialog_showsSampleSwitchesFilesUpdatesRenameAndTriggersSave() {
    var savedToDownloads = false
    var pickedFolder = false

    composeTestRule.setContent {
      var prefix by remember { mutableStateOf("") }
      var suffix by remember { mutableStateOf("") }

      val items = listOf(
          PendingExportPreviewItem(
              fileId = "1",
              originalFileName = "dialogs.csv",
              outputFileName = WarbandTextProcessor.buildExportFileName("dialogs.csv", prefix, suffix),
              formattedContent = "dlga_greet|Salam hangat, {playername}!\r\ndlga_bye|Sampai jumpa!\r\n",
              lineCount = 2
          ),
          PendingExportPreviewItem(
              fileId = "2",
              originalFileName = "troops.csv",
              outputFileName = WarbandTextProcessor.buildExportFileName("troops.csv", prefix, suffix),
              formattedContent = "trp_swadian_knight|Ksatria Swadia\r\n",
              lineCount = 1
          )
      )

      WarbandTranslatorTheme {
        ExportPreviewAndSaveDialog(
            visible = true,
            previewItems = items,
            exportDelimiter = ExportDelimiter.SOURCE_PIPE,
            exportEncoding = ExportEncoding.UTF_8,
            prefix = prefix,
            suffix = suffix,
            targetLang = "id",
            tr = { I18nStrings.get(UiLanguage.EN, it) },
            onPrefixChange = { prefix = it },
            onSuffixChange = { suffix = it },
            onSaveToDownloads = { savedToDownloads = true },
            onPickFolder = { pickedFolder = true },
            onDismiss = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("export_preview_sample_box").assertIsDisplayed()
    composeTestRule.onNodeWithTag("export_preview_output_filename").assertTextContains("dialogs.csv")

    // Switch to second batch file
    composeTestRule.onNodeWithTag("export_preview_file_chip_1").performClick()
    composeTestRule.onNodeWithTag("export_preview_output_filename").assertTextContains("troops.csv")

    // Apply _translated suffix and custom prefix
    composeTestRule.onNodeWithTag("preview_rename_preset_translated").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("export_preview_prefix_input").performScrollTo().performTextInput("id_")
    composeTestRule.onNodeWithTag("export_preview_output_filename")
        .assertTextContains("id_troops_translated.csv", substring = true)

    // Trigger both save destinations
    composeTestRule.onNodeWithTag("save_to_downloads_option").performScrollTo().performClick()
    assertTrue(savedToDownloads)

    composeTestRule.onNodeWithTag("save_to_folder_option").performScrollTo().performClick()
    assertTrue(pickedFolder)
  }
}
