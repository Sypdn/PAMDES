# 💧 PAMDES - Pengelola Air Minum Desa Wanasaba Daya

Aplikasi Android native modern untuk mencatat pelanggan air desa, menghitung tagihan rekening air bulanan otomatis, dan mencetak struk/nota fisik serta rekapitulasi bulanan ke PDF & printer thermal.

Aplikasi ini dirancang khusus untuk pengelola air minum **PAMDES "UTUH BERSATU" Desa Wanasaba Daya, Kec. Wanasaba, Lombok Timur, NTB**.

---

## 🌟 Fitur Utama

- 👥 **Kelola Data Pelanggan**:
  - Catat nama, wilayah/dusun, jenis tarif/kategori pelanggan, dan meteran awal (7 digit).
  - Pilihan Kategori / Jenis Tarif:
    - **Rumah Tangga**: Rp 500 / m³
    - **Pengusaha**: Rp 1.000 / m³
  - Fitur pencarian cepat dan konfirmasi hapus data.

- 🧮 **Hitung Tagihan Bulanan Real-time**:
  - Fitur autokomplit pencarian nama pelanggan dengan proteksi wajib memilih dari daftar pop-up.
  - Perhitungan otomatis real-time dengan animasi *count-up*:
    - **Pakai Air (m³)** = Meter Akhir - Meter Awal
    - **Jumlah Air (Rp)** = Pakai Air × Harga Perkubikasi
    - **Biaya Beban** = Rp 2.000
    - **Dana PMA** = Rp 500
    - **Total Bayar** = Jumlah Air + Beban + PMA
  - Otomatis memperbarui angka meteran terakhir pelanggan setelah tagihan disimpan.
  - Deteksi dan cegah tagihan ganda pada periode bulan/tahun yang sama.

- 🖨️ **Cetak Struk Rekening Air (Format Nota Fisik Asli)**:
  - Format cetak PDF A5 Landscape 100% persis dengan nota fisik asli PAMDES "UTUH BERSATU".
  - Terhubung langsung dengan **Android PrintManager** untuk mencetak ke printer Bluetooth / Thermal / Wi-Fi atau menyimpan sebagai PDF.
  - Tersedia opsi Buka PDF dan Bagikan Struk ke WhatsApp/Email via `FileProvider`.

- 📊 **Rekapitulasi Bulanan Massal (6 Struk Siap Potong per A4)**:
  - Filter rekap bulanan berdasarkan periode bulan, tahun, wilayah/dusun, dan pencarian nama.
  - Fitur **Ekspor Struk Massal A4**: Menggenerasi kertas A4 berisi **6 Struk Nota Rekening Air (2 Kolom x 3 Baris)** berbingkai potong ganda yang siap dicetak dan digunting untuk dibagikan ke warga.

- ⚙️ **Pengaturan Fleksibel**:
  - Ubah tarif Rumah Tangga, tarif Pengusaha, Biaya Beban, Dana PMA, dan Nama Desa/Header secara mandiri.

- 🎨 **Tampilan Liquid Glass (Control Center iOS 17 style)**:
  - Latar belakang animasi *Liquid Water Mesh* yang hidup.
  - Panel *frosted glass* semi-transparan dengan efek *specular highlight* dan animasi tekan *spring bounce*.

---

## 📱 Arsitektur & Teknologi

- **Bahasa**: Java Native (Android SDK)
- **Min SDK**: API 29 (Android 10)
- **Target / Compile SDK**: API 37
- **Arsitektur**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Database Lokal**: SQLite via Room Persistence Library
- **UI Binding**: ViewBinding & Material Design 3
- **PDF Engine**: `android.graphics.pdf.PdfDocument` & Scoped Storage MediaStore
- **Printing**: Native `android.print.PrintManager`

---

## 🚀 Cara Menjalankan Proyek

1. Clone repositori ini:
   ```bash
   git clone https://github.com/Sypdn/PAMDES.git
   ```
2. Buka proyek di **Android Studio**.
3. Lakukan **Gradle Sync**.
4. Jalankan aplikasi (*Run 'app'*) di emulator atau perangkat fisik dengan minimal Android 10 (API 29).

---

## 📷 Hak Cipta & Lisensi

Dibuat khusus untuk pengelola air minum **PAMDES "UTUH BERSATU" Wanasaba Daya**.
© 2026 Sypdn. Hak Cipta Dilindungi Undang-Undang.
