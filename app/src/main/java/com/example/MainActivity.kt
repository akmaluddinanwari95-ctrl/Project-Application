package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import com.example.ui.TranslatorScreen
import com.example.ui.TranslatorViewModel
import com.example.ui.UiEvent
import com.example.ui.theme.WarbandTranslatorTheme

class MainActivity : ComponentActivity() {
  private val viewModel: TranslatorViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      WarbandTranslatorTheme {
        val snackbarHostState = remember { SnackbarHostState() }

        val csvPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenMultipleDocuments()
        ) { uris ->
          if (uris.isNotEmpty()) {
            viewModel.handlePickedFiles(uris)
          }
        }

        val singleCsvPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->
          uri?.let { viewModel.handlePickedFiles(listOf(it)) }
        }

        val glossaryImportLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->
          uri?.let { viewModel.importGlossaryFromUri(it) }
        }

        val glossaryExportLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("text/plain")
        ) { uri ->
          uri?.let { viewModel.exportGlossaryToUri(it) }
        }

        val errorLogExportLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("text/plain")
        ) { uri ->
          uri?.let { viewModel.exportErrorLogToUri(it) }
        }

        val folderTreeLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocumentTree()
        ) { treeUri ->
          treeUri?.let { viewModel.savePendingToTreeUri(it) }
        }

        val legacyCreateDocumentLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("text/csv")
        ) { uri ->
          viewModel.handleLegacySaveUri(uri)
        }

        val notificationPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { _ ->
          // Start translation regardless of whether notifications are granted or denied
          viewModel.startTranslation()
        }

        LaunchedEffect(Unit) {
          viewModel.uiEvents.collect { event ->
            when (event) {
              is UiEvent.ShowToast -> {
                snackbarHostState.showSnackbar(event.message)
              }
              is UiEvent.LaunchLegacyCreateDocument -> {
                legacyCreateDocumentLauncher.launch(event.fileName)
              }
            }
          }
        }

        val csvMimeTypes = arrayOf(
            "text/csv",
            "text/comma-separated-values",
            "application/csv",
            "text/tab-separated-values",
            "text/plain",
            "text/*",
            "application/octet-stream"
        )

        TranslatorScreen(
            viewModel = viewModel,
            snackbarHostState = snackbarHostState,
            onPickCsvFiles = {
              csvPickerLauncher.launch(csvMimeTypes)
            },
            onPickSingleCsvFile = {
              singleCsvPickerLauncher.launch(csvMimeTypes)
            },
            onImportGlossary = {
              glossaryImportLauncher.launch(arrayOf("text/plain", "text/*"))
            },
            onExportGlossary = {
              glossaryExportLauncher.launch("warband_glossary.txt")
            },
            onExportErrorLog = {
              errorLogExportLauncher.launch("warband_translation_errors.txt")
            },
            onPickSaveFolder = {
              folderTreeLauncher.launch(null)
            },
            onStartWithNotificationCheck = {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!granted) {
                  notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                  viewModel.startTranslation()
                }
              } else {
                viewModel.startTranslation()
              }
            }
        )
      }
    }
  }
}
