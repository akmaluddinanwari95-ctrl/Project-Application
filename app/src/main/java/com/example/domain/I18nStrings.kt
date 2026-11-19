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
          "btnImpGloss" to "Impor (.txt)",
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
          "lblErrExport" to "Ekspor Log Baris Gagal"
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
          "btnImpGloss" to "Import (.txt)",
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
          "lblErrExport" to "Export Error Log"
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
          "btnImpGloss" to "İçe Aktar (.txt)",
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
          "lblErrExport" to "Hata Günlüğünü Dışa Aktar"
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
