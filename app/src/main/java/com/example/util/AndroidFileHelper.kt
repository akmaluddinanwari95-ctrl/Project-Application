package com.example.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import com.example.domain.FileQueueStatus
import com.example.domain.QueuedFileItem
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class FileReadOutcome(
    val loaded: List<QueuedFileItem>,
    val rejectedNames: List<String>,
    val failedNames: List<String>
)

sealed class SaveOutcome {
  data class Saved(val count: Int, val locationLabel: String) : SaveOutcome()
  data class RequiresLegacyPicker(val pendingFiles: List<Pair<String, String>>) : SaveOutcome()
  data class Failed(val errorMessage: String) : SaveOutcome()
}

object AndroidFileHelper {
  private val allowedExtensions = setOf(".csv", ".txt", ".tsv")

  suspend fun readQueuedFiles(context: Context, uris: List<Uri>): FileReadOutcome =
      withContext(Dispatchers.IO) {
        val loaded = mutableListOf<QueuedFileItem>()
        val rejected = mutableListOf<String>()
        val failed = mutableListOf<String>()

        uris.forEach { uri ->
          val displayName = getDisplayName(context, uri) ?: "warband_file.csv"
          val dotIdx = displayName.lastIndexOf('.')
          val ext = if (dotIdx != -1) displayName.substring(dotIdx).lowercase() else ""
          if (ext !in allowedExtensions) {
            rejected.add(displayName)
            return@forEach
          }
          try {
            takePersistableReadPermission(context, uri)
            val content = readTextFromUri(context, uri)
            val lines = content
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .split('\n')
                .filter { it.trim().isNotEmpty() }

            loaded.add(
                QueuedFileItem(
                    id = UUID.randomUUID().toString(),
                    name = displayName,
                    uriString = uri.toString(),
                    lines = lines,
                    results = emptyList(),
                    status = FileQueueStatus.WAIT
                )
            )
          } catch (_: Exception) {
            failed.add(displayName)
          }
        }

        FileReadOutcome(loaded = loaded, rejectedNames = rejected, failedNames = failed)
      }

  fun takePersistableReadPermission(context: Context, uri: Uri) {
    try {
      context.contentResolver.takePersistableUriPermission(
          uri,
          android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
      )
    } catch (_: SecurityException) {
      // Some providers (e.g., temporary document URIs) do not support persistable grants
    } catch (_: Exception) {
    }
  }

  suspend fun readTextFromUri(context: Context, uri: Uri): String =
      withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { stream ->
          val bytes = stream.readBytes()
          val detectedCharset = when {
            bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte() -> StandardCharsets.UTF_16BE
            bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte() -> StandardCharsets.UTF_16LE
            else -> StandardCharsets.UTF_8
          }
          String(bytes, detectedCharset).removePrefix("\uFEFF")
        } ?: throw IllegalStateException("Tidak dapat membuka file")
      }

  suspend fun writeTextToUri(
      context: Context,
      uri: Uri,
      content: String,
      charset: Charset = StandardCharsets.UTF_8
  ) = withContext(Dispatchers.IO) {
    context.contentResolver.openOutputStream(uri, "wt")?.use { out ->
      out.write(content.toByteArray(charset))
      out.flush()
    } ?: throw IllegalStateException("Gagal menulis ke file tujuan")
  }

  suspend fun saveToDownloads(
      context: Context,
      files: List<Pair<String, String>>,
      mimeType: String = "text/csv",
      charset: Charset = StandardCharsets.UTF_8
  ): SaveOutcome = withContext(Dispatchers.IO) {
    if (files.isEmpty()) return@withContext SaveOutcome.Failed("Tidak ada file untuk disimpan")

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
      return@withContext SaveOutcome.RequiresLegacyPicker(files)
    }

    try {
      val resolver = context.contentResolver
      val relativeDir = "${Environment.DIRECTORY_DOWNLOADS}/WarbandTranslator"
      var savedCount = 0

      files.forEach { (fileName, content) ->
        val values = ContentValues().apply {
          put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
          put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
          put(MediaStore.MediaColumns.RELATIVE_PATH, relativeDir)
          put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("Gagal membuat file $fileName di Downloads")

        try {
          resolver.openOutputStream(uri, "w")?.use { out ->
            out.write(content.toByteArray(charset))
            out.flush()
          } ?: throw IllegalStateException("Gagal menulis isi $fileName")

          val doneValues = ContentValues().apply {
            put(MediaStore.MediaColumns.IS_PENDING, 0)
          }
          resolver.update(uri, doneValues, null, null)
          savedCount++
        } catch (e: Exception) {
          resolver.delete(uri, null, null)
          throw e
        }
      }

      SaveOutcome.Saved(savedCount, "Download/WarbandTranslator")
    } catch (e: Exception) {
      SaveOutcome.Failed(e.message ?: "Gagal menyimpan ke Downloads")
    }
  }

  suspend fun saveToFolderTree(
      context: Context,
      treeUri: Uri,
      files: List<Pair<String, String>>,
      charset: Charset = StandardCharsets.UTF_8
  ): SaveOutcome = withContext(Dispatchers.IO) {
    if (files.isEmpty()) return@withContext SaveOutcome.Failed("Tidak ada file untuk disimpan")

    try {
      val resolver = context.contentResolver
      val treeDocId = DocumentsContract.getTreeDocumentId(treeUri)
      val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, treeDocId)
      val existingDocs = queryChildDocuments(context, treeUri, treeDocId)
      var savedCount = 0

      files.forEach { (fileName, content) ->
        val existingUri = existingDocs[fileName]
        val targetUri = existingUri
            ?: DocumentsContract.createDocument(
                resolver,
                parentUri,
                "application/octet-stream",
                fileName
            )
            ?: throw IllegalStateException("Gagal membuat file $fileName di folder pilihan")

        resolver.openOutputStream(targetUri, "wt")?.use { out ->
          out.write(content.toByteArray(charset))
          out.flush()
        } ?: throw IllegalStateException("Gagal menulis $fileName")

        savedCount++
      }

      SaveOutcome.Saved(savedCount, "Folder pilihan")
    } catch (e: Exception) {
      SaveOutcome.Failed(e.message ?: "Gagal menyimpan ke folder pilihan")
    }
  }

  private fun queryChildDocuments(
      context: Context,
      treeUri: Uri,
      parentDocId: String
  ): Map<String, Uri> {
    val map = mutableMapOf<String, Uri>()
    val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
    val projection = arrayOf(
        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
        DocumentsContract.Document.COLUMN_DISPLAY_NAME
    )
    try {
      context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
        val idIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
        val nameIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
        while (cursor.moveToNext()) {
          if (idIdx >= 0 && nameIdx >= 0) {
            val docId = cursor.getString(idIdx)
            val name = cursor.getString(nameIdx)
            if (!docId.isNullOrEmpty() && !name.isNullOrEmpty()) {
              map[name] = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
            }
          }
        }
      }
    } catch (_: Exception) {
      // Ignore query failures and fall back to createDocument
    }
    return map
  }

  fun getDisplayName(context: Context, uri: Uri): String? {
    var name: String? = null
    if (uri.scheme == "content") {
      try {
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
          if (cursor.moveToFirst()) {
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0) name = cursor.getString(idx)
          }
        }
      } catch (_: Exception) {}
    }
    if (name == null) {
      name = uri.lastPathSegment?.substringAfterLast('/')
    }
    return name
  }
}
