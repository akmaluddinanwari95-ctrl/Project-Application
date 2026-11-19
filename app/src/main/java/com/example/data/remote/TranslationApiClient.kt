package com.example.data.remote

import android.net.Uri
import com.example.domain.TranslationEngine
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class TranslationApiException(message: String, val httpCode: Int? = null) : IOException(message)

class TranslationApiClient {
  private val client: OkHttpClient = OkHttpClient.Builder()
      .connectTimeout(10, TimeUnit.SECONDS)
      .readTimeout(12, TimeUnit.SECONDS)
      .writeTimeout(10, TimeUnit.SECONDS)
      .build()

  suspend fun translate(
      engine: TranslationEngine,
      text: String,
      sourceLang: String,
      targetLang: String
  ): String = withContext(Dispatchers.IO) {
    when (engine) {
      TranslationEngine.GOOGLE -> queryGoogleTranslate(text, sourceLang, targetLang)
      TranslationEngine.LINGVA -> queryLingvaTranslate(text, sourceLang, targetLang)
      TranslationEngine.MYMEMORY -> queryMyMemoryTranslate(text, sourceLang, targetLang)
    }
  }

  private fun queryGoogleTranslate(text: String, sl: String, tl: String): String {
    val encoded = URLEncoder.encode(text, "UTF-8")
    val url =
        "https://translate.googleapis.com/translate_a/single?client=gtx&sl=$sl&tl=$tl&dt=t&q=$encoded"
    val body = executeGet(url)
    val root = JSONArray(body)
    val segments = root.optJSONArray(0)
        ?: throw TranslationApiException("Respons Google Translate tidak valid")
    val sb = StringBuilder()
    for (i in 0 until segments.length()) {
      val chunk = segments.optJSONArray(i)
      if (chunk != null && chunk.length() > 0 && !chunk.isNull(0)) {
        sb.append(chunk.optString(0, ""))
      }
    }
    return sb.toString()
  }

  private fun queryLingvaTranslate(text: String, sl: String, tl: String): String {
    val source = if (sl == "auto") "auto" else sl
    val encodedSegment = Uri.encode(text)
    val mirrors = listOf(
        "https://lingva.ml/api/v1/$source/$tl/$encodedSegment",
        "https://lingva.lunar.icu/api/v1/$source/$tl/$encodedSegment"
    )
    var lastError: Exception? = null
    for (url in mirrors) {
      try {
        val body = executeGet(url)
        val obj = JSONObject(body)
        if (!obj.has("translation")) {
          throw TranslationApiException("Respons Lingva kosong")
        }
        return obj.getString("translation")
      } catch (e: Exception) {
        lastError = e
      }
    }
    throw lastError ?: TranslationApiException("Lingva gagal merespons")
  }

  private fun queryMyMemoryTranslate(text: String, sl: String, tl: String): String {
    val pair = if (sl == "auto") "en|$tl" else "$sl|$tl"
    val encodedText = URLEncoder.encode(text, "UTF-8")
    val encodedPair = URLEncoder.encode(pair, "UTF-8")
    val url = "https://api.mymemory.translated.net/get?q=$encodedText&langpair=$encodedPair"
    val body = executeGet(url)
    val obj = JSONObject(body)
    val status = obj.optInt("responseStatus", 0)
    if (status != 200) {
      val details = obj.optString("responseDetails", "Status $status")
      throw TranslationApiException("MyMemory: $details", status)
    }
    val dataObj = obj.optJSONObject("responseData")
        ?: throw TranslationApiException("Respons MyMemory kosong")
    return dataObj.getString("translatedText")
  }

  private fun executeGet(url: String): String {
    val request = Request.Builder()
        .url(url)
        .header(
            "User-Agent",
            "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
        )
        .get()
        .build()

    client.newCall(request).execute().use { response ->
      if (!response.isSuccessful) {
        throw TranslationApiException("HTTP ${response.code}", response.code)
      }
      return response.body?.string()
          ?: throw TranslationApiException("Body respons kosong")
    }
  }
}
