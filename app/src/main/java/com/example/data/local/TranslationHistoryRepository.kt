package com.example.data.local

import kotlinx.coroutines.flow.Flow

class TranslationHistoryRepository(private val historyDao: TranslationHistoryDao) {
  val allHistory: Flow<List<TranslationHistoryEntity>> = historyDao.getAllHistory()

  suspend fun recordSession(entry: TranslationHistoryEntity): Long = historyDao.insert(entry)

  suspend fun deleteEntry(id: Long) = historyDao.deleteById(id)

  suspend fun clearAllHistory() = historyDao.clearAll()
}
