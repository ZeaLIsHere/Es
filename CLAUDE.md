# TaskForge — Project Hub untuk Mahasiswa
> File ini dibaca otomatis oleh Claude Code di setiap sesi. Jangan hapus atau pindahkan.

---

## Identitas Proyek

**Nama Aplikasi:** TaskForge  
**Deskripsi:** Aplikasi manajemen proyek kelompok mahasiswa berbasis desktop dengan fitur task management (Kanban board), file hub terpusat, contribution scoring, dan activity log. Dibangun dengan JavaFX (frontend) dan Spring Boot (backend REST API).  
**Mata Kuliah:** Pemrograman Berbasis Objek (PBO)  
**Tipe:** Proyek UAS — Java Desktop App dengan GUI

---

## Stack Teknologi

| Layer | Teknologi |
|---|---|
| Frontend (GUI) | JavaFX |
| Backend | Spring Boot |
| Database | H2 (in-memory, embedded) |
| ORM | JPA / Hibernate |
| Keamanan | Spring Security + JWT |
| Validasi | Spring Validation (Bean Validation) |
| Build Tool | Maven |
| Java Version | Java 17+ |

---

## Struktur Proyek

```
taskforge/
├── backend/                          # Spring Boot REST API
│   ├── src/main/java/com/taskforge/
│   │   ├── TaskforgeApplication.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java   # Spring Security + JWT config
│   │   │   └── CorsConfig.java
│   │   ├── controller/               # MVC: Layer Controller
│   │   │   ├── AuthController.java
│   │   │   ├── ProjectController.java
│   │   │   ├── TaskController.java
│   │   │   ├── FileController.java
│   │   │   └── ReportController.java
│   │   ├── service/                  # Layer Service (logika bisnis)
│   │   │   ├── AuthService.java
│   │   │   ├── ProjectService.java
│   │   │   ├── TaskService.java
│   │   │   ├── FileService.java
│   │   │   ├── ContributionService.java
│   │   │   └── ReportService.java
│   │   ├── repository/               # Layer Repository (akses DB)
│   │   │   ├── UserRepository.java
│   │   │   ├── ProjectRepository.java
│   │   │   ├── TaskRepository.java
│   │   │   ├── ProjectFileRepository.java
│   │   │   └── ActivityLogRepository.java
│   │   ├── model/                    # JPA Entity + OOP hierarchy
│   │   │   ├── User.java
│   │   │   ├── Project.java
│   │   │   ├── task/
│   │   │   │   ├── BaseTask.java     # Abstract — Abstraction
│   │   │   │   ├── SimpleTask.java   # Inheritance
│   │   │   │   └── MilestoneTask.java# Inheritance (punya sub-task)
│   │   │   ├── file/
│   │   │   │   ├── ProjectFile.java  # Abstract — Abstraction
│   │   │   │   ├── UploadedFile.java # Inheritance (file fisik)
│   │   │   │   └── LinkedFile.java   # Inheritance (URL eksternal)
│   │   │   ├── ActivityLog.java
│   │   │   └── ContributionScore.java
│   │   ├── dto/                      # Data Transfer Object (request/response)
│   │   │   ├── request/
│   │   │   └── response/
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── ResourceNotFoundException.java
│   │   ├── security/
│   │   │   ├── JwtUtil.java
│   │   │   └── JwtAuthFilter.java
│   │   └── interfaces/               # Interface untuk Polymorphism
│   │       ├── Scorable.java         # calculateScore()
│   │       ├── Accessible.java       # getAccessUrl()
│   │       └── Reportable.java       # generate()
│   └── src/main/resources/
│       └── application.properties
│
└── frontend/                         # JavaFX Desktop App
    ├── src/main/java/com/taskforge/ui/
    │   ├── MainApp.java              # Entry point JavaFX
    │   ├── controller/               # JavaFX Controller (FXML)
    │   │   ├── LoginController.java
    │   │   ├── DashboardController.java
    │   │   ├── KanbanController.java
    │   │   ├── FileHubController.java
    │   │   └── ReportController.java
    │   ├── service/                  # HTTP client ke backend
    │   │   └── ApiClient.java
    │   └── model/                    # DTO untuk UI
    └── src/main/resources/
        ├── fxml/
        │   ├── login.fxml
        │   ├── dashboard.fxml
        │   ├── kanban.fxml
        │   ├── filehub.fxml
        │   └── report.fxml
        └── css/
            └── styles.css
```

---

## Aturan Arsitektur (WAJIB DIIKUTI)

1. **Pemisahan layer ketat:** Controller hanya terima request dan kembalikan response. TIDAK boleh ada logika bisnis di Controller — semua harus di Service.
2. **Repository hanya untuk query database.** Jangan ada logika bisnis di Repository.
3. **Gunakan DTO** untuk semua request dan response API — jangan expose Entity JPA langsung ke client.
4. **Setiap endpoint REST wajib ada validasi** menggunakan anotasi `@Valid`, `@NotBlank`, `@NotNull`, `@Future`, dll.
5. **Gunakan `@PreAuthorize`** untuk otorisasi berbasis role (KETUA / ANGGOTA).
6. **Abstract class dan interface** sudah didefinisikan di `/model/task/`, `/model/file/`, dan `/interfaces/` — ikuti hierarki yang ada, jangan bypass dengan flat class.

---

## Konvensi Kode

### Naming
- Class: `PascalCase` — contoh: `ProjectService`, `BaseTask`
- Method & variable: `camelCase` — contoh: `calculateScore()`, `assignedUser`
- Konstanta: `UPPER_SNAKE_CASE` — contoh: `MAX_FILE_SIZE`
- Package: `lowercase` — contoh: `com.taskforge.service`
- REST endpoint: `kebab-case` — contoh: `/api/project-files`, `/api/task-assignments`

### Spring Boot
- Selalu pakai `@Slf4j` dari Lombok untuk logging, jangan `System.out.println`
- Response API selalu dibungkus dalam `ApiResponse<T>` wrapper class
- Exception selalu di-throw sebagai custom exception, tangkap di `GlobalExceptionHandler`

### JavaFX
- Setiap scene punya file `.fxml` sendiri — jangan build UI secara programatik
- Controller JavaFX hanya untuk binding UI, panggil `ApiClient` untuk data
- Gunakan `Task<T>` JavaFX untuk HTTP call (non-blocking UI thread)

---

## Pemetaan 4 Pilar OOP

> Ini penting untuk UAS — pastikan implementasi ini selalu ada dan bisa dijelaskan.

### 1. Encapsulation
- `BaseTask`: field `status`, `score`, `assignee` adalah `private`. Hanya bisa diubah via method `complete()`, `reassign(User u)`, `markOverdue()`
- `ProjectFile`: field `storagePath` dan `externalUrl` adalah `private`. Pemanggil hanya pakai `getAccessUrl()`

### 2. Inheritance
- `SimpleTask` dan `MilestoneTask` extends `BaseTask`
- `UploadedFile` dan `LinkedFile` extends `ProjectFile`
- Shared field di parent class, override behavior di child

### 3. Polymorphism
- Interface `Scorable` → method `calculateScore()` diimplementasikan berbeda oleh `SimpleTask` dan `MilestoneTask`
- Interface `Accessible` → method `getAccessUrl()` diimplementasikan berbeda oleh `UploadedFile` (return path lokal) dan `LinkedFile` (return URL eksternal)
- Interface `Reportable` → method `generate()` diimplementasikan oleh `ContributionReport` dan `FileActivityReport`

### 4. Abstraction
- `BaseTask` adalah abstract class — tidak bisa di-instantiate langsung
- `ProjectFile` adalah abstract class — tidak bisa di-instantiate langsung
- Detail implementasi storage disembunyikan dari Controller dan Service

---

## Database (H2 + JPA)

### Relasi Utama
```
User          --< Project        (One owner, many members via ProjectMember)
Project       --< Task           (@OneToMany, cascade = ALL)
Task          --< ProjectFile    (@OneToMany, cascade = ALL)
Task          --> User           (@ManyToOne, assignee)
Project       --< ActivityLog    (@OneToMany)
Project+User  --> ContributionScore
```

### Perintah H2 Console
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:taskforgedb`
- Username: `[GANTI_DENGAN_USERNAME_H2_KALIAN]`
- Password: `[GANTI_DENGAN_PASSWORD_H2_KALIAN]`

### application.properties (template)
```properties
# H2 Database
spring.datasource.url=jdbc:h2:mem:taskforgedb;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=[GANTI_USERNAME_H2]
spring.datasource.password=[GANTI_PASSWORD_H2]
spring.h2.console.enabled=true

# JPA / Hibernate
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# JWT
jwt.secret=[GANTI_DENGAN_SECRET_KEY_MINIMAL_256_BIT]
jwt.expiration=86400000

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
file.upload-dir=[GANTI_DENGAN_PATH_FOLDER_UPLOAD_LOKAL]

# Server
server.port=8080
```

---

## Endpoint REST API

### Auth
| Method | Endpoint | Deskripsi | Role |
|--------|----------|-----------|------|
| POST | `/api/auth/register` | Daftar akun baru | Public |
| POST | `/api/auth/login` | Login, return JWT | Public |

### Project
| Method | Endpoint | Deskripsi | Role |
|--------|----------|-----------|------|
| GET | `/api/projects` | Semua project user login | KETUA, ANGGOTA |
| POST | `/api/projects` | Buat project baru | KETUA |
| GET | `/api/projects/{id}` | Detail project | KETUA, ANGGOTA |
| PUT | `/api/projects/{id}` | Update project | KETUA |
| DELETE | `/api/projects/{id}` | Hapus project | KETUA |
| POST | `/api/projects/{id}/members` | Tambah anggota | KETUA |

### Task
| Method | Endpoint | Deskripsi | Role |
|--------|----------|-----------|------|
| GET | `/api/projects/{id}/tasks` | Semua task dalam project | KETUA, ANGGOTA |
| POST | `/api/projects/{id}/tasks` | Buat task baru | KETUA |
| PUT | `/api/tasks/{id}` | Update task (status, dll) | KETUA, ANGGOTA* |
| PUT | `/api/tasks/{id}/assign` | Assign task ke anggota | KETUA |
| DELETE | `/api/tasks/{id}` | Hapus task | KETUA |

*Anggota hanya bisa update task yang di-assign ke dirinya

### File Hub
| Method | Endpoint | Deskripsi | Role |
|--------|----------|-----------|------|
| GET | `/api/tasks/{id}/files` | Semua file dalam task | KETUA, ANGGOTA |
| POST | `/api/tasks/{id}/files/upload` | Upload file fisik | KETUA, ANGGOTA |
| POST | `/api/tasks/{id}/files/link` | Tambah link eksternal | KETUA, ANGGOTA |
| GET | `/api/files/{id}/download` | Download file | KETUA, ANGGOTA |
| DELETE | `/api/files/{id}` | Hapus file | KETUA, uploader |

### Report & Scoring
| Method | Endpoint | Deskripsi | Role |
|--------|----------|-----------|------|
| GET | `/api/projects/{id}/scores` | Contribution score semua anggota | KETUA, ANGGOTA |
| GET | `/api/projects/{id}/report` | Generate laporan proyek | KETUA |
| GET | `/api/projects/{id}/activity` | Activity log | KETUA |

---

## Validasi yang Harus Ada

```java
// Contoh: TaskRequest DTO
@NotBlank(message = "Judul task tidak boleh kosong")
private String title;

@NotNull(message = "Deadline wajib diisi")
@Future(message = "Deadline harus di masa depan")
private LocalDateTime deadline;

@NotNull(message = "Assignee wajib ditentukan")
private Long assigneeId;

// Contoh: FileUpload
@Size(max = 10_000_000, message = "Ukuran file maksimal 10MB")
private MultipartFile file;

// Contoh: LinkedFile
@NotBlank(message = "URL tidak boleh kosong")
@URL(message = "Format URL tidak valid")
private String externalUrl;
```

---

## Security Rules

```java
// Contoh konfigurasi role
@PreAuthorize("hasRole('KETUA')")
public ResponseEntity<?> assignTask(...) { ... }

@PreAuthorize("hasRole('KETUA') or @taskService.isAssignee(#taskId, authentication.name)")
public ResponseEntity<?> updateTask(@PathVariable Long taskId, ...) { ... }

@PreAuthorize("hasRole('KETUA') or @fileService.isUploader(#fileId, authentication.name)")
public ResponseEntity<?> deleteFile(@PathVariable Long fileId, ...) { ... }
```

---

## Perintah Build & Run

### Backend
```bash
# Di folder /backend
mvn clean install              # Build
mvn spring-boot:run            # Jalankan server (port 8080)
mvn test                       # Jalankan semua unit test
```

### Frontend
```bash
# Di folder /frontend
mvn clean javafx:run           # Jalankan aplikasi JavaFX
mvn clean package              # Build JAR
```

---

## Panduan Kerja dengan Claude Code

### Saat memulai fitur baru
1. Baca dulu file model dan interface yang relevan
2. Buat DTO request/response terlebih dahulu
3. Buat Repository → Service → Controller (urutan ini)
4. Tambah validasi di DTO
5. Tambah otorisasi di Controller
6. Update JavaFX Controller untuk panggil endpoint baru

### Saat ada bug
1. Cek log di console Spring Boot
2. Cek H2 console untuk verifikasi data
3. Test endpoint via curl atau Postman sebelum debug di JavaFX

### Sebelum commit
```bash
mvn test                       # Pastikan semua test lulus
mvn checkstyle:check           # Cek code style (jika dikonfigurasi)
```

---

## Placeholder yang Harus Diisi Tim

> Cari teks `[GANTI_...]` di seluruh proyek dan isi sesuai konfigurasi lokal kalian.

| Placeholder | Keterangan |
|---|---|
| `[GANTI_USERNAME_H2]` | Username H2 database (bebas, contoh: `sa`) |
| `[GANTI_PASSWORD_H2]` | Password H2 database (boleh kosong untuk dev) |
| `[GANTI_DENGAN_SECRET_KEY_MINIMAL_256_BIT]` | String acak panjang untuk JWT signing key |
| `[GANTI_DENGAN_PATH_FOLDER_UPLOAD_LOKAL]` | Path absolut folder penyimpanan file upload, contoh: `C:/taskforge/uploads` atau `/home/user/taskforge/uploads` |
| `[NAMA_KETUA_KELOMPOK]` | Nama ketua untuk data seed awal |
| `[EMAIL_KETUA]` | Email login ketua untuk demo |
| `[NAMA_ANGGOTA_1]` sampai `[NAMA_ANGGOTA_N]` | Nama anggota lain untuk data demo |

---

## Data Demo untuk Presentasi

> Buat data seed di `DataSeeder.java` (implementasi `CommandLineRunner`) dengan skenario berikut:

**Project:** "Tugas Akhir Sistem Informasi Perpustakaan"  
**Anggota:** 4 orang (1 ketua + 3 anggota) → isi nama tim kalian  
**Task yang sudah selesai:** 3 task (untuk menampilkan contribution score berwarna hijau)  
**Task overdue:** 1 task yang di-assign ke [NAMA_ANGGOTA_YANG_SENGAJA_TIDAK_KERJA] (untuk efek demo dramatis)  
**File yang sudah diupload:** minimal 2 file PDF dan 1 link Google Drive  

Skenario demo: tunjukkan contribution score anggota yang overdue berwarna merah, lalu buka File Hub dan tunjukkan semua file rapi per task — bandingkan dengan "alternatif" WhatsApp yang berantakan.
