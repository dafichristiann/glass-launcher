# PRD: Glass Launcher (Android)

Versi 1.0. Dokumen ini adalah spesifikasi lengkap untuk membangun launcher Android bertema "Clear Glass". Tempel utuh ke AI coding tool (Claude Code, Cursor, Gemini, dll), lalu kerjakan **per fase** di Bagian 15. Jangan minta AI membangun semuanya sekaligus.

---

## 0. Cara memakai dokumen ini

**Untuk pemilik proyek**
1. Isi Bagian 2 (Profil Perangkat). Cukup 8 baris. Bagian yang kosong akan memakai default AOSP/Pixel.
2. Tempel seluruh dokumen sekali di awal sesi, lalu kirim prompt fase satu per satu (Bagian 15).
3. Setelah satu fase selesai dan berjalan di HP, baru lanjut fase berikutnya.

**Untuk AI builder**
- Dokumen ini adalah sumber kebenaran tunggal. Jika ada konflik antara kode lama dan dokumen ini, dokumen ini menang.
- Baca Bagian 16 (Aturan AI Builder) sebelum menulis kode.
- Jika spesifikasi tidak menyebut sesuatu, pilih perilaku default HP pengguna (Bagian 2), bukan membuat desain baru.

---

## 1. Ringkasan produk

**Nama kerja:** Glass Launcher
**Platform:** Android, Kotlin, Jetpack Compose
**Tujuan:** Launcher pengganti home yang tampil sama persis dengan desain Figma/pen.dev pengguna: wallpaper pegunungan berkabut biru, widget dan dock berbahan *clear glass*, ikon aplikasi tetap bawaan (default) tetapi ditaruh di tile kaca. Perilaku (app drawer, gestur, grid, dsb.) mengikuti HP pengguna, bukan dirancang ulang.

**Prinsip utama**
1. **Tampilan mengikuti desain.** Warna, radius, blur, dan tata letak dari Bagian 4 dan 9 tidak boleh diubah.
2. **Perilaku mengikuti HP.** Hal yang tidak ada di desain mengikuti perilaku launcher bawaan HP pengguna.
3. **Ikon tetap default.** Tidak ada icon pack. Ikon asli tiap aplikasi dipakai, hanya wadahnya yang dibuat kaca.
4. **Mulus nomor satu.** Kaca yang cantik tapi patah-patah dianggap gagal. Lihat Bagian 6 dan 13.
5. **Semua data dinamis.** Tanggal, hari, cuaca, baterai, jam tidak boleh hardcode. Angka di mockup (Kamis 14, 9°, dll.) hanyalah placeholder.

**Bukan tujuan (non-goals) versi 1**
- Mengganti lock screen sistem (tidak diizinkan Android untuk app biasa).
- Mengganti aplikasi Pengaturan sistem atau Quick Settings sistem.
- Icon pack pihak ketiga, tema ikon kustom per aplikasi.
- Dukungan tablet, foldable, dan Wear OS.
- Cloud sync.

---

## 2. Profil perangkat (ISI BAGIAN INI)

Ganti nilai di kolom kanan. AI wajib membaca tabel ini dan menjadikannya nilai default di `DeviceDefaults.kt`.

| Item | Nilai pengguna | Jika dikosongkan |
|---|---|---|
| Merek dan model HP | `{ISI}` | Pixel (AOSP) |
| Versi Android | `{ISI}` | 14 |
| Skin / OS | `{ISI: One UI / HyperOS / ColorOS / OriginOS / Pixel / lainnya}` | Pixel |
| Refresh rate layar | `{ISI: 60 / 90 / 120}` | 60 |
| Gaya app drawer bawaan | `{ISI: geser atas, scroll vertikal / halaman horizontal / tanpa drawer}` | geser atas, scroll vertikal |
| Grid home bawaan | `{ISI: mis. 4x6}` | 5 kolom x 5 baris |
| Navigasi | `{ISI: gestur / 3 tombol}` | gestur |
| Posisi search bar drawer | `{ISI: atas / bawah}` | atas |
| Aplikasi yang dipakai di dock | `{ISI}` | Telepon, Pesan, Kamera, Browser |

### Aturan "Ikuti Perangkat" (Follow Device)
Semua nilai berikut **diambil dari sistem saat runtime**, tidak ditebak:

| Hal | Sumber |
|---|---|
| Bahasa, format tanggal, 12/24 jam | `Locale.getDefault()`, `DateFormat.is24HourFormat()` |
| Hari pertama pekan | `WeekFields.of(Locale)`; user bisa override (Senin/Minggu) |
| Mode gelap/terang | `Configuration.uiMode` |
| Skala font dan skala tampilan | `Configuration.fontScale`, `densityDpi` (jangan dikunci) |
| Skala animasi | `Settings.Global.ANIMATOR_DURATION_SCALE` (hormati, 0 = tanpa animasi) |
| Refresh rate | `Display.supportedModes`, aktifkan mode tertinggi saat scroll/animasi |
| Zona waktu | `TimeZone.getDefault()` |
| Haptic | `HapticFeedbackConstants`, hormati pengaturan sistem |
| Ikon monokrom/tema | `AdaptiveIconDrawable.getMonochrome()` jika Android 13+ |

Jika sebuah perilaku tidak ada di desain **dan** tidak ada di tabel ini, gunakan perilaku Pixel Launcher (Launcher3) sebagai acuan.

---

## 3. Batasan platform (jujur, harus dipatuhi)

Beberapa layar di desain tidak bisa diganti total oleh app biasa. Ini keputusan implementasinya:

| Layar di desain | Kenyataan Android | Keputusan |
|---|---|---|
| Lock Screen | Tidak bisa diganti app biasa | **Tidak dibangun.** Wallpaper lock screen diset lewat `WallpaperManager.FLAG_LOCK`. Desain lock screen hanya jadi aset wallpaper dan referensi visual |
| Panel notifikasi + Quick Settings | Itu System UI | **Panel pengganti di dalam launcher** (Fase 7, opsional): dibuka dengan swipe-down di Home. Memakai `NotificationListenerService`. Tombol cepat dibatasi pada yang boleh diubah app (Senter, Jangan Ganggu dengan izin, Kecerahan dengan izin WRITE_SETTINGS). Wi-Fi/Bluetooth/Lokasi/Pesawat hanya membuka halaman sistem |
| Pengaturan sistem (Wi-Fi, Bluetooth, Seluler, dsb.) | App biasa tidak boleh mengubahnya | Hub Pengaturan di launcher berisi **pengaturan launcher yang sungguhan** + baris pintasan ke halaman sistem via `Intent` (`Settings.ACTION_WIFI_SETTINGS`, dst.) |
| Layar Wi-Fi | Tidak bisa menyambungkan jaringan sendiri | Tampilkan daftar jaringan (butuh izin lokasi + `WifiManager.scanResults`, hanya baca) dan tombol "Buka pengaturan Wi-Fi". Boleh dijadikan **fase terakhir** atau dibuang |
| Blur di belakang widget pihak ketiga | `RemoteViews` tidak bisa diblur dari dalam | Widget pihak ketiga dibungkus kartu kaca (blur ada di belakang wrapper, bukan di dalam isi widget) |
| Blur wallpaper sistem | Wallpaper sistem digambar di luar proses app | Launcher menggambar **wallpaper-nya sendiri** (bawaan = wallpaper desain). Wallpaper sistem dipakai hanya sebagai mode cadangan tanpa blur (Bagian 8) |

---

## 4. Design tokens: Clear Glass (tidak boleh diubah)

### Warna
| Token | Nilai |
|---|---|
| `textPrimary` | `#1F2F4A` |
| `textSecondary` | `#5B6B85` |
| `accent` | `#3F5A85` |
| `accentSoft` | `#A9C4E6` |
| `glassFill` | putih 12% (rentang 8-15%, ikut slider Transparansi) |
| `glassStrokeStart` | putih 60% (kiri atas) |
| `glassStrokeEnd` | putih 10% (kanan bawah) |
| `glassHighlight` | putih 35%, garis 1dp di sisi atas bagian dalam |
| `shadow` | `#3F5A85` @ 12%, blur 24dp, offset y 8dp |
| `hairline` | putih 25% |
| `badge` | merah lembut `#E57373` |
| `overlayReadable` | putih 20% (dipakai di drawer/settings agar teks terbaca) |

Dilarang: hitam murni, warna jenuh, dan neon. Mode gelap: `textPrimary` menjadi `#EAF1FB`, `glassFill` menjadi putih 8% di atas overlay biru tua 30%. Wallpaper tetap sama.

### Radius (satu skala saja)
Ikon 18dp, widget dan kartu 28dp, dock dan sheet 32dp, kartu panel notifikasi 24dp, tile quick setting 22dp, pill/search penuh (999dp), ikon tile pengaturan 10dp.

### Tipografi (Inter, fallback sistem)
Title 28 SemiBold, header bagian 13 Medium UPPERCASE tracking +4%, body 15 Regular, label ikon 12 Regular, caption 12 Regular. Angka besar widget: 40 SemiBold.

### Ikon UI (bukan ikon aplikasi)
Gaya outline, stroke 1.75dp, ujung bulat, warna `textPrimary`, glyph 28dp di tile 60dp. Gunakan Material Symbols Rounded (weight 300) atau Lucide. Jangan campur dua set ikon.

### Spasi
Basis 4dp. Margin layar 16dp, jarak antar widget 12dp, padding kartu 16dp, tinggi baris pengaturan 56dp, hairline inset 16dp.

### Variasi "Tepi kaca"
Tipis: stroke 0.75dp, highlight 20%. Sedang (default): 1dp, 35%. Tebal: 1.5dp, 50%.

---

## 5. Arsitektur teknis

**Stack:** Kotlin, Jetpack Compose (Material3 hanya untuk komponen dasar, tampilan kustom), Coroutines + Flow, Hilt (atau Koin), Room (tata letak), DataStore Preferences (pengaturan), Coil (hanya untuk gambar non-ikon), Haze (`dev.chrisbanes.haze`) untuk mode blur langsung. Gunakan versi stabil terbaru dan kunci versinya di `libs.versions.toml`.

**SDK:** `minSdk 26` (adaptive icon), `targetSdk` terbaru. Blur langsung hanya untuk Android 12+ (API 31). Ikon monokrom dari sistem hanya Android 13+ (API 33).

**Modul (satu Gradle module `app` untuk versi 1, dipisah dengan package):**
```
com.example.glasslauncher
├── core
│   ├── glass        // GlassSurface, GlassTokens, GlassRenderer, blur cache
│   ├── icons        // IconPipeline, IconCache, GlassIconRenderer
│   ├── wallpaper    // WallpaperRepository, BlurredWallpaperCache
│   ├── device       // DeviceDefaults, DeviceProfile, FollowDevice
│   └── util
├── data
│   ├── apps         // AppRepository (LauncherApps), PackageChangeReceiver
│   ├── workspace    // Room: WorkspaceDao, entities, migrations
│   ├── settings     // SettingsRepository (DataStore), SettingKeys
│   ├── weather      // Open-Meteo client
│   ├── calendar     // CalendarContract reader (opsional)
│   └── widgets      // AppWidgetHost wrapper
├── ui
│   ├── home         // HomeScreen, Pager, Dock, DragDrop, EditMode
│   ├── widgets      // DateWidget, WeatherWidget, MonthCalendarWidget, WidgetPicker
│   ├── drawer       // AppDrawerScreen, FastScrollRail, SearchBar
│   ├── folder       // FolderOverlay
│   ├── panel        // NotificationPanel (Fase 7)
│   └── settings     // SettingsHub, LauncherSettings, WallpaperStyle, Display, WifiShortcut
└── MainActivity.kt  // HOME + DEFAULT intent filter, launchMode singleTask
```

**Manifest wajib:** `MainActivity` dengan intent-filter `MAIN` + `HOME` + `DEFAULT`, `launchMode="singleTask"`, `stateNotNeeded`, `excludeFromRecents="false"`, `windowSoftInputMode="adjustPan"`. `QUERY_ALL_PACKAGES` atau `<queries>` untuk intent launcher. Layanan notifikasi hanya ditambahkan di Fase 7.

**Aliran data:** `LauncherApps` (daftar app + callback instal/hapus) → `AppRepository` (StateFlow) → `IconCache` (bitmap kaca siap pakai) → UI. Tata letak home dari Room (Flow). Pengaturan dari DataStore (Flow). UI hanya mengonsumsi state, tidak mengakses sistem langsung.

---

## 6. Mesin kaca (Glass Rendering Engine): kunci anti-lag

Blur langsung pada banyak elemen adalah penyebab utama lag. Maka **jangan blur per elemen per frame**. Gunakan tiga strategi, dipilih otomatis dan bisa diatur di Pengaturan > Performa.

### Strategi A: Precomputed Frosted (DEFAULT)
Karena wallpaper di launcher statis, "kaca" cukup digambar sebagai potongan wallpaper yang sudah diburamkan.
1. Saat wallpaper dimuat/diganti, buat **tiga bitmap buram** dari wallpaper (downscale ke 1/4, blur Gaussian/RenderEffect sekali di background thread): blur ringan (12), sedang (24, default), berat (40, untuk drawer).
2. Simpan di `BlurredWallpaperCache` (memori + disk). Hitung ulang hanya jika wallpaper atau nilai blur berubah.
3. `GlassSurface` menggambar bagian bitmap buram yang sesuai **koordinat layar elemen** (`positionInRoot`), dipotong ke bentuk rounded, lalu lapisan: fill putih, gradient stroke, highlight atas, shadow.
4. Hasil: nol blur realtime, biaya per elemen hanya `drawImage` + beberapa `drawRect`. Tetap mulus di 120Hz.
5. Saat wallpaper parallax (opsional), offset sumber bitmap mengikuti offset yang sama.

### Strategi B: Live Blur (opsional, "Kualitas tinggi")
Haze (`Modifier.haze` / `hazeChild`) atau `RenderEffect.createBlurEffect`, hanya Android 12+. Dibatasi **maksimal 3 permukaan blur langsung per layar**. Matikan otomatis jika frame time rata-rata > 11ms (120Hz) atau > 14ms (60Hz) selama 2 detik (auto-downgrade ke Strategi A).

### Strategi C: Flat Glass (fallback)
Tanpa blur: fill putih 18% + stroke + highlight + shadow ringan. Dipakai untuk Android < 12 pada mode B, perangkat low RAM (`ActivityManager.isLowRamDevice`), mode Hemat Baterai, atau pilihan manual.

### API komponen
```kotlin
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    blur: GlassBlur = GlassBlur.Medium,   // Light, Medium, Heavy
    emphasis: GlassEmphasis = Standard,    // Standard, Strong (untuk drawer/settings: +overlayReadable)
    content: @Composable BoxScope.() -> Unit
)
```
Aturan: semua kartu, tile, dock, search bar, sheet, toggle track, dan slider track **wajib** lewat `GlassSurface`. Tidak ada `Box` dengan `background(Color.White.copy(...))` manual di luar komponen ini.

Pada kontras buruk (wallpaper berubah ke yang gelap/ramai) naikkan `overlayReadable`, bukan opasitas kaca.

---

## 7. Pipeline ikon: ikon default, wadah kaca

Pengguna ingin ikon **tetap bawaan aplikasi** tetapi bertema kaca.

### Alur render satu ikon
1. Ambil `Drawable` dari `LauncherApps.getActivityList(...).getIcon(density)`.
2. Jika `AdaptiveIconDrawable`: ambil **foreground layer** (ikon asli, tanpa background bawaan). Jika ikon legacy (bukan adaptive): gunakan bitmap-nya, scale ke 66% area aman.
3. Gambar di atas **tile kaca** (60dp, radius 18dp) memakai token Bagian 4 (fill putih, stroke gradient, highlight atas, shadow lembut). Bagian blur memakai bitmap buram wallpaper (Strategi A) sesuai posisi tile.
4. Render menjadi **satu `ImageBitmap` jadi**, simpan di `IconCache` (LruCache memori + file cache di `cacheDir`, kunci: `package + component + userHandle + iconStyle + tileSize + glassSettingsHash`).
5. UI hanya menampilkan bitmap jadi dengan `Image`. Tidak menghitung kaca saat scroll.

### Gaya ikon (Pengaturan > Ikon > "Gaya ikon")
| Opsi | Perilaku | Default |
|---|---|---|
| **Asli di atas kaca** | Foreground berwarna asli, saturasi bisa diturunkan lewat slider "Kelembutan warna" (0-40%) | **Ya** |
| Monokrom kaca | Foreground diubah jadi satu warna `textPrimary` (luminance → alpha mask). Android 13+ pakai layer monochrome bila ada, jika tidak ada pakai mask luminance | |
| Ikuti ikon tema sistem | Memakai layer monochrome sistem, tile tetap kaca | |

### Bentuk tile
Kotak membulat (default, 18dp), Lingkaran, Squircle. Ukuran tile 48-72dp, default 60dp. Glyph = 28dp pada tile 60dp (rasio ~47%). Pada ikon asli, foreground diskalakan agar muat 70% tile.

### Ikon UI dock dan pengaturan
Ikon dock (Telepon, Pesan, Kamera, Browser) memakai aplikasi default sistem untuk peran masing-masing (`RoleManager`/`Intent` resolve), dengan pipeline yang sama (bukan glyph outline manual), kecuali pengguna memilih gaya Monokrom.

### Badge notifikasi
Titik/angka kecil `badge` di pojok kanan atas tile (ukuran 16dp), dari `NotificationListenerService` bila izin diberikan; tanpa izin, fitur badge dimatikan dan baris izin muncul di Pengaturan.

### Invalidasi cache
Terjadi pada: aplikasi diperbarui (`ACTION_PACKAGE_REPLACED`), ganti bentuk/gaya/ukuran, ubah blur/transparansi/tepi, ganti wallpaper. Render ulang dilakukan di `Dispatchers.Default` bertahap (batch 20 ikon), tidak boleh memblokir UI. Selama menunggu, tampilkan bitmap lama.

---

## 8. Wallpaper dan latar

**Mode wallpaper**
1. **Wallpaper Desain (default):** aset gambar pegunungan berkabut dari desain, disertakan di `res/drawable-nodpi` (resolusi 1170x2532 minimal). Digambar oleh launcher, mendukung blur kaca penuh.
2. **Wallpaper pilihan pengguna (dari galeri/kumpulan 6 bawaan):** disalin ke penyimpanan app, di-downscale ke ukuran layar x1.2, dipakai seperti mode 1. 6 bawaan: pegunungan berkabut, danau, hutan berkabut, langit senja, bukit pasir, abstrak biru.
3. **Wallpaper sistem:** `windowShowWallpaper` aktif. Kaca otomatis memakai Strategi C (tanpa blur) karena konten wallpaper tidak bisa dibaca. Peringatan kecil ditampilkan: "Efek kaca penuh butuh wallpaper dari launcher".

**Lock screen:** tombol "Terapkan juga ke layar kunci" memanggil `WallpaperManager.setBitmap(bitmap, null, true, FLAG_LOCK)`.

**Latar tiap layar**
| Layar | Latar |
|---|---|
| Home | wallpaper terang, tanpa overlay |
| App drawer | blur berat (40) + overlay putih 20% |
| Folder terbuka | home di belakang di-blur 30 + overlay biru tua 25% |
| Pengaturan | gradien biru berkabut dari wallpaper, blur berat |
| Panel notifikasi | blur berat + overlay putih 15% |

**Parallax (opsional, default mati):** geser halus wallpaper saat pindah halaman home, maksimum 8% lebar.

---

## 9. Spesifikasi layar (tata letak persis desain)

Semua frame 390x844dp acuan. Gunakan `WindowInsets` untuk status bar dan navigasi, bukan angka tetap. Status bar memakai status bar sistem (transparan, ikon gelap pada wallpaper terang). Jangan menggambar jam 9:41 palsu.

### 9.1 Home halaman 1 (Home 1)
- Baris atas: dua widget sejajar. **Widget Tanggal** (kiri, 28dp radius): nama hari penuh dari sistem (mis. "Sabtu"), angka tanggal besar 40, nama bulan. **Widget Cuaca** (kanan): kota, suhu besar, ikon kondisi + deskripsi ("Hujan ringan"). Keduanya tinggi sama (~96dp), lebar masing-masing setengah dikurangi jarak.
- Di bawahnya: **Widget Kalender Bulanan** (lebar penuh, tinggi menyesuaikan jumlah minggu, tidak ada ruang kosong di antara header bulan dan nama hari). Header: panah kiri, "Februari 2026" (nama bulan dan tahun dari data), panah kanan. Baris nama hari (inisial, mengikuti hari pertama pekan). Grid tanggal 7 kolom. Hari ini: lingkaran `accent` dengan angka putih. Ketuk tanggal membuka aplikasi kalender default pada tanggal itu. Geser kiri/kanan atau panah pindah bulan.
- Tengah bawah: indikator halaman (titik, halaman aktif lebih gelap).
- Dock di bawah (kaca radius 32dp, 4 ikon, jarak sama).
- Sisa area boleh diisi pengguna (mode edit): widget dan ikon dapat ditambah.

### 9.2 Home halaman 2 (grid ikon dan folder)
- Grid 4 kolom x 5 baris (default; ikut Pengaturan Grid). Ikon 60dp, label 12sp di bawah, 1 baris, ellipsis.
- Aplikasi awal: otomatis dari app terpasang dengan prioritas (Kamera, Galeri, Musik, Peta, Catatan, Jam, Kalkulator, Cuaca, Kontak, Email, File, Unduhan, Pengaturan, Play Store). Yang tidak terpasang dilewati.
- Folder "Sosial" dan "Utilitas": tile kaca berisi 2x2 mini ikon. Satu ikon tampil badge.
- Titik indikator: titik ke-2 aktif.
- Dock sama dengan halaman 1.

### 9.3 Folder terbuka
- Home di belakang blur 30 + overlay biru tua 25%.
- Kartu kaca tengah lebar 340dp radius 32dp. Judul dapat diedit (ketuk judul: keyboard, ikon pensil kecil). Grid 3x3 dengan label, titik indikator jika lebih dari 9 aplikasi.
- Drag: ikon yang digeser membesar 1.08x dengan bayangan; geser keluar kartu memindahkan ikon ke home.
- Tombol "x" bulat kaca di bawah kartu. Ketuk area luar atau tombol Back menutup. Animasi: kartu berkembang dari posisi tile folder (shared element, 280ms).

### 9.4 App drawer
Bentuk dan perilaku **mengikuti drawer bawaan HP pengguna** (Bagian 2): urutan, gestur buka, posisi search. Dari desain yang pasti:
- Dibuka dengan swipe-up dari Home. Drag handle di tengah atas.
- Search bar pill kaca tinggi 48dp, placeholder "Cari aplikasi...".
- Baris "Disarankan" dengan 4 aplikasi (dari statistik pemakaian lokal, `UsageStatsManager` bila izin diberikan; tanpa izin: aplikasi paling sering dibuka oleh launcher itu sendiri).
- Daftar aplikasi alfabetis 4 kolom dengan header huruf (13 UPPERCASE `textSecondary`).
- Rail A-Z di kanan (kaca, tipis, lebar 20dp). Menyentuh huruf melompat, huruf aktif berwarna `accent`, dengan balon huruf besar saat ditekan.
- Latar blur berat + overlay putih 20%.
- Ikon pakai pipeline kaca yang sama (Bagian 7).
- Pencarian: filter instan (nama aplikasi, tanpa hasil web). Tombol Enter membuka hasil pertama. Dukungan pencarian kontak dan pengaturan sistem adalah opsional (Fase 8).
- Tekan lama ikon: menu konteks (Info aplikasi, Sembunyikan, Copot/Hapus, Tambah ke Home, plus shortcut dari `LauncherApps.getShortcuts`).

### 9.5 Panel notifikasi (opsional, Fase 7)
Dibuka dengan swipe-down di Home. Isi persis desain: jam dan tanggal ("08:57", "Sabtu, 14 Februari"), grid tile 4x2 (Wi-Fi, Bluetooth, Mode Pesawat, Senter, Rotasi, Hemat Baterai, Jangan Ganggu, Lokasi), dua slider (Kecerahan, Volume), kartu notifikasi, tombol "Hapus semua". Tile yang tidak bisa dikontrol app membuka halaman sistem dengan indikator panah kecil. Tile aktif: fill `accentSoft` 60% + ikon `accent`. Slider: track kaca, thumb putih 24dp. Kartu notifikasi radius 24dp dari `NotificationListenerService`, ketuk membuka `contentIntent`, swipe menghapus.

### 9.6 Pengaturan (hub utama)
Layar ini adalah pintu masuk pengaturan launcher. Latar gradien kabut blur. Judul "Pengaturan" 28 SemiBold, search bar "Cari pengaturan" (mencari di katalog Bagian 10, termasuk sinonim). Kartu profil (avatar bulat, "Pengguna", "Akun & cadangan"): berfungsi sebagai **Cadangan & Pulihkan** pengaturan launcher. Grup baris (tinggi 56dp, tile ikon 36dp radius 10dp, label, nilai kanan `textSecondary`, chevron, hairline inset 16dp):

| Grup | Baris | Perilaku |
|---|---|---|
| 1. Pintasan sistem | Wi-Fi (menampilkan "Terhubung" jika terbaca), Bluetooth, Jaringan Seluler, Hotspot | Membuka halaman sistem via `Intent` |
| 2. Tampilan | Tampilan, Wallpaper & Gaya, Suara & Getaran (pintasan sistem), Notifikasi (izin akses notifikasi) | Layar internal untuk Tampilan dan Wallpaper & Gaya |
| 3. Perangkat | Baterai, Penyimpanan, Aplikasi, Privasi & Keamanan | Pintasan sistem |
| 4. Launcher | Launcher, Sistem, Tentang Ponsel | "Launcher" ke 9.7, "Sistem" ke pintasan, "Tentang" menampilkan info versi launcher dan perangkat |

Baris pintasan sistem diberi ikon kecil "buka luar" (arrow-up-right) agar jelas berpindah aplikasi.

### 9.7 Pengaturan Launcher (inti)
Back button, judul "Launcher". **Kartu pratinjau langsung** di atas: miniatur Home hidup (instance `HomeScreen` skala 0.45, non-interaktif) yang bereaksi seketika ke setiap slider (blur, transparansi, bentuk ikon, grid, dock). Setelah itu bagian-bagian di Bagian 10. Komponen: kartu kaca per bagian, baris 56dp, toggle (track kaca, `accent` saat aktif, thumb putih), slider (track kaca, thumb putih 24dp, nilai di kanan), segmented control kaca (segmen aktif putih 70%), stepper (- angka +).

### 9.8 Wallpaper & Gaya
Pratinjau berdampingan: layar kunci dan home dengan wallpaper terpilih. Carousel 6 thumbnail wallpaper bawaan + tombol "Dari galeri". 5 swatch warna aksen (default `#3F5A85`, plus biru langit, hijau kabut, ungu senja, abu pasir). Segmented Terang / Gelap / Otomatis. Tombol "Terapkan ke layar kunci" (toggle). Mengubah aksen mengubah `accent` dan `accentSoft` di semua layar.

### 9.9 Tampilan (internal launcher)
Hanya hal yang bisa dikontrol launcher: Ukuran font launcher (skala 85-130%, ditumpuk di atas skala sistem), Ukuran ikon, Mode gelap launcher (ikuti sistem / terang / gelap), Refresh rate preferensi (ikuti sistem / tinggi), Waktu layar mati dan Kecerahan = baris pintasan ke halaman sistem. Pratinjau "Aa" hidup untuk ukuran font.

### 9.10 Wi-Fi (opsional, boleh dibuang)
Toggle besar di atas mengarahkan ke `Settings.Panel.ACTION_WIFI` (panel internet bawaan Android 10+) karena app tidak boleh menyalakan Wi-Fi. Daftar "Jaringan tersimpan" dan "Jaringan tersedia" dibaca dari `WifiManager` (izin `ACCESS_FINE_LOCATION`, `NEARBY_WIFI_DEVICES` di 13+). Tombol "Tambah jaringan" membuka `ACTION_WIFI_ADD_NETWORKS`. Jika izin ditolak, tampilkan keadaan kosong dengan tombol "Buka pengaturan Wi-Fi".

### 9.11 Layar tambahan yang belum ada di desain (ikuti gaya yang sama, perilaku ikuti HP)
| Layar | Isi |
|---|---|
| Menu tekan-lama Home | Sheet kaca kecil: Wallpaper & Gaya, Widget, Pengaturan Launcher |
| Mode edit Home | Ikon bergoyang halus (atau tombol - kecil), halaman di-zoom-out 0.9, tombol "+ halaman", area hapus di atas, tombol Selesai |
| Pemilih widget | Sheet penuh kaca: daftar widget bawaan (Tanggal, Cuaca, Kalender Bulanan) di atas, widget pihak ketiga dikelompokkan per aplikasi. Pratinjau di tiap item. Dimensi tampil (mis. 4x2) |
| Menu konteks ikon | Popup kaca di atas/bawah ikon dengan shortcut aplikasi |
| Dialog pencopotan | Sheet kaca "Copot instalasi?" |
| Layar izin | Kartu penjelasan sebelum meminta izin (lokasi, kalender, notifikasi, pemakaian aplikasi) |
| Onboarding | 3 halaman: sambutan (wallpaper), "Jadikan launcher utama" (`RoleManager.ROLE_HOME`), izin opsional. Dapat dilewati |
| Empty/error | Cuaca gagal dimuat: "Cuaca tidak tersedia" + ikon awan putus, tombol coba lagi |

---

## 10. Katalog pengaturan launcher (lengkap)

Setiap pengaturan memiliki kunci DataStore, tipe, default, dan efek langsung. Kolom "Default" dengan tanda ◆ berarti diambil dari Profil Perangkat (Bagian 2).

### Efek Kaca
| Pengaturan | Tipe | Default | Efek |
|---|---|---|---|
| Mode render kaca | pilihan: Otomatis / Efisien (A) / Kualitas tinggi (B) / Datar (C) | Otomatis | Bagian 6 |
| Intensitas blur | slider 0-40 | 24 | Memilih/menurunkan bitmap buram, render ulang kaca |
| Transparansi | slider 8-20% (fill putih) | 12% | `glassFill` |
| Tepi kaca | Tipis / Sedang / Tebal | Sedang | Bagian 4 |
| Sorotan atas | toggle | Aktif | Garis highlight 1dp |
| Bayangan | toggle | Aktif | Bagian 4 |
| Kekuatan overlay keterbacaan | slider 0-40% | 20% | `overlayReadable` pada drawer/settings |

### Ikon
| Pengaturan | Tipe | Default |
|---|---|---|
| Gaya ikon | Asli di atas kaca / Monokrom kaca / Ikuti tema sistem | Asli di atas kaca |
| Kelembutan warna | slider 0-40% | 10% |
| Bentuk tile | Kotak membulat / Lingkaran / Squircle | Kotak membulat |
| Ukuran ikon | slider 48-72dp | 60dp (◆ ikut ukuran bawaan HP jika ada) |
| Tampilkan label | toggle | Aktif |
| Baris label | 1 / 2 | 1 |
| Ukuran label | slider 10-14sp | 12sp |
| Badge notifikasi | toggle (butuh izin) | Aktif jika izin ada |
| Animasi tekan | toggle (skala 0.94) | Aktif |

### Grid Home
| Pengaturan | Tipe | Default |
|---|---|---|
| Kolom | stepper 3-6 | 4 (◆) |
| Baris | stepper 4-8 | 5 (◆) |
| Jarak antar ikon | slider | Otomatis |
| Kunci tata letak | toggle | Mati |
| Tambah ikon baru otomatis ke Home | toggle | Mati |
| Putar layar | Ikuti sistem / Potret saja | Potret saja |
| Loop halaman (geser terus) | toggle | Mati |
| Indikator halaman | Titik / Garis / Sembunyi | Titik |

### Dock
| Pengaturan | Tipe | Default |
|---|---|---|
| Tampilkan dock | toggle | Aktif |
| Jumlah ikon | segmented 4 / 5 | 4 (◆) |
| Latar dock | Kaca / Transparan | Kaca |
| Tinggi dock | slider | Otomatis |

### Widget
| Pengaturan | Tipe | Default |
|---|---|---|
| Gaya latar widget | Kaca / Transparan / Tanpa latar | Kaca |
| Radius widget | slider 12-36dp | 28dp |
| Widget Cuaca: sumber lokasi | Otomatis (izin lokasi kasar) / Pilih kota manual | Pilih kota manual |
| Widget Cuaca: satuan | °C / °F | °C (◆ ikut locale) |
| Widget Cuaca: interval segar | 30 mnt / 1 jam / 3 jam | 1 jam |
| Widget Kalender: hari pertama pekan | Ikuti sistem / Senin / Minggu | Ikuti sistem |
| Widget Kalender: tampilkan acara | toggle (butuh izin kalender) | Mati |
| Widget Kalender: tandai akhir pekan | toggle | Mati |
| Izinkan widget pihak ketiga | toggle | Aktif |
| Kunci ukuran widget | toggle | Mati |

### Laci Aplikasi
| Pengaturan | Tipe | Default |
|---|---|---|
| Gaya laci | Scroll vertikal / Halaman horizontal | ◆ ikut HP |
| Gestur buka | Geser atas / Tombol / Keduanya | Geser atas (◆) |
| Urutan | A-Z / Terakhir dipasang / Paling sering | A-Z (◆) |
| Kolom | stepper 3-6 | 4 |
| Tampilkan "Disarankan" | toggle (izin penggunaan opsional) | Aktif |
| Rail A-Z | toggle | Aktif |
| Posisi search bar | Atas / Bawah | ◆ ikut HP |
| Keyboard otomatis saat buka | toggle | Mati |
| Aplikasi tersembunyi | daftar kelola | kosong |
| Pencarian termasuk kontak/pengaturan | toggle | Mati |

### Gestur
| Pengaturan | Pilihan | Default |
|---|---|---|
| Geser atas (di Home) | Buka laci / Tidak ada | Buka laci |
| Geser bawah (di Home) | Panel notifikasi sistem / Panel launcher / Tidak ada | Panel notifikasi sistem |
| Ketuk dua kali area kosong | Kunci layar / Tidak ada | Kunci layar (butuh Accessibility atau Device Admin; jika ditolak, sembunyikan opsi ini) |
| Tekan lama area kosong | Menu Home / Tidak ada | Menu Home |
| Cubit di Home | Mode edit / Tidak ada | Mode edit |
| Tombol Home saat di Home | Kembali ke halaman 1 / Tidak ada | Kembali ke halaman 1 |

### Wallpaper & Gaya, Tampilan
Seperti Bagian 9.8 dan 9.9 (mode wallpaper, aksen, tema, parallax, skala font launcher, mode gelap launcher).

### Performa
| Pengaturan | Tipe | Default |
|---|---|---|
| Hemat baterai otomatis | toggle (beralih ke Datar saat Battery Saver) | Aktif |
| Animasi | Penuh / Berkurang / Mati | Penuh (hormati skala sistem) |
| Pantau frame (overlay debug) | toggle | Mati |
| Bersihkan cache ikon | tombol | |

### Cadangan, Reset, Tentang
Ekspor/impor pengaturan dan tata letak ke file JSON (`ActivityResultContracts.CreateDocument`), Reset tata letak, Reset semua pengaturan, Info versi, Lisensi pihak ketiga, tautan umpan balik.

---

## 11. Model data

```kotlin
@Entity data class WorkspaceItem(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val type: ItemType,          // APP, FOLDER, WIDGET_BUILTIN, WIDGET_EXTERNAL, SHORTCUT
  val container: Container,    // DESKTOP, DOCK, FOLDER
  val containerId: Long?,      // id folder jika container = FOLDER
  val page: Int, val cellX: Int, val cellY: Int,
  val spanX: Int = 1, val spanY: Int = 1,
  val packageName: String?, val componentName: String?, val userSerial: Long?,
  val title: String?,          // judul folder/kustom
  val widgetId: Int?,          // appWidgetId bila widget eksternal
  val builtinKind: String?     // "date", "weather", "month_calendar"
)
```
Pengaturan di DataStore dengan kunci terpusat di `SettingKeys` (satu sumber, tidak ada string literal tersebar). `SettingsRepository` mengekspos `Flow<LauncherSettings>` tunggal (data class immutable) agar UI mudah dibuat stabil.

Aturan integritas: tidak ada dua item pada sel yang sama; saat grid diubah, item direlokasi otomatis (cari sel kosong terdekat, buat halaman baru bila perlu). Hapus item saat paket dicopot (`PackageChangeReceiver`). Migrasi Room wajib ditulis, jangan `fallbackToDestructiveMigration`.

Cuaca: Open-Meteo (`https://api.open-meteo.com/v1/forecast?latitude=..&longitude=..&current=temperature_2m,weather_code`), tanpa API key. Terjemahkan `weather_code` ke teks Indonesia dan ikon (tabel lokal). Cache hasil 1 jam, timeout 8 detik, tampilkan data lama dengan label waktu jika gagal. Geocoding kota: `https://geocoding-api.open-meteo.com/v1/search`.

---

## 12. Interaksi, gestur, dan animasi

- **Mulai/pulang:** Home dibuka < 300ms dari tombol Home (aktivitas singleTask, state dipertahankan).
- **Swipe-up drawer:** gestur diikat ke progres (interpolasi 0..1), bukan animasi on/off. Sambil ditarik, ikon home memudar dan drawer naik. Selesai < 320ms. Hanya `graphicsLayer` (translate/alpha), tanpa memicu recomposition per frame.
- **Folder:** shared-element dari tile ke kartu, 280ms, easing `EaseOutCubic`.
- **Drag & drop:** mulai setelah tekan lama 350ms dengan haptic `LONG_PRESS`; ikon mengikuti jari di lapisan overlay (bukan mengubah state per piksel). Penyisipan ke sel dihitung di callback ringan. Tahan di tepi layar 600ms pindah halaman.
- **Tekan ikon:** skala 0.94 selama ditekan, buka app dengan `LauncherApps.startMainActivity` dan animasi buka dari ikon (`ActivityOptions.makeScaleUpAnimation`).
- **Back:** menutup lapisan teratas dulu (folder, drawer, menu, edit mode), baru kembali ke halaman 1. Gunakan `PredictiveBackHandler` pada Android 14+.
- **Rotasi:** ikut pengaturan, default potret.
- **Aksesibilitas:** `contentDescription` untuk semua ikon, target sentuh ≥ 48dp, dukung TalkBack, dan jaga kontras teks ≥ 4.5:1 terhadap kaca di wallpaper bawaan.

---

## 13. Budget performa dan aturan anti-lag

**Target terukur (di HP pengguna)**
| Metrik | Target |
|---|---|
| Jank frame saat scroll drawer/pindah halaman | < 2% |
| Waktu frame | < 8.3ms (120Hz) / < 16ms (60Hz) |
| Cold start sampai Home terlihat | < 700ms |
| Buka drawer | < 320ms, tanpa frame drop |
| RAM rata-rata | < 180MB |
| Render ulang seluruh cache ikon (300 app) | < 4 detik di latar belakang, UI tetap responsif |
| Ukuran APK release | < 15MB |

**Aturan wajib**
1. Kaca = Strategi A kecuali diminta lain (Bagian 6). Maksimum 3 blur langsung per layar.
2. Ikon selalu bitmap jadi dari `IconCache`; dilarang membuat `Drawable`, `Canvas`, atau modifier blur per sel di dalam daftar.
3. `LazyVerticalGrid` dengan `key` stabil dan `contentType`. Item daftar menerima parameter stabil (`@Immutable`/`@Stable`), bukan `List` mentah.
4. State dibaca selambat mungkin: gunakan lambda untuk nilai yang berubah cepat (`Modifier.graphicsLayer { ... }`), jangan baca state animasi di composable parent.
5. `remember`, `derivedStateOf`, dan `snapshotFlow` untuk turunan; tidak ada pekerjaan berat di composition.
6. Dekode gambar dan render ikon di `Dispatchers.Default`/`IO`; hanya menyerahkan hasil ke UI.
7. Wallpaper di-downscale sesuai layar, jangan menyimpan bitmap 4K di memori. Gunakan `Bitmap.Config.HARDWARE` untuk bitmap yang hanya digambar.
8. Nonaktifkan overdraw: satu lapisan latar per layar, hindari kaca bertumpuk di dalam kaca (kartu kaca di dalam kartu kaca memakai fill tipis tanpa blur kedua).
9. Gunakan **Baseline Profile** dan **R8 full mode** pada build release. Tambahkan modul `benchmark` (Macrobenchmark) untuk scroll drawer dan cold start.
10. Pantau dengan overlay frame (Pengaturan > Performa) dan `JankStats`. Auto-downgrade ke Strategi A/C jika melebihi budget (Bagian 6).
11. Cuaca dan kalender di-refresh lewat `WorkManager`, bukan loop di UI. Tidak ada polling saat layar mati.
12. Jangan memakai `Thread.sleep`, `runBlocking`, atau akses disk/jaringan pada main thread. Aktifkan `StrictMode` pada build debug.

---

## 14. Izin dan privasi

| Izin | Untuk | Kapan diminta |
|---|---|---|
| `QUERY_ALL_PACKAGES` / `<queries>` | Daftar aplikasi | Manifest |
| `BIND_APPWIDGET` (via `AppWidgetHost`) | Widget pihak ketiga | Saat menambah widget |
| `ACCESS_COARSE_LOCATION` | Cuaca otomatis | Hanya jika pengguna memilih lokasi otomatis |
| `READ_CALENDAR` | Acara di widget kalender | Hanya jika toggle diaktifkan |
| Akses notifikasi (`NotificationListener`) | Badge dan panel | Hanya jika diaktifkan, dengan penjelasan |
| `PACKAGE_USAGE_STATS` | "Disarankan" | Opsional |
| `SET_WALLPAPER` | Terapkan wallpaper lock | Normal |
| `REQUEST_DELETE_PACKAGES` | Copot aplikasi | Normal |
| `WRITE_SETTINGS` | Kecerahan di panel | Hanya Fase 7 |

Prinsip: semua izin **opsional** dan fitur yang bersangkutan mati dengan anggun jika ditolak. Data tidak dikirim ke server selain permintaan cuaca (koordinat dibulatkan 2 desimal). Tanpa analitik dan iklan.

---

## 15. Rencana pembangunan per fase

Setiap fase: kirim prompt-nya, tunggu hasil, jalankan di HP, lalu lanjut. Fase tidak boleh digabung.

### Fase 0: Fondasi proyek
**Prompt:** "Buat proyek Android Kotlin + Compose bernama Glass Launcher sesuai Bagian 5 PRD. Atur `MainActivity` sebagai HOME launcher, tampilkan layar kosong dengan wallpaper desain (Bagian 8, mode 1) sebagai latar penuh. Buat `GlassTokens` (Bagian 4), `DeviceDefaults` (Bagian 2), dan tema Compose. Jangan membuat fitur lain."
**Selesai jika:** aplikasi bisa dipilih sebagai launcher utama, wallpaper tampil penuh di bawah status bar transparan, token terdefinisi di satu file.

### Fase 1: Mesin kaca dan ikon
**Prompt:** "Implementasikan Bagian 6 dan 7. Buat `BlurredWallpaperCache`, `GlassSurface` (Strategi A dan C), `IconCache`, dan `GlassIconRenderer`. Buat layar uji `GlassPlayground` berisi: satu kartu kaca 28dp, satu dock dengan 4 ikon dari aplikasi terpasang, satu grid 40 ikon yang dapat di-scroll, dan slider blur/transparansi/tepi yang mengubah semuanya secara langsung."
**Selesai jika:** scroll grid 40 ikon mulus (tanpa blur realtime), mengubah slider memperbarui tampilan tanpa lag, tile kaca sesuai token.

### Fase 2: Home dan dock
**Prompt:** "Implementasikan Bagian 9.1 dan 9.2: HomeScreen dengan pager halaman, dock, indikator halaman, penempatan ikon dari Room (Bagian 11), tap untuk membuka aplikasi, dan inisialisasi layout awal otomatis. Belum ada drag-drop dan widget."
**Selesai jika:** dua halaman Home sesuai desain, ikon membuka aplikasi dengan animasi, data tersimpan setelah aplikasi dimatikan.

### Fase 3: Widget bawaan
**Prompt:** "Implementasikan DateWidget, WeatherWidget (Open-Meteo, Bagian 11), dan MonthCalendarWidget sesuai Bagian 9.1 dengan data sepenuhnya dinamis (hari, tanggal, hari pertama pekan dari sistem). Tambahkan ke halaman Home 1. Tangani keadaan loading dan error."
**Selesai jika:** hari dan tanggal sama dengan jam sistem, kalender benar untuk bulan manapun, cuaca tampil atau error dengan rapi.

### Fase 4: App drawer
**Prompt:** "Implementasikan Bagian 9.4: drawer dengan swipe-up berbasis progres, search, 'Disarankan', header huruf, rail A-Z, menu tekan-lama ikon, dan sembunyikan aplikasi. Ikuti gaya dan urutan dari Profil Perangkat."
**Selesai jika:** buka/tutup 60-120fps, rail A-Z melompat tepat, pencarian instan.

### Fase 5: Edit, drag-drop, folder, widget pihak ketiga
**Prompt:** "Implementasikan Bagian 9.3, 9.11 (menu tekan-lama, mode edit, pemilih widget), drag-drop ikon antar sel, antar halaman, ke dock, ke folder, pembuatan folder dengan menumpuk dua ikon, dan `AppWidgetHost` untuk widget pihak ketiga dengan pembungkus kaca."
**Selesai jika:** semua operasi tersimpan, tidak ada tabrakan sel, folder terbuka sesuai desain.

### Fase 6: Pengaturan dan pratinjau langsung
**Prompt:** "Implementasikan Bagian 9.6 sampai 9.9 dan seluruh katalog Bagian 10 memakai DataStore. Pratinjau langsung di Pengaturan Launcher harus bereaksi seketika. Tambahkan pencarian pengaturan, cadangan/pulihkan, dan reset. Semua pengaturan harus benar-benar berefek, tidak ada tombol palsu."
**Selesai jika:** setiap baris di katalog bekerja atau disembunyikan dengan alasan jelas (mis. izin), reset mengembalikan default dari Profil Perangkat.

### Fase 7 (opsional): Panel notifikasi
**Prompt:** "Implementasikan Bagian 9.5 dengan batasan Bagian 3. Tile yang tidak bisa dikontrol app membuka halaman sistem. Tampilkan notifikasi lewat NotificationListenerService."

### Fase 8 (opsional): Wi-Fi, pencarian lanjutan, polishing
**Prompt:** "Implementasikan Bagian 9.10, pencarian kontak/pengaturan di drawer, onboarding, mode gelap, Baseline Profile, dan benchmark. Optimalkan sampai target Bagian 13 tercapai."

---

## 16. Aturan untuk AI builder (anti ngawur dan anti lag)

1. **Satu fase per satu waktu.** Jangan mengerjakan fase berikutnya sebelum diminta. Jika diminta mengerjakan beberapa hal, urutkan sesuai Bagian 15 dan berhenti di akhir fase.
2. **Jangan mengubah desain.** Warna, radius, dan tata letak dari Bagian 4 dan 9 adalah tetap. Jangan menambahkan elemen, layar, atau gaya baru yang tidak ada di dokumen. Jika perlu keputusan visual baru, ikuti token yang ada.
3. **Jangan mengarang API.** Gunakan hanya API Android/Compose yang benar-benar ada. Jika ragu pada suatu API, tulis komentar `// VERIFY` dan pilih alternatif yang pasti ada, bukan menebak. Jangan menggunakan library yang tidak disebut di Bagian 5 tanpa alasan tertulis.
4. **Kompilasi harus bersih.** Setiap fase menghasilkan proyek yang bisa dibangun (`./gradlew assembleDebug`) tanpa error dan tanpa peringatan deprecation baru. Jangan meninggalkan `TODO` yang membuat aplikasi crash.
5. **Tidak ada hardcode data.** Tanggal, hari, cuaca, daftar aplikasi, dan nilai warna datang dari sistem, data, atau token. Angka di mockup hanya contoh.
6. **Patuhi Bagian 13.** Dilarang menaruh blur langsung pada item daftar. Dilarang menghitung ikon kaca di dalam composable daftar.
7. **Struktur kecil dan jelas.** File maksimum ~300 baris; satu tanggung jawab per file. Nama sesuai struktur Bagian 5.
8. **Tangani kegagalan.** Setiap operasi jaringan, izin, dan I/O punya status loading/sukses/error. Aplikasi tidak boleh crash saat izin ditolak.
9. **Jangan menulis ulang file yang sudah jalan** hanya untuk gaya. Ubah seminimal mungkin, sebutkan file yang diubah.
10. **Akhiri setiap fase** dengan: (a) daftar file yang dibuat/diubah, (b) cara menjalankan dan mengujinya di HP, (c) daftar hal yang belum selesai atau ditunda, (d) satu pertanyaan jika ada keputusan yang butuh pengguna. Lalu berhenti.
11. **Bahasa:** kode dan komentar dalam bahasa Inggris, seluruh teks UI dalam Bahasa Indonesia, disimpan di `strings.xml` (siap lokalisasi).
12. **Kalau macet atau berputar:** jika dua percobaan memperbaiki error yang sama gagal, hentikan, jelaskan akar masalahnya, dan usulkan dua alternatif. Jangan mengulang pendekatan yang sama.

---

## 17. Checklist QA (uji di HP sebelum rilis)

- [ ] Bisa dijadikan launcher utama; tombol Home dan Recent bekerja; tidak ada flicker saat kembali ke Home.
- [ ] Kaca terlihat sama dengan desain pada Home 1, Home 2, Drawer, Folder, Pengaturan.
- [ ] Hari, tanggal, kalender, dan jam benar untuk bulan dan zona waktu manapun (uji dengan mengubah tanggal sistem).
- [ ] Hari pertama pekan benar untuk locale Indonesia dan saat diganti manual.
- [ ] Cuaca: sukses, offline, dan lokasi ditolak semua tampil rapi.
- [ ] Ikon: aplikasi adaptive, legacy, dan tanpa ikon monokrom semuanya tampil rapi di tile kaca; aplikasi baru muncul otomatis, yang dicopot hilang otomatis.
- [ ] Ganti bentuk/ukuran/gaya ikon memperbarui semua layar tanpa membekukan UI.
- [ ] Scroll drawer 300 aplikasi mulus; benchmark memenuhi Bagian 13.
- [ ] Drag-drop: ke halaman lain, ke dock, ke folder, ke tempat sampah; data konsisten setelah restart.
- [ ] Widget pihak ketiga bisa ditambah, diubah ukurannya, dan dihapus.
- [ ] Mode gelap, ukuran font 130%, dan TalkBack tidak merusak tata letak.
- [ ] Hemat baterai beralih ke Kaca Datar otomatis.
- [ ] Semua izin ditolak: aplikasi tetap berfungsi.
- [ ] Reset, cadangan, dan pulihkan berfungsi.
- [ ] Tidak ada crash di Logcat selama 30 menit pemakaian normal.

---

## 18. Asumsi dan keputusan terbuka

- Nama dan package (`com.example.glasslauncher`) bersifat sementara.
- Jika Profil Perangkat (Bagian 2) tidak diisi, default = Pixel/AOSP, Android 14, drawer scroll vertikal, grid 5x5.
- Font Inter disertakan sebagai aset; jika gagal dimuat, fallback ke font sistem tanpa mengubah ukuran.
- Wi-Fi (9.10) dan Panel notifikasi (9.5) adalah fitur opsional dengan batasan Bagian 3; boleh dibuang tanpa memengaruhi inti launcher.
- Desain lock screen dipertahankan sebagai referensi visual dan aset wallpaper, bukan fitur yang dibangun.
