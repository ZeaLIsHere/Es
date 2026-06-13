# Product Requirements Document (PRD)
## TaskForge — Project Hub untuk Mahasiswa

---

| | |
|---|---|
| **Versi Dokumen** | 1.0 |
| **Tanggal** | 14/06/2026 |
| **Status** | Draft |
| **Dibuat oleh** | [GANTI_NAMA_KETUA_KELOMPOK] |
| **Mata Kuliah** | Pemrograman Berbasis Objek (PBO) |
| **Institusi** | GTLP |
| **Asisten Lab** | BANG ANDRE |

---

## Daftar Isi

1. Ringkasan Eksekutif
2. Latar Belakang & Masalah
3. Tujuan Produk
4. Target Pengguna
5. Fitur & Kebutuhan Produk
6. User Stories
7. Kriteria Keberhasilan
8. Batasan Produk
9. Asumsi & Dependensi
10. Rencana Rilis

---

## 1. Ringkasan Eksekutif

TaskForge adalah aplikasi desktop berbasis Java yang dirancang khusus untuk membantu mahasiswa mengelola proyek kelompok secara terpusat. Aplikasi ini menyelesaikan dua masalah utama yang dihadapi mahasiswa: ketidakjelasan pembagian tugas dan file proyek yang tersebar di berbagai platform (WhatsApp, Google Drive, Gmail, dll.).

Dengan TaskForge, setiap kelompok memiliki satu ruang kerja digital yang memuat manajemen tugas bergaya Kanban, penyimpanan file terpusat, sistem penilaian kontribusi otomatis, dan log aktivitas yang transparan.

Aplikasi dibangun menggunakan JavaFX untuk antarmuka desktop, Spring Boot untuk REST API backend, H2 Database dengan JPA/Hibernate untuk persistensi data, serta Spring Security dengan JWT untuk autentikasi dan otorisasi.

---

## 2. Latar Belakang & Masalah

### 2.1 Konteks

Kerja kelompok adalah bagian tak terpisahkan dari kehidupan akademik mahasiswa. Namun, mayoritas kelompok masih mengandalkan kombinasi alat komunikasi umum (WhatsApp, LINE) dan penyimpanan awan (Google Drive, OneDrive) yang tidak dirancang khusus untuk manajemen proyek akademik.

### 2.2 Masalah yang Diidentifikasi

**Masalah 1 — Pembagian tugas tidak jelas**
Tugas dibagi lewat pesan chat yang mudah terlewat, tidak ada tempat resmi untuk melihat siapa mengerjakan apa, dan tidak ada mekanisme untuk melacak apakah tugas sudah dikerjakan atau belum.

**Masalah 2 — File proyek berserakan**
File-file proyek tersebar di berbagai platform tanpa struktur. Anggota kelompok harus scroll ratusan pesan WhatsApp atau mencari di folder Drive yang tidak terorganisir hanya untuk menemukan satu file.

**Masalah 3 — Tidak ada transparansi kontribusi**
Tidak ada cara objektif untuk mengukur siapa yang benar-benar berkontribusi dalam proyek. Hal ini sering menimbulkan konflik dan ketidakadilan dalam penilaian kelompok.

**Masalah 4 — Tidak ada jejak aktivitas**
Perubahan, keputusan, dan update progres tidak terdokumentasi. Jika ada anggota yang tidak aktif, tidak ada bukti formal yang bisa ditunjukkan.

### 2.3 Dampak Masalah

- Deadline terlewat karena komunikasi tidak efektif
- Konflik antar anggota kelompok
- Kualitas hasil proyek di bawah potensi sebenarnya
- Anggota aktif merasa tidak dihargai karena tidak ada pengakuan kontribusi

---

## 3. Tujuan Produk

### 3.1 Tujuan Utama

1. Menyediakan satu platform terpusat untuk seluruh aktivitas proyek kelompok mahasiswa
2. Membuat pembagian dan pelacakan tugas menjadi transparan dan terstruktur
3. Memusatkan semua file dan referensi proyek dalam satu tempat yang terorganisir
4. Mengukur kontribusi setiap anggota secara objektif dan otomatis

### 3.2 Tujuan Akademik (UAS PBO)

1. Mengimplementasikan 4 pilar Pemrograman Berbasis Objek: Encapsulation, Inheritance, Polymorphism, dan Abstraction
2. Membangun antarmuka GUI menggunakan JavaFX
3. Membangun REST API menggunakan Spring Boot dengan arsitektur MVC + Service + Repository
4. Mengimplementasikan ORM dengan JPA/Hibernate dan database H2
5. Menerapkan validasi data dan keamanan aplikasi dengan Spring Security

---

## 4. Target Pengguna

### 4.1 Pengguna Utama

**Ketua Kelompok**
- Mahasiswa yang bertanggung jawab atas koordinasi proyek
- Membutuhkan kontrol penuh atas pembagian tugas, deadline, dan persetujuan hasil
- Ingin melihat gambaran besar progress proyek setiap saat

**Anggota Kelompok**
- Mahasiswa yang mengerjakan bagian tugas tertentu
- Membutuhkan kejelasan tentang apa yang harus dikerjakan dan kapan
- Ingin tempat yang mudah untuk upload file hasil kerja

### 4.2 Karakteristik Pengguna

- Mahasiswa aktif S1/D3 yang mengikuti mata kuliah dengan proyek kelompok
- Terbiasa menggunakan smartphone dan laptop
- Tidak memiliki latar belakang teknis khusus (non-developer)
- Terbiasa dengan aplikasi seperti Trello, Notion, atau Google Workspace

---

## 5. Fitur & Kebutuhan Produk

### 5.1 Modul Autentikasi

**F-01: Registrasi Akun**
Pengguna dapat membuat akun baru dengan mengisi nama lengkap, email, dan password. Sistem memvalidasi format email dan panjang password minimal 8 karakter.

**F-02: Login**
Pengguna dapat masuk menggunakan email dan password yang terdaftar. Sistem menerbitkan JWT token yang berlaku 24 jam untuk sesi pengguna.

**F-03: Manajemen Sesi**
Sistem mempertahankan sesi login selama token masih berlaku. Pengguna dapat logout untuk mengakhiri sesi.

### 5.2 Modul Manajemen Proyek

**F-04: Buat Proyek Baru**
Ketua dapat membuat proyek baru dengan mengisi nama proyek, deskripsi, dan deadline proyek.

**F-05: Undang Anggota**
Ketua dapat mengundang anggota ke proyek menggunakan email yang terdaftar di sistem.

**F-06: Daftar Proyek**
Setiap pengguna dapat melihat semua proyek yang mereka ikuti, baik sebagai ketua maupun anggota.

**F-07: Detail Proyek**
Setiap proyek menampilkan ringkasan: jumlah task, progress keseluruhan, daftar anggota, dan deadline.

### 5.3 Modul Manajemen Task (Kanban)

**F-08: Buat Task**
Ketua dapat membuat task baru dalam proyek dengan mengisi judul, deskripsi, prioritas (Rendah/Sedang/Tinggi), deadline task, dan menentukan pemilik (assignee).

**F-09: Kanban Board**
Semua task dalam proyek ditampilkan dalam board Kanban dengan 4 kolom: To-Do, In Progress, Review, dan Done. Setiap task menampilkan judul, assignee, prioritas, dan sisa hari menuju deadline.

**F-10: Update Status Task**
Anggota yang di-assign dapat memindahkan task miliknya antar kolom. Ketua dapat memindahkan task siapapun.

**F-11: Task Overdue**
Task yang melewati deadline otomatis diberi penanda visual merah dan dikategorikan sebagai overdue.

**F-12: Tipe Task**
Sistem mendukung dua tipe task: Simple Task (satu item tunggal) dan Milestone Task (task utama yang memiliki beberapa sub-task di dalamnya).

**F-13: Assign & Reassign**
Ketua dapat mengubah assignee suatu task kapan saja. Setiap perubahan assignee dicatat di activity log.

### 5.4 Modul File Hub

**F-14: Upload File**
Setiap anggota dapat mengupload file ke dalam task tertentu. File yang didukung: PDF, Word (.docx), PowerPoint (.pptx), gambar (JPG, PNG), dan file kode (semua ekstensi). Ukuran maksimal per file: 10 MB.

**F-15: Tambah Link Eksternal**
Setiap anggota dapat menambahkan link eksternal (Google Drive, Figma, GitHub, YouTube, atau URL apapun) ke dalam task, disertai judul deskriptif.

**F-16: Lihat File per Task**
Semua file dan link yang terkait dengan suatu task ditampilkan dalam satu panel, terurut berdasarkan tanggal upload terbaru.

**F-17: Download File**
Semua anggota dapat mendownload file yang ada di proyek mereka.

**F-18: Hapus File**
File hanya dapat dihapus oleh pengunggah aslinya atau oleh ketua kelompok.

**F-19: Cari File**
Pengguna dapat mencari file berdasarkan nama atau tipe file di seluruh proyek.

### 5.5 Modul Kontribusi & Laporan

**F-20: Contribution Score**
Sistem secara otomatis menghitung skor kontribusi setiap anggota berdasarkan: jumlah task selesai, persentase task on-time, dan bobot/prioritas task yang dikerjakan. Skor ditampilkan dalam bentuk angka dan bar berwarna (hijau = baik, kuning = cukup, merah = rendah).

**F-21: Activity Log**
Setiap perubahan dalam proyek dicatat: siapa yang melakukan apa, kapan. Log mencakup: pembuatan task, perubahan status, upload file, dan perubahan assignee.

**F-22: Laporan Proyek**
Ketua dapat men-generate laporan ringkasan proyek yang berisi: daftar task beserta statusnya, contribution score seluruh anggota, dan file yang telah diupload. Laporan dapat diekspor atau ditampilkan dalam tampilan yang dapat di-print.

---

## 6. User Stories

### Sebagai Ketua Kelompok:

- Saya ingin membuat proyek baru dan mengundang anggota, agar semua bisa bekerja dalam satu ruang yang sama.
- Saya ingin membuat task dan mengassign ke anggota spesifik dengan deadline yang jelas, agar tidak ada kebingungan tentang siapa mengerjakan apa.
- Saya ingin melihat Kanban board dan langsung tahu mana task yang overdue, agar saya bisa follow-up ke anggota yang bersangkutan.
- Saya ingin melihat contribution score semua anggota, agar ada dasar objektif saat evaluasi kontribusi kelompok.
- Saya ingin men-generate laporan proyek, agar bisa ditunjukkan ke dosen sebagai bukti kerja kelompok.

### Sebagai Anggota Kelompok:

- Saya ingin melihat semua task yang di-assign ke saya, agar saya tahu persis apa yang harus saya kerjakan.
- Saya ingin memperbarui status task saya dari In Progress ke Done setelah selesai, agar ketua dan anggota lain tahu progress saya.
- Saya ingin mengupload file hasil kerja langsung ke task terkait, agar file tidak perlu dikirim ulang lewat WhatsApp.
- Saya ingin menambahkan link Google Drive ke task, agar semua referensi tersimpan rapi di satu tempat.
- Saya ingin melihat contribution score saya sendiri, agar saya bisa mengevaluasi seberapa aktif kontribusi saya.

---

## 7. Kriteria Keberhasilan

### 7.1 Kriteria Fungsional

- Semua fitur F-01 sampai F-22 dapat dijalankan tanpa error kritis
- JWT authentication berfungsi dengan benar — anggota tidak bisa mengakses proyek yang bukan miliknya
- Role-based access control berfungsi — anggota tidak bisa menghapus task atau melihat laporan lengkap
- Validasi input berfungsi di semua form
- File upload dan download berfungsi untuk semua tipe file yang didukung
- Contribution score ter-update secara otomatis setelah task selesai

### 7.2 Kriteria Teknis (UAS)

- Semua 8 kriteria teknis UAS terpenuhi (JavaFX, Spring Boot, MVC+Service+Repository, H2, JPA, Validation, Security, 4 Pilar OOP)
- Hierarki class OOP dapat dijelaskan dengan jelas: BaseTask → SimpleTask/MilestoneTask, ProjectFile → UploadedFile/LinkedFile
- Arsitektur MVC bersih: tidak ada logika bisnis di Controller, tidak ada query di Service
- Kode dapat dikompilasi dan dijalankan tanpa error pada mesin penguji

### 7.3 Kriteria Demo

- Skenario demo berjalan lancar: buat proyek → assign task → upload file → tampilkan contribution score
- Efek "task overdue + contribution score merah" dapat ditampilkan secara live
- Perbandingan "sebelum TaskForge vs sesudah TaskForge" dapat divisualisasikan dengan jelas

---

## 8. Batasan Produk

### 8.1 Batasan Teknis

- Aplikasi berjalan secara lokal (localhost) — tidak di-deploy ke server publik
- Database H2 bersifat in-memory: data hilang saat aplikasi dimatikan (kecuali dikonfigurasi file-based)
- Tidak ada fitur notifikasi push atau email
- Tidak ada fitur real-time collaboration (perubahan hanya terlihat setelah refresh)
- Aplikasi desktop — tidak tersedia versi web atau mobile

### 8.2 Batasan Lingkup

- Tidak ada fitur version control untuk file (seperti Git)
- Tidak ada fitur chat/komentar antar anggota
- Tidak ada integrasi langsung dengan Google Drive, Figma, atau platform lain (hanya simpan link-nya)
- Maksimal anggota per proyek: [GANTI_BATAS_ANGGOTA, contoh: 10 orang]
- Maksimal proyek per akun: [GANTI_BATAS_PROYEK, contoh: tidak dibatasi / 20 proyek]

---

## 9. Asumsi & Dependensi

### 9.1 Asumsi

- Semua pengguna mengakses aplikasi dari mesin yang sama atau jaringan LAN yang sama
- Pengguna memiliki Java 17+ terinstall di komputer mereka
- Ukuran penyimpanan lokal mencukupi untuk file yang diupload selama demo (minimal 500 MB)

### 9.2 Dependensi Teknis

| Dependensi | Versi | Keterangan |
|---|---|---|
| Java JDK | 17+ | Runtime wajib |
| JavaFX SDK | 17+ | GUI framework |
| Maven | 3.8+ | Build tool |
| Spring Boot | 3.x | Backend framework |
| H2 Database | 2.x | Embedded database |
| Spring Security | 6.x | Autentikasi & otorisasi |

---

## 10. Rencana Rilis

### Fase 1 — Fondasi (Minggu 1)
- Setup proyek Maven (backend + frontend)
- Konfigurasi Spring Boot + H2 + JPA
- Implementasi model/entity dasar (User, Project, Task, ProjectFile)
- Implementasi autentikasi JWT
- Halaman login JavaFX

### Fase 2 — Fitur Inti (Minggu 2)
- CRUD Project dan Task
- Kanban Board JavaFX
- Role-based access control (Ketua & Anggota)
- Activity Log

### Fase 3 — File Hub (Minggu 3)
- Upload file fisik (MultipartFile)
- Simpan link eksternal
- Tampilan File Hub di JavaFX

### Fase 4 — Kontribusi & Laporan (Minggu 4)
- Contribution Score engine
- Laporan proyek
- Polish UI, data seed untuk demo
- Persiapan presentasi

---

*Dokumen ini adalah bagian dari proyek UAS mata kuliah Pemrograman Berbasis Objek.*
*Versi terbaru selalu mengacu pada dokumen yang ada di repository proyek.*
