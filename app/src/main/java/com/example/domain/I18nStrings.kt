package com.example.domain

data class HelpTopicContent(
    val title: String,
    val paragraphs: List<String>
)

object I18nStrings {
  private val stringsByLang: Map<UiLanguage, Map<String, String>> = mapOf(
      UiLanguage.ID to mapOf(
          "navTitle" to "M&B Warband Translator",
          "navSub" to "Gratis · Tanpa API Key · Auto-Resume",
          "heroBadge" to "Mendukung Mod PC & Android (Dynasties of Calradia, Diplomacy, dll.)",
          "heroTitle" to "Penerjemah CSV Warband",
          "heroDesc" to "Terjemahkan ribuan baris dialogs.csv, game_strings.csv, dan troops.csv secara massal langsung di HP Android tanpa kunci API berbayar.",
          "secEngine" to "1. Mesin Terjemahan Gratis & Kecepatan",
          "statusReady" to "Siap digunakan",
          "engGoogleSub" to "Tercepat · Langsung",
          "engLingvaSub" to "Alternatif Google · Stabil",
          "engMmSub" to "~1000 baris/hari · Cadangan",
          "lblSpeed" to "Jeda Permintaan (Anti-Blokir HTTP 429)",
          "spdFast" to "⚡ Cepat (80 ms) — Cocok untuk file kecil",
          "spdNormal" to "⚖️ Normal (200 ms) — Direkomendasikan",
          "spdSafe" to "🛡️ Aman (500 ms) — Anti-limit untuk ribuan baris",
          "lblCache" to "Memori Cache (Auto-Resume)",
          "cacheUnit" to "baris",
          "chkFallback" to "Auto-Fallback ke mesin lain jika gagal",
          "btnClearCache" to "Hapus Cache",
          "secFormat" to "2. Bahasa & Struktur Teks Warband",
          "lblSrcLang" to "Bahasa Sumber (Mod Asli)",
          "optAuto" to "Deteksi Otomatis",
          "lblTgtLang" to "Bahasa Tujuan (Hasil)",
          "lblSep" to "Pemisah Kode (Separator)",
          "hintSep" to "Kode di kiri (str_...) tidak diterjemahkan.",
          "lblStrip" to "Hapus Tag (Opsional)",
          "hintStrip" to "Kosongkan jika tag {***} ingin dipertahankan.",
          "lblRegex" to "Lewati Baris (Regex)",
          "hintRegex" to "Baris angka saja dilewati otomatis.",
          "regexValid" to "✓ Pola regex valid",
          "regexInvalid" to "✕ Pola regex tidak valid",
          "varProtDesc" to "Pelindung Variabel Aktif: Token seperti {s0}, {reg1}, {playername}, {lord}, ^^, dan ^ dikunci otomatis agar tidak rusak atau ikut diterjemahkan.",
          "secGloss" to "3. Glosarium Istilah (Opsional)",
          "glossHint" to "Kosong secara default agar Anda bebas mengisi istilah sendiri. Tulis 1 istilah per baris: tanpa '=' (misal: Swadia) agar tidak diterjemahkan, atau dengan '=' (misal: Vassal=Vasali) untuk terjemahan khusus.",
          "glossPlaceholder" to "Contoh (1 istilah per baris):\nSwadia\nCalradia\nVassal=Vasali\nFief=Tanah Wilayah",
          "btnExpGloss" to "Ekspor (.txt)",
          "btnImpGloss" to "Impor (.txt/.csv/.json)",
          "btnClearGloss" to "Kosongkan",
          "secQueue" to "4. Unggah File CSV / TXT / TSV",
          "queueHint" to "Bisa pilih banyak file sekaligus",
          "dropTitle" to "Ketuk di sini untuk memilih file modul Warband",
          "dropSub" to "Contoh: dialogs.csv, game_strings.csv, quick_strings.csv, troops.csv, factions.csv",
          "btnClearQueue" to "Kosongkan Antrean",
          "btnStart" to "Mulai Terjemahkan",
          "btnPause" to "Jeda",
          "btnResume" to "Lanjutkan",
          "btnStop" to "Berhenti",
          "btnDlAll" to "Simpan Semua File Selesai",
          "secPreview" to "Pratinjau & Edit Cepat (5 Baris Terakhir)",
          "previewHint" to "Ketuk tombol Edit untuk revisi manual sebelum simpan",
          "secLog" to "Log Aktivitas Sistem",
          "btnClearLog" to "Bersihkan Log",
          "secGuide" to "Panduan Memasang Hasil Terjemahan ke Warband Android",
          "gStep1" to "Simpan file CSV yang sudah selesai diterjemahkan (misalnya dialogs.csv, game_strings.csv, dll.).",
          "gStep2" to "Buka ZArchiver, lalu masuk ke folder mod Anda di: /Penyimpanan Internal/mnb/Modules/Dynasties_of_Calradia/languages/.",
          "gStep3" to "Buat folder baru bernama id (atau salin isi folder en ke dalam folder id).",
          "gStep4" to "Tempel dan timpa file CSV hasil terjemahan ke dalam folder languages/id/ tersebut (atau pilih langsung folder tersebut saat menyimpan).",
          "gStep5" to "Buka MnBTool atau pengaturan bahasa di dalam game dan pilih bahasa Indonesian (id).",
          "footerCredit" to "Terinspirasi dari Warband CSV Translator (Abdullah Zubeyr Akyüz) · Dioptimalkan untuk Komunitas Mount & Blade Indonesia",
          "stWait" to "Menunggu",
          "stRun" to "Menerjemahkan...",
          "stDone" to "Selesai",
          "stErr" to "Gagal",
          "btnDlFile" to "Simpan",
          "btnCloseHelp" to "Mengerti",
          "glossBadgeUnit" to "istilah",
          "btnEdit" to "Edit",
          "btnSave" to "Simpan",
          "btnCancel" to "Batal",
          "saveDialogTitle" to "Pilih Lokasi Penyimpanan",
          "saveDialogDesc" to "Pilih apakah ingin menyimpan langsung ke folder Downloads atau memilih folder tujuan khusus (seperti folder languages/id/ mod).",
          "saveToDownloads" to "Simpan ke Folder Downloads",
          "saveToDownloadsSub" to "Langsung masuk ke Download/WarbandTranslator",
          "saveToFolder" to "Pilih Folder Tujuan (SAF)",
          "saveToFolderSub" to "Misalnya langsung ke folder languages/id/ mod Anda",
          "lblErrExport" to "Ekspor Log Baris Gagal",
          "btnPickSingle" to "Tambah 1 File",
          "autoResumeReady" to "Auto-Resume",
          "btnInspectCsv" to "Pratinjau 10 Baris",
          "secCsvStruct" to "Pratinjau Struktur CSV (10 Baris Pertama)",
          "csvStructHint" to "Periksa pemisahan kolom ID & teks sebelum memulai terjemahan",
          "csvColIdx" to "#",
          "csvColKey" to "Kolom 1 (ID / Kode)",
          "csvColText" to "Kolom 2 (Teks Sumber)",
          "csvColStatus" to "Info",
          "csvSepMatchOk" to "baris cocok dengan pemisah",
          "csvSepMatchWarn" to "Perhatian: sebagian besar baris tidak memiliki pemisah",
          "csvSkipTag" to "SKIP",
          "csvNoSepTag" to "Tanpa pemisah",
          "secTelemetry" to "Grafik Kecepatan & Progres Real-Time",
          "telemetryHint" to "Memantau kecepatan (baris/dtk) dan persentase selesai file aktif",
          "legendSpeed" to "Kecepatan (baris/dtk)",
          "legendProgress" to "Progres (%)",
          "kpiCurSpeed" to "Kecepatan",
          "kpiPeakSpeed" to "Puncak",
          "kpiUnitRate" to "brs/dtk",
          "telemetryEmpty" to "Grafik real-time akan aktif saat terjemahan berjalan",
          "tabTranslator" to "Penerjemah",
          "tabHistory" to "Riwayat",
          "tabSettings" to "Pengaturan",
          "secExportSettings" to "Pengaturan Ekspor File CSV",
          "exportSettingsDesc" to "Atur pemisah kolom (delimiter) dan pengodean karakter saat menyimpan hasil terjemahan.",
          "lblExportDelimiter" to "Pemisah CSV Ekspor (Delimiter)",
          "delPipeDesc" to "Format asli game Mount & Blade: Warband (direkomendasikan untuk mod)",
          "delCommaDesc" to "Standar CSV internasional (Excel / Google Sheets)",
          "delTabDesc" to "Tab-Separated Values (.tsv / editor teks)",
          "delSemicolonDesc" to "CSV Eropa / Excel regional titik koma",
          "lblExportEncoding" to "Pengodean Karakter (Encoding)",
          "encUtf8Desc" to "Standar universal Android & Warband modern (ringan & kompatibel luas)",
          "encUtf16Desc" to "Unicode 16-bit dengan BOM (cocok untuk tools PC/Excel lama)",
          "lblExportPreview" to "Contoh Baris Ekspor",
          "secUiPref" to "Bahasa Antarmuka & Memori",
          "secHistory" to "Riwayat Sesi Terjemahan",
          "historyDesc" to "Catatan file CSV yang pernah diproses. Anda dapat membuka ulang ke antrean atau membersihkan log.",
          "btnClearHistory" to "Bersihkan Riwayat",
          "btnReopenFile" to "Buka Ulang",
          "historyEmptyTitle" to "Belum Ada Riwayat Sesi",
          "historyEmptySub" to "File CSV yang selesai atau dihentikan saat diterjemahkan akan tercatat otomatis di sini.",
          "histStatusDone" to "Selesai",
          "histStatusStopped" to "Dihentikan",
          "histStatusError" to "Gagal",
          "lblBatchRename" to "Penamaan Ulang File Output (Batch Rename)",
          "hintBatchRename" to "Tambahkan awalan (prefix) atau akhiran (suffix) otomatis sebelum ekstensi file saat menyimpan.",
          "lblFilePrefix" to "Awalan (Prefix)",
          "lblFileSuffix" to "Akhiran (Suffix)",
          "presetOriginal" to "Nama Asli",
          "presetTranslated" to "_translated",
          "presetLangSuffix" to "Kode Bahasa",
          "lblRenamePreview" to "Contoh Nama File Output",
          "btnPreviewExport" to "Pratinjau Ekspor",
          "previewExportTitle" to "Pratinjau Ekspor & Simpan",
          "previewExportDesc" to "Periksa contoh isi file hasil terjemahan dan nama file sebelum menyimpan ke perangkat.",
          "lblSampleHeader" to "Contoh Isi File Ekspor (12 Baris Pertama)",
          "lblOutputFile" to "Nama Output",
          "lblQuickRename" to "Ubah Awalan / Akhiran Output",
          "lblSaveDestination" to "Pilih Tujuan Penyimpanan",
          "lblBatchSize" to "Mode Batch Permintaan",
          "batchSize1" to "🎯 Presisi (1 baris/req) — Standar",
          "batchSize5" to "🚀 Cepat (5 baris/req) — ~3x lebih ngebut",
          "batchSize10" to "⚡ Turbo (10 baris/req) — ~5x lebih ngebut",
          "chkMedievalTone" to "Penghalus Gaya Bahasa Medieval (Post-Processing)",
          "lblGlossPresets" to "Sisip Paket Glosarium Warband:",
          "presetGlossFactions" to "+ Faksi Calradia",
          "presetGlossPolitics" to "+ Politik & Gelar",
          "presetGlossMilitary" to "+ Militer Medieval",
          "btnExpCacheJson" to "Ekspor Cache (.json)",
          "btnImpCacheJson" to "Impor Cache (.json)",
          "encUtf8BomDesc" to "UTF-8 dengan BOM (direkomendasikan Excel & mod PC tertentu)",
          "encUtf16LeDesc" to "16-bit Little Endian + BOM (standar Windows & Warband klasik)",
          "encUtf16BeDesc" to "16-bit Big Endian + BOM (Unicode BE)",
          "btnQaEditor" to "QA & Editor",
          "qaEditorTitle" to "QA Audit & Editor Penuh",
          "qaFilterAll" to "Semua Baris",
          "qaFilterIssues" to "Bermasalah QA",
          "qaSearchPlaceholder" to "Cari ID atau teks terjemahan...",
          "qaFindLabel" to "Cari",
          "qaReplaceLabel" to "Ganti dengan",
          "qaBtnReplaceAll" to "Ganti Semua",
          "qaBtnAutoFix" to "Perbaiki Token Hilang",
          "qaCaseSensitive" to "Peka Huruf",
          "qaNoIssuesBanner" to "Mantap! Semua token Warband & pemisah lolos audit QA.",
          "qaIssuesFoundBanner" to "baris ditemukan memiliki potensi masalah QA",
          "btnModDiff" to "Gabung Update Mod (Diff)",
          "modDiffTitle" to "Gabung Update Mod (Diff Merge)",
          "modDiffDesc" to "Bawa terjemahan lama Anda ke versi mod terbaru: ID yang sama disalin otomatis ke cache sehingga hanya baris baru yang diterjemahkan.",
          "modDiffPickNew" to "1. Pilih CSV Mod Versi Baru",
          "modDiffPickOld" to "2. Pilih CSV Terjemahan Lama Anda",
          "modDiffSummary" to "Hasil Analisis Diff",
          "modDiffReused" to "Dipertahankan (Auto-Cache)",
          "modDiffNew" to "Baru / Perlu Diterjemahkan",
          "modDiffSkipped" to "Dilewati",
          "modDiffApply" to "Masukkan ke Antrean",
          "secSummary" to "Dasbor Ringkasan Sesi & Produktivitas",
          "summarySub" to "Laporan lengkap baris asli vs terjemahan, galat, durasi sesi, dan statistik harian.",
          "summaryTransVsOrig" to "Baris Diterjemahkan / Asli",
          "summaryErrors" to "Error Ditemukan",
          "summaryDuration" to "Total Waktu Proses",
          "summaryWords" to "Kata Diproses",
          "secProductivity" to "Grafik Produktivitas Terjemahan",
          "prodWordsPerDay" to "Kata Diproses per Hari (7 Hari Terakhir)",
          "prodPassVsFail" to "File Sukses vs Gagal/Berhenti",
          "prodSucceeded" to "Sukses",
          "prodFailed" to "Gagal/Berhenti",
          "secErrorLog" to "Log Error Terjemahan & Coba Ulang",
          "errLogHint" to "Periksa baris gagal atau format CSV tidak valid, lalu coba ulang baris tertentu tanpa mengulang batch.",
          "btnRetryRow" to "Coba Ulang",
          "btnRetryAllErrors" to "Coba Ulang Semua Baris",
          "btnInspectError" to "Periksa",
          "errBadgeFormat" to "FORMAT CSV",
          "errBadgeRow" to "BARIS GAGAL",
          "errRetrying" to "Mencoba ulang...",
          "historySearchPlaceholder" to "Cari nama file, status, atau bahasa...",
          "histFilterAll" to "Semua",
          "histFilterDone" to "Selesai",
          "histFilterStopped" to "Dihentikan",
          "histFilterError" to "Gagal",
          "lblMedievalTheme" to "Tema Medieval (Perkamen & Gotik)",
          "lblMedievalThemeSub" to "Terapkan gradasi perkamen gelap hangat serta tipografi bergaya gotik Mount & Blade di seluruh UI.",
          "csvValTitle" to "Peringatan Validasi Struktur CSV",
          "csvValDesc" to "Pemeriksaan latar belakang menemukan kejanggalan struktur kolom/pemisah sebelum menerjemahkan:",
          "csvValOk" to "Struktur CSV Valid",
          "csvValWarn" to "Peringatan Struktur",
          "csvValMalformed" to "Format CSV Bermasalah",
          "btnProceedAnyway" to "Tetap Lanjutkan",
          "btnFixSeparator" to "Periksa Pemisah",
          "btnAddCustomLang" to "+ Bahasa Kustom",
          "addLangTitle" to "Tambah Bahasa Tujuan (Room DB)",
          "addLangCode" to "Kode Bahasa (misal: de, fr, ja)",
          "addLangLabel" to "Nama Tampilan (misal: Deutsch (de))",
          "qaIssueColMismatch" to "Jumlah Kolom Tidak Cocok",
          "qaIssueQuotes" to "Tanda Kutip Tidak Seimbang",
          "btnResumeProcess" to "Lanjutkan",
          "btnRetryNow" to "Coba Lagi",
          "rateLimitPausedTitle" to "Limit Permintaan Server (HTTP 429)",
          "rateLimitPausedDetail" to "Progres sudah disimpan ke checkpoint agar baris yang selesai tidak hilang. Tunggu beberapa detik, lalu ketuk Lanjutkan atau Coba Lagi."
      ),
      UiLanguage.EN to mapOf(
          "navTitle" to "M&B Warband Translator",
          "navSub" to "Free · No API Key · Auto-Resume",
          "heroBadge" to "Supports PC & Android Mods (Dynasties of Calradia, Diplomacy, etc.)",
          "heroTitle" to "Warband CSV Translator",
          "heroDesc" to "Batch translate thousands of lines from dialogs.csv, game_strings.csv, and troops.csv right on Android without paid API keys.",
          "secEngine" to "1. Free Translation Engine & Speed",
          "statusReady" to "Ready to use",
          "engGoogleSub" to "Fastest · Direct",
          "engLingvaSub" to "Google Mirror · Stable",
          "engMmSub" to "~1000 lines/day · Backup",
          "lblSpeed" to "Request Delay (Anti-HTTP 429 Rate Limit)",
          "spdFast" to "⚡ Fast (80 ms) — Best for small files",
          "spdNormal" to "⚖️ Normal (200 ms) — Recommended",
          "spdSafe" to "🛡️ Safe (500 ms) — Anti-ban for thousands of lines",
          "lblCache" to "Cache Memory (Auto-Resume)",
          "cacheUnit" to "lines",
          "chkFallback" to "Auto-Fallback to other engines on error",
          "btnClearCache" to "Clear Cache",
          "secFormat" to "2. Language & Warband Text Structure",
          "lblSrcLang" to "Source Language (Original Mod)",
          "optAuto" to "Auto-Detect",
          "lblTgtLang" to "Target Language (Output)",
          "lblSep" to "Code Separator",
          "hintSep" to "Left-side ID (str_...) is never translated.",
          "lblStrip" to "Strip Tag (Optional)",
          "hintStrip" to "Leave empty to preserve {***} tags.",
          "lblRegex" to "Skip Lines (Regex)",
          "hintRegex" to "Number-only lines are skipped automatically.",
          "regexValid" to "✓ Valid regex pattern",
          "regexInvalid" to "✕ Invalid regex pattern",
          "varProtDesc" to "Variable Guard Active: Tokens like {s0}, {reg1}, {playername}, {lord}, ^^, and ^ are locked safely.",
          "secGloss" to "3. Custom Glossary (Optional)",
          "glossHint" to "Empty by default so you can add your own terms freely. One term per line: without '=' (e.g., Swadia) to keep untranslated, or with '=' (e.g., Vassal=Vasali) for custom translation.",
          "glossPlaceholder" to "Example (1 term per line):\nSwadia\nCalradia\nVassal=Vasali\nFief=Tanah Wilayah",
          "btnExpGloss" to "Export (.txt)",
          "btnImpGloss" to "Import (.txt/.csv/.json)",
          "btnClearGloss" to "Clear",
          "secQueue" to "4. Upload CSV / TXT / TSV Files",
          "queueHint" to "Select multiple files at once",
          "dropTitle" to "Tap here to pick Warband module files",
          "dropSub" to "Example: dialogs.csv, game_strings.csv, quick_strings.csv, troops.csv, factions.csv",
          "btnClearQueue" to "Clear Queue",
          "btnStart" to "Start Translating",
          "btnPause" to "Pause",
          "btnResume" to "Resume",
          "btnStop" to "Stop",
          "btnDlAll" to "Save All Completed Files",
          "secPreview" to "Live Preview & Quick Edit (Last 5 Lines)",
          "previewHint" to "Tap Edit to manually tweak any line before saving",
          "secLog" to "System Activity Log",
          "btnClearLog" to "Clear Log",
          "secGuide" to "How to Install Translated Files on Warband Android",
          "gStep1" to "Save the translated CSV files (e.g., dialogs.csv, game_strings.csv, etc.).",
          "gStep2" to "Open ZArchiver and navigate to: /Internal Storage/mnb/Modules/Dynasties_of_Calradia/languages/.",
          "gStep3" to "Create a folder named id (or copy the contents of en folder into id).",
          "gStep4" to "Paste and overwrite the translated CSV files inside languages/id/ (or save directly into that folder).",
          "gStep5" to "Open MnBTool or in-game language settings and select Indonesian (id).",
          "footerCredit" to "Inspired by Warband CSV Translator (Abdullah Zubeyr Akyüz) · Enhanced for Mount & Blade Modders",
          "stWait" to "Waiting",
          "stRun" to "Translating...",
          "stDone" to "Done",
          "stErr" to "Error",
          "btnDlFile" to "Save",
          "btnCloseHelp" to "Got it",
          "glossBadgeUnit" to "terms",
          "btnEdit" to "Edit",
          "btnSave" to "Save",
          "btnCancel" to "Cancel",
          "saveDialogTitle" to "Choose Save Destination",
          "saveDialogDesc" to "Choose whether to save directly to Downloads or pick a specific folder (such as your mod's languages/id/ directory).",
          "saveToDownloads" to "Save to Downloads Folder",
          "saveToDownloadsSub" to "Saved into Download/WarbandTranslator",
          "saveToFolder" to "Choose Target Folder (SAF)",
          "saveToFolderSub" to "For example directly into your mod's languages/id/ folder",
          "lblErrExport" to "Export Error Log",
          "btnPickSingle" to "Add 1 File",
          "autoResumeReady" to "Auto-Resume",
          "btnInspectCsv" to "Preview 10 Rows",
          "secCsvStruct" to "CSV Structure Preview (First 10 Rows)",
          "csvStructHint" to "Verify ID and text column separation before starting translation",
          "csvColIdx" to "#",
          "csvColKey" to "Column 1 (ID / Key)",
          "csvColText" to "Column 2 (Source Text)",
          "csvColStatus" to "Info",
          "csvSepMatchOk" to "rows match separator",
          "csvSepMatchWarn" to "Warning: most rows do not contain separator",
          "csvSkipTag" to "SKIP",
          "csvNoSepTag" to "No separator",
          "secTelemetry" to "Real-Time Speed & Progress Graph",
          "telemetryHint" to "Live lines/sec throughput and completion percentage of the active CSV file",
          "legendSpeed" to "Speed (lines/s)",
          "legendProgress" to "Progress (%)",
          "kpiCurSpeed" to "Current Speed",
          "kpiPeakSpeed" to "Peak Speed",
          "kpiUnitRate" to "lines/s",
          "telemetryEmpty" to "Real-time graph activates once translation begins",
          "tabTranslator" to "Translator",
          "tabHistory" to "History",
          "tabSettings" to "Settings",
          "secExportSettings" to "CSV Export Settings",
          "exportSettingsDesc" to "Choose the column delimiter and character encoding applied when exporting translated files.",
          "lblExportDelimiter" to "Export CSV Delimiter",
          "delPipeDesc" to "Original Mount & Blade: Warband format (recommended for mods)",
          "delCommaDesc" to "Standard CSV format (Excel / Google Sheets)",
          "delTabDesc" to "Tab-Separated Values (.tsv / spreadsheets)",
          "delSemicolonDesc" to "Semicolon CSV (European Excel locale)",
          "lblExportEncoding" to "Character Encoding",
          "encUtf8Desc" to "Universal modern Android & Warband encoding (compact & widely supported)",
          "encUtf16Desc" to "16-bit Unicode with BOM (for legacy desktop spreadsheets/tools)",
          "lblExportPreview" to "Export Output Sample",
          "secUiPref" to "Interface Language & Cache",
          "secHistory" to "Translation Session History",
          "historyDesc" to "Metadata log of previously processed CSV files. Re-open any session into the queue or clear logs.",
          "btnClearHistory" to "Clear History",
          "btnReopenFile" to "Re-open",
          "historyEmptyTitle" to "No Session History Yet",
          "historyEmptySub" to "Processed or stopped CSV translation sessions will automatically appear here.",
          "histStatusDone" to "Completed",
          "histStatusStopped" to "Stopped",
          "histStatusError" to "Error",
          "lblBatchRename" to "Batch Rename Output Files",
          "hintBatchRename" to "Automatically add a prefix or suffix before the file extension when exporting files in the queue.",
          "lblFilePrefix" to "Prefix",
          "lblFileSuffix" to "Suffix",
          "presetOriginal" to "Original",
          "presetTranslated" to "_translated",
          "presetLangSuffix" to "Language Suffix",
          "lblRenamePreview" to "Output Filename Preview",
          "btnPreviewExport" to "Preview Export",
          "previewExportTitle" to "Preview Export & Save",
          "previewExportDesc" to "Review a sample of your formatted Warband mod export and batch-renamed filenames before saving.",
          "lblSampleHeader" to "Translated Output Sample (First 12 Rows)",
          "lblOutputFile" to "Output File",
          "lblQuickRename" to "Quick Batch Rename",
          "lblSaveDestination" to "Choose Save Destination",
          "lblBatchSize" to "Batch Request Mode",
          "batchSize1" to "🎯 Precision (1 line/req) — Standard",
          "batchSize5" to "🚀 Fast (5 lines/req) — ~3x faster",
          "batchSize10" to "⚡ Turbo (10 lines/req) — ~5x faster",
          "chkMedievalTone" to "Medieval Tone Post-Processing",
          "lblGlossPresets" to "Insert Warband Glossary Presets:",
          "presetGlossFactions" to "+ Calradia Factions",
          "presetGlossPolitics" to "+ Politics & Titles",
          "presetGlossMilitary" to "+ Medieval Military",
          "btnExpCacheJson" to "Export Cache (.json)",
          "btnImpCacheJson" to "Import Cache (.json)",
          "encUtf8BomDesc" to "UTF-8 with BOM (recommended for Excel & older PC tools)",
          "encUtf16LeDesc" to "16-bit Little Endian + BOM (Windows & classic Warband)",
          "encUtf16BeDesc" to "16-bit Big Endian + BOM (Unicode BE)",
          "btnQaEditor" to "QA & Editor",
          "qaEditorTitle" to "QA Audit & Full Editor",
          "qaFilterAll" to "All Rows",
          "qaFilterIssues" to "QA Issues",
          "qaSearchPlaceholder" to "Search ID or translated text...",
          "qaFindLabel" to "Find",
          "qaReplaceLabel" to "Replace with",
          "qaBtnReplaceAll" to "Replace All",
          "qaBtnAutoFix" to "Auto-Fix Missing Tokens",
          "qaCaseSensitive" to "Match Case",
          "qaNoIssuesBanner" to "All Warband tokens and separators passed QA audit!",
          "qaIssuesFoundBanner" to "rows flagged with potential QA issues",
          "btnModDiff" to "Merge Mod Update (Diff)",
          "modDiffTitle" to "Mod Update Diff Merge",
          "modDiffDesc" to "Carry forward your old translations into a newer mod CSV: matching IDs are pre-cached so only new or changed lines get translated.",
          "modDiffPickNew" to "1. Pick New Mod Version CSV",
          "modDiffPickOld" to "2. Pick Your Old Translated CSV",
          "modDiffSummary" to "Diff Analysis Summary",
          "modDiffReused" to "Carried Over (Auto-Cached)",
          "modDiffNew" to "New / Needs Translation",
          "modDiffSkipped" to "Skipped",
          "modDiffApply" to "Load Merged File into Queue",
          "secSummary" to "Session Summary & Productivity Dashboard",
          "summarySub" to "Report of translated vs original lines, encountered errors, total duration, and daily productivity.",
          "summaryTransVsOrig" to "Translated / Original Lines",
          "summaryErrors" to "Errors Encountered",
          "summaryDuration" to "Total Processing Time",
          "summaryWords" to "Words Processed",
          "secProductivity" to "Translation Productivity Chart",
          "prodWordsPerDay" to "Words Processed per Day (Last 7 Days)",
          "prodPassVsFail" to "Successful vs Failed/Stopped Files",
          "prodSucceeded" to "Succeeded",
          "prodFailed" to "Failed/Stopped",
          "secErrorLog" to "Translation Error Log & Manual Retry",
          "errLogHint" to "Inspect failed rows or malformed CSV alerts and manually retry single rows without restarting the batch.",
          "btnRetryRow" to "Retry Row",
          "btnRetryAllErrors" to "Retry All Rows",
          "btnInspectError" to "Inspect",
          "errBadgeFormat" to "CSV FORMAT",
          "errBadgeRow" to "ROW ERROR",
          "errRetrying" to "Retrying...",
          "historySearchPlaceholder" to "Search filename, status, or language...",
          "histFilterAll" to "All",
          "histFilterDone" to "Done",
          "histFilterStopped" to "Stopped",
          "histFilterError" to "Error",
          "lblMedievalTheme" to "Medieval Theme (Parchment & Gothic)",
          "lblMedievalThemeSub" to "Apply warm dark parchment gradients and gothic-inspired Mount & Blade typography across all UI components.",
          "csvValTitle" to "CSV Structure Validation Warning",
          "csvValDesc" to "Background validation detected column alignment or delimiter issues before starting translation:",
          "csvValOk" to "CSV Structure Valid",
          "csvValWarn" to "Structure Warning",
          "csvValMalformed" to "Malformed CSV Format",
          "btnProceedAnyway" to "Proceed Anyway",
          "btnFixSeparator" to "Review Delimiter",
          "btnAddCustomLang" to "+ Custom Language",
          "addLangTitle" to "Add Target Language (Room DB)",
          "addLangCode" to "Language Code (e.g., de, fr, ja)",
          "addLangLabel" to "Display Name (e.g., Deutsch (de))",
          "qaIssueColMismatch" to "Column Count Mismatch",
          "qaIssueQuotes" to "Unbalanced Quotes",
          "btnResumeProcess" to "Resume",
          "btnRetryNow" to "Retry",
          "rateLimitPausedTitle" to "Server Rate Limit (HTTP 429)",
          "rateLimitPausedDetail" to "Progress is safely checkpointed so completed rows are preserved. Wait a few seconds, then tap Resume or Retry."
      ),
      UiLanguage.TR to mapOf(
          "navTitle" to "M&B Warband Çeviri Aracı",
          "navSub" to "Ücretsiz · API Anahtarsız · Otomatik Devam",
          "heroBadge" to "PC ve Android Modlarını Destekler (Dynasties of Calradia, Diplomacy vb.)",
          "heroTitle" to "Warband CSV Çeviri Aracı",
          "heroDesc" to "dialogs.csv, game_strings.csv ve troops.csv dosyalarındaki binlerce satırı API anahtarı olmadan Android cihazınızda topluca çevirin.",
          "secEngine" to "1. Ücretsiz Çeviri Motoru ve Hız",
          "statusReady" to "Kullanıma hazır",
          "engGoogleSub" to "En Hızlı · Doğrudan",
          "engLingvaSub" to "Google Aynası · Kararlı",
          "engMmSub" to "~1000 satır/gün · Yedek",
          "lblSpeed" to "İstek Gecikmesi (HTTP 429 Koruması)",
          "spdFast" to "⚡ Hızlı (80 ms) — Küçük dosyalar için",
          "spdNormal" to "⚖️ Normal (200 ms) — Önerilen",
          "spdSafe" to "🛡️ Güvenli (500 ms) — Binlerce satır için güvenli",
          "lblCache" to "Önbellek (Otomatik Devam)",
          "cacheUnit" to "satır",
          "chkFallback" to "Hata durumunda otomatik yedek motora geç",
          "btnClearCache" to "Önbelleği Temizle",
          "secFormat" to "2. Dil ve Warband Metin Biçimi",
          "lblSrcLang" to "Kaynak Dil (Orijinal Mod)",
          "optAuto" to "Otomatik Algıla",
          "lblTgtLang" to "Hedef Dil (Çıktı)",
          "lblSep" to "Kod Ayracı (Separator)",
          "hintSep" to "Soldaki kod (str_...) çevrilmez.",
          "lblStrip" to "Silinecek Etiket (İsteğe Bağlı)",
          "hintStrip" to "{***} etiketini korumak için boş bırakın.",
          "lblRegex" to "Satır Atla (Regex)",
          "hintRegex" to "Sadece rakam içeren satırlar atlanır.",
          "regexValid" to "✓ Geçerli regex deseni",
          "regexInvalid" to "✕ Geçersiz regex deseni",
          "varProtDesc" to "Değişken Koruması Aktif: {s0}, {reg1}, {playername}, {lord}, ^^ ve ^ belirteçleri bozulmaya karşı kilitlenir.",
          "secGloss" to "3. Özel Sözlük (İsteğe Bağlı)",
          "glossHint" to "Kendi terimlerinizi serbestçe girebilmeniz için varsayılan olarak boştur. Her satıra bir terim yazın: '=' olmadan (ör. Swadia) çevrilmez, '=' ile (ör. Vassal=Vasali) özel çeviri uygulanır.",
          "glossPlaceholder" to "Örnek (her satıra 1 terim):\nSwadia\nCalradia\nVassal=Vasali\nFief=Tımar",
          "btnExpGloss" to "Dışa Aktar (.txt)",
          "btnImpGloss" to "İçe Aktar (.txt/.csv/.json)",
          "btnClearGloss" to "Temizle",
          "secQueue" to "4. CSV / TXT / TSV Dosyalarını Yükle",
          "queueHint" to "Aynı anda birden fazla dosya seçilebilir",
          "dropTitle" to "Warband mod dosyalarını seçmek için dokunun",
          "dropSub" to "Örnek: dialogs.csv, game_strings.csv, quick_strings.csv, troops.csv, factions.csv",
          "btnClearQueue" to "Kuyruğu Temizle",
          "btnStart" to "Çeviriyi Başlat",
          "btnPause" to "Duraklat",
          "btnResume" to "Devam Et",
          "btnStop" to "Durdur",
          "btnDlAll" to "Tamamlanan Tüm Dosyaları Kaydet",
          "secPreview" to "Canlı Önizleme ve Hızlı Düzenleme (Son 5 Satır)",
          "previewHint" to "Kaydetmeden önce manuel düzenlemek için Düzenle düğmesine dokunun",
          "secLog" to "Sistem Etkinlik Günlüğü",
          "btnClearLog" to "Günlüğü Temizle",
          "secGuide" to "Çevrilen Dosyaları Warband Android Moduna Kurma",
          "gStep1" to "Çevirisi tamamlanan CSV dosyalarını kaydedin (ör. dialogs.csv, game_strings.csv).",
          "gStep2" to "ZArchiver uygulamasını açın ve /Dahili Depolama/mnb/Modules/Dynasties_of_Calradia/languages/ dizinine gidin.",
          "gStep3" to "id adında yeni bir klasör oluşturun (veya en klasörünün içeriğini id içine kopyalayın).",
          "gStep4" to "Çevrilen CSV dosyalarını languages/id/ klasörüne yapıştırıp üzerine yazın.",
          "gStep5" to "MnBTool veya oyun içi dil ayarlarından hedef dilinizi seçin.",
          "footerCredit" to "Warband CSV Translator (Abdullah Zubeyr Akyüz) temel alınarak geliştirilmiştir",
          "stWait" to "Bekliyor",
          "stRun" to "Çevriliyor...",
          "stDone" to "Tamamlandı",
          "stErr" to "Hata",
          "btnDlFile" to "Kaydet",
          "btnCloseHelp" to "Anladım",
          "glossBadgeUnit" to "terim",
          "btnEdit" to "Düzenle",
          "btnSave" to "Kaydet",
          "btnCancel" to "İptal",
          "saveDialogTitle" to "Kayıt Konumu Seçin",
          "saveDialogDesc" to "Doğrudan İndirilenler klasörüne mi yoksa seçtiğiniz özel bir klasöre mi kaydetmek istersiniz?",
          "saveToDownloads" to "İndirilenler (Downloads) Klasörüne Kaydet",
          "saveToDownloadsSub" to "Download/WarbandTranslator içine kaydedilir",
          "saveToFolder" to "Hedef Klasör Seç (SAF)",
          "saveToFolderSub" to "Örneğin doğrudan modunuzun languages/id/ klasörü",
          "lblErrExport" to "Hata Günlüğünü Dışa Aktar",
          "btnPickSingle" to "1 Dosya Ekle",
          "autoResumeReady" to "Oto-Devam",
          "btnInspectCsv" to "10 Satır Önizle",
          "secCsvStruct" to "CSV Yapı Önizleyici (İlk 10 Satır)",
          "csvStructHint" to "Çeviriye başlamadan önce kod ve metin sütunlarını doğrulayın",
          "csvColIdx" to "#",
          "csvColKey" to "Sütun 1 (ID / Kod)",
          "csvColText" to "Sütun 2 (Kaynak Metin)",
          "csvColStatus" to "Bilgi",
          "csvSepMatchOk" to "satır ayraç ile eşleşiyor",
          "csvSepMatchWarn" to "Uyarı: satırların çoğunda ayraç bulunamadı",
          "csvSkipTag" to "ATLA",
          "csvNoSepTag" to "Ayraç yok",
          "secTelemetry" to "Gerçek Zamanlı Hız ve İlerleme Grafiği",
          "telemetryHint" to "Aktif CSV dosyasının çeviri hızını (satır/sn) ve ilerleme yüzdesini gösterir",
          "legendSpeed" to "Hız (satır/sn)",
          "legendProgress" to "İlerleme (%)",
          "kpiCurSpeed" to "Anlık Hız",
          "kpiPeakSpeed" to "En Yüksek",
          "kpiUnitRate" to "satır/sn",
          "telemetryEmpty" to "Çeviri başladığında gerçek zamanlı grafik etkinleşir",
          "tabTranslator" to "Çevirmen",
          "tabHistory" to "Geçmiş",
          "tabSettings" to "Ayarlar",
          "secExportSettings" to "CSV Dışa Aktarma Ayarları",
          "exportSettingsDesc" to "Çevrilen dosyalar kaydedilirken kullanılacak ayırıcıyı ve karakter kodlamasını seçin.",
          "lblExportDelimiter" to "Dışa Aktarma CSV Ayracı",
          "delPipeDesc" to "Orijinal Mount & Blade: Warband biçimi (modlar için önerilir)",
          "delCommaDesc" to "Standart CSV biçimi (Virgül)",
          "delTabDesc" to "Sekme ile ayrılmış değerler (Tab)",
          "delSemicolonDesc" to "Noktalı virgül CSV biçimi",
          "lblExportEncoding" to "Karakter Kodlaması",
          "encUtf8Desc" to "Evrensel modern Android ve Warband kodlaması",
          "encUtf16Desc" to "BOM içeren 16-bit Unicode kodlaması",
          "lblExportPreview" to "Dışa Aktarım Örneği",
          "secUiPref" to "Arayüz Dili ve Önbellek",
          "secHistory" to "Çeviri Oturum Geçmişi",
          "historyDesc" to "Daha önce işlenen CSV dosyalarının meta verileri. Yeniden açabilir veya geçmişi temizleyebilirsiniz.",
          "btnClearHistory" to "Geçmişi Temizle",
          "btnReopenFile" to "Yeniden Aç",
          "historyEmptyTitle" to "Henüz Oturum Geçmişi Yok",
          "historyEmptySub" to "Tamamlanan veya durdurulan CSV çeviri oturumları burada listelenir.",
          "histStatusDone" to "Tamamlandı",
          "histStatusStopped" to "Durduruldu",
          "histStatusError" to "Hata",
          "lblBatchRename" to "Toplu Çıktı Dosyası Yeniden Adlandırma",
          "hintBatchRename" to "Kaydederken dosya uzantısından önce otomatik olarak ön ek veya son ek ekleyin.",
          "lblFilePrefix" to "Ön Ek (Prefix)",
          "lblFileSuffix" to "Son Ek (Suffix)",
          "presetOriginal" to "Orijinal Ad",
          "presetTranslated" to "_translated",
          "presetLangSuffix" to "Dil Son Eki",
          "lblRenamePreview" to "Çıktı Dosyası Önizlemesi",
          "btnPreviewExport" to "Dışa Aktarımı Önizle",
          "previewExportTitle" to "Dışa Aktarım Önizleme ve Kayıt",
          "previewExportDesc" to "Kaydetmeden önce biçimlendirilmiş çıktı örneğini ve dosya adlarını kontrol edin.",
          "lblSampleHeader" to "Çevrilmiş Çıktı Örneği (İlk 12 Satır)",
          "lblOutputFile" to "Çıktı Dosyası",
          "lblQuickRename" to "Hızlı Toplu Yeniden Adlandırma",
          "lblSaveDestination" to "Kayıt Konumunu Seçin",
          "lblBatchSize" to "Toplu İstek Modu",
          "batchSize1" to "🎯 Hassas (1 satır/istek) — Standart",
          "batchSize5" to "🚀 Hızlı (5 satır/istek) — ~3 kat hızlı",
          "batchSize10" to "⚡ Turbo (10 satır/istek) — ~5 kat hızlı",
          "chkMedievalTone" to "Ortaçağ Üslubu İyileştirmesi (Son İşleme)",
          "lblGlossPresets" to "Hazır Warband Sözlük Paketleri:",
          "presetGlossFactions" to "+ Calradia Krallıkları",
          "presetGlossPolitics" to "+ Siyaset ve Unvanlar",
          "presetGlossMilitary" to "+ Ortaçağ Askeriye",
          "btnExpCacheJson" to "Önbelleği Dışa Aktar (.json)",
          "btnImpCacheJson" to "Önbellek İçe Aktar (.json)",
          "encUtf8BomDesc" to "BOM içeren UTF-8 (Excel ve masaüstü araçları için)",
          "encUtf16LeDesc" to "16-bit Little Endian + BOM (Windows / Klasik Warband)",
          "encUtf16BeDesc" to "16-bit Big Endian + BOM (Unicode BE)",
          "btnQaEditor" to "QA ve Editör",
          "qaEditorTitle" to "QA Denetimi ve Tam Editör",
          "qaFilterAll" to "Tüm Satırlar",
          "qaFilterIssues" to "QA Sorunlu",
          "qaSearchPlaceholder" to "ID veya çevrilmiş metin ara...",
          "qaFindLabel" to "Bul",
          "qaReplaceLabel" to "Değiştir",
          "qaBtnReplaceAll" to "Tümünü Değiştir",
          "qaBtnAutoFix" to "Eksik Belirteçleri Onar",
          "qaCaseSensitive" to "Büyük/Küçük Harf",
          "qaNoIssuesBanner" to "Tüm Warband belirteçleri ve ayraçlar QA kontrolünden geçti!",
          "qaIssuesFoundBanner" to "satırda olası QA sorunu tespit edildi",
          "btnModDiff" to "Mod Güncellemesi Birleştir (Diff)",
          "modDiffTitle" to "Mod Güncellemesi Fark Birleştirme",
          "modDiffDesc" to "Eski çevirinizi yeni mod sürümüne aktarın: eşleşen kimlikler önbelleğe alınır ve yalnızca yeni satırlar çevrilir.",
          "modDiffPickNew" to "1. Yeni Mod CSV Dosyasını Seçin",
          "modDiffPickOld" to "2. Eski Çevrilmiş CSV Dosyasını Seçin",
          "modDiffSummary" to "Fark Analiz Özeti",
          "modDiffReused" to "Korunan (Oto-Önbellek)",
          "modDiffNew" to "Yeni / Çevrilecek",
          "modDiffSkipped" to "Atlanan",
          "modDiffApply" to "Birleştirilmiş Dosyayı Kuyruğa Ekle",
          "secSummary" to "Oturum Özeti ve Verimlilik Panosu",
          "summarySub" to "Çevrilen/orijinal satır raporu, hatalar, toplam süre ve günlük verimlilik grafiği.",
          "summaryTransVsOrig" to "Çevrilen / Orijinal Satır",
          "summaryErrors" to "Karşılaşılan Hatalar",
          "summaryDuration" to "Toplam İşlem Süresi",
          "summaryWords" to "İşlenen Kelime",
          "secProductivity" to "Çeviri Verimlilik Grafiği",
          "prodWordsPerDay" to "Günlük İşlenen Kelime (Son 7 Gün)",
          "prodPassVsFail" to "Başarılı vs Hatalı/Durdurulan Dosyalar",
          "prodSucceeded" to "Başarılı",
          "prodFailed" to "Hatalı/Durduruldu",
          "secErrorLog" to "Çeviri Hata Günlüğü ve Yeniden Deneme",
          "errLogHint" to "Hatalı satırları veya biçim sorunlarını inceleyin ve tüm toplu işi başlatmadan tek tek yeniden deneyin.",
          "btnRetryRow" to "Tekrar Dene",
          "btnRetryAllErrors" to "Tüm Satırları Dene",
          "btnInspectError" to "İncele",
          "errBadgeFormat" to "CSV BİÇİMİ",
          "errBadgeRow" to "SATIR HATASI",
          "errRetrying" to "Yeniden deneniyor...",
          "historySearchPlaceholder" to "Dosya adı, durum veya dil ara...",
          "histFilterAll" to "Tümü",
          "histFilterDone" to "Tamamlandı",
          "histFilterStopped" to "Durduruldu",
          "histFilterError" to "Hata",
          "lblMedievalTheme" to "Ortaçağ Teması (Parşömen ve Gotik)",
          "lblMedievalThemeSub" to "Tüm arayüz bileşenlerine koyu parşömen gradyanları ve gotik Mount & Blade yazı tipleri uygular.",
          "csvValTitle" to "CSV Yapı Doğrulama Uyarısı",
          "csvValDesc" to "Arka plan doğrulaması çeviri başlamadan önce sütun hizalama veya ayraç sorunları tespit etti:",
          "csvValOk" to "CSV Yapısı Geçerli",
          "csvValWarn" to "Yapı Uyarısı",
          "csvValMalformed" to "Bozuk CSV Biçimi",
          "btnProceedAnyway" to "Yine de Devam Et",
          "btnFixSeparator" to "Ayracı Kontrol Et",
          "btnAddCustomLang" to "+ Özel Dil",
          "addLangTitle" to "Hedef Dil Ekle (Room DB)",
          "addLangCode" to "Dil Kodu (ör. de, fr, ja)",
          "addLangLabel" to "Görünen Ad (ör. Deutsch (de))",
          "qaIssueColMismatch" to "Sütun Sayısı Uyuşmuyor",
          "qaIssueQuotes" to "Eşleşmeyen Tırnak",
          "btnResumeProcess" to "Devam Et",
          "btnRetryNow" to "Tekrar Dene",
          "rateLimitPausedTitle" to "Sunucu Hız Sınırı (HTTP 429)",
          "rateLimitPausedDetail" to "İlerleme kontrol noktasına kaydedildi, tamamlanan satırlar korunur. Birkaç saniye bekleyip Devam Et veya Tekrar Dene'ye dokunun."
      )
  )

  private val helpTopics: Map<String, Map<UiLanguage, HelpTopicContent>> = mapOf(
      "uiLang" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Bahasa Antarmuka (UI)",
              listOf(
                  "Mengubah bahasa tampilan menu, tombol, dan panduan pada aplikasi ini antara Bahasa Indonesia (ID), English (EN), dan Türkçe (TR).",
                  "Pengaturan ini tidak memengaruhi bahasa terjemahan file CSV mod Anda."
              )
          ),
          UiLanguage.EN to HelpTopicContent(
              "Interface Language (UI)",
              listOf(
                  "Switches the display language of menus, buttons, and guides between Indonesian (ID), English (EN), and Turkish (TR).",
                  "This does not change the target translation language of your mod CSV files."
              )
          ),
          UiLanguage.TR to HelpTopicContent(
              "Arayüz Dili (UI)",
              listOf("Menülerin, düğmelerin ve kılavuzların görünüm dilini Endonezce (ID), İngilizce (EN) ve Türkçe (TR) arasında değiştirir.")
          )
      ),
      "secEngine" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Mesin Terjemahan Gratis & Kecepatan",
              listOf(
                  "Menentukan layanan penerjemah daring gratis yang digunakan tanpa memerlukan API Key berbayar.",
                  "Pilih Google Translate untuk kecepatan tertinggi, atau gunakan Lingva / MyMemory sebagai alternatif saat salah satu sedang sibuk."
              )
          ),
          UiLanguage.EN to HelpTopicContent(
              "Free Translation Engine & Speed",
              listOf(
                  "Selects which free online translation service is used without requiring a paid API key.",
                  "Choose Google Translate for the fastest speed, or switch to Lingva / MyMemory if an endpoint becomes temporarily rate-limited."
              )
          ),
          UiLanguage.TR to HelpTopicContent(
              "Ücretsiz Çeviri Motoru ve Hız",
              listOf("Ücretli API anahtarı gerektirmeden kullanılacak ücretsiz çeviri servisini seçmenizi sağlar.")
          )
      ),
      "engGoogle" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Google Translate (Koneksi Langsung)",
              listOf(
                  "Menggunakan endpoint Google Translate secara langsung dari Android untuk kalimat dialog panjang maupun istilah game dengan akurasi tinggi."
              )
          ),
          UiLanguage.EN to HelpTopicContent(
              "Google Translate (Direct)",
              listOf("Uses Google Translate endpoints directly over HTTPS for fast, accurate translations of long dialogues and game strings.")
          ),
          UiLanguage.TR to HelpTopicContent(
              "Google Translate (Doğrudan)",
              listOf("Uzun diyaloglar ve oyun metinleri için hızlı ve doğru çeviri sağlar.")
          )
      ),
      "engLingva" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Lingva Mirror",
              listOf(
                  "Antarmuka alternatif sumber terbuka yang mengambil hasil terjemahan Google tanpa pelacakan.",
                  "Sangat berguna sebagai mesin cadangan apabila endpoint utama Google sedang terkena limit sementara."
              )
          ),
          UiLanguage.EN to HelpTopicContent(
              "Lingva Mirror",
              listOf("An open-source alternative front-end for Google Translate. Ideal as a reliable backup engine when direct requests hit temporary limits.")
          ),
          UiLanguage.TR to HelpTopicContent(
              "Lingva Mirror",
              listOf("Google Çeviri için açık kaynaklı alternatif bir arayüzdür. Ana bağlantı sınırlandığında yedek motor olarak idealdir.")
          )
      ),
      "engMyMemory" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "MyMemory API",
              listOf(
                  "Layanan memori terjemahan kolaboratif gratis dengan batas wajar sekitar 1.000 baris per hari per alamat IP.",
                  "Cocok untuk file berukuran kecil atau sebagai mesin cadangan terakhir saat Auto-Fallback aktif."
              )
          ),
          UiLanguage.EN to HelpTopicContent(
              "MyMemory API",
              listOf("Collaborative translation memory API offering roughly 1,000 lines/day free per IP. Great for small files or as a secondary fallback.")
          ),
          UiLanguage.TR to HelpTopicContent(
              "MyMemory API",
              listOf("IP başına günlük yaklaşık 1.000 satır ücretsiz çeviri sunan yedek çeviri belleği servisidir.")
          )
      ),
      "lblSpeed" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Jeda Permintaan (Anti-Blokir HTTP 429)",
              listOf(
                  "Mengatur waktu tunggu (dalam milidetik) di antara setiap permintaan jaringan ke server penerjemah.",
                  "HTTP 429 terjadi jika aplikasi mengirim permintaan terlalu cepat. Gunakan 200 ms untuk standar, atau 500 ms saat menerjemahkan file besar seperti dialogs.csv."
              )
          ),
          UiLanguage.EN to HelpTopicContent(
              "Request Delay (Anti-HTTP 429 Rate Limit)",
              listOf(
                  "Sets the wait interval (in milliseconds) between network requests to prevent HTTP 429 temporary bans.",
                  "Use 200 ms normally, or 500 ms when translating huge files like dialogs.csv."
              )
          ),
          UiLanguage.TR to HelpTopicContent(
              "İstek Gecikmesi (HTTP 429 Koruması)",
              listOf("Çeviri sunucusuna gönderilen istekler arasındaki bekleme süresini milisaniye cinsinden ayarlar. Büyük dosyalarda 500 ms seçmeniz önerilir.")
          )
      ),
      "lblCache" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Memori Cache (Auto-Resume)",
              listOf(
                  "Setiap baris yang sudah diterjemahkan otomatis disimpan di database SQLite lokal di perangkat Anda.",
                  "Jika proses terhenti atau internet terputus, baris yang sudah tersimpan akan dipulihkan instan tanpa kuota jaringan."
              )
          ),
          UiLanguage.EN to HelpTopicContent(
              "Cache Memory (Auto-Resume)",
              listOf("Every translated line is saved locally in your on-device SQLite database. Re-running the file instantly restores cached lines without re-fetching.")
          ),
          UiLanguage.TR to HelpTopicContent(
              "Önbellek (Otomatik Devam)",
              listOf("Çevrilen her satır cihaz içi SQLite veritabanına kaydedilir. İşlem kesilirse kayıtlı satırlar anında geri yüklenir.")
          )
      ),
      "chkFallback" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Auto-Fallback ke Mesin Lain",
              listOf(
                  "Jika mesin utama gagal menerjemahkan suatu baris setelah 3 kali percobaan, sistem otomatis mencoba mesin cadangan lainnya.",
                  "Disarankan tetap dicentang agar tingkat keberhasilan mendekati 100%."
              )
          ),
          UiLanguage.EN to HelpTopicContent(
              "Auto-Fallback to Other Engines",
              listOf("If the primary engine fails after 3 retries, the tool automatically tries backup engines so no line is left behind unnecessarily.")
          ),
          UiLanguage.TR to HelpTopicContent(
              "Otomatik Yedek Motor Geçişi",
              listOf("Ana motor 3 denemeden sonra başarısız olursa sistem otomatik olarak diğer yedek çeviri motorlarını dener.")
          )
      ),
      "secFormat" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Bahasa & Struktur Teks Warband",
              listOf("Mengatur bahasa asal mod, bahasa tujuan hasil terjemahan, serta aturan pemisahan kode ID baris khas file lokalisasi Mount & Blade: Warband.")
          ),
          UiLanguage.EN to HelpTopicContent(
              "Language & Warband Text Structure",
              listOf("Configures the original mod language, target output language, and line ID parsing rules specific to Mount & Blade: Warband localization files.")
          ),
          UiLanguage.TR to HelpTopicContent(
              "Dil ve Warband Metin Biçimi",
              listOf("Modun kaynak dilini, hedef çeviri dilini ve Warband CSV satır ayrıştırma kurallarını belirler.")
          )
      ),
      "lblSrcLang" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Bahasa Sumber (Mod Asli)",
              listOf(
                  "Pilih bahasa asli yang digunakan di dalam file CSV mod yang Anda unggah (umumnya English, atau Türkçe untuk mod buatan Turki).",
                  "Pilih Deteksi Otomatis jika satu file berisi campuran beberapa bahasa."
              )
          ),
          UiLanguage.EN to HelpTopicContent(
              "Source Language (Original Mod)",
              listOf("Select the original language written inside your mod CSV files (usually English or Turkish). Use Auto-Detect if the file contains mixed languages.")
          ),
          UiLanguage.TR to HelpTopicContent(
              "Kaynak Dil (Orijinal Mod)",
              listOf("Yüklediğiniz mod CSV dosyalarının orijinal dilini seçin (genellikle İngilizce veya Türkçe).")
          )
      ),
      "lblTgtLang" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Bahasa Tujuan (Hasil)",
              listOf("Pilih bahasa akhir yang akan ditampilkan di dalam game setelah file CSV dipasang ke folder languages/ mod Anda.")
          ),
          UiLanguage.EN to HelpTopicContent(
              "Target Language (Output)",
              listOf("Select the output language you want to see in-game after placing the translated CSV files into your mod's languages/ folder.")
          ),
          UiLanguage.TR to HelpTopicContent(
              "Hedef Dil (Çıktı)",
              listOf("Çeviri tamamlandıktan sonra oyun içinde görmek istediğiniz hedef dili seçin.")
          )
      ),
      "lblSep" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Pemisah Kode (Separator)",
              listOf(
                  "Format standar baris Warband adalah kode_id|Teks dialog di sini.",
                  "Karakter pemisah default adalah pipa (|). Semua teks di sebelah kiri pemisah dikunci utuh agar game tetap mengenali ID string dan tidak crash."
              )
          ),
          UiLanguage.EN to HelpTopicContent(
              "Code Separator",
              listOf("Standard Warband CSV rows follow string_id|Dialogue text here. Everything on the left side of the pipe (|) is untouched so the game engine never breaks.")
          ),
          UiLanguage.TR to HelpTopicContent(
              "Kod Ayracı (Separator)",
              listOf("Warband satırları kod_id|Metin biçimindedir. Soldaki kod kısmı çevrilmeden korunur.")
          )
      ),
      "lblStrip" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Hapus Tag (Opsional)",
              listOf(
                  "Beberapa file terjemahan mentah mod menyertakan penanda seperti {***} di awal kalimat untuk menandai baris yang belum diterjemahkan.",
                  "Isi kolom ini untuk menghapus tag tersebut otomatis, atau kosongkan jika ingin mempertahankan tag aslinya."
              )
          ),
          UiLanguage.EN to HelpTopicContent(
              "Strip Tag (Optional)",
              listOf("Unfinished mod templates often prefix lines with {***}. Enter a tag here to strip it automatically, or leave empty to keep all original tags.")
          ),
          UiLanguage.TR to HelpTopicContent(
              "Silinecek Etiket (İsteğe Bağlı)",
              listOf("Çevrilmemiş satırların başındaki {***} gibi etiketleri otomatik temizlemek için kullanılır.")
          )
      ),
      "lblRegex" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Lewati Baris (Regex)",
              listOf(
                  "Pola ekspresi reguler (Regular Expression) untuk melewati baris tertentu agar tidak dikirim ke server terjemahan.",
                  "Default ^\\d+$ melewati baris yang hanya berisi angka saja sehingga menghemat waktu dan kuota."
              )
          ),
          UiLanguage.EN to HelpTopicContent(
              "Skip Lines (Regex)",
              listOf("Regular expression pattern used to skip matching lines without translating them. Default ^\\d+$ skips number-only lines.")
          ),
          UiLanguage.TR to HelpTopicContent(
              "Satır Atla (Regex)",
              listOf("Belirtilen düzenli ifadeye uyan satırları çevirmeden atlar. Varsayılan ^\\d+$ yalnızca rakam içeren satırları atlar.")
          )
      ),
      "varProt" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Pelindung Variabel Warband",
              listOf(
                  "Mesin game Warband menggunakan variabel dinamis seperti {s0} (nama), {reg1} (angka), {playername}, {lord}, dan pemisah baris ^^.",
                  "Fitur ini mengunci seluruh token tersebut sebelum diterjemahkan dan mengembalikannya persis seperti semula agar teks di dalam game tidak rusak."
              )
          ),
          UiLanguage.EN to HelpTopicContent(
              "Warband Variable Guard",
              listOf("Warband scripts rely on placeholders like {s0}, {reg1}, {playername}, and line breaks ^^. This guard locks them before translation and restores them intact afterward.")
          ),
          UiLanguage.TR to HelpTopicContent(
              "Warband Değişken Koruması",
              listOf("{s0}, {reg1}, {playername} ve ^^ gibi oyun içi değişkenleri çeviri sırasında bozulmaya karşı kilitler.")
          )
      ),
      "secGloss" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Glosarium Istilah Kustom",
              listOf(
                  "Kolom ini sengaja dikosongkan secara default agar Anda dapat memasukkan daftar istilah sendiri sesuai mod yang dimainkan.",
                  "Tulis 1 baris = 1 istilah. Nama saja (Swadia) → dipertahankan asli; dengan '=' (Vassal=Vasali) → menggunakan terjemahan buatan Anda."
              )
          ),
          UiLanguage.EN to HelpTopicContent(
              "Custom Glossary",
              listOf(
                  "Empty by default so you can define your own terminology for any mod.",
                  "Format (one per line): term only (Swadia) stays untranslated; with '=' (Vassal=Vasali) forces your exact translation."
              )
          ),
          UiLanguage.TR to HelpTopicContent(
              "Özel Terim Sözlüğü",
              listOf("Kendi mod terimlerinizi girebilmeniz için varsayılan olarak boştur. Tek kelime yazarsanız çevrilmez, Kelime=Çeviri yazarsanız özel çeviriniz uygulanır.")
          )
      ),
      "secQueue" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Unggah & Antrean File CSV / TXT / TSV",
              listOf("Anda dapat memilih satu atau beberapa file sekaligus seperti dialogs.csv, game_strings.csv, troops.csv, maupun quick_strings.csv. File diproses berurutan dan mempertahankan nama file aslinya saat disimpan.")
          ),
          UiLanguage.EN to HelpTopicContent(
              "Upload & File Queue (CSV / TXT / TSV)",
              listOf("Select one or multiple Warband localization files at once. Files are processed sequentially and retain their original filenames when saved.")
          ),
          UiLanguage.TR to HelpTopicContent(
              "CSV / TXT / TSV Dosya Yükleme ve Kuyruk",
              listOf("Birden fazla Warband dil dosyasını aynı anda seçip sırayla çevirebilirsiniz. Kaydedilen dosyalar orijinal adlarını korur.")
          )
      ),
      "secPreview" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Pratinjau & Edit Cepat (Live Preview)",
              listOf("Menampilkan 5 baris terakhir yang baru diproses antara teks asli mod ([ASLI]) dan hasil terjemahan ([HASIL]). Ketuk tombol Edit pada baris mana pun untuk memperbaikinya dan memperbarui cache otomatis.")
          ),
          UiLanguage.EN to HelpTopicContent(
              "Live Preview & Quick Edit",
              listOf("Shows the last 5 processed rows comparing original and translated text. Tap Edit on any row to refine it manually and update both the file output and cache.")
          ),
          UiLanguage.TR to HelpTopicContent(
              "Canlı Önizleme ve Hızlı Düzenleme",
              listOf("İşlenen son 5 satırı karşılaştırır. Herhangi bir satırdaki Düzenle düğmesine dokunarak çeviriyi manuel olarak düzeltebilirsiniz.")
          )
      ),
      "secLog" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Log Aktivitas Sistem",
              listOf("Mencatat riwayat pemuatan file, pergantian mesin otomatis (fallback), penyimpanan cache, serta baris yang mengalami gangguan koneksi.")
          ),
          UiLanguage.EN to HelpTopicContent(
              "System Activity Log",
              listOf("Logs file loading, automatic engine fallbacks, cache saves, and any rows that encountered connection errors.")
          ),
          UiLanguage.TR to HelpTopicContent(
              "Sistem Etkinlik Günlüğü",
              listOf("Dosya yüklemelerini, otomatik motor geçişlerini ve bağlantı hatalarını kronolojik olarak kaydeder.")
          )
      ),
      "secGuide" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Panduan Pemasangan Mod Warband",
              listOf("Menjelaskan langkah-langkah menyalin atau menyimpan langsung file CSV hasil terjemahan ke dalam folder languages/id/ pada Android menggunakan ZArchiver dan MnBTool.")
          ),
          UiLanguage.EN to HelpTopicContent(
              "Mod Installation Guide",
              listOf("Step-by-step instructions on placing your translated CSV files into your mod's languages/ folder on Android using ZArchiver and MnBTool.")
          ),
          UiLanguage.TR to HelpTopicContent(
              "Mod Kurulum Kılavuzu",
              listOf("Çevrilen CSV dosyalarının modun languages/ klasörüne nasıl yerleştirileceğini adım adım açıklar.")
          )
      ),
      "secCsvStruct" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Pratinjau Struktur CSV (10 Baris Pertama)",
              listOf(
                  "Menampilkan 10 baris pertama dari file antrean yang dipilih dan memisahkan kolom ID (kiri) serta Teks Sumber (kanan) sesuai karakter pemisah aktif.",
                  "Gunakan pratinjau ini sebelum memulai terjemahan untuk memastikan pemisah (misal '|') dan pola Regex skip sudah tepat."
              )
          ),
          UiLanguage.EN to HelpTopicContent(
              "CSV Structure Preview (First 10 Rows)",
              listOf(
                  "Displays the first 10 rows of the selected queued file parsed into ID (left) and Source Text (right) columns using your active separator.",
                  "Verify your separator and skip regex here before starting batch translation."
              )
          ),
          UiLanguage.TR to HelpTopicContent(
              "CSV Yapı Önizleyici (İlk 10 Satır)",
              listOf("Seçilen dosyanın ilk 10 satırını ID ve Kaynak Metin sütunlarına ayırarak çeviri öncesi yapıyı doğrulamanızı sağlar.")
          )
      ),
      "secTelemetry" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Grafik Kecepatan & Progres Real-Time",
              listOf(
                  "Menampilkan kurva kecepatan terjemahan (baris per detik) dan persentase kemajuan file yang sedang diproses secara langsung.",
                  "Lonjakan kecepatan tinggi menandakan baris dipulihkan instan dari cache Auto-Resume."
              )
          ),
          UiLanguage.EN to HelpTopicContent(
              "Real-Time Speed & Progress Graph",
              listOf(
                  "Plots live translation throughput (lines per second) alongside completion percentage for the currently active CSV file.",
                  "Sharp speed spikes indicate instant restoration from the on-device Auto-Resume cache."
              )
          ),
          UiLanguage.TR to HelpTopicContent(
              "Gerçek Zamanlı Hız ve İlerleme Grafiği",
              listOf("İşlenmekte olan CSV dosyasının anlık çeviri hızını ve tamamlanma yüzdesini canlı olarak çizer.")
          )
      ),
      "secExportSettings" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Pengaturan Ekspor CSV & Encoding",
              listOf(
                  "Mengatur pemisah kolom (Koma, Tab, Titik Koma, atau Pipa '|') dan pengodean karakter (UTF-8 atau UTF-16) saat menyimpan file terjemahan.",
                  "Untuk dipasang langsung ke mod Warband, disarankan tetap memakai '|' dan UTF-8. Gunakan Koma/Tab/Titik Koma atau UTF-16 jika ingin mengedit file di Excel atau software lain."
              )
          ),
          UiLanguage.EN to HelpTopicContent(
              "CSV Export Delimiter & Encoding",
              listOf(
                  "Configures the output column delimiter (Comma, Tab, Semicolon, or Warband Pipe '|') and character encoding (UTF-8 or UTF-16) used when saving files.",
                  "Keep '|' and UTF-8 for direct Warband mod installation, or pick Comma/Tab/Semicolon for spreadsheet editing."
              )
          ),
          UiLanguage.TR to HelpTopicContent(
              "CSV Dışa Aktarma Ayarları",
              listOf("Kaydedilen çeviri dosyaları için sütun ayracını (Virgül, Sekme, Noktalı Virgül veya '|') ve karakter kodlamasını (UTF-8 / UTF-16) seçmenizi sağlar.")
          )
      ),
      "secHistory" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Riwayat Sesi Terjemahan",
              listOf(
                  "Menyimpan daftar file CSV yang pernah diproses beserta waktu dan statusnya secara lokal di perangkat.",
                  "Ketuk 'Buka Ulang' untuk memuat kembali file ke antrean penerjemah (termasuk hasil terjemahan atau cache Auto-Resume)."
              )
          ),
          UiLanguage.EN to HelpTopicContent(
              "Translation Session History",
              listOf(
                  "Stores local metadata (filename, timestamp, status) of processed CSV files.",
                  "Tap 'Re-open' to load any past file back into the translation queue with its results or Auto-Resume cache."
              )
          ),
          UiLanguage.TR to HelpTopicContent(
              "Çeviri Oturum Geçmişi",
              listOf("İşlenen CSV dosyalarının adını, zamanını ve durumunu saklar. Herhangi bir oturumu kuyruğa yeniden açabilirsiniz.")
          )
      ),
      "secSummary" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Dasbor Ringkasan Sesi & Produktivitas",
              listOf("Menampilkan perbandingan jumlah baris terjemahan vs baris asli, jumlah galat, total durasi proses batch, serta grafik kata per hari.")
          ),
          UiLanguage.EN to HelpTopicContent(
              "Session Summary & Productivity Dashboard",
              listOf("Shows translated vs original line counts, encountered errors, total batch duration, and daily productivity charts.")
          ),
          UiLanguage.TR to HelpTopicContent(
              "Oturum Özeti ve Verimlilik Panosu",
              listOf("Çevrilen/orijinal satır sayısını, hataları, toplam işlem süresini ve günlük verimlilik grafiğini gösterir.")
          )
      ),
      "secErrorLog" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Log Error Terjemahan",
              listOf("Mencatat baris yang gagal diterjemahkan atau file CSV dengan format bermasalah. Anda dapat memeriksa detail dan mencoba ulang baris tertentu tanpa mengulang seluruh batch.")
          ),
          UiLanguage.EN to HelpTopicContent(
              "Translation Error Log",
              listOf("Records failed rows and unsupported/malformed CSV formats so you can inspect and manually retry individual rows without restarting the entire batch.")
          ),
          UiLanguage.TR to HelpTopicContent(
              "Çeviri Hata Günlüğü",
              listOf("Başarısız satırları ve geçersiz CSV biçimlerini kaydeder; tüm toplu işlemi yeniden başlatmadan tek satırları yeniden denemenizi sağlar.")
          )
      ),
      "batchSize" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Mode Batch Permintaan",
              listOf(
                  "Menggabungkan 5 atau 10 baris sekaligus dalam 1 permintaan HTTP untuk mempercepat penerjemahan file besar (7.000–8.000 baris).",
                  "Jika pemecahan segmen tidak cocok atau terkena limit HTTP 429, sistem otomatis turun ke mode 1 baris/permintaan agar akurasi tetap terjaga."
              )
          ),
          UiLanguage.EN to HelpTopicContent(
              "Batch Request Mode",
              listOf(
                  "Bundles 5 or 10 consecutive lines per HTTP request to translate large 7,000–8,000 row files significantly faster.",
                  "Automatically falls back to single-line mode whenever segment alignment mismatches or rate limits occur."
              )
          ),
          UiLanguage.TR to HelpTopicContent(
              "Toplu İstek Modu",
              listOf("Büyük dosyaları (7.000–8.000 satır) hızlandırmak için tek HTTP isteğinde 5 veya 10 satırı birlikte gönderir.")
          )
      ),
      "medievalTone" to mapOf(
          UiLanguage.ID to HelpTopicContent(
              "Penghalus Gaya Bahasa Medieval",
              listOf(
                  "Merapikan spasi tanda baca dan mengganti frasa terjemahan kaku (seperti 'Tuanku saya' -> 'Tuanku', 'kastil saya' -> 'kastilku') agar lebih cocok dengan nuansa Mount & Blade: Warband.",
                  "Seluruh token variabel ({playername}, ^^, \\n, kode warna) dikunci sementara sehingga tidak pernah berubah."
              )
          ),
          UiLanguage.EN to HelpTopicContent(
              "Medieval Tone Post-Processing",
              listOf(
                  "Cleans punctuation spacing and normalizes stiff literal translations into Mount & Blade: Warband medieval phrasing while masking variables and color tags."
              )
          ),
          UiLanguage.TR to HelpTopicContent(
              "Ortaçağ Üslubu İyileştirmesi",
              listOf("Noktalama boşluklarını düzeltir ve değişkenleri koruyarak çevirileri Mount & Blade: Warband atmosferine uyarlar.")
          )
      )
  )

  fun get(lang: UiLanguage, key: String): String {
    return stringsByLang[lang]?.get(key) ?: stringsByLang[UiLanguage.ID]?.get(key) ?: key
  }

  fun getHelp(lang: UiLanguage, topicKey: String): HelpTopicContent? {
    val topicMap = helpTopics[topicKey] ?: return null
    return topicMap[lang] ?: topicMap[UiLanguage.ID]
  }
}
