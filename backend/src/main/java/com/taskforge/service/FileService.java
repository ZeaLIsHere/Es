package com.taskforge.service;

import com.taskforge.dto.request.LinkedFileRequest;
import com.taskforge.dto.response.FileResponse;
import com.taskforge.exception.ResourceNotFoundException;
import com.taskforge.exception.ValidationException;
import com.taskforge.model.ActivityLog.ActionType;
import com.taskforge.model.User;
import com.taskforge.model.file.LinkedFile;
import com.taskforge.model.file.ProjectFile;
import com.taskforge.model.file.UploadedFile;
import com.taskforge.model.task.BaseTask;
import com.taskforge.repository.ProjectFileRepository;
import com.taskforge.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private static final long MAX_FILE_SIZE = 10_000_000L; // 10 MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "ppt", "pptx",
            "jpg", "jpeg", "png", "gif",
            "txt", "java", "py", "js", "ts", "html", "css",
            "zip", "rar"
    );

    private final ProjectFileRepository fileRepository;
    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final ActivityLogService activityLogService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        File dir = new File(uploadDir);
        if (!dir.exists() && dir.mkdirs()) {
            log.info("Upload directory created: {}", uploadDir);
        }
    }

    // ── SpEL helper ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public boolean isUploader(Long fileId, String email) {
        return fileRepository.findById(fileId)
                .map(f -> f.getUploadedBy().getEmail().equals(email))
                .orElse(false);
    }

    // ── Upload & Link ─────────────────────────────────────────────────────

    @Transactional
    public FileResponse upload(Long taskId, String actorEmail, MultipartFile file, String description) {
        validateFile(file);

        BaseTask task = getTask(taskId);
        User actor = projectService.getUser(actorEmail);

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String extension = getExtension(originalName);
        String storedName = UUID.randomUUID() + "." + extension;
        Path targetPath = Paths.get(uploadDir, storedName);

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ValidationException("Gagal menyimpan file: " + e.getMessage());
        }

        String mimeType = URLConnection.guessContentTypeFromName(originalName);
        if (mimeType == null) mimeType = "application/octet-stream";

        UploadedFile entity = new UploadedFile();
        entity.setName(originalName);
        entity.setDescription(description);
        entity.setTask(task);
        entity.setUploadedBy(actor);
        entity.setStoragePath(targetPath.toString());
        entity.setFileSize(file.getSize());
        entity.setMimeType(mimeType);

        ProjectFile saved = fileRepository.save(entity);

        activityLogService.log(task.getProject(), actor,
                actor.getName() + " mengupload file '" + originalName + "' ke task '" + task.getTitle() + "'",
                ActionType.FILE_UPLOADED);

        log.info("File '{}' diupload ke task id={}", originalName, taskId);
        return FileResponse.from(saved);
    }

    @Transactional
    public FileResponse addLink(Long taskId, String actorEmail, LinkedFileRequest request) {
        BaseTask task = getTask(taskId);
        User actor = projectService.getUser(actorEmail);

        LinkedFile entity = new LinkedFile();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setTask(task);
        entity.setUploadedBy(actor);
        entity.setExternalUrl(request.getUrl());

        ProjectFile saved = fileRepository.save(entity);

        activityLogService.log(task.getProject(), actor,
                actor.getName() + " menambahkan link '" + request.getName() + "' ke task '" + task.getTitle() + "'",
                ActionType.LINK_ADDED);

        log.info("Link '{}' ditambahkan ke task id={}", request.getName(), taskId);
        return FileResponse.from(saved);
    }

    // ── Read ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FileResponse> getTaskFiles(Long taskId) {
        return fileRepository.findByTaskIdOrderByUploadedAtDesc(taskId).stream()
                .map(FileResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> download(Long fileId) {
        ProjectFile file = getFile(fileId);

        if (!(file instanceof UploadedFile uf)) {
            throw new ValidationException("File ini adalah link eksternal — gunakan URL langsung");
        }

        Path path = Paths.get(uf.getStoragePath());
        Resource resource = new FileSystemResource(path);
        if (!resource.exists()) {
            throw new ResourceNotFoundException("File fisik tidak ditemukan di server");
        }

        String mime = uf.getMimeType() != null ? uf.getMimeType() : "application/octet-stream";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.parseMediaType(mime))
                .body(resource);
    }

    // ── Delete ────────────────────────────────────────────────────────────

    @Transactional
    public void delete(Long fileId, String actorEmail) {
        ProjectFile file = getFile(fileId);
        User actor = projectService.getUser(actorEmail);

        boolean isKetua = actor.getRole() == com.taskforge.model.User.Role.KETUA;
        boolean isUploader = file.getUploadedBy().getEmail().equals(actorEmail);
        if (!isKetua && !isUploader) {
            throw new AccessDeniedException("Hanya uploader atau ketua yang bisa menghapus file ini");
        }

        if (file instanceof UploadedFile uf && uf.getStoragePath() != null) {
            try {
                Files.deleteIfExists(Paths.get(uf.getStoragePath()));
            } catch (IOException e) {
                log.warn("Gagal menghapus file fisik: {}", e.getMessage());
            }
        }

        fileRepository.delete(file);
        log.info("File id={} '{}' dihapus oleh {}", fileId, file.getName(), actorEmail);
    }

    // ── Internal ──────────────────────────────────────────────────────────

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) throw new ValidationException("File tidak boleh kosong");
        if (file.getSize() > MAX_FILE_SIZE) throw new ValidationException("Ukuran file maksimal 10 MB");

        String name = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        String ext = getExtension(name).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new ValidationException("Tipe file tidak didukung: ." + ext);
        }
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1) : "";
    }

    public ProjectFile getFile(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", fileId));
    }

    private BaseTask getTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
    }
}
